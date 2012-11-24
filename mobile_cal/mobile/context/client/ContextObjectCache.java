public ContextObjectCache{
    private static ContextObjectCache thisCache = null;
    private static Vector<CacheLine> cacheTable = null;
    
    private ContextObjectCache(){
        cacheTable = new Vector<CacheLine>();
    }

    public static ContextObjectCache getInstance(){
        if(thisCache==null)
            thisCache = new ContextObjectCache();
    }

    public void insertOrUpdateCacheEntry(ApplicationObject object){
        try {
            CacheLine cline = new CacheLine(object.getName(), object);
            if(cacheTable.contains(cline))
                cacheTable.get(cacheTable.indexOf(cline)).update(object);
            else
                cacheTable.add(cline);
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    public void deleteCacheEntry(ApplicationObject object){
        try{
            CacheLine cline = new CacheLine(object.getName(), object);
            if(cacheTable.contains(cline))
                cacheTable.remove(cacheTable.indexOf(cline));
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    public class CacheLine{
        private long timestamp = -1L;
        private ObjectName objectName = null;
        private ApplicationObject appObject= null;

        public CacheLine(ObjectName name, ApplicationObject object){
            objectName = name;
            appObject = object;
            timestamp = System.currentTimeMillis();
        }

        public update(ApplicationObject object){
            timestamp = System.currentTimeMillis();
            appObject = object;
        }

        public long getLastUpdateTime(){
            return timestamp;
        }

        public ApplicationObject getApplicationObject(){
            return appObject;
        }

        public boolean equals(Object o){
            if(o instanceof CacheLine && appObject.equals(((CacheLine)o).getApplicationObject()))
                return true;
            return false;
        }
    }
}
