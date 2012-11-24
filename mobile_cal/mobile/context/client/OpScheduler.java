import java.util.concurrent.ConcurrentLinkedQueue;

public class OpScheduler{
    public OpScheduler thisScheduler = null;
    private static ConcurrentLinkedQueue<ReadWriteTaskQueueElement> queue = null;

    private OpScheduler(){
        queue = new ConcurrentLinkedQueue<ReadWriteTaskQueueElement>();
    }

    public OpScheduler getInstance(){
        if(thisScheduler == null)
            thisScheduler = new OpScheduler();
        return thisScheduler;
    }


}
