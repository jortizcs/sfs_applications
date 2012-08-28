package sfs.lib;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import android.util.Log;


public class CurlOps {
	
	public static String get(String urlString) throws Exception {
		String total = "";
		// Create a URL for the desired page
		URL url = new URL(urlString);

		// Read all the text returned by the server
		BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
		String str="";
		while ((str = in.readLine()) != null) {
			// str is one line of text; readLine() strips the newline
			// character(s)
			total = total + str;
			Log.i("ConnApp::resp_internal=", str);
			System.out.println("ConnApp::resp_internal="+str);
		}
		in.close();
		return total;

	}
	
	public static String post(String data, String urlString) throws Exception {
		URL url = new URL(urlString);
		HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
		httpCon.setDoOutput(true);
		//httpCon.setRequestMethod("POST");
		OutputStreamWriter out = new OutputStreamWriter(httpCon.getOutputStream());
		out.write(data);
		out.close();
		return httpCon.getResponseMessage();

	}
	
	public static String put(String data, String urlString) throws Exception {
		URL url = new URL(urlString);
		HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
		httpCon.setDoOutput(true);
		httpCon.setRequestMethod("PUT");
		OutputStreamWriter out = new OutputStreamWriter(httpCon.getOutputStream());
		out.write(data);
		out.close();
		return httpCon.getResponseMessage();
	}
	
	public static synchronized String delete(String urlString) throws Exception {
		HttpURLConnection httpCon=null;
		try {
			URL url = new URL(urlString);
			httpCon = (HttpURLConnection) url.openConnection();
			httpCon.setDoOutput(true);
			httpCon.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			httpCon.setRequestMethod("DELETE");
			httpCon.connect();
			//return urlString;
			return httpCon.getResponseMessage();
		} catch(Exception e){
			e.printStackTrace();
			throw e;
		} finally{
			try {
				if(httpCon!=null){
					httpCon.getInputStream().close();
					httpCon.getOutputStream().close();
					httpCon.disconnect();
				}
			}catch(Exception e2){}
		}
	}
	
	public static String getQrcFromUrl(String tinyURL) throws Exception {
		HttpURLConnection conn = (HttpURLConnection)(new URL(tinyURL)).openConnection();
		conn.setInstanceFollowRedirects(false);
		String loc = conn.getHeaderField("Location"); //null pointer exception here
		int i = loc.indexOf("qrc=");
		return (i >= 0) ? loc.substring(i+4) : "Invalid URL";
	}
	
	public static String getConfigObjStrFromUrl(String configTinyURL) throws Exception {
		HttpURLConnection conn = (HttpURLConnection)(new URL(configTinyURL)).openConnection();
		conn.setInstanceFollowRedirects(false);
		String loc = conn.getHeaderField("Location");
		return CurlOps.get(loc);
	}
}
