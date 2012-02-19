package mobile.SFS;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONObject;

public class Taxonomy {
	private Hashtable<String, String> symlinks_ = new Hashtable<String, String>();
	private Hashtable<String, String> leaves_ = new Hashtable<String, String>();
	
	public Taxonomy(String taxPath) {
		popLeaves(taxPath);
	}
	
	private void popLeaves(String taxPath) {
		try {
			String slItems = CurlOps.get(taxPath + "/*?query=true&type=symlink");
			JSONObject slItemsObj = new JSONObject(slItems);
			Iterator sPaths = slItemsObj.keys();
			
			while(sPaths.hasNext())
				symlinks_.put((String) sPaths.next(), "");

			String taxAll = CurlOps.get(taxPath + "/*");
			JSONObject taxAllObj = new JSONObject(taxAll);
			Iterator taxAllKeys = taxAllObj.keys();

			while(taxAllKeys.hasNext()) {
				String tKey = (String) taxAllKeys.next();
				JSONObject getObj = taxAllObj.getJSONObject(tKey);
				JSONArray children = getObj.getJSONArray("children");
				int numChildren = children.length();
				
				if(numChildren == 0 && !symlinks_.contains(tKey)) {
					StringTokenizer tokenizer = new StringTokenizer(tKey, "//");
					Vector allTokens = new Vector();
					
					while (tokenizer.hasMoreTokens())
						allTokens.addElement(tokenizer.nextToken());
					
					String tLeafName = (String) allTokens.elementAt(allTokens.size() - 1);
					leaves_.put(tLeafName, tKey);
				}
				else if(numChildren > 0 && !symlinks_.contains(tKey)) {
					boolean allSymlinks = true;
					int j = 0;
					
					while(j < numChildren) {
						String thisChild = (String) children.get(j);
						
						if(!thisChild.contains("->")) {
							allSymlinks = false;
							break;
						}
						
						j += 1;
					}

					if(allSymlinks) {
						StringTokenizer tokenizer = new StringTokenizer(tKey, "//");
						Vector allTokens = new Vector();
						
						while (tokenizer.hasMoreTokens())
							allTokens.addElement(tokenizer.nextToken());
						
						String tLeafName = (String) allTokens.elementAt(allTokens.size() - 1);
						leaves_.put(tLeafName, tKey);
					}
				}
			}

		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public ArrayList<String> getLeaves() {
		ArrayList<String> arr = new ArrayList<String>();
		
		for(String s : leaves_.values())
			arr.add(s);
		
		return arr;
	}
	
	public String getPath(String leaf) {
		return leaf == null ? null : leaves_.get(leaf);
	}
}
