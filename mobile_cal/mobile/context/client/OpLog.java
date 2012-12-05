package mobile.context.client;

import mobile.context.app.*;
import java.util.*;
//import mobile.context.msg.OpLogDumpMessageProto;
//import mobile.context.msg.OpLogDumpMessageProto.OpLogDumpMessage;


public class OpLog{
    private ArrayList<OpLogEntry> oplogBuffer = null;
    private static OpLog oplog = null;

    private OpLog(){
        oplogBuffer = new ArrayList<OpLogEntry>();
    }

    public OpLog getInstance(){
        if(oplog==null)
            oplog = new OpLog();
        return oplog;
    }

    public void addEntry(ApplicationObject object, Operation op){
        OpLogEntry entry = new OpLogEntry(object, op, System.currentTimeMillis());
        oplogBuffer.add(entry);
    }

    public void flush(){
        //encode into OpLogDumpMessage
    }

    public class OpLogEntry{
        public ApplicationObject object = null;
        public Operation op = null;
        public long timestamp = -1L;

        public OpLogEntry (ApplicationObject appObj, Operation o, long ts){
            object = appObj;
            op = o;
            timestamp = ts;
        }

        public long getTimestamp (){
            return timestamp;
        }

        public ApplicationObject getAppObject(){
            return object;
        }

        public Operation getOp(){
            return op;
        }
    }


}
