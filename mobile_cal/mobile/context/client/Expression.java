package mobile.context.client;

import  mobile.context.app.*;

/**
 * A set of operations.  The expression is evaluated atomically on the server
 * if the server supports it.  The expression is evaluated in array-input order.
 */
public abstract class Expression {
    public Expression(ObjectName[][] objectNames, byte[][] data, Operation[] op){
    }

    public abstract void addOperation(ObjectName[] name, byte[] data, Operation op);

    public abstract void removeOperation(ObjectName[] name, Operation op);

    public abstract Operation[] getOperations();

    public abstract boolean containsOperation(Operation op, ObjectName name);

    public abstract ApplicationObject[] executeLocal();
}
