package mobile.SFS;

import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONArray;
import org.json.JSONObject;

import android.util.Log;

public class SfsCache implements Cache {
	
	private ConcurrentHashMap<String, JSONObject> map_ = null;
	private static SfsCache cache = null;
	
	public SfsCache(){
		map_ = new ConcurrentHashMap<String, JSONObject>();
	}
	
	public static SfsCache getInstance(){
		if(cache == null)
			cache = new SfsCache();
		return cache;
	}

	public void addEntry(String path, JSONObject pathinfo, JSONObject tsqueryres) {
		path = cleanPath(path); //do this here so all entries are well formatted
		try {
			JSONObject value = new JSONObject();
			String linksTo = null;
			value.put("info", pathinfo);
			if((linksTo=getLink(path))!=null)
				value.put("links_to", linksTo);
			else if(tsqueryres!=null && pathinfo.has("pubid"))
				value.put("ts_hist", tsqueryres);
			
			map_.put(path, value);
		} catch(Exception e){
			Log.e(SfsCache.class.getName(), "", e);
		}

	}

	public void flush() {
		map_.clear();
	}

	public JSONObject performOp(String op, String path, JSONObject data) {
		if(op!=null && path!=null) {
			path = cleanPath(path);
			
			//DELETE
			if(op.equalsIgnoreCase("delete") && !path.equals("/"))
				removeEntry(path);
			
			//GET
			else if(op.equalsIgnoreCase("get"))
				return getEntry(path);
			
			//PUT or POST
			else if(op.equalsIgnoreCase("put") || op.equalsIgnoreCase("post")) {
				if(!map_.contains(path))
					addEntry(path, data, null);
				else {
					try {
						map_.get(path).put("info", data); //not sure about this, probably need to replace field by field
					}
					catch(Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		return null;
	}

	public void populateCache(String rootpath) {
		try {
			JSONObject json = new JSONObject(CurlOpsReal.get(rootpath + "/*"));
			Iterator<String> iter = json.keys();
			String s;
			
			while(iter.hasNext())
				addEntry(s = iter.next(), json.getJSONObject(s), null);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}

	public void removeEntry(String path) {
		map_.remove(path);
	}
	
	/**
	 * 
	 * @param path
	 * @return the JSONObject at the specified path, or null if no such path exists
	 */
	public JSONObject getEntry(String path) {
		return map_.get(path);
	}
	
	private String getLink(String path) {
		try {
			String parent = null;
			if(path !=null && !path.equals("/")) {
				StringBuffer buf = new StringBuffer();
				StringTokenizer tokenizer = new StringTokenizer(path, "/");
				Vector<String> tokens = new Vector<String>();
				while(tokenizer.hasMoreElements())
					tokens.add(tokenizer.nextToken());
				for(int i=0; i<tokens.size()-1; i++)
					buf.append("/").append(tokens.get(i));
				parent=buf.toString();
				String child = tokens.lastElement();
				
				if(map_.containsKey(parent)) {
					JSONArray children = map_.get(parent).getJSONObject("info").getJSONArray("children");
					
					for(int j=0; j<children.length(); j++) {
						if(children.getString(j).startsWith(child) && children.getString(j).contains("->")) { //not guaranteed to work
							tokenizer = new StringTokenizer(children.getString(j), " -> ");
							tokenizer.nextToken();
							return tokenizer.nextToken();
						}
					}
				}
			}
		} catch(Exception e) {
			Log.e(SfsCache.class.getName(), "", e);
		}
		return null;
	}
	
	private String cleanPath(String path) {
        //clean up the path
        if(path == null)
            return path;
        if(path.equals("") || path.equals("/"))
            return path;

        if(!path.startsWith("/"))
            path = "/" + path;
        path = path.replaceAll("/+", "/");
        if(path.endsWith("/"))
            path = path.substring(0,path.length()-1);
        return path;
    }
}
