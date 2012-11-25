package mobile.context.client;

import java.util.concurrent.*;

public class OpScheduler{
    public OpScheduler thisScheduler = null;
    private static ConcurrentLinkedQueue<ReadWriteQueryTaskQueueElement> queue = null;

    private OpScheduler(){
        queue = new ConcurrentLinkedQueue<ReadWriteQueryTaskQueueElement>();
    }

    public OpScheduler getInstance(){
        if(thisScheduler == null)
            thisScheduler = new OpScheduler();
        return thisScheduler;
    }


}
