package mobile.SFS;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

public class CurlOpsReal {
	public static String get(String urlString) throws Exception {
		String total = new String("");
		// Create a URL for the desired page
		URL url = new URL(urlString);

		// Read all the text returned by the server
		BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
		String str;
		while ((str = in.readLine()) != null) {
			// str is one line of text; readLine() strips the newline
			// character(s)
			total = total + str;
		}
		in.close();
		return total;

	}
	
	public static String post(String data, String urlString) throws Exception {
		String total = new String("");
		// Construct data
		// String data = URLEncoder.encode("key1", "UTF-8") + "=" +
		// URLEncoder.encode("value1", "UTF-8");
		// data += "&" + URLEncoder.encode("key2", "UTF-8") + "=" +
		// URLEncoder.encode("value2", "UTF-8");

		// Send data
		URL url = new URL(urlString);
		URLConnection conn = url.openConnection();
		conn.setDoOutput(true);
		OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
		wr.write(data);
		wr.flush();

		// Get the response
		BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		String line;
		while ((line = rd.readLine()) != null) {
			// Process line...
			total = total + line;
		}
		wr.close();
		rd.close();
		return total;

	}
	
	public static String put(String data, String urlString) throws Exception {
		URL url = new URL(urlString);
		HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
		httpCon.setDoOutput(true);
		httpCon.setRequestMethod("PUT");
		OutputStreamWriter out = new OutputStreamWriter(httpCon.getOutputStream());
		out.write(data);
		out.close();
		try {
			return httpCon.getResponseMessage();
		}
		catch(EOFException e) {
			return "";
		}
	}
	
	public static String delete(String urlString) throws Exception {
		URL url = new URL(urlString);
		HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
		httpCon.setDoOutput(true);
		httpCon.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		httpCon.setRequestMethod("DELETE");
		httpCon.connect();
		//return urlString;
		return httpCon.getResponseMessage();
	}
}
