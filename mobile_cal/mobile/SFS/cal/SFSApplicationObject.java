package mobile.SFS.cal;

import mobile.context.app.*;
import mobile.context.client.*;

public class SFSApplicationObject extends ApplicationObject {
    JSONObject nodeInfo = null;

    public SFSApplicationObject(String path, JSONObject info){
        //create the ObjectName and instantiate the super constructor
        ObjectName name = new ObjectName(path);
        super(name);
        nodeInfo = info;
    }

    public byte[] getBytes(){
        ByteBuffer byteRep = ByteBuffer.allocate(nodeInfo.toString().getBytes().length + 
            name.getByteName().length);
        return byteRep.put(name.getByteName()).put(nodeInfo.toString.getBytes()).array();
    }

    public JSONObject getInfo(){
        return nodeInfo;
    }

    public JSONObject getProperties(){
        if(nodeInfo.containsKey("properties"))
            return (JSONObject)nodeInfo.get("properties");
        return null;
    }

}
