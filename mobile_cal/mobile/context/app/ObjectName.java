package mobile.context.app;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * To support multiple kinds of namespaces, we generally interpret names as an array of bytes, internally.
 * However, an ObjectName object can be set with a numeric value, a byte array, or a string.
 */

public class ObjectName{
    private byte[] name = null;

    public ObjectName(String n){
        setName(n);
    }


    public ObjectName(int n){
        setName(n);
    }

    public ObjectName(byte[] n){
        setName(n);
    }

    
    public byte[] getByteName(){
        return name;
    }

    public String getStringName(){
        return new String(name);
    }

    public int getIntName(){
        return ByteBuffer.wrap(name).getInt();
    }

    private void setName(byte[] n){
        name = n;
    }

    private void setName(int n){
        //default is big-endian
        name = ByteBuffer.allocate(4).putInt(n).array();
    }

    private void setName(String n){
        name = n.getBytes();
    }

    public boolean equals(Object o){
        if(o instanceof ObjectName)
            return Arrays.equals(((ObjectName)o).getByteName(), name);
        return false;
    }
}
