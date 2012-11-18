package com.example.firstapp;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.*;

import org.json.JSONObject;

public class OpLog {
	LinkedBlockingQueue<Operation> log;
	public OpLog(){
		log = new LinkedBlockingQueue<Operation>();
	}
	public void addEntry(String path, JSONObject data){
		Date date = new Date();
		long ts = date.getTime();
		log.add(new Operation(path,data,ts));
	}
	public boolean isEmpty(){
		return log.isEmpty();
	}
	public LinkedBlockingQueue<Operation> flushLog(){
		LinkedBlockingQueue<Operation> retval = log;
		log = new LinkedBlockingQueue<Operation>();
		return retval;
	}
}
