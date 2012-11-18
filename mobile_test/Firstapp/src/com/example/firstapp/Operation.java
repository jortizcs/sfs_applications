package com.example.firstapp;
import org.json.*;

public class Operation {
	String type;
	String path;
	JSONObject data;
	long ts;
	public Operation(String path, JSONObject data,long ts){
		this.path = path;
		this.data = data;
		type = "PUT";
		this.ts = ts;
	}
}
