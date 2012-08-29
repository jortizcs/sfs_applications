package sfs.lib;

import java.util.zip.GZIPInputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import android.util.Log;
import java.io.ByteArrayOutputStream;


public class CurlOps {
	
	private static final int timeout=500;
	
	public static String get(String urlString) throws Exception {
		Log.i("CurlOps.get.ConnApp::", urlString);
		URL url = new URL(urlString);
		HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
		httpCon.setRequestMethod("GET");
		httpCon.setConnectTimeout(timeout);
		httpCon.setReadTimeout(timeout);
		Object resp = httpCon.getContent();
		String r= null;
		if(resp instanceof GZIPInputStream){
			byte[] buf = new byte[255];
			ByteArrayOutputStream dataBuf = new ByteArrayOutputStream();
			int bytesRead = -1;
			while((bytesRead=((GZIPInputStream) resp).read(buf))>0){
				dataBuf.write(buf, 0, bytesRead);
				buf = new byte[255];
			}
			dataBuf.write((byte)'\n');
			r = dataBuf.toString();
		}
		Log.i("CurlOps.get.ConnApp::", r);
		httpCon.disconnect();
		return r;

	}
	
	public static String post(String data, String urlString) throws Exception {
		Log.i("ConnApp::", "POST(data=" + data + ", url=" + urlString + ")");
		URL url = new URL(urlString);
		HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
		httpCon.setConnectTimeout(timeout);
		httpCon.setReadTimeout(timeout);
		httpCon.setDoOutput(true);
		//httpCon.setRequestMethod("POST");
		OutputStreamWriter out = new OutputStreamWriter(httpCon.getOutputStream());
		out.write(data);
		out.close();
		Log.i("ConnApp::", "POST(data=" + data + ", url=" + urlString + ")="+httpCon.getResponseMessage());
		return httpCon.getResponseMessage();

	}
	
	public static String put(String data, String urlString) throws Exception {
		URL url = new URL(urlString);
		HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
		httpCon.setConnectTimeout(timeout);
		httpCon.setReadTimeout(timeout);
		httpCon.setDoOutput(true);
		httpCon.setRequestMethod("PUT");
		OutputStreamWriter out = new OutputStreamWriter(httpCon.getOutputStream());
		out.write(data);
		out.close();
		return httpCon.getResponseMessage();
	}
	
	public static synchronized String delete(String urlString) throws Exception {
		/*HttpURLConnection httpCon=null;
		try {
			URL url = new URL(urlString);
			httpCon = (HttpURLConnection) url.openConnection();
			httpCon.setConnectTimeout(timeout);
			httpCon.setDoOutput(true);
			httpCon.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			httpCon.setRequestMethod("DELETE");
			httpCon.connect();
			//return urlString;
			httpCon.disconnect();
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
		}*/
		
		URL url = new URL(urlString);
		HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
		httpCon.setConnectTimeout(timeout);
		httpCon.setReadTimeout(timeout);
		httpCon.setRequestMethod("DELETE");
		httpCon.disconnect();
		return httpCon.getResponseMessage();
	}
	
	public static String getQrcFromUrl(String tinyURL) throws Exception {
		HttpURLConnection conn = (HttpURLConnection)(new URL(tinyURL)).openConnection();
		conn.setConnectTimeout(timeout);
		conn.setInstanceFollowRedirects(false);
		String loc = conn.getHeaderField("Location"); //null pointer exception here
		int i = loc.indexOf("qrc=");
		return (i >= 0) ? loc.substring(i+4) : "Invalid URL";
	}
	
	public static String getConfigObjStrFromUrl(String configTinyURL) throws Exception {
		HttpURLConnection conn = (HttpURLConnection)(new URL(configTinyURL)).openConnection();
		conn.setConnectTimeout(timeout);
		conn.setInstanceFollowRedirects(false);
		String loc = conn.getHeaderField("Location");
		return CurlOps.get(loc);
	}
}
