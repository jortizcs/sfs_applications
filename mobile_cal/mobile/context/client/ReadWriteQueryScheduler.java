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

    public synchronized CallbackHandle schedule(ObjectName objectName, ReadDoneCallback callback, boolean checkBudget){
        try {
            RWQTask task = new RWQTask(objectName, callback, checkBudget);
            Future<?> future = executorService.submit(task);
            CallbackHandle h = new CallbackHandle(future);
            return h;
        } catch(Exception e){}
        return null;
    }

    public synchronized CallbackHandle schedule(Operation op, WriteDoneCallback callback, boolean checkBudget){
        try {
            RWQTask task = new RWQTask(op, callback, checkBudget);
            Future<?> future = executorService.submit(task);
            CallbackHandle h = new CallbackHandle(future);
            return h;
        } catch(Exception e){}
        return null;
    }

    public synchronized CallbackHandle schedule(String query, QueryDoneCallback callback, boolean checkBudget){
        try {
            ObjectName objName = new ObjectName(query);
            RWQTask task = new RWQTask(objName, callback, checkBudget);
            Future<?> future = executorService.submit(task);
            CallbackHandle h = new CallbackHandle(future);
            return h;
        } catch(Exception e){}
        return null;
    }

    public synchronized CallbackHandle schedule(Expression exp, WriteDoneCallback callback, boolean checkBudget){
        try {
            RWQTask task = new RWQTask(expression, callback, checkBudget);
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
        public ObjectName objName = null;
        public Expression exp = null;
        public ReadDoneCallback readCallback = null;
        public WriteDoneCallback writeCallback = null;
        public QueryDoneCallback queryCallback = null;

        public CallbackType callbackType = CallbackType.READ;
        public boolean budgetCheck = false;
        
        public RWQTask(ObjectName objectName, ReadDoneCallback callback, boolean checkBudget){
            callbackType = CallbackType.READ;
            readCallback = callback;
            budgetCheck = checkBudget;
            objName = objectName;
        }

        public RWQTask(Operation op, WriteDoneCallback callback, boolean checkBudget){
            callbackType = CallbackType.WRITE;
            operation = op;
            writeCallback = callback;
            budgetCheck = checkBudget;
        }

        public RWQTask(Expression expression, WriteDoneCallback callback, boolean checkBudget){
            callbackType = CallbackType.WRITE_EXPRESSION;
            exp = expression;
            writeCallback = callback;
            budgetCheck = checkBudget;
        }

        public RWQTask(Operation op, QueryDoneCallback callback, boolean checkBudget){
            callbackType = CallbackType.QUERY;
            operation = op;
            queryCallback = callback;
            budgetCheck = checkBudget;
        }

        public CallbackType getCallbackType(){
            return callbackType;
        }

        public void run(){
            while(true){
                try {
                    switch(callbackType){
                        case READ:
                            if(!budgetCheck){
                                //do it if budget there are no budget concerns
                                ApplicationObject object = server.doRead(objName);
                                readCallback.readDone(object);
                                if(object!=null)
                                    cache.updateEntry(object);
                                return;
                            } else {
                                //check your budget before sending, don't send until you can afford to
                                boolean inCache = cache.contains(objectName);
                                boolean canAfford = false;
                                if(inCache)
                                    canAfford = budgeter.canAfford(cache.get(objectName).getBytes().length);
                                else
                                    canAfford = budgeter.canAfford(ApplicationObjectCache.AVG_OBJ_SIZE);
                                if(canAfford){
                                    ApplicationObject object = server.doRead(objName);
                                    readCallback.readDone(object);
                                    if(object!=null)
                                        cache.updateEntry(object);
                                    return;
                                }
                            }
                        case WRITE:
                            if(!budgetCheck){
                                //do it if budget there are no budget concerns
                                ApplicationObject[] objects = server.doWrite(operation);
                                writeCallback.writeDone(objects);
                                if(object!=null)
                                    cache.updateEntries(objects);
                                return;
                            } else {
                                //check your budget before sending, don't send until you can afford to
                                ApplicationObject[] objectNames = operation.getObjectParamNames();
                                int totalObjSize = 0;
                                if(objectsNames!=null && objectNames.length>0){
                                    int appObjCnt =0;
                                    for(int i=0; i<objectNames.length; i++){
                                        ApplicationObject thisObj = cache.get(objectNames[i]);
                                        if(cache.contains(objectNames[i])){
                                            appObjCnt+=1;
                                            totalObjSize += thisObj.getBytes().length;
                                        } else{
                                            totalObjSize += ApplicationObjectCache.AVG_OBJ_SIZE;
                                        }
                                    }
                                }
                                boolean canAfford = false;
                                if(inCache)
                                    canAfford = budgeter.canAfford(totalObjSize);
                                else
                                    canAfford = budgeter.canAfford(totalObjSize);
                                if(canAfford){
                                    ApplicationObject[] objects = server.doWrite(operation);
                                    writeCallback.writeDone(objects);
                                    if(objects!=null)
                                        cache.updateEntries(objects);
                                    return;
                                }
                            }
                        case WRITE_EXPRESSION:
                            if(!budgetCheck){
                                //do it if budget there are no budget concerns
                                ApplicationObject[] objects = server.doWrite(exp);
                                writeCallback.writeDone(objects);
                                if(objects!=null)
                                    cache.updateEntries(objects);
                                return;
                            } else {
                                //check your budget before sending, don't send until you can afford to
                                Operation[] operations = exp.getOperations();
                                int totalObjSize = 0;
                                for(int j=0; j<operations.length; j++){
                                    Operation thisOp = operations[j];
                                    ApplicationObject[] objectNames = thisOp.getObjectParamNames();
                                    if(objectsNames!=null && objectNames.length>0){
                                        int appObjCnt =0;
                                        for(int i=0; i<objectNames.length; i++){
                                            ApplicationObject thisObj = cache.get(objectNames[i]);
                                            if(cache.contains(objectNames[i])){
                                                appObjCnt+=1;
                                                totalObjSize += thisObj.getBytes().length;
                                            } else{
                                                totalObjSize += ApplicationObjectCache.AVG_OBJ_SIZE;
                                            }
                                        }
                                    }
                                }
                                boolean canAfford = false;
                                if(inCache)
                                    canAfford = budgeter.canAfford(totalObjSize);
                                else
                                    canAfford = budgeter.canAfford(totalObjSize);
                                if(canAfford){
                                    ApplicationObject[] objects = server.doWrite(exp);
                                    writeCallback.writeDone(objects);
                                    if(objects!=null)
                                        cache.updateEntries(objects);
                                    return;
                                }
                            }
                            break;
                        case QUERY:
                            if(!budgetCheck){
                                //do it if budget there are no budget concerns
                                ApplicationObject queryRes = server.doQuery(operation)
                                queryCallback.queryDone(queryRes);
                                if(queryRes!=null)
                                    cache.updatEntry(queryRes);
                                return;
                            } else {
                                //check your budget before sending, don't send until you can afford to
                                boolean inCache = cache.contains(objectName);
                                boolean canAfford = false;
                                if(inCache)
                                    canAfford = budgeter.canAfford(cache.get(objectName).getBytes().length);
                                else
                                    canAfford = budgeter.canAfford(ApplicationObjectCache.AVG_OBJ_SIZE);
                                if(canAfford){
                                    ApplicationObject queryRes = server.doQuery(objectName.getStringName());
                                    queryCallback.queryDone(queryRes);
                                    if(queryRes!=null)
                                        cache.updatEntry(queryRes);
                                    return;
                                }
                            }
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
