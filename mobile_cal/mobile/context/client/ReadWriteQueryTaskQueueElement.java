package mobile.context.client;

public class ReadWriteQueryTaskQueueElement{
    Operation[] operations= null;

    public ReadWriteQueryTaskQueueElement(Operation[] ops){
        operations = ops;
    }

    public ReadWriteQueryTaskQueueElement(Operation op){
        operations = new Operation[1];
        operations[0] = op;
    }

    public Operation[] getOperations(){
        return operations;
    }
}
