/**
 * A set of operations.  The expression is evaluated atomically on the server
 * if the server supports it.  The expression is evaluated in array-input order.
 */
public abstract interface Expression {
    public Expression(ObjectName[], byte[][] data, Operation[] op){
    }

    public void addOperation(ObjectName name, byte[] data, Operation op){
    }

    public void removeOperation(ObjectName name, Operation op){
    }

    public Operation[] getOperations(){
    }

    public boolean containsOperation(Operation op, ObjectName name){
    }

    public void execute(){
    }

    public ApplicationObject execute(long freshness){
    }

    public CallbackHandler execute(WriteDoneCallback callback){
    }
}
