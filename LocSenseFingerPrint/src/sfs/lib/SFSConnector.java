package sfs.lib;

import java.net.*;
import org.json.*;

import android.util.Log;


public class SFSConnector {

	private static String host = null;
	private static int port = -1;
	private static URL sfsurl = null;
	
	public SFSConnector(String h, int p){
		try {
			host = h;
			port = p;
			sfsurl= new URL("http://"+host+":"+port);
		} catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void print(String s){
		System.out.println(s);
	}
	
	public String mkrsrc(String path, String name, String type){
		try {
			JSONObject request = new JSONObject();
			if(type.equalsIgnoreCase("default")){
				request.put("operation", "create_resource");
				request.put("resourceName", name);
				request.put("resourceType", type);
			} else if(type.equalsIgnoreCase("genpub")){
				request.put("operation", "create_generic_publisher");
				request.put("resourceName", name);
			}
			
			return CurlOps.put(request.toString(),sfsurl.toString() + path);
		} catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}
	
	public String deleteRsrc(String path){
		try {
			return CurlOps.delete(sfsurl.toString() + path);
		} catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}

    public String mksymlink(String parentpath, String name, String links_to){
        try {
        	JSONObject request = new JSONObject();
	        request.put("operation", "create_symlink");
	        request.put("name", name);
	        if(links_to.startsWith("http"))
	            request.put("url", links_to);
	        else
	            request.put("uri", links_to);

			return CurlOps.put(request.toString(),sfsurl.toString() + parentpath);
		} catch(Exception e){
			e.printStackTrace();
		}
		return null;
    }
	
	public  String mksmappub(String path, URL smapurl){
		try{
			JSONObject request = new JSONObject();
			request.put("operation", "create_smap_publisher");
			request.put("smap_urls", smapurl.toString());
			return CurlOps.put(request.toString(),sfsurl.toString() + path);
		} catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}
	
	public String overwriteProps(String path, String propsStr){
		try {
			JSONObject request = new JSONObject();
			request.put("operation", "overwrite_properties");
			JSONObject props = null;
			props = new JSONObject(propsStr);
		
			request.put("properties", props);
			return CurlOps.post(request.toString(),sfsurl.toString() + path);
		} catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}
	
	public String updateProps(String path, String propsObj){
		try {
			JSONObject request = new JSONObject();
			request.put("operation", "update_properties");
			JSONObject props = null;
			props = new JSONObject(propsObj);
			request.put("properties", props);
			return CurlOps.post(request.toString(),sfsurl.toString() + path);
		} catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}
	
	public boolean exists(String path){
		Log.i(SFSConnector.class.toString(), "Check if " + sfsurl.toString() + path +  " exists");
		return Util.isExistingResource(sfsurl.toString() + path);
	}
	
	public JSONObject tsQuery(String path, long timestamp){
		try {
			String url = new String(sfsurl.toString() + path + "?query=true&ts_timestamp=" + timestamp);
			String v = CurlOps.get(url);
			if(v!=null)
				return new JSONObject(v);
		} catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}
	
	public JSONObject tsRangeQuery(String path, long tslowerbound, boolean includelb, 
									long tsupperbound, boolean includeub){
		String queryParams = "?query=true&";
		if(includelb){
			queryParams = new String(queryParams+"ts_timestamp=gte:"+tslowerbound);
		} else {
			queryParams = new String(queryParams+"ts_timestamp=gt:"+tslowerbound);
		}
		
		if(includeub){
			queryParams =  new String(queryParams+",lte:"+tsupperbound);
		} else {
			queryParams = new String (queryParams+",lt:"+tsupperbound);
		}
		
		try {
			String url = new String(sfsurl.toString() + path + queryParams);
			String v = CurlOps.get(url);
			
			if(v!= null){
				return new JSONObject(v);
			}
		} catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}
	
	public JSONObject tsNowRangeQuery(String path, long tslowerbound, boolean includelb, 
									long tsupperbound, boolean includeub){
		String queryParams = "?query=true&";
		if(includelb){
			queryParams = new String(queryParams+"ts_timestamp=gte:"+tslowerbound);
		} else {
			queryParams = new String(queryParams+"ts_timestamp=gt:"+tslowerbound);
		}
		
		if(includeub){
			queryParams =  new String(queryParams+",lte:"+tsupperbound);
		} else {
			queryParams = new String (queryParams+",lt:"+tsupperbound);
		}
		
		try {
			String url = new String(sfsurl.toString() + path + queryParams);
			String v= CurlOps.get(url);
			if(v!=null)
				return new JSONObject(v);
		} catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}
	
	public JSONObject getSFSTime(){
		try {
			String url = new String(sfsurl.toString() + "/is4/time");
			String v = CurlOps.get(url);
			if(v!=null)
				return new JSONObject(v);
		} catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}
	
	public boolean putStreamData(String streamPath, String pubid, String dataObjStr) throws Exception, JSONException{
        try {
            if(streamPath==null || pubid==null || dataObjStr==null)
                return false;
            String url;
            JSONObject dataObj = new JSONObject(dataObjStr);
            if(dataObj.has("ts"))
                url = new String(sfsurl.toString() + streamPath + "?type=generic&addts=false&pubid=" + pubid);
            else
                url = new String(sfsurl.toString() + streamPath + "?type=generic&pubid=" + pubid);
            

            //Put it
            Log.i(SFSConnector.class.toString(), "POSTing data to " + url);
            String respStr = CurlOps.put(dataObjStr, url);
            if(respStr !=null && respStr.length()>0){
            	/*JSONObject respObj = new JSONObject(respStr);
            	if(respObj.getString("status").equals("success"))
            		return true;*/
            	return true;
            }
        } catch(Exception e){
            e.printStackTrace();
        }
        return false;
    }

	public String getPubId(String path){
		  try {
			  String respStr = CurlOps.get(sfsurl.toString() + path);
			  if(respStr != null){
				  JSONObject respObj = new JSONObject(respStr);
				  String loc_pubid = respObj.getString("pubid");
				  return loc_pubid;
			  }
		  } catch(Exception e){
			  e.printStackTrace();
		  }
		  return null;
		  
	  }
}
