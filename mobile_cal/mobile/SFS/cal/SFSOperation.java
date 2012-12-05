package mobile.SFS.cal;

import mobile.context.client;

public class SFSOperation implements Operation{
    enum Operator {
        ADD_OBJECT, ADD_SPACE, ADD_METER, BIND, ATTACH, REMOVE
    }

    private ObjectName[] objectParams = null;
    private String dataStr = null;
    private Operator operator = ADD_OBJECT;
    private static ApplicationObjectCache cache = ApplicationObjectCache.getInstance(0);

    public void setParams(ObjectName[] objectNames){
        objectParams = objectNames;

        //clean all the path names
        if(objectParams!=null){
            for(int i=0; i<objectParams.length; i++){
                String thisPath = objectParams[i].getStringName();
                //check that you're not trying to clean a query
                if(!thisPath.contains("*") && !thisPath.contains("ts_")){
                    objectParams[i] = new ObjectName(
                            SFSApplicationServer.cleanPath(thisPath));
                }
            }
        }
    }

    public void setData(byte[] data){
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

    public ApplicationObject[] executeLocal(){
        switch(operator){
            case ADD_OBJECT:
                return handleAddObject("Item");
            case ADD_SPACE:
                return handleAddObject("Space");
            case ADD_METER:
                return handleAddObject("Meter");
            case BIND:
                return handleLink("Bind");
            case ATTACH:
                return handleLink("Attach");
            case REMOVE:
                return handleRemove();
            default:
                return null;
        }
    }


    private synchronized ApplicationObject[] handleAddObject(String type){
        ArrayList<ApplicationObject> updatedObjs = new ArrayList<ApplicationObject>();
        if(objectParams!=null && objectParams.length){
            String path = objectParams[0].getStringName();
            String parent = SFSApplicationServer.getParent(path);
            String newNodeName = SFSApplicationServer.getFileNameWoPath(path);

            //update the parent node
            try {
                ObjectName parentObjName = new ObjectName(parent);
                if(cache.contains(parentObjName){
                    SFSApplicationObject parentObj = (SFSApplicationObject)cache.get(parentObjName);
                    JSONObject parentInfo = parentObj.getInfo();
                    String parentType = parentInfo.get("properties").get("Type");
                    /*boolean cond1 = (type.equals("Meter") || type.equals("Item")) && (parentType==null || parentType.equals("Space")) && parent.contains("/dev");
                    boolean cond2 = type.equals("Space") && (parentType==null || parentType.equals("Space"));*/
                    //if(cond1 || cond2){
                    if(parentInfo!=null && parentInfo.containsKey("children")){
                        JSONArray children =(JSONArray) parentInfo.get("children");
                        children.add(newNodeName);
                        parentInfo.replace("children", children);
                        parentObj.updateEntry(parentObj);
                        cache.updateEntry(parentObj);
                        updatedObjs.add(parentObj);
                    }
                    /*} else {
                        return null;
                    }*/
                }
            } catch(Exception e){
                e.printStackTrace();
                return null;
            }

            //construct the info jsonobject
            JSONObject props = new JSONObject();
            props.put("Type", type);
            JSONObject newInfo = new JSONObject();
            newInfo.put("status", "success");
            newInfo.put("type", "DEFAULT");
            newInof.put("properties", props);
            newIno.put("children", new JSONArray());
            SFSApplicationObject newObj = new SFSApplicationObject(path, newInfo.toString());

            //add the new object to the cache
            cache.updateEntry(newObj);
            updatedObjs.add(newObj);

            //insert this operation in the OpLog
            oplog.addEntry(null, this);

            return updatedObjs.toArray();
        }
        return null;
    }

    private synchronized ApplicationObject[] handleLink(String type){
        ArrayList<ApplicationObject> updatedObjs = new ArrayList<ApplicationObject>();
        if(objectParams!=null && objectParams.length==2 && 
                cache.contains(objectParams[0]) &&  cache.contains(objectParams[1])){
            String sourcePath = objectParams[0].getStringName();
            String destPath = objectParam[1].getStringName();

            //get the local objects
            SFSApplicationObject sourceObj = (SFSApplicationObject)cache.get(objectParams[0]);
            JSONObject sourceInfo = sourceObj.getInfo();
            JSONArray children =(JSONArray) sourceInfo.get("children");
            StringBuffer newChildName = new StringBuffer().
                append(SFSApplicationServer.getFileNameWoPath(destPath)).
                append(" -> ").append(destPath);
            if(!children.contains(newChildName)){
                children.add(newChildName.toString());
                parentInfo.replace("children", children);
                parentObj.updateEntry(parentObj);
                cache.updateEntry(parentObj);
                updatedObjs.add(parentObj);
                return updateObjs.toArray();
            }
        }
        return null;
    }

}

