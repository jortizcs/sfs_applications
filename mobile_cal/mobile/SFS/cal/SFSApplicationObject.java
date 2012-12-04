package mobile.SFS.cal;

import mobile.context.app.*;
import mobile.context.client.*;

public class SFSApplicationObject extends ApplicationObject {
    private JSONObject nodeInfo = null;
    private String  nodeInfoStr = null;

    public SFSApplicationObject(String path, JSONObject info){
        //create the ObjectName and instantiate the super constructor
        ObjectName name = new ObjectName(path);
        super(name);
        nodeInfo = info;
    }

    public SFSApplicationObject(String path, String infoStr){
        //create the ObjectName and instantiate the super constructor
        ObjectName name = new ObjectName(path);
        super(name);
        nodeInfoStr = infoStr;
    }

    public byte[] getBytes(){
        if(nodeInfo!=null){
            ByteBuffer byteRep = ByteBuffer.allocate(nodeInfo.toString().getBytes().length + 
                name.getByteName().length);
            return byteRep.put(name.getByteName()).put(nodeInfo.toString.getBytes()).array();
        } else if(nodeInfoStr!=null{
            ByteBuffer byteRep = ByteBuffer.allocate(nodeInfoStr.getBytes().length + 
                name.getByteName().length);
            return byteRep.put(name.getByteName()).put(nodeInfoStr.getBytes()).array();
        } else {
            return name.getByteName();
        }
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
