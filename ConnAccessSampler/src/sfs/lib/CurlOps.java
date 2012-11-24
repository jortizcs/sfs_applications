package sfs.lib;

import java.util.zip.GZIPInputStream;
import java.io.OutputStreamWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import android.util.Log;
import java.io.ByteArrayOutputStream;

import org.json.JSONObject;


public class CurlOps {
	
	private static final int timeout=500;
	
	public static synchronized String get(String urlString) throws Exception {
		HttpURLConnection httpCon=null;
		ByteArrayOutputStream dataBuf=null;
		String r= null;
		try {
			Log.i("CurlOps.get.ConnApp::", urlString);
			URL url = new URL(urlString);
			httpCon = (HttpURLConnection) url.openConnection();
			httpCon.setRequestProperty("Connection", "close");
			httpCon.setRequestMethod("GET");
			httpCon.setConnectTimeout(timeout);
			httpCon.setReadTimeout(timeout);
			Object resp = httpCon.getContent();
			if(resp instanceof GZIPInputStream){
				byte[] buf = new byte[255];
				for(int i=0; i<255; i++)
					buf[i]=0;
				dataBuf = new ByteArrayOutputStream();
				int bytesRead = -1;
				while((bytesRead=((GZIPInputStream) resp).read(buf))>0){
					dataBuf.write(buf, 0, bytesRead);
					for(int i=0; i<255; i++)
						buf[i]=0;
				}
				dataBuf.write((byte)'\n');
				r = dataBuf.toString();
			}
			Log.i("CurlOps.get.ConnApp::", r);
		} catch(Exception e){
			throw e;
		} finally{
			try {
				if(httpCon!=null){
					httpCon.disconnect();
				}
				dataBuf = null;
				
			} catch(Exception e){
				
			}
		}
		return r;

	}
	
	public static synchronized String post(String data, String urlString) throws Exception {
		HttpURLConnection httpCon=null;
		OutputStreamWriter out = null;
		BufferedReader rd = null;
		StringBuilder sb = null;
		String line = null;
		String responseMsg = null;
		try {
			Log.i("CurlOps.post.ConnApp::", "POST(data=" + data + ", url=" + urlString + ")");
			URL url = new URL(urlString);
			httpCon = (HttpURLConnection) url.openConnection();
			httpCon.setConnectTimeout(timeout);
			httpCon.setReadTimeout(timeout);
			httpCon.setDoOutput(true);
			//httpCon.setRequestMethod("POST");
			out = new OutputStreamWriter(httpCon.getOutputStream());
			out.write(data);
			out.flush();
			responseMsg = httpCon.getResponseMessage();;
			Log.i("CurlOps.post.ConnApp::", "POST(data=" + data + ", url=" + urlString + ")="+responseMsg);
			
			//read the result from the server
			rd  = new BufferedReader(new InputStreamReader(httpCon.getInputStream()));
			sb = new StringBuilder();
			
			while ((line = rd.readLine()) != null)
			{
				sb.append(line + '\n');
			}
			Log.i("CurlOps.post.ConnApp::", sb.toString());
		} catch(Exception e){
			Log.e("CurlOps.post.ConnApp::", "", e);
			throw e;
		} finally{
			try {
				if(httpCon!=null){
					httpCon.disconnect();
				}
				rd = null;
		        sb = null;
		        out=null;
		        Log.i("CurlOps.post.ConnApp::", "closing");
			} catch(Exception e2){
				Log.e("CurlOps.post.ConnApp::", "", e2);
			}
		}
		return responseMsg;

	}
	
	public static synchronized int put(String data, String urlString) throws Exception {
		HttpURLConnection httpCon=null;
		OutputStreamWriter out = null;
		BufferedReader rd = null;
		StringBuilder sb = null;
		String line = null;
		String responseMsg = null;
		int responseCode = -1;
		try {
			Log.i("CurlOps.put.ConnApp::", "PUT(data=" + data + ", url=" + urlString + ")");
			URL url = new URL(urlString);
			httpCon = (HttpURLConnection) url.openConnection();
			httpCon.setConnectTimeout(timeout);
			httpCon.setReadTimeout(timeout);
			httpCon.setDoOutput(true);
			httpCon.setRequestMethod("PUT");
			out = new OutputStreamWriter(httpCon.getOutputStream());
			out.write(data);
			out.flush();
			responseMsg = httpCon.getResponseMessage();
			responseCode = httpCon.getResponseCode();
			Log.i("CurlOps.put.ConnApp::", "PUT(data=" + data + ", url=" + urlString + ")="+ 
					responseMsg + ",code=" + responseCode);
			
			//read the result from the server
			rd  = new BufferedReader(new InputStreamReader(httpCon.getInputStream()));
			sb = new StringBuilder();
			
			while ((line = rd.readLine()) != null)
			{
				sb.append(line + '\n');
			}
			Log.i("CurlOps.put.ConnApp::", sb.toString());
			
		} catch(Exception e){
			Log.e("CurlOps.put.ConnApp::", "", e);
			throw e;
		} finally{
			try {
				if(httpCon!=null){
					httpCon.disconnect();
				}
				rd = null;
		        sb = null;
		        out=null;
		        Log.i("CurlOps.put.ConnApp::", "closing");
			} catch(Exception e2){
				Log.e("CurlOps.put.ConnApp::", "", e2);
			}
		}
		return responseCode;
	}
	
	public static synchronized JSONObject putAndGetResponse(String data, String urlString) throws Exception {
		HttpURLConnection httpCon=null;
		OutputStreamWriter out = null;
		BufferedReader rd = null;
		StringBuilder sb = null;
		String line = null;
		String responseMsg = null;
		int responseCode = -1;
		JSONObject returnObj = new JSONObject();
		try {
			Log.i("CurlOps.put.ConnApp::", "PUT(data=" + data + ", url=" + urlString + ")");
			URL url = new URL(urlString);
			httpCon = (HttpURLConnection) url.openConnection();
			httpCon.setConnectTimeout(timeout);
			httpCon.setReadTimeout(timeout);
			httpCon.setDoOutput(true);
			httpCon.setRequestMethod("PUT");
			out = new OutputStreamWriter(httpCon.getOutputStream());
			out.write(data);
			out.flush();
			responseMsg = httpCon.getResponseMessage();
			responseCode = httpCon.getResponseCode();
			Log.i("CurlOps.put.ConnApp::", "PUT(data=" + data + ", url=" + urlString + ")="+ 
					responseMsg + ",code=" + responseCode);
			
			//read the result from the server
			rd  = new BufferedReader(new InputStreamReader(httpCon.getInputStream()));
			sb = new StringBuilder();
			
			while ((line = rd.readLine()) != null)
			{
				sb.append(line + '\n');
			}
			Log.i("CurlOps.put.ConnApp::", sb.toString());
			
			returnObj.put("response_code", responseCode);
			returnObj.put("body", sb.toString());
			
		} catch(Exception e){
			Log.e("CurlOps.put.ConnApp::", "", e);
			throw e;
		} finally{
			try {
				if(httpCon!=null){
					httpCon.disconnect();
				}
				rd = null;
		        sb = null;
		        out=null;
		        Log.i("CurlOps.put.ConnApp::", "closing");
			} catch(Exception e2){
				Log.e("CurlOps.put.ConnApp::", "", e2);
			}
		}
		return returnObj;
	}
	
	public static synchronized String delete(String urlString) throws Exception {
		HttpURLConnection httpCon=null;
		ByteArrayOutputStream dataBuf=null;
		String r= null;
		try {
			Log.i("CurlOps.delete.ConnApp::", urlString);
			URL url = new URL(urlString);
			httpCon = (HttpURLConnection) url.openConnection();
			httpCon.setRequestProperty("Connection", "close");
			httpCon.setRequestMethod("DELETE");
			httpCon.setConnectTimeout(timeout);
			httpCon.setReadTimeout(timeout);
			Object resp = httpCon.getContent();
			if(resp instanceof GZIPInputStream){
				byte[] buf = new byte[255];
				for(int i=0; i<255; i++)
					buf[i]=0;
				dataBuf = new ByteArrayOutputStream();
				int bytesRead = -1;
				while((bytesRead=((GZIPInputStream) resp).read(buf))>0){
					dataBuf.write(buf, 0, bytesRead);
					for(int i=0; i<255; i++)
						buf[i]=0;
				}
				dataBuf.write((byte)'\n');
				r = dataBuf.toString();
			}
			Log.i("CurlOps.delete.ConnApp::", r);
		} catch(Exception e){
			throw e;
		} finally{
			try {
				if(httpCon!=null){
					httpCon.disconnect();
				}
				dataBuf = null;
				
			} catch(Exception e){
				
			}
		}
		return r;

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
