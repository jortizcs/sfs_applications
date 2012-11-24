
public abstract class CallbackHandler{
    private ReadWriteTaskQueueElement queueElt = null;
    private static ReadWriteScheduler scheduler = null;

    protected CallbackHandler(ReadWriteTaskQueueElement request, OpScheduler sched){
        queueElt = request;
        scheduler = sched;
    }

    /**
     * Unschedules the given operation or transaction.  If the job reference is null or is no longer in the queue (or never was),
     * we return false, otherwise the job is unscheduled and true is returned.
     */
    public boolean cancel(){
        return sched.unschedule(queueElt);    
    }
}
