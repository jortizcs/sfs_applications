package mobile.context.client;

import mobile.context.app.*;
import java.util.*;
import java.util.concurrent.*;

public class ApplicationObjectCache {

    private static int maxCacheSize= -1;
    private static int cacheSize = -1;
    private static ConcurrentHashMap<ObjectName, ApplicationObject> nameCacheMap= null;
    private static ConcurrentHashMap<ApplicationObject, Date> cacheMap = null;
    private static ConcurrentHashMap<Date, ArrayList<ApplicationObject>> reverseCacheMap = null;
    private static ApplicationObjectCache cache = null; 

    public static int AVG_OBJ_SIZE = 0;
    public static int NUM_OBJ_SEEN = 0;

    private ApplicationObjectCache(int sizeInBytes){
        maxCacheSize = sizeInBytes;

        nameCacheMap = new ConcurrentHashMap<ObjectName, ApplicationObject>();
        cacheMap = new ConcurrentHashMap<ApplicationObject, Date>();
        reverseCacheMap = new ConcurrentHashMap<Date, ArrayList<ApplicationObject>>();
    }

    public static ApplicationObjectCache getInstance(int sizeInBytes){
        if(sizeInBytes>0 && cache ==null)
            cache = new ApplicationObjectCache(sizeInBytes);
        else if(sizeInBytes<=0 && cache == null)
            cache = new ApplicationObjectCache(1073741824/*1GB*/);
        return cache;

    }

    public synchronized void updateEntries(ApplicationObject[] objects){
        for(int i=0; i<objects.length; i++)
            updateEntry(objects[i]);
    }

    public synchronized void updateEntry(ApplicationObject object){
        if(cacheMap.containsKey(object)){

            //update the map
            Date oldDate = cacheMap.get(object);
            Date d = new Date(System.currentTimeMillis());
            cacheMap.replace(object, d);

            //update the reverse map
            ArrayList<ApplicationObject> l = reverseCacheMap.get(oldDate);
            l.remove(object);
            if(reverseCacheMap.contains(d))
                l = reverseCacheMap.get(d);
            else 
                l = new ArrayList<ApplicationObject>();

            l.add(object);
            reverseCacheMap.replace(d, l);

        }
        else{
            int newSize = object.getBytes().length + 8;
            if(cacheSize+newSize<=maxCacheSize){

                //put it in the lookup map
                Date d = new Date(System.currentTimeMillis());
                cacheMap.put(object, d);
                
                //put it in the reverse lookup map
                ArrayList<ApplicationObject> l = null;
                if(reverseCacheMap.contains(d))
                    l = reverseCacheMap.get(d);
                else 
                    l = new ArrayList<ApplicationObject>();
                l.add(object);
                reverseCacheMap.replace(d, l);

                //add it to the name cache
                nameCacheMap.put(object.getName(), object);
            }
            else{
                remove(newSize);

                //put it in the lookup map
                Date d = new Date(System.currentTimeMillis());
                cacheMap.put(object, d);

                //put it in the reverse lookup map
                ArrayList<ApplicationObject> l = null;
                if(reverseCacheMap.contains(d))
                    l = reverseCacheMap.get(d);
                else 
                    l = new ArrayList<ApplicationObject>();
                l.add(object);
                reverseCacheMap.replace(d, l);

                //add it to the name cache
                nameCacheMap.put(object.getName(), object);
                cacheSize+=newSize;
            }
        }

        AVG_OBJ_SIZE = (AVG_OBJ_SIZE+object.getBytes().length)/NUM_OBJ_SEEN;
    }

    private void remove(int sizeBytes){
        int totalRemoved = 0;
        //removes >=sizeBytes worth of entries from the cache
        ArrayList<Date> list = new ArrayList<Date>(cacheMap.values());
        Collections.sort(list);

        Date thisDate = null;
        ApplicationObject o = null;
        ArrayList<ApplicationObject> l = null;
        while(totalRemoved<sizeBytes && list.size()>0){
            thisDate = list.get(0);
            l = reverseCacheMap.get(thisDate);
            if(l.size()>0){
                o= l.get(0);
                l.remove(0);
                cacheMap.remove(o);
                nameCacheMap.remove(o.getName());
                totalRemoved += o.getBytes().length+4;
                cacheSize -= totalRemoved;
                if(l.size()==0)
                    reverseCacheMap.remove(thisDate);
            } else {
                reverseCacheMap.remove(thisDate);
            }
            list.remove(0);
        }
    }

    public boolean contains(ApplicationObject object){
        return cacheMap.containsKey(object);
    }

    public boolean contains(ObjectName name){
        return nameCacheMap.containsKey(name);
    }

    public Date getLastUpdateTime(ApplicationObject object){
        return cacheMap.get(object);
    }

    public ApplicationObject get(ObjectName objectName){
        return (ApplicationObject)nameCacheMap.get(objectName);
    }

}
