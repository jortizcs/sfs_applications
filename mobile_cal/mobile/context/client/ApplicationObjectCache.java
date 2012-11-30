package mobile.context.client;

import java.util.*;
import java.util.concurrent.*;

public class ApplicationObjectCache {

    private static int maxCacheSize= -1;
    private static int cacheSize = -1;
    private static ConcurrentHashMap<ObjectName, ApplicationObject> nameCacheMap= null;
    private static ConcurrentHashMap<ApplicationObject, Date> cacheMap = null;
    private static ConcurrentHashMap<Date, ArrayList<ApplicationObject>> reverseCacheMap = null;
    
    public ApplicationObjectCache(int sizeInBytes){
        maxCacheSize = sizeInBytes;

        nameCacheMap = new ConcurrentHashMap<ObjectName, ApplicationObject>();
        cacheMap = new ConcurrentHashMap<ApplicationObject, Date>();
        reverseCacheMap = new ConcurrentHashMap<Date, ArrayList<ApplicationObject>>();
    }

    public synchronized boolean updateEntry(ApplicationObject object){
        if(cacheMap.containsKey(object))

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
            int newsize = object.getBytes().length + 8;
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

    public ApplicationObject get(ObjectName object){
        return nameCacheMap.get(name);
    }
}
