package mobile.context.client;

import mobile.context.app.*;

public abstract class WriteDoneCallback{
    public WriteDoneCallback(){}

    /**
     * Called after the write is complete.  Returns the object after the operation has been applied to it.
     */
    public void writeDone(ApplicationObject object){}

    public void writeDone(ApplicationObject[] object){}
}
