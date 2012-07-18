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

	//only default and stream streamfs files
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
		
		try {
			if(op!=null && path!=null) {
				path = cleanPath(path);
				String path2=(path.endsWith("/"))?path.substring(0, path.length()-1):path+"/";
				String thepath = null;
				if(map.containsKey(path))
					thepath = path;
				else if(map.containsKey(path2))
					thepath = path2;
				JSONObject finfo = performOp("GET", thepath, data);
				String pubid = null;
				try {pubid=finfo.getJSONObject("info").getString("pubid");}
				catch(Exception e){}
				
				if(finfo!=null){
					
					//DELETE
					if(op.equalsIgnoreCase("delete") && !thepath.equals("/")){
						map.remove(path);
						map.remove(path2);
					} 
					
					//GET
					else if(op.equalsIgnoreCase("get")){
						return map.get(thepath);
					}
					
					//PUT
					else if(op.equalsIgnoreCase("put")){
						//symlink
						String linksTo = getLink(path);
						if(linksTo!=null)
							return this.performOp(op, linksTo, data);
						
						if(pubid==null){
							//default
							return handleDefaultPut(path, data);
						} else {
							//stream
							return handleStreamPut(path, data);
						}
					}
					
					//POST
					else if(op.equalsIgnoreCase("post")){
						
					}
				}
			}
		} catch(Exception e){
			Log.e(SfsCache.class.toString(), "", e);
		}
		return null;
	}

	public void populateCache(String rootpath) {

	}

	public void removeEntry(String path) {

	}
	
	private String getParent(String path){
		if(path !=null && !path.equals("/")){
			path = cleanPath(path);
			StringBuffer buf = new StringBuffer();
			StringTokenizer tokenizer = new StringTokenizer(path, "/");
			Vector<String> tokens = new Vector<String>();
			while(tokenizer.hasMoreElements())
				tokens.add(tokenizer.nextToken());
			for(int i=0; i<tokens.size()-1; i++)
				buf.append("/").append(tokens.get(i));
			return buf.toString();
		}
		return null;
	}
	
	private String getLink(String path){
		try {
			String parent=null;
			String parent2=null;
			if(path !=null && !path.equals("/")){
				path = cleanPath(path);
				StringBuffer buf = new StringBuffer();
				StringTokenizer tokenizer = new StringTokenizer(path, "/");
				Vector<String> tokens = new Vector<String>();
				while(tokenizer.hasMoreElements())
					tokens.add(tokenizer.nextToken());
				for(int i=0; i<tokens.size()-1; i++)
					buf.append("/").append(tokens.get(i));
			
				parent = buf.toString();;
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
	
	private JSONObject handleDefaultPut(String path, JSONObject dataObj){
		try{
			if(dataObj != null){
				String op = dataObj.optString("operation");
				String resourceName = dataObj.optString("resourceName");
				if(op.equalsIgnoreCase("create_resource") && !resourceName.equals("")){
					
					//set it up
					String rType = dataObj.getString("resourceType");
					if(rType.equalsIgnoreCase("default") || rType.equalsIgnoreCase("stream") || 
							rType.equalsIgnoreCase("generic_publisher") || rType.equalsIgnoreCase("symlink")){
						String rName = dataObj.optString("resourceName");

						JSONArray children = map.get(path).getJSONObject("info").getJSONArray("children");
						if(children != null && !rName.equals("") && !children.toString().contains(rName)){
							String newpath = cleanPath(path + "/" + rName);
							JSONObject newpath_info = new JSONObject();
							newpath_info.put("status", "success");
							newpath_info.put("type", "DEFAULT");
							newpath_info.put("properties", new JSONObject());
							newpath_info.put("children", new JSONArray());
							addEntry(newpath, newpath_info, null);
						}
					}
					JSONObject resp = new JSONObject();
					JSONObject header = new JSONObject();
					header.put("HTTP/1.1", "201 Created");
					resp.put("header", header);
					return resp;
				}

				else if ((op.equalsIgnoreCase("create_generic_publisher") || 
                            (op.equalsIgnoreCase("create_stream")) ||
                            (op.equalsIgnoreCase("create_publisher"))) &&
					!dataObj.optString("resourceName").equals("")){
					UUID newPubId = new UUID(0L,0L);
					String rName = dataObj.optString("resourceName");
					String newpath = cleanPath(path + "/" + rName);
					JSONObject newpath_info = new JSONObject();
					newpath_info.put("status", "success");
					newpath_info.put("properties", new JSONObject());
					newpath_info.put("pubid", newPubId.toString());
					newpath_info.put("head", new JSONObject());
					addEntry(newpath, newpath_info, new JSONObject());
					
					if(newPubId != null){
						JSONObject resp = new JSONObject();
						JSONObject header = new JSONObject();
						header.put("HTTP/1.1", "201 Created");
						resp.put("header", header);
						JSONObject response = new JSONObject();
						response.put("status", "success");
						response.put("is4_uri", newpath);
						response.put("PubId", newPubId.toString());
						resp.put("response", resp);
						return resp;
					} else {
						JSONObject resp = new JSONObject();
						JSONObject header = new JSONObject();
						header.put("HTTP/1.1", "500 Internal Server Error");
						resp.put("header", header);
						return resp;
					}
				}

				else if(op.equalsIgnoreCase("create_symlink")) {
					String name = dataObj.optString("name");
					String localUri = dataObj.optString("uri");
					if(localUri.equals(""))
						localUri = dataObj.optString("path");
					localUri = cleanPath(localUri);
					JSONObject pathinfo = performOp("GET", path, null);

					if(!name.equals("") && !localUri.equals("") && (map.contains(localUri)||map.contains(localUri+"/")) &&
							!pathinfo.getJSONArray("children").toString().contains(name)){
						String newpath = path + "/" + localUri;
						JSONArray children = pathinfo.getJSONArray("children");
						children.put(name + " -> " + localUri);
						pathinfo.put("children", children);
						JSONObject value = new JSONObject();
						value.put("info", pathinfo);
						map.put(path, value);
						
						value = performOp("GET", localUri, null);
						map.put(newpath, value);

						JSONObject resp = new JSONObject();
						JSONObject header = new JSONObject();
						header.put("HTTP/1.1", "201 Created");
						resp.put("header", header);
						return resp;
					}
				}
				return null;
			}

		} catch (Exception e){
			Log.e(SfsCache.class.getName(), "", e);
		}
		return null;
	}
	
	private JSONObject handleStreamPut(String path, JSONObject data){
		return null;
	}


}
