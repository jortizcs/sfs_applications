package mobile.SFS;

import org.json.JSONObject;

public interface Cache {

	public void addEntry(String path, JSONObject pathinfo, JSONObject tsqueryres);
	
	public void addEntry(String path, JSONObject pathinfo);
	
	public void removeEntry(String path);
	
	public void flush();
	
	public void performOp(String op, String path, JSONObject pathinfo);
	
	public void populateCache(String rootpath);
}
