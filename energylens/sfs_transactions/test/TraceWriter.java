import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;


import java.util.*;

public class TraceWriter{
    public static final long sfstime = 1343286861L;
    private static final String path = "/xactions_tests";
    public static void main(String[] args){
        makeSample01();
        System.out.println("\n\n\n");
        makeSample02();
        System.out.println("\n\n\n");
    }

    public static void makeSample01(){
        JSONObject s01 = new JSONObject();
        s01.put("operation", "create_resource");
        s01.put("resourceName", "qrc");
        s01.put("resourceType", "default");
        System.out.println(s01.toString());
    }

    public static void makeSample02(){
        JSONObject log = new JSONObject();
        JSONArray ops = new JSONArray();

        JSONObject data = new JSONObject();
        data.put("operation", "create_resource");
        data.put("resourceType", "default");
     
        Random rand = new Random();
        long opts = sfstime;// + rand.nextInt( );
        log.put("type", "log"); 
        for(int i=0; i<10; i++){
            JSONObject entry = new JSONObject();
            entry.put("ts", opts);
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
            }
            entry.put("op", "put");
            entry.put("data", data);
            ops.add(entry);
        }
        log.put("ops", ops);
        System.out.println(log.toString());
    }
}
