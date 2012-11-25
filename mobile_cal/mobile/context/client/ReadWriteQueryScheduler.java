package mobile.context.client;

public class ReadWriteQueryScheduler{
    private static ReadWriteQueryScheduler scheduler = null;

    public ReadWriteQueryScheduler(){
    }

    public static ReadWriteQueryScheduler getInstance(){
        if(scheduler == null)
            scheduler = new ReadWriteQueryScheduler();
        return scheduler;
    }

    public boolean cancel(ReadWriteQueryTaskQueueElement thisElt){
        return true;
    }
}
