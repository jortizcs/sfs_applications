package mobile.SFS;

import org.json.JSONArray;
import org.json.JSONObject;

import android.util.Log;

import java.util.concurrent.*;
import android.util.Log;
import java.util.*;

public class SfsCache implements Cache {
	
	private static ConcurrentHashMap<String, JSONObject> map = null;
	private static SfsCache cache = null;
	
	public SfsCache(){
		map = new ConcurrentHashMap<String, JSONObject>();
	}
	
	public SfsCache getInstance(){
		if(cache == null)
			cache = new SfsCache();
		return cache;
	}

	public void addEntry(String path, JSONObject pathinfo, JSONObject tsqueryres) {
		try {
			JSONObject value = new JSONObject();
			String linksTo = null;
			value.put("info", pathinfo);
			if((linksTo=getLink(path))!=null)
				value.put("links_to", linksTo);
			else if(tsqueryres!=null && pathinfo.has("pubid"))
				value.put("ts_hist", tsqueryres);
			
			map.put(path, value);
		} catch(Exception e){
			Log.e(SfsCache.class.getName(), "", e);
		}

	}

	public void flush() {
		map.clear();
	}

	public JSONObject performOp(String op, String path, JSONObject data) {
		
		if(op!=null && path!=null) {
			path = cleanPath(path);
			String path2=(path.endsWith("/"))?path.substring(0, path.length()-1):path+"/";
			
			//DELETE
			if(op.equalsIgnoreCase("delete") && !path.equals("/")){
				map.remove(path);
				map.remove(path2);
			} 
			
			//GET
			else if(op.equalsIgnoreCase("get")){
				JSONObject resp = (map.containsKey(path))?map.get(path):map.get(path2);
				return resp;
			}
			
			//PUT
			else if(op.equalsIgnoreCase("put")){
				
			}
			
			//POST
			else if(op.equalsIgnoreCase("post")){
				
			}
		}
		return null;
	}

	public void populateCache(String rootpath) {

	}

	public void removeEntry(String path) {

	}
	
	private String getLink(String path){
		try {
			String parent = null;
			String parent2 = null;
			if(path !=null && !path.equals("/")){
				StringBuffer buf = new StringBuffer();
				StringTokenizer tokenizer = new StringTokenizer(path, "/");
				Vector<String> tokens = new Vector<String>();
				while(tokenizer.hasMoreElements())
					tokens.add(tokenizer.nextToken());
				for(int i=0; i<tokens.size()-1; i++)
					buf.append("/").append(tokens.get(i));
				parent=buf.toString();
				parent2 = parent + "/";
				String child = tokens.lastElement();
				
				if(map.containsKey(parent) || map.containsKey(parent2)){
					JSONObject parentInfo = map.get(parent)==null?map.get(parent):map.get(parent2);
					JSONArray children = (JSONArray)parentInfo.get("children");
					for(int j=0; j<children.length(); j++){
						if(children.getString(j).startsWith(child) && children.getString(j).contains("->")){
							tokenizer = new StringTokenizer(children.getString(j), " -> ");
							tokenizer.nextToken();
							return tokenizer.nextToken();
						}
					}
				}
			}
		} catch(Exception e){
			Log.e(SfsCache.class.getName(), "", e);
		}
		return null;
	}
	
	private String cleanPath(String path){
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
