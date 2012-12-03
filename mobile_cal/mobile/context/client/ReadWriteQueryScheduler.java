package mobile.context.client;

import mobile.context.app.*;

import java.util.*;
import java.util.concurrent.*;

public class ReadWriteQueryScheduler{
    private static ReadWriteQueryScheduler scheduler = null;
    private static ApplicationServer server = null;
    private static ApplicationObjectCache cache = null;

    private static ExecutorService executorService = null;
    private static boolean isConnected = false;

    enum CallbackType {
        READ,WRITE,WRITE_EXPRESSION,QUERY
    }

    private ReadWriteQueryScheduler(ApplicationServer s){
        server = s;
        executorService = Executors.newCachedThreadPool();
        cache = ApplicationObjectCache.getInstance(0/*uses default cache size*/);
        setupConnectionInfo();
    }

    public static ReadWriteQueryScheduler getInstance(ApplicationServer s){
        if(scheduler == null)
            scheduler = new ReadWriteQueryScheduler(s);
        return scheduler;
    }

    public boolean cancel(ReadWriteQueryTaskQueueElement thisElt){
        return true;
    }

    public synchronized CallbackHandle schedule(ObjectName objectName, ReadDoneCallback callback){
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

    public synchronized CallbackHandle schedule(String query, QueryDoneCallback callback){
        try {
            RWQTask task = new RWQTask(op, callback);
            Future<?> future = executorService.submit(task);
            CallbackHandle h = new CallbackHandle(future);
            return h;
        } catch(Exception e){}
        return null;
    }

    public synchronized CallbackHandle schedule(Expression exp, QueryDoneCallback callback){
        try {
            RWQTask task = new RWQTask(expression, callback);
            Future<?> future = executorService.submit(task);
            CallbackHandle h = new CallbackHandle(future);
            return h;
        } catch(Exception e){}
        return null;
    }

    private void setupConnectionInfo(){
        isConnected = CALObjectLayer.appServerIsUp;
        CALObjectLayer.registerOnConnStateChange(new OnConnStateChangeCallback(){
            public void stateChange(boolean up){
                isConnected = up;
            }
        });
    }

    public class RWQTask implements Runnable{

        public Operation operation = null;
        public Expression exp = null;
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

        public RWQTask(Expression expression, WriteDoneCallback callback){
            callbackType = CallbackType.WRITE_EXPRESSION;
            exp = expression;
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
            while(true){
                try {
                    switch(callbackType){
                        case READ:
                            ApplicationObject object = server.doRead(operation);
                            readCallback.readDone(object);
                            if(object!=null)
                                cache.updateEntry(object);
                            return;
                        case WRITE:
                            ApplicationObject object = server.doWrite(operation);
                            writeCallback.writeDone(object);
                            if(object!=null)
                                cache.updateEntry(object);
                            return;
                        case WRITE_EXPRESSION:
                            ApplicationObject[] objects = server.doWrite(exp);
                            writeCallback.writeDone(objects);
                            if(objects!=null)
                                cache.updateEntries(objects);
                            return;
                        case QUERY:
                            ApplicationObject queryRes = server.doQuery(operation)
                            queryCallback.queryDone(queryRes);
                            if(queryRes!=null)
                                cache.updatEntry(queryRes);
                            return;
                    }
                } catch(Exception e){
                    localSleep();
                }
            }
        }

        private void localSleep(){
            try{
                //to prevent an explosion of threads all trying to read from the network
                int jitter = Random.nextInt();
                Thread.sleep(CALObjectLayer.accessFrequency+1000+jitter); 
            } catch(Exception e){
                e.printStackTrace();
            }
        }
    }
}
