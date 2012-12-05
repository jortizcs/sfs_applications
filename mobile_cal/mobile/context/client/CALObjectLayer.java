package mobile.context.client;

import mobile.context.app.*;

import java.util.*;

public abstract class CALObjectLayer{

    protected static ApplicationServer appServer = null;
    protected static boolean appServerIsUp = false;
    private static ApplicationObjectCache cache = null;
    private static ReadWriteQueryScheduler scheduler = null;
    private static NetAccessThread netAccessThread = null;
    private static EnergyBudgeter budgeter = null;

    /**
     * Instantiate the CALObjectLayer;
     */
    public CALObjectLayer(ApplicationServer server){
        appServer = server;
        if(cache==null)
            cache = ApplicationObjectCache.getInstance(0 /*sizeInBytes, default is 1GB*/);
        if(scheduler == null)
            scheduler = ReadWriteQueryScheduler.getInstance(appServer);
        if(netAccessThread == null){
            netAccessThread = new NetAccessThread();
            netAccessThread.start();
        }
    }
    
    /**
     * Attempts to read from network, if connection inactive; read local copy or return null
     * @param objectName the name of the object
     * @return the associated ApplicationObject or null if it does not exist.  Non-existence implies either that the 
     *          object does not exist or that it does, but is not accessible.  ApplicationObject's are not accessible
     *          when there is no local copy and the server is inaccessible.
     */
    public ApplicationObject read(ObjectName objectName, boolean cacheResult){
        try {
            ApplicationObject thisObject = appServer.doRead(objectName);
            if(thisObject == null && cache.contains(objectName)){
                return cache.get(objectName);
            } else if (cacheResult && thisObject!=null){
                cache.updateEntry(thisObject);
            }
            return thisObject;
        } catch(Exception e){
        }
        return null;
    }

    /**
     * Attempts to read a local copy first, if it was last refreshed <= freshness milliseconds ago.
     * Otherwise, tries to read it from the network.  If the freshness threshold is exceeded and the 
     * network/server is down, returns null.  Otherwise it returns the object.
     * 
     * @param objectName the name of the object
     * @param freshness maximum threshold, in milliseconds, since the last time the ApplicationObject was updated in
                the cache.
     * @return the associated ApplicationObject or null if it does not exist.  Non-existence implies either that the 
     *          object does not exist or that it does, but is not accessible.  ApplicationObject's are not accessible
     *          when there is no local copy and the server is inaccessible.
     */
    public ApplicationObject read(ObjectName objectName, long freshness, boolean cacheResult){
        ApplicationObject appObject = null;
        if(cache.contains(objectName)){
            appObject = cache.get(objectName);
            if((System.currentTimeMillis()-cache.getLastUpdateTime(appObject).getTime())<=freshness)
                return appObject;
        } else {
            try {
                appObject = appServer.doRead(objectName);
                if(cacheResult && appObject!=null)
                    cache.updateEntry(appObject);
            } catch(Exception e){
            }
        }
        return appObject;
    }

    /**
     * Registers a callback that is called when the object referred to by objectName is attained from the server
     * and refereshed in the cache.
     *
     * @param objectName the name of the object.
     * @param callback called when the given object is read from the server and placed in the local cache.
     * @return CallbackHandle allows you to check the state of the callback and cancel the request if necessary.
     */
    public CallbackHandle read(ObjectName objectName, ReadDoneCallback callback, boolean cacheResult){
        return scheduler.schedule(objectName, callback, false, cacheResult);
    }

    /**
     * Attempts to read from network, you can afford to fetch it and the connection is active.
     * If the connection is inactive; read local copy or return null.
     * @param objectName the name of the object
     * @return the associated ApplicationObject or null if it does not exist.  Non-existence implies either that the 
     *          object does not exist or that it does, but is not accessible.  ApplicationObject's are not accessible
     *          when there is no local copy and the server is inaccessible.
     */
    public ApplicationObject readEOP(ObjectName objectName, boolean cacheResult){
        boolean inCache = cache.contains(objectName);
        boolean canAfford = false;
        if(inCache)
            canAfford = budgeter.canAfford(cache.get(objectName).getBytes().length);
        else
            canAfford = budgeter.canAfford(ApplicationObjectCache.AVG_OBJ_SIZE);

        if(canAfford){
            return read(objectName, cacheResult);
        } else if(inCache && !canAfford){
            return cache.get(objectName);
        }
        return null;
    }

    /**
     * Attempts to read a local copy first, if it was last refreshed <= freshness milliseconds ago.
     * Otherwise, tries to read it from the network.  If the freshness threshold is exceeded and the 
     * network/server is down, returns null.  Otherwise it returns the object.
     * 
     * @param objectName the name of the object
     * @param freshness maximum threshold, in milliseconds, since the last time the ApplicationObject was updated in
                the cache.
     * @return the associated ApplicationObject or null if it does not exist.  Non-existence implies either that the 
     *          object does not exist or that it does, but is not accessible.  ApplicationObject's are not accessible
     *          when there is no local copy and the server is inaccessible.
     */
    public ApplicationObject readEOP(ObjectName objectName, long freshness, boolean cacheResult){
        boolean inCache = cache.contains(objectName);
        boolean canAfford = false;
        if(inCache)
            canAfford = budgeter.canAfford(cache.get(objectName).getBytes().length);
        else
            canAfford = budgeter.canAfford(ApplicationObjectCache.AVG_OBJ_SIZE);


        if(canAfford){
            return read(objectName, freshness, cacheResult);
        } else if(inCache && !canAfford){
            ApplicationObject appObject = cache.get(objectName);
            if((System.currentTimeMillis()-cache.getLastUpdateTime(appObject).getTime())<=freshness)
                return appObject;
        }
        return null;
    }

    /**
     * Registers a callback that is called when the object referred to by objectName is attained from the server
     * and refereshed in the cache.
     *
     * @param objectName the name of the object.
     * @param callback called when the given object is read from the server and placed in the local cache.
     * @return CallbackHandle allows you to check the state of the callback and cancel the request if necessary.
     */
    public CallbackHandle readEOP(ObjectName objectName, ReadDoneCallback callback, boolean cacheResult){
        return scheduler.schedule(objectName, callback, true, cacheResult);
    }

    /**
     * Attempts to write to the server first.  If connection is inactive, writes to the local copy or returns null
     * if the object is not accessible at all.
     * @param objectName the name of the object
     * @param op the operation to perform on the object
     * @return the ApplicationObject after the operation has been applied to it, or null if it does not exist.  
     *          Non-existence implies either that the 
     *          object does not exist or that it does, but is not accessible.  ApplicationObject's are not accessible
     *          when there is no local copy and the server is inaccessible.
     */
    public ApplicationObject[] write(ObjectName[] objectNames, byte[] data, Operation op ){
        ApplicationObject[] xformedObjects = null;
        if(op!=null && objectNames.length>0){
            op.setParams(objectNames);
            op.setData(data);
            try {
                xformedObjects = appServer.doWrite(op);
            } catch(Exception e){
                return op.executeLocal();
            }
            return xformedObjects;
        }
        return xformedObjects;
    }

    /**
     * Attempts to write a local copy first, if it was last refreshed <= freshness milliseconds ago.
     * Otherwise, tries to write it to the server.  If the freshness threshold is exceeded and the 
     * network/server is down, returns null.  Otherwise it returns the object.
     * 
     * @param objectName the name of the object
     * @param op the operation to apply
     * @param freshness maximum threshold, in milliseconds, since the last time the ApplicationObject was updated in
                the cache.
     * @return the associated ApplicationObject or null if it does not exist.  Non-existence implies either that the 
     *          object does not exist or that it does, but is not accessible.  ApplicationObject's are not accessible
     *          when there is no local copy and the server is inaccessible.
     */
    public ApplicationObject[] write(ObjectName[] objectNames, byte[] data, Operation op, long freshness){
        ApplicationObject[] xformedObjects=null;
        boolean allFresh = true;
        int appObjCnt =0;
        if(op!=null && objectNames.length>0){
            op.setParams(objectNames);
            op.setData(data);
            try {
                xformedObjects = appServer.doWrite(op);
            } catch(Exception e){
                for(int i=0; i<objectNames.length; i++){
                    ApplicationObject thisObj = cache.get(objectNames[i]);
                    if(cache.contains(objectNames[i])){
                        appObjCnt+=1;
                        if(System.currentTimeMillis()-cache.getLastUpdateTime(thisObj).getTime()>freshness)
                            allFresh = false;
                    } 
                }
                if(allFresh && objectNames.length==appObjCnt)
                    return op.executeLocal();
            }
            if(xformedObjects==null){
                for(int i=0; i<objectNames.length; i++){
                    ApplicationObject thisObj = cache.get(objectNames[i]);
                    if(cache.contains(objectNames[i])){
                        appObjCnt+=1;
                        if(System.currentTimeMillis()-cache.getLastUpdateTime(thisObj).getTime()>freshness)
                            allFresh = false;
                    } 
                }
                if(allFresh && objectNames.length==appObjCnt)
                    return op.executeLocal();
            }
            return xformedObjects;
        }
        return null;
    }

    /**
     * Registers a callback that is called when the object referred to by objectName is written to the server
     * and refereshed in the cache.
     *
     * @param objectName the name of the object.
     * @param op the operation to be applied to the ApplicationObject
     * @param callback called when the given object is read from the server and placed in the local cache.
     * @return CallbackHandle allows you to check the state of the callback and cancel the request if necessary.
     */
    public CallbackHandle write(ObjectName[] objectNames, byte[] data, Operation op, WriteDoneCallback callback){
        if(objectNames!=null && objectNames.length>0){
            op.setParams(objectNames);
            op.setData(data);
            return scheduler.schedule(op, callback, false);
        }
        return null;
    }

    /**
     * Attempts to write to the server first.  If connection is inactive, writes to the local copy or returns null
     * if the object is not accessible at all.
     * @param objectName the name of the object
     * @param op the operation to perform on the object
     * @return the ApplicationObject after the operation has been applied to it, or null if it does not exist.  
     *          Non-existence implies either that the 
     *          object does not exist or that it does, but is not accessible.  ApplicationObject's are not accessible
     *          when there is no local copy and the server is inaccessible.
     */
    public ApplicationObject[] writeEOP(ObjectName[] objectNames, byte[] data, Operation op){
        int totalObjSize = 0;
        if(objectNames!=null && objectNames.length>0){

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

            boolean canAfford = false;
            canAfford = budgeter.canAfford(totalObjSize);
            if(canAfford){
                op.setParams(objectNames);
                op.setData(data);
                try {
                    ApplicationObject[] modifiedObjects = appServer.doWrite(op);
                    return modifiedObjects;
                } catch(Exception e){
                    if(appObjCnt==objectNames.length){
                        return op.executeLocal();
                    }
                }
            }
        }
        return null;
    }

    /**
     * Attempts to write a local copy first, if it was last refreshed <= freshness milliseconds ago.
     * Otherwise, tries to write it to the server.  If the freshness threshold is exceeded and the 
     * network/server is down, returns null.  Otherwise it returns the object.
     * 
     * @param objectName the name of the object
     * @param op the operation to apply
     * @param freshness maximum threshold, in milliseconds, since the last time the ApplicationObject was updated in
                the cache.
     * @return the associated ApplicationObject or null if it does not exist.  Non-existence implies either that the 
     *          object does not exist or that it does, but is not accessible.  ApplicationObject's are not accessible
     *          when there is no local copy and the server is inaccessible.
     */
    public ApplicationObject[] writeEOP(ObjectName[] objectNames, byte[] data, Operation op, long freshness){
        int totalObjSize = 0;
        boolean allFresh = true;
        if(objectNames!=null && objectNames.length>0){
            int appObjCnt =0;
            for(int i=0; i<objectNames.length; i++){
                ApplicationObject thisObj = cache.get(objectNames[i]);
                if(cache.contains(objectNames[i])){
                    appObjCnt+=1;
                    totalObjSize += thisObj.getBytes().length;
                    if(System.currentTimeMillis()-cache.getLastUpdateTime(thisObj).getTime()>freshness)
                        allFresh = false;
                } else{
                    totalObjSize += ApplicationObjectCache.AVG_OBJ_SIZE;
                }
            }

            boolean canAfford = false;
            canAfford = budgeter.canAfford(totalObjSize);
            if(canAfford){
                op.setParams(objectNames);
                op.setData(data);
                try {
                    ApplicationObject[] modifiedObjects = appServer.doWrite(op);
                    return modifiedObjects;
                } catch(Exception e){
                    if(appObjCnt==objectNames.length && allFresh){
                        return op.executeLocal();
                    }
                }
            }
        }
        return null;
    }

    /**
     * Registers a callback that is called when the object referred to by objectName is written to the server
     * and refereshed in the cache.
     *
     * @param objectName the name of the object.
     * @param op the operation to be applied to the ApplicationObject
     * @param callback called when the given object is read from the server and placed in the local cache.
     * @return CallbackHandle allows you to check the state of the callback and cancel the request if necessary.
     */
    public CallbackHandle writeEOP(ObjectName[] objectNames, byte[] data, Operation op, WriteDoneCallback callback){
        if(objectNames!=null && objectNames.length>0){
            op.setParams(objectNames);
            op.setData(data);
            return scheduler.schedule(op, callback, true);
        }
        return null;
    }

    /**
     * Attempts to write to the server first.  If connection is inactive, writes to the local copy or returns null
     * if the object is not accessible at all.
     * @param objectName the name of the object
     * @param op the operation to perform on the object
     * @return the ApplicationObject after the operation has been applied to it, or null if it does not exist.  
     *          Non-existence implies either that the 
     *          object does not exist or that it does, but is not accessible.  ApplicationObject's are not accessible
     *          when there is no local copy and the server is inaccessible.
     */
    public ApplicationObject[] write(Expression e){
        ApplicationObject[] xformedObjects=null;
        if(e!=null && e.getOperations()!=null && e.getOperations().length>0){
            try {
                xformedObjects = appServer.doWriteExpression(e);
            } catch(Exception ex){
                return e.executeLocal();
            }
        }
        return xformedObjects;
    }

    /**
     * Attempts to write a local copy first, if it was last refreshed <= freshness milliseconds ago.
     * Otherwise, tries to write it to the server.  If the freshness threshold is exceeded and the 
     * network/server is down, returns null.  Otherwise it returns the object.
     * 
     * @param e the expression to the apply.
     * @param freshness maximum threshold, in milliseconds, since the last time the ApplicationObject was updated in
                the cache.
     * @return the associated ApplicationObjects or null if it does not exist.  Non-existence implies either that the 
     *          object does not exist or that it does, but is not accessible.  ApplicationObject's are not accessible
     *          when there is no local copy and the server is inaccessible.
     */
    public ApplicationObject[] write(Expression e, long freshness){
        if(e!=null && e.getOperations()!=null && e.getOperations().length>0){
            Operation[] operations = e.getOperations();
            int objCnt = 0;
            int totalObjCnt = 0;
            for(int i=0; i<operations.length; i++){
                Operation thisOp = operations[i];
                ObjectName[] objectNames = thisOp.getObjectParamNames();
                if(thisOp!=null && objectNames!=null && objectNames.length>0){
                    for(int j=0; j<objectNames.length; j++){
                        totalObjCnt +=1;
                        if(cache.contains(objectNames[j]) && (System.currentTimeMillis()-cache.getLastUpdateTime(cache.get(objectNames[j])).getTime())<=freshness)
                            objCnt +=1;
                    }
                }
            }

            //if all the objects are in cache and they're all fresh then execute it locally
            //and have the synchronizer flush it to the server later
            if(objCnt == totalObjCnt){
                return e.executeLocal();
            } else {
                try {
                    return appServer.doWriteExpression(e);
                } catch(Exception ex){
                    //couldn't contact the server, ignore it and return null
                }
            }
        }
        return null;
    }

    /**
     * Registers a callback that is called when the object referred to by objectName is written to the server
     * and refereshed in the cache.
     *
     * @param e the Expression to be applied atomically.
     * @param callback called when the given object is read from the server and placed in the local cache.
     * @return CallbackHandle allows you to check the state of the callback and cancel the request if necessary.
     */
    public CallbackHandle write(Expression e, WriteDoneCallback callback){
        if(e!=null && e.getOperations()!=null && e.getOperations().length>0)
            return scheduler.schedule(e, callback, false);
        return null;
    }

    /**
     * Attempts to write to the server first.  If connection is inactive, writes to the local copy or returns null
     * if the object is not accessible at all.
     * @param objectName the name of the object
     * @param op the operation to perform on the object
     * @return the ApplicationObject after the operation has been applied to it, or null if it does not exist.  
     *          Non-existence implies either that the 
     *          object does not exist or that it does, but is not accessible.  ApplicationObject's are not accessible
     *          when there is no local copy and the server is inaccessible.
     */
    public ApplicationObject[] writeEOP(Expression e){
        if(e!=null && e.getOperations()!=null && e.getOperations().length>0){
            Operation[] operations = e.getOperations();
            int objCnt = 0;
            int totalObjCnt = 0;
            int totalReqSize = 0;
            for(int i=0; i<operations.length; i++){
                Operation thisOp = operations[i];
                ObjectName[] objectNames = thisOp.getObjectParamNames();
                if(thisOp!=null && objectNames!=null && objectNames.length>0){
                    for(int j=0; j<objectNames.length; j++){
                        totalObjCnt +=1;
                        if(cache.contains(objectNames[j])){
                            totalReqSize += cache.get(objectNames[j]).getBytes().length;
                            objCnt +=1;
                        } else {
                            totalReqSize += ApplicationObjectCache.AVG_OBJ_SIZE;
                        }
                    }
                }
            }

            if(budgeter.canAfford(totalReqSize)){
                try {
                    return appServer.doWriteExpression(e);
                } catch(Exception ex){
                    if(objCnt == totalObjCnt)
                        return e.executeLocal();
                }
            } else if(objCnt == totalObjCnt){
                return e.executeLocal();
            }
        }
        return null;
    }

    /**
     * Attempts to write a local copy first, if it was last refreshed <= freshness milliseconds ago.
     * Otherwise, tries to write it to the server.  If the freshness threshold is exceeded and the 
     * network/server is down, returns null.  Otherwise it returns the object.
     * 
     * @param e the expression to the apply.
     * @param freshness maximum threshold, in milliseconds, since the last time the ApplicationObject was updated in
                the cache.
     * @return the associated ApplicationObjects or null if it does not exist.  Non-existence implies either that the 
     *          object does not exist or that it does, but is not accessible.  ApplicationObject's are not accessible
     *          when there is no local copy and the server is inaccessible.
     */
    public ApplicationObject[] writeEOP(Expression e, long freshness){
        if(e!=null && e.getOperations()!=null && e.getOperations().length>0){
            Operation[] operations = e.getOperations();
            int objCnt = 0;
            int totalObjCnt = 0;
            int totalReqSize = 0;
            for(int i=0; i<operations.length; i++){
                Operation thisOp = operations[i];
                ObjectName[] objectNames = thisOp.getObjectParamNames();
                if(thisOp!=null && objectNames!=null && objectNames.length>0){
                    for(int j=0; j<objectNames.length; j++){
                        totalObjCnt +=1;
                        if(cache.contains(objectNames[j]) && (System.currentTimeMillis()-cache.getLastUpdateTime(cache.get(objectNames[j])).getTime())<=freshness ){
                            totalReqSize += cache.get(objectNames[j]).getBytes().length;
                            objCnt +=1;
                        } else {
                            totalReqSize += ApplicationObjectCache.AVG_OBJ_SIZE;
                        }
                    }
                }
            }

            if(budgeter.canAfford(totalReqSize)){
                try {
                    return appServer.doWriteExpression(e);
                } catch(Exception ex){
                    if(objCnt == totalObjCnt)
                        return e.executeLocal();
                }
            } else if(objCnt == totalObjCnt){
                return e.executeLocal();
            }
                    
        }
        return null;
    }

    /**
     * Registers a callback that is called when the object referred to by objectName is written to the server
     * and refereshed in the cache.
     *
     * @param e the Expression to be applied atomically.
     * @param callback called when the given object is read from the server and placed in the local cache.
     * @return CallbackHandle allows you to check the state of the callback and cancel the request if necessary.
     */
    public CallbackHandle writeEOP(Expression e, WriteDoneCallback callback){
        if(e!=null && e.getOperations()!=null && e.getOperations().length>0)
            return scheduler.schedule(e, callback, true);
        return null;
    }

    /**
     * Returns the current connection state (network access bit).
     *
     * @returns true if connect, false otherwise.
     */
    public boolean getConnectionState(){
        return appServerIsUp;
    }

    /**
     * Triggers the callback when the connection state changes.
     *
     * @param callback triggered when the connection state changes.
     */
    public void registerOnConnStateChange(OnConnStateChangeCallback callback){
        netAccessThread.register(callback);
    }

    /**
     * Removes the callback. 
     *
     * @param callback triggered when the connection state changes.
     */
    public void unregisterOnConnStateChange(OnConnStateChangeCallback callback){
        netAccessThread.unregister(callback);
    }

    public class NetAccessThread extends Thread{
        private ArrayList<OnConnStateChangeCallback> callbacks = null;
        private int freqSec = 60;
        
        public NetAccessThread(){
            callbacks = new ArrayList<OnConnStateChangeCallback>();
        }

        public void register(OnConnStateChangeCallback c){
            callbacks.add(c);
        }

        public void unregister(OnConnStateChangeCallback c){
            callbacks.remove(c);
        }

        public void setFrequency(int freq/*seconds*/){
            if(freq>0)
                freqSec = freq;
        }

        public void run(){
            while(true){
                try {
                    boolean thisState = appServer.isUp();
                    if(thisState!=appServerIsUp){
                        for(int i=0; i<callbacks.size(); i++)
                            callbacks.get(i).stateChanged(thisState);
                        appServerIsUp = thisState;
                    }
                    Thread.sleep(freqSec*1000);
                } catch(Exception e){
                    e.printStackTrace();
                }
            }
        }

    }
}
