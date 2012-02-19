package mobile.SFS;

import java.io.BufferedReader;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

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
		try {
			return getResponseCode(url) != 404;
		}
		catch(IOException e) {
			return false;
		}
	}
	
	public static String createResource(String name, String type, String targetUrl) throws Exception, JSONException {
		JSONObject jsonObj = new JSONObject();
		
		if(type.equalsIgnoreCase("default")){
			jsonObj.put("operation", "create_resource");
			jsonObj.put("resourceType", type);			
			jsonObj.put("resourceName", name);
		} else if(type.equalsIgnoreCase("meter")){
			jsonObj.put("operation", "create_generic_publisher");
			jsonObj.put("resourceName", name);
		}
		return CurlOps.put(jsonObj.toString(), targetUrl);
	}
	
	//hostAndPort = "http://is4server.com:8083"
	public static String createSymlink(String origin, String target, String hostAndPort) throws Exception, JSONException {
		int i = target.lastIndexOf("/");
		
		JSONObject jsonObj = new JSONObject();
		jsonObj.put("operation", "create_symlink");
		jsonObj.put("name", target.substring(i+1));
		jsonObj.put("uri", target);
		return CurlOps.put(jsonObj.toString(), hostAndPort + origin);
	}
	
	public static String getUriFromQrc(String qrc) throws Exception {
		String s  = CurlOps.get(GlobalConstants.HOST + GlobalConstants.QRCHOME + "/" + qrc);
		//String s = CurlOps.get("http://is4server.com:8083/buildings/SDH/qrc/" + qrc);
		//String s = CurlOps.get("http://is4server.com:8084/buildings/home/qrc/" + qrc);
		Log.i("UTIL", "getRes: " + s);
		try {
			JSONObject respObj = new JSONObject(s);
			if(s != null && respObj.getJSONArray("children").length()>0){
				String str = s.split("->")[1].replaceAll("[\\W&&[^\\/]]", "");
				Log.i("UTIL", str);
				return str;
			}
		} catch(Exception e){
			Log.e("UTIL", "", e);
		}
		return null;
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

	public static JSONObject getProperties(String url) throws JSONException {
		JSONObject json = new JSONObject(getJsonString(url));
		if (json.has("properties"))
			return json.getJSONObject("properties");
		else
			return null;
	}
}
