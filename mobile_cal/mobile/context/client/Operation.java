package mobile.context.client;

import mobile.context.app.*;

public interface Operation {
    
    /**
     * All the objects to perform the operation on.
     */
    public void setParams(ObjectName... objectNames);
   
    /**
     * The corresponding byte array will be matched with the objectname in the setParams method.
     * This supports operations that require that some new data be applied to the object.
     */ 
    public void setData(byte[]... data);

    /**
     * Returns the operator.
     */
    public int getOperator();

    public ObjectName[] getObjectParamNames();

    public ApplicationObject[] getObjectsParams();

    public byte[] getData();


    /**
     * Executes the operation.  Tries to execute the operation on server first.  If server is not
     * availble, attempts to execute the operation locally.  Returns the result of the transformation
     * on the original ApplicationObject.
     */
    public ApplicationObject execute();

    /**
     * Executes the operation, if the objects are all <= freshness milliseocnds old.
     * Otherwise, tries to execute it through the network.  If the freshness threshold is exceeded and the 
     * network/server is down, returns null.  Otherwise it returns the object.
     * 
     * @param freshness maximum threshold, in milliseconds, since the last time the ApplicationObject was updated in
                the cache.
     * @return the associated ApplicationObject or null if it does not exist.  Non-existence implies either that the 
     *          object does not exist or that it does, but is not accessible.  ApplicationObject's are not accessible
     *          when there is no local copy and the server is inaccessible.
     */
    public ApplicationObject execute(long freshness);

    /**
     * Registers a callback that is called when the object referred to by objectName is written to the server
     * and refereshed in the cache.
     *
     * @param objectName the name of the object.
     * @param op the operation to be applied to the ApplicationObject
     * @param callback called when the given object is read from the server and placed in the local cache.
     * @return CallbackHandler allows you to check the state of the callback and cancel the request if necessary.
     */
    public CallbackHandler execute(WriteDoneCallback callback);


}
