package com.example.firstapp;
import java.util.*;

import org.json.JSONObject;

import android.util.Log;

public class Cache {
	HashMap<String,JSONObject> entries;
	public Cache(){
		entries = new HashMap<String,JSONObject>();
	}
	public void addEntry(String path, JSONObject value){
		entries.put(path, value);
	}
	public JSONObject getEntry(String path){
		return entries.get(path);
	}
}
