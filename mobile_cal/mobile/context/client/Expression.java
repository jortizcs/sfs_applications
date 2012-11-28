package mobile.context.client;

import  mobile.context.app.*;

/**
 * A set of operations.  The expression is evaluated atomically on the server
 * if the server supports it.  The expression is evaluated in array-input order.
 */
public abstract class Expression {
    public Expression(ObjectName[][] objectNames, byte[][] data, Operation[] op){
    }

    public void addOperation(ObjectName[] name, byte[] data, Operation op){
    }

    public void removeOperation(ObjectName[] name, Operation op){
    }

    public Operation[] getOperations(){
        return null;
    }

    public boolean containsOperation(Operation op, ObjectName name){
        return true;
    }

    public void execute(){
    }

    public ApplicationObject execute(long freshness){
        return null;
    }

    public CallbackHandle execute(WriteDoneCallback callback){
        return null;
    }
}
