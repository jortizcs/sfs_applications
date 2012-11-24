public class ReadWriteTaskQueueElement{
    Operation[] operations= null;

    public ReadWriteTaskQueueElement(Operation[] ops){
        operations = ops;
    }

    public ReadWriteTaskQueueElement(Operation op){
        operations = new Operation[1];
        operations[0] = ops;
    }

    public operations getOperations(){
        return operations;
    }
}
