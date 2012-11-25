package mobile.context.client;

import mobile.context.app.*;

import java.util.*;
import java.util.concurrent.*;

public class ReadWriteQueryScheduler{
    private static ReadWriteQueryScheduler scheduler = null;
    private static ApplicationServer server = null;

    private static ExecutorService executorService = null;

    enum CallbackType {
        READ,WRITE,QUERY
    }

    private ReadWriteQueryScheduler(ApplicationServer s){
        server = s;
        executorService = Executors.newCachedThreadPool();
    }

    public static ReadWriteQueryScheduler getInstance(ApplicationServer s){
        if(scheduler == null)
            scheduler = new ReadWriteQueryScheduler(s);
        return scheduler;
    }

    public boolean cancel(ReadWriteQueryTaskQueueElement thisElt){
        return true;
    }

    public synchronized CallbackHandle schedule(Operation op, ReadDoneCallback callback){
        try {
            RWQTask task = new RWQTask(op, callback);
            Future<?> future = executorService.submit(task);
            CallbackHandle h = new CallbackHandle(future);
            return h;
        } catch(Exception e){}
        return null;
    }

    public synchronized CallbackHandle schedule(Operation op, WriteDoneCallback callback){
        try {
            RWQTask task = new RWQTask(op, callback);
            Future<?> future = executorService.submit(task);
            CallbackHandle h = new CallbackHandle(future);
            return h;
        } catch(Exception e){}
        return null;
    }

    public synchronized CallbackHandle schedule(Operation op, QueryDoneCallback callback){
        try {
            RWQTask task = new RWQTask(op, callback);
            Future<?> future = executorService.submit(task);
            CallbackHandle h = new CallbackHandle(future);
            return h;
        } catch(Exception e){}
        return null;
    }

    public class RWQTask implements Runnable{

        public Operation operation = null;
        public ReadDoneCallback readCallback = null;
        public WriteDoneCallback writeCallback = null;
        public QueryDoneCallback queryCallback = null;

        public CallbackType callbackType = CallbackType.READ;
        
        public RWQTask(Operation op, ReadDoneCallback callback){
            callbackType = CallbackType.READ;
            operation = op;
            readCallback = callback;
        }

        public RWQTask(Operation op, WriteDoneCallback callback){
            callbackType = CallbackType.WRITE;
            operation = op;
            writeCallback = callback;
        }

        public RWQTask(Operation op, QueryDoneCallback callback){
            callbackType = CallbackType.QUERY;
            operation = op;
            queryCallback = callback;
        }

        public CallbackType getCallbackType(){
            return callbackType;
        }

        public void run(){
            //go get the data -- blocking
            switch(callbackType){
                case READ:
                    readCallback.readDone(server.doRead(operation));
                    break;
                case WRITE:
                    writeCallback.writeDone(server.doRead(operation));
                    break;
                case QUERY:
                    queryCallback.queryDone(server.doQuery(operation));
                    break;
            }
        }
    }
}
