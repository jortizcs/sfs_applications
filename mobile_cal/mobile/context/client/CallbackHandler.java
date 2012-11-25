package mobile.context.client;

public abstract class CallbackHandler{
    private ReadWriteQueryTaskQueueElement queueElt = null;
    private static ReadWriteQueryScheduler scheduler = null;

    protected CallbackHandler(ReadWriteQueryTaskQueueElement request, ReadWriteQueryScheduler sched){
        queueElt = request;
        scheduler = sched;
    }

    /**
     * Unschedules the given operation or transaction.  If the job reference is null or is no longer in the queue (or never was),
     * we return false, otherwise the job is unscheduled and true is returned.
     */
    public boolean cancel(){
        return scheduler.cancel(queueElt);    
    }
}
