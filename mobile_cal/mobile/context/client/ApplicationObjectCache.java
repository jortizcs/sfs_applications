package mobile.context.client;

import java.util.*;
import java.util.concurrent.*;

public class ApplicationObjectCache {

    private static int maxCacheSize= -1;
    private static int cacheSize = -1;
    private static ConcurrentHashMap<ApplicationObject, Date> cacheMap = null;
    private static ConcurrentHashMap<Date, ApplicationObject> reverseCacheMap = null;
    
    public ApplicationObjectCache(int sizeInBytes){
        maxCacheSize = sizeInBytes;
        cacheMap = new ConcurrentHashMap<ApplicationObject, Date>();
        reverseCacheMap = new ConcurrentHashMap<Date, ApplicationObject>();
    }

    public synchronized boolean updateEntry(ApplicationObject object){
        if(cacheMap.containsKey(object))
            cacheMap.replace(object, new Date(System.currentTimeMillis()));
        else{
            int newsize = object.getBytes().length + 8;
            if(cacheSize+newSize<=maxCacheSize){
                Date d = new Date(System.currentTimeMillis());
                cacheMap.put(object, d);
                reverseCacheMap.put(d, object);
            }
            else{
                remove(newSize);
                Date d = new Date(System.currentTimeMillis());
                cacheMap.put(object, d);
                reverseCacheMap.put(d, object);
                cacheSize+=newSize;
            }
        }
    }

    private void remove(int sizeBytes){
        int totalRemoved = 0;
        //removes >=sizeBytes worth of entries from the cache
        ArrayList<Date> list = new ArrayList<Date>(cacheMap.values());
        Collections.sort(list);
        Date thisDate = list.get(0);
        ApplicationObject o = reverseCacheMap.get(thisDate);
        reverseCacheMap.remove(thisDate);
        cacheMap.remove(o);
        totalRemoved += o.getBytes().length+4;
        cacheSize -= totalRemoved;
        list.remove(0);
        while(totalRemoved<sizeBytes && list.size()>0){
            thisDate = list.get(0);
            ApplicationObject o = reverseCacheMap.get(thisDate);
            reverseCacheMap.remove(thisDate);
            cacheMap.remove(o);
            totalRemoved += o.getBytes().length+4;
            cacheSize -= totalRemoved;
            list.remove(0);
        }

    }

    public boolean contains(ApplicationObject object){
        return cacheMap.containsKey(object);
    }

    public Date getLastUpdateTime(ApplicationObject object){
        return cacheMap.get(object);
    }
}
