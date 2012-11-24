package sfs.lib;

import java.net.*;
import java.util.logging.Logger;

import org.json.*;

import sfs.apps.connaccess.GlobalConstants;

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
	
	public long getTime(){
		try{
			JSONObject resp = new JSONObject(CurlOps.get(GlobalConstants.HOST + "/time"));
			return resp.getLong("Now");
		} catch(Exception e){
			Log.e("SFSCPnnector.ConnApp::error", "", e);
			return -1L;
		}
	}
	
	public boolean mkrsrc(String path, String name, String type){
		try {
			JSONObject request = new JSONObject();
			if(type.equalsIgnoreCase("default")){
				request.put("operation", "create_resource");
				request.put("resourceName", name);
				request.put("resourceType", type);
			} else if(type.equalsIgnoreCase("genpub") || type.equalsIgnoreCase("stream")){
				request.put("operation", "create_generic_publisher");
				request.put("resourceName", name);
			}
			
			int code= CurlOps.put(request.toString(),sfsurl.toString() + path);
			if(code==201)
				return true;
			
		} catch(Exception e){
			e.printStackTrace();
		}
		return false;
	}
	
	/**
	 * {   
	 *	    "operation":"create_resources",
	 *	    "list":[
	 *	            {
	 *	                "path":"/temp/one/two/stream3", 
	 *	                "type":"stream",
	 *	                "data":[{"value":0, "ts":1347307033196},{"value":1, "ts":1347307033197},{"value":2, "ts":1347307033198},{"value":3, "ts":1347307033199}]
	 *	            },
	 *	            {
	 *	                "path":"/temp/one/three/stream4", 
	 *	                "type":"stream",
	 *	                "data":[{"value":0, "ts":1347307033196},{"value":1, "ts":1347307033197},{"value":2, "ts":1347307033198},{"value":3, "ts":1347307033199}]
	 *	            },
	 *	            {
	 *	                "path":"/temp/one_four/two/", 
	 *					"properties":{"desc":"test instance"},
	 *	                "type":"default"
	 *	            }
	 *	    ]
	 *	}
	 *
	 * @param path Default file path to post the data to.
	 * @param list List object with similar format as shown above.
	 * @return true if successful, false otherwise.
	 */
	public JSONObject bulkResourceCreate(String path, JSONArray list){
		JSONObject responseObj=null;
		try{
			JSONObject request = new JSONObject();
			request.put("operation", "create_resources");
			request.put("list", list);
			responseObj = CurlOps.putAndGetResponse(request.toString(), sfsurl.toString()+ path);
			Log.i("ConnApp::bulkResourceCreate", responseObj.toString());
			if(responseObj.has("response_code") && responseObj.getInt("response_code")==201)
				return 	new JSONObject(responseObj.get("body").toString());
			return null;
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

    public boolean mksymlink(String parentpath, String name, String links_to){
        try {
        	JSONObject request = new JSONObject();
	        request.put("operation", "create_symlink");
	        request.put("name", name);
	        if(links_to.startsWith("http"))
	            request.put("url", links_to);
	        else
	            request.put("uri", links_to);

	        int code = CurlOps.put(request.toString(),sfsurl.toString() + parentpath);
			if(code==201)
				return true;
		} catch(Exception e){
			e.printStackTrace();
		}
		return false;
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
	
	public boolean postStreamData(String streamPath, String pubid, String dataObjStr) throws Exception, JSONException{
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
            String respStr = CurlOps.post(dataObjStr, url);
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
		String respStr=null;
		  try {
			  respStr = CurlOps.get(sfsurl.toString() + path);
			  Log.i("ConnApp::" + SFSConnector.class.toString(), "getPubId("+ sfsurl.toString() + path+")="+respStr);
			  //System.exit(1);
			  if(respStr != null){
				  JSONObject respObj = new JSONObject(respStr);
				  String loc_pubid = respObj.getString("pubid");
				  return loc_pubid;
			  }
		  } catch(Exception e){
			  e.printStackTrace();
			  try {
				  if(respStr != null){
					  JSONObject respObj = new JSONObject(respStr);
					  String loc_pubid = respObj.getString("pubid");
					  return loc_pubid;
				  }
			  }
			  catch(Exception e2){
				  e2.printStackTrace();
			  }
		  }
		  return null;
		  
	  }
}
