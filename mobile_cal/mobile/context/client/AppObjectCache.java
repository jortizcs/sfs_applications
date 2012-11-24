public class AppObjectCache{
    public ConcurrentHashMap<ObjectName, AppObjectCacheEntry> cache = null;
    private AppObjectCache thisCacheObj = null;

    private AppObjectCache(){
        if(thisCacheObj==null)
            thisCacheObj = new AppObjectCache();
        return thisCacheObj;
    }

    public class AppObjectCacheEntry {

        public long lastUpdatedTime=-1L;
        public ApplicationObject appObj=null;

        public AppObjectCacheEntry(long updateTime ApplicationObject obj){
            lastUpdateTime = updateTime;
            appObj = obj;
        }

        public long getLastUpdateTime(){
            return lastUpdateTime;
        }

        public ApplicationObject getApplicationObject(){
            return appObj;
        }
    }
}
