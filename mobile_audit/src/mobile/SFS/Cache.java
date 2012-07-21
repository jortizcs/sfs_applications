package mobile.SFS;

import org.json.JSONObject;

public interface Cache {

	/**
	 * Adds default/stream file entry to the cache.
	 * 
	 * @param path the path for the stream file to cache.
	 * @param pathinfo the json object that's returned on a fetch of the information associated with this file.
	 * @param tsqueryres the query results from a timeseries query on this streamfs file.
	 */
	public void addEntry(String path, JSONObject pathinfo, JSONObject tsqueryres);
	
	/**
	 * Remove the given entry from the cache.
	 * 
	 * @param path
	 */
	public void removeEntry(String path);
	
	/**
	 * Clears the cache.
	 */
	public void flush();
	
	/**
	 * Performs the StreamFS operation on the file with the given path.
	 * 
	 * @param op GET, PUT, POST, DELETE
	 * @param path the path to perform the operation on.
	 * @param data input object for PUT, POST operations.
	 */
	public JSONObject performOp(String op, String path, JSONObject data);
	
	/**
	 * Fetch all the paths starting from the rootpath.  Populating the cache include getting 
	 * all associated information for default files, and 1 HR's worth of data from stream files.
	 * 
	 * @param rootpath
	 */
	public void populateCache(String rootpath);
}
