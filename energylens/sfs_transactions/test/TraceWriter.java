import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;


import java.util.*;

public class TraceWriter{
    public static final long sfstime = 1343286861L;
    private static final String path = "/xactions_tests";
    public static void main(String[] args){
        System.out.println("#####   makeSample01 #####");
        makeSample01();
        System.out.println("\n\n\n#####   makeSample02 #####");
        makeSample02();
        System.out.println("\n\n\n#####   unmakeSample02 #####");
        unmakeSample02();
        System.out.println("\n\n\n#####   makeSample03 and makeSample04 #####");
        makeSample03();
        System.out.println("\n\n\n#####   makeSample05 #####");
        makeSample05();
        System.out.println("\n\n\n");
    }

    public static void makeSample01(){
        JSONObject s01 = new JSONObject();
        s01.put("operation", "create_resource");
        s01.put("resourceName", "xactions_tests");
        s01.put("resourceType", "default");
        System.out.println(s01.toString());
    }

    public static void makeSample02(){
        JSONObject log = new JSONObject();
        JSONArray ops = new JSONArray();

        Random rand = new Random();
        long opts = sfstime;// + rand.nextInt( );
        log.put("type", "log"); 
        for(int i=0; i<10; i++){
            JSONObject entry = new JSONObject();
            JSONObject data = new JSONObject();
            data.put("operation", "create_resource");
            data.put("resourceType", "default");
            entry.put("ts", opts);
            opts = opts + 2*(i+1);
            if(i==0){
                data.put("resourceName", "spaces");
                entry.put("path", path);
            } else if(i==2){
                data.put("resourceName", "dev");
                entry.put("path", path);
            } else {
                data.put("resourceName", "PC" + (new Integer(i)).toString());
                entry.put("path", path + "/dev");
            }
            entry.put("op", "put");
            entry.put("data", data);
            ops.add(entry);
        }
        log.put("ops", ops);
        System.out.println(log.toString());
    }

    public static void unmakeSample02(){
        JSONObject log = new JSONObject();
        JSONArray ops = new JSONArray();

        Random rand = new Random();
        long opts = sfstime+2000*3;// + rand.nextInt( );
        log.put("type", "log"); 
        for(int i=9; i>=0; i--){
            JSONObject entry = new JSONObject();
            entry.put("ts", opts);
            opts = opts + 2*(i+1);
            if(i==0){
                entry.put("path", path + "/spaces");
            } else if(i==1){
                entry.put("path", path + "/dev");
            } else {
                entry.put("path", path + "/dev/PC" + (new Integer(i)).toString());
            }
            entry.put("op", "delete");
            ops.add(entry);
        }
        log.put("ops", ops);
        System.out.println(log.toString());
    }

    public static void makeSample03(){
        JSONObject log = new JSONObject();
        JSONArray ops = new JSONArray();
        JSONObject log2 = new JSONObject();
        JSONArray ops2 = new JSONArray();

        Random rand = new Random();
        long opts = sfstime;// + rand.nextInt( );
        log.put("type", "log"); 
        for(int i=0; i<20; i+=2){
            JSONObject entry = new JSONObject();
            JSONObject data = new JSONObject();
            data.put("operation", "create_resource");
            data.put("resourceType", "default");
            entry.put("ts", opts);

            JSONObject entry2 = new JSONObject();
            JSONObject data2 = new JSONObject();
            data2.put("operation", "create_resource");
            data2.put("resourceType", "default");
            entry2.put("ts", opts+1);
            opts = opts + 2*(i+1);

            if(i==0){
                data.put("resourceName", "spaces");
                entry.put("path", path);
            } else if(i==1){
                data.put("resourceName", "dev");
                entry.put("path", path);
            } else {
                data.put("resourceName", "r" + (new Integer(i)).toString());
                entry.put("path", path + "/dev");
                data2.put("resourceName", "r" + (new Integer(i+1)).toString());
                entry2.put("path", path + "/dev");
            }
            entry.put("op", "put");
            entry.put("data", data);
            entry2.put("op", "put");
            entry2.put("data", data2);
            ops.add(entry);
            ops2.add(entry2);
        }
        log.put("ops", ops);
        log2.put("ops", ops2);
        System.out.println(log.toString());
        System.out.println("\n\n\n");
        System.out.println(log2.toString());
    }


    public static void makeSample05(){
        JSONObject log = new JSONObject();
        JSONArray ops = new JSONArray();

        Random rand = new Random();
        long opts = sfstime;// + rand.nextInt( );
        log.put("type", "log"); 
        for(int i=0; i<2; i++){
            JSONObject entry = new JSONObject();
            JSONObject data = new JSONObject();
            data.put("operation", "create_resource");
            data.put("resourceType", "default");
            if(i==0){
                entry.put("path", path + "/dev/r2");
                entry.put("ts", 1343286865);
            } else if(i==1){
                entry.put("ts", 1343286866);
                entry.put("path", path + "/dev");
            }
            entry.put("op", "delete");
            ops.add(entry);
        }
        log.put("ops", ops);
        System.out.println(log.toString());
    }

    /*public static void unmakeSample02(){
        JSONObject log = new JSONObject();
        JSONArray ops = new JSONArray();

        Random rand = new Random();
        long opts = sfstime+2000*3;// + rand.nextInt( );
        log.put("type", "log"); 
        for(int i=9; i>=0; i--){
            JSONObject entry = new JSONObject();
            entry.put("ts", opts);
            opts = opts + 2*(i+1);
            if(i==0){
                entry.put("path", path + "/spaces");
            } else if(i==1){
                entry.put("path", path + "/dev");
            } else {
                entry.put("path", path + "/dev/PC" + (new Integer(i)).toString());
            }
            entry.put("op", "delete");
            ops.add(entry);
        }
        log.put("ops", ops);
        System.out.println(log.toString());
    }*/
}
