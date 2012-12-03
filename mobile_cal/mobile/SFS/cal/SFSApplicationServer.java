package mobile.SFS.cal;

import mobile.context.app.*;
import mobile.context.client.*;
import mobile.SFS.*;

import org.json.*;

public class SFSApplicationServer implements ApplicationServer {
    private static String host = null;
    private SFSApplicationServer server = null;

    private static ApplicationObjectCache cache = null;

    public SFSApplicationServer getInstance(String sfsHost){
        if(server ==null)
            server = new SFSApplicationServer(sfsHost);
        return server;
    }

    private SFSApplicationServer(String sfsHost){
        //  http://<host>:<port>
        host = sfsHost;
        cache = new ApplicationObjectCache(1073741824);
    }

    public ApplicationObject doRead(ObjectName objectName){
        SFSApplicationObject obj=  null;
        try {
            if(objectName.getStringName().startsWith("config:")){
                StringTokenizer tokenizer = new StringTokenizer(objectName.getStringName(), ":");
                String url = tokenizer.nextToken();
                url= tokenizer.nextToken();
                JSONObject info = new JSONObject(CurlOps.get(url));
                obj = new SFSApplicationObject(objectName, info);
            } else{
                JSONObject info = new JSONObject(CurlOps.get(host + objectName.getStringName()));
                obj = new SFSApplicationObject(objectName, info);
            }
        } catch(Exception e){}
        return obj;
    }

    public ApplicationObject doWrite(Operation op){
        ApplicationObject modObj = null;
        try {
            int operation = op.getOperation();
            switch(operation){
                case Operation.ADD_OBJECT:
                    modObj = addObject(op);
                    break;
                case Operation.ADD_METER:
                    modObj = addMeter(op);
                    break;
                case Operation.ADD_SPACE:
                    modObk = addSpace(op);
                    break;
                case Operation.BIND:
                    modObj = bind(op);
                    break;
                case Operatoin.LINK:
                    modObj = link(op);
                    break;
                case Operation.ATTACH:
                    modObj = attach(op);
                    break;
                case Operation.REMOVE:
                    modObj = remove(op);
                    break;
            } 
        } catch(Exception e){
        }

        return modObj;

    }

    public ApplicationObject[] doWriteExpression(Expression exp){
    }

    public byte[] doQuery(String queryString){
    }

    //private
    private ApplicationObject addObject(Operation op){
        return addNode(op, "Item");
    }

    private ApplicationObject.addSpace(Operation op){
        return addNode(op, "Space");
    }


    private ApplicationObject addMeter(Operation op){
        return addNode(op, "Meter");
    }

    private ApplicationObject addNode(Operation op, String type){
        ApplicationObject retObj = null;
        try {
            ObjectName[] names = op.getObjectParams();
            if(names.length >0){
                String newPath = cleanPath(names[0].getStringName());
                String nodeName  = getFileNameWoPath(newPath);
                String parentPath = getParent(newPath);
                if(nodeName!=null){
                    JSONObject jsonObj = new JSONObject();
                    jsonObj.put("operation", "create_resource");
                    jsonObj.put("resourceName", nodeName);
                    jsonObj.put("resourceType", "default");
                    CurlOps.put(jsonObj.toString(), host + parent);
                    String info = CurlOps.get(newPath);
                    if(obj!=null){
                        jsonObj.clear();
                        jsonObj.put("operation", "overwrite_properties");
                        JSONObject properties = new JSONObject();
                        properties.put("Type", type);
                        jsonObj.put("properties", properties);
                        CurlOps.put(jsonObj.toString(), host+newPath);
                        String info = CurlOps.get(newPath);
                        if(info!=null)
                            retObj = new SFSApplicationObject(newPath, info);
                    }
                }
            }
        } catch(Exception e){
        }
        return retObj;
    }

    public String cleanPath(String path){
        //clean up the path
        if(path == null)
            return null;
        if(!path.startsWith("/"))
            path = "/" + path;
        path = path.replaceAll("/+", "/");
        if(path.endsWith("/"))
            path = path.substring(0, path.length()-1);
       /*if(!path.endsWith("/"))
            path += "/";*/
        return path;
    }

    private String getFileNameWoPath(String path){
        if(path==null || path.equals("") || path.equals("/"))
            return null;
        Vector<String> tokens = new Vector<String>();
        StringTokenizer tokenizer = new StringTokenizer(path, "/");
        while(tokenizer.hasMoreTokens())
            tokens.add(tokenizer.nextToken());
        return tokens.lastElement();
    }

    private String getParent(String path){
        path = cleanPath(path);
        if(path==null || path == "/")
            return "/";
        StringTokenizer tokenizer = new StringTokenizer(path, "/");
        Vector<String> tokens = new Vector<String>();
        while(tokenizer.hasMoreTokens())
            tokens.add(tokenizer.nextToken());
        StringBuffer buf = new StringBuffer();
        if(tokens.size()==1){
            buf.append("/");
        } else {
            for(int i=0; i<tokens.size()-1; i++)
                buf.append("/").append(tokens.elementAt(i));
        }
        return cleanPath(buf.toString());
    }

    private ApplicationObject bind(Operation op){
        //symlink TO meter FROM item
        ApplicationObject retObj = null;
        try {
            ObjectName[] objParams = op.getParams();
            if(objParams.length>1){
                String source = cleanPath(objParams[0].getStringName());
                String dest  = cleanPath(objParams[1].getStringName());

                //check if the object are in the cache and check their type
                JSONObject srcGet = null;
                if(cache.contains(objParams[0])){
                    srcGet = ((SFSApplicationObject)cache.get(objParams[0])).
                        getInfo;
                } else {
                    srcGet = new JSONObject(CurlOps.get(host + source));
                }
                String srcType = ((String)((JSONObject)srcGet.get("properties")).
                        get("Type"));
                JSONObject dstGet = null;
                //check if the object are in the cache and check their type
                if(cache.contains(objParams[1])){
                    dstGet = ((SFSApplicationObject)cache.get(objParams[1])).
                        getInfo;
                } else {
                    //otherwise execute the following code:
                    dstGet = new JSONObject(CurlOps.get(host + dest));
                }
                String dstType = ((String)((JSONObject)srcGet.get("properties")).
                        get("Type"));

                if(srcType.equals("Item") && dstType.equals("Meter"))
                    retObj = addSymlink(source, dest);
            }
        } catch(Exception e){}
        return retObj;
    }

    private ApplicationObject link(Operation op){
        //symlink TO item/space/meter FROM qrc
        //symlink TO item/meter FROM space
        ApplicationObject retObj = null;
        try {
            ObjectName[] objParams = op.getParams();
            if(objParams.length>1){
                String source = cleanPath(objParams[0].getStringName());
                String dest  = cleanPath(objParams[1].getStringName());

                if(source.contains("/qrc")){
                    JSONObject dstGet = null;
                    if(cache.contains(objParams[1])){
                        dstGet = ((SFSApplicationObject)cache.get(objParams[1])).
                            getInfo;
                    } else {
                        dstGet = new JSONObject(CurlOps.get(host + dest));
                    }
                    String dstType = ((String)((JSONObject)srcGet.get("properties")).
                            get("Type"));
                    if(dstType.equals("Item") || dstType.equals("Meter") ||
                             dstType.equals("Space"))
                        retObj = addSymlink(source, dest);
                } else {
                    JSONObject srcGet = null;
                    if(cache.contains(objParams[0])){
                        srcGet = ((SFSApplicationObject)cache.get(objParams[0])).
                            getInfo;
                    } else {
                        srcGet = new JSONObject(CurlOps.get(host + source));
                    }
                    String srcType = ((String)((JSONObject)srcGet.get("properties")).
                            get("Type"));
                    JSONObject dstGet = null;
                    if(cache.contains(objParams[1])){
                        dstGet = ((SFSApplicationObject)cache.get(objParams[1])).
                            getInfo;
                    } else {
                        dstGet = new JSONObject(CurlOps.get(host + dest));
                    }
                    String dstType = ((String)((JSONObject)srcGet.get("properties")).
                            get("Type"));   
                    if(srcType.equals("Space") && 
                            (dstType.equals("Item") || dstType.equals("Meter")) )
                        retObj = addSymlink(source, dest);
                }
            }
        } catch(Exception e){
        }
        return retObj;
    }

    private ApplicationObject attach(Operation op){
        //symlink TO item2 (possibly attached to meter) FROM item1 attached to item2
        ApplicationObject retObj = null;
        try {
            ObjectName[] objParams = op.getParams();
            if(objParams.length>1){
                String source = cleanPath(objParams[0].getStringName());
                String dest  = cleanPath(objParams[1].getStringName());

                JSONObject srcGet = null;
                if(cache.contains(objParams[0])){
                    srcGet = ((SFSApplicationObject)cache.get(objParams[0])).
                        getInfo;
                } else {
                    srcGet = new JSONObject(CurlOps.get(host + source));
                }
                String srcType = ((String)((JSONObject)srcGet.get("properties")).
                        get("Type"));
                //check if the object are in the cache and check their type
                //otherwise execute the following code:
                
                JSONObject dstGet = null;
                if(cache.contains(objParams[1])){
                    dstGet = ((SFSApplicationObject)cache.get(objParams[1])).
                        getInfo;
                } else {
                    dstGet = new JSONObject(CurlOps.get(host + dest));
                }
                String dstType = ((String)((JSONObject)srcGet.get("properties")).
                        get("Type"));
                if(srcType.equals("Item") && dstType.equals("Item"))
                    retObj = addSymlink(source, dest);
            }
        } catch(Exception e){
        }
        return retObj;
    }

    private ApplicationObject addSymlink(String source, String target){
        ApplicationObject retObj = null;
        try {
            source = cleanPath(source);
            target = cleanPath(target);
            int i = target.lastIndexOf("/");
            JSONObject jsonObj = new JSONObject();
            jsonObj.put("operation", "create_symlink");
            jsonObj.put("name", target.substring(i+1));
            jsonObj.put("uri", target);
            CurlOps.put(jsonObj.toString(), host + source);
            String info = CurlOps.get(source);
            if(info!=null)
                retObj = new SFSApplicationObject(source, info);
        } catch(Exception e){
        }
        return retObj;
    }
}
