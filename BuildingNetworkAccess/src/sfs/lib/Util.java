package sfs.lib;

import java.io.BufferedReader;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Util {
	public static int getResponseCode(String urlString) throws MalformedURLException, IOException {
		URL u = new URL(urlString);
		HttpURLConnection huc = (HttpURLConnection) u.openConnection();
		huc.setRequestMethod("GET");
		huc.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 6.0; en-US; rv:1.9.1.2) Gecko/20090729 Firefox/3.5.2 (.NET CLR 3.5.30729)");
		huc.connect();
		return huc.getResponseCode();
	}
	
	public static boolean isExistingResource(String url) {
		String resp=null;
		try {
			resp = CurlOps.get(url);
			if(resp!=null && resp.length()>0)
				return true;
		}
		catch(Exception e) {
			return false;
		}
		finally{
			try {
				Log.i("ConnApp::resp=", resp);
				System.out.println("ConnApp::resp="+resp);
			} catch(Exception e){
				
			}
		}
		return false;
	}
	
	public static String cleanPath(String path){
	  if(path==null || path.equals("/"))
		  return "/";
	  path = path.replaceAll("/+", "/");
	  if(path.endsWith("/"))
		  path = path.substring(0,path.length()-1);
	  return path;
	}
	  
	
	public String createResource(String name, String type, String targetUrl) throws Exception, JSONException {
		JSONObject jsonObj = new JSONObject();
		
		if(type.equalsIgnoreCase("default")){
			jsonObj.put("operation", "create_resource");
			jsonObj.put("resourceType", type);			
			jsonObj.put("resourceName", name);
		} else if(type.equalsIgnoreCase("meter")){
			jsonObj.put("operation", "create_generic_publisher");
			jsonObj.put("resourceName", name);
		}
		if(CurlOps.put(jsonObj.toString(), targetUrl)==201)
			return "OK";
		return "ERROR";
	}
	
	//hostAndPort = "http://is4server.com:8083"
	public static String createSymlink(String origin, String target, String hostAndPort) throws Exception, JSONException {
		if(target.endsWith("/"))
			target=target.substring(0, target.length()-1);
		int i = target.lastIndexOf("/");
		Log.i(Util.class.toString(), "target=" + target + ", i=" + i);
		
		JSONObject jsonObj = new JSONObject();
		jsonObj.put("operation", "create_symlink");
		jsonObj.put("name", target.substring(i+1));
		jsonObj.put("uri", target);
		Log.i(Util.class.toString(), "Sending Request:: " + jsonObj.toString());
		if(CurlOps.put(jsonObj.toString(), hostAndPort + origin)==201)
			return "OK";
		return "ERROR";
	}
	
	public static String getJsonString(String url) {
		HttpClient httpclient = new DefaultHttpClient();
		HttpGet httpget = new HttpGet(url);
		HttpResponse response;
		
		try {
			response = httpclient.execute(httpget);
			HttpEntity entity = response.getEntity();
			InputStream instream = entity.getContent();
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(instream));
			StringBuilder sb = new StringBuilder();
			String line = null;
			
			while ((line = reader.readLine()) != null)
				sb.append(line + "\n");
				
			instream.close();			
			return sb.toString();
			
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static JSONArray getChildren(String url)	throws JSONException {
		Log.i("UTIL", "getChildren.url="+url);
		JSONObject json = new JSONObject(getJsonString(url));
		if(json.has("children"))
			return json.getJSONArray("children");
		else
			return null;
	}
	
	public static JSONArray getIncidentPaths(String url){
		try {
			Log.i("UTIL", "getting incident paths: " + url + "?incident_paths=true");
			String respStr = CurlOps.get(url  + "?incident_paths=true");
			Log.i("UTIL", "respStr=" + respStr);
		
			JSONObject obj = new JSONObject(respStr);
			if(obj!=null && obj.has("paths"))
				return obj.getJSONArray("paths");
		} catch(Exception e){
			Log.d("UTIL", "Problem getting incident paths");
		}
		return null;
	}

	public static JSONObject getProperties(String url) throws JSONException {
		JSONObject json = new JSONObject(getJsonString(url));
		if (json.has("properties"))
			return json.getJSONObject("properties");
		else
			return null;
	}
	
	public static String getParent(String path){
        path = cleanPath(path);
        if(path==null || path == "/")
            return "/";
        StringTokenizer tokenizer = new StringTokenizer(path, "/");
        Vector<String> tokens = new Vector<String>();
        while(tokenizer.hasMoreTokens())
            tokens.add(tokenizer.nextToken());
        StringBuffer buf = new StringBuffer();
        if(tokens.size()==1)
        	buf.append("/");
        else {
	        for(int i=0; i<tokens.size()-1; i++)
	            buf.append("/").append(tokens.elementAt(i));
        }
        return buf.toString();
    }
}

