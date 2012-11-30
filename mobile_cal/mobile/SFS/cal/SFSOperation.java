package mobile.SFS.cal;

import mobile.context.client;

public class SFSOperation implements Operation{
    enum Operator {
        ADD_OBJECT, ADD_SPACE, ADD_METER, BIND, LINK, ATTACH,
        REMOVE
    }

    private ObjectName[] objectParams = null;
    private String dataStr = null;
    private Operator operator = ADD_OBJECT;

    public void setParams(ObjectName... objectNames){
        objectParams = objectNames;
    }

    public void setData(byte[]... data){
        ByteBuffer buf = new ByteBuffer().put(data);
        dataStr = buf.toString();
    }

    public ObjectName[] getObjectParams(){
        return objectParams;
    }

    public byte[] getData(){
        return dataStr.getBytes();
    }

    public void setOperator(int op){
        switch(op){
            case ADD_OBJECT:
                operator = ADD_OBJECT;
                break;
            case ADD_METER:
                operator = ADD_METER;
            case ADD_SPACE:
                operator = ADD_SPACE;
                break;
            case BIND:
                operator = BIND;
                break;
            case LINK:
                operator = LINK;
                break;
            case ATTACH:
                operator = ATTACH;
                break;
            case REMOVE:
                operator = REMOVE;
                break;
        }
    }

    public int getOperator(){
        return operator;
    }

    public ApplicationObject execute(){}

    public ApplicationObject execute(long freshness){}

    public CallbackHandler execute(WriteDoneCallback callback){}
}

