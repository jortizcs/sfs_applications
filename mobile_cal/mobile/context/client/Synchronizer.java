public class Syncrhonizer implements PrefetchDoneEventHandler{
    private static Prefetch prefetcher = null;

    private Sychronizer (){
        if(prefetcher ==null)
            prefetcher = Prefetcher.getInstance();
        
    }

    public Synchronizer getInstance(){
        if(syncrhonizer == null)
            synchronizer = new Synchronizer();
    }

    public void fetchDone(ArrayList<ApplicationObject> objects){
        //for each object, check that the state of the object is
        //the same or different.  If it is different and there's a
        //onChange callback registered for that object, trigger it.
    }
}
