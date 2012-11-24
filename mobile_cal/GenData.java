import java.util.*;
import java.util.concurrent.*;
import java.io.*;

public class GenData{
    public static void main(String[] args){
        long now = System.currentTimeMillis();
        Date d = new Date(now);
        ConcurrentHashMap<Integer, Long> map = new ConcurrentHashMap<Integer, Long>();
        Vector<Vector<Long>> dataVec = new Vector<Vector<Long>>();

        int k=0;
        while(k<5){
            long l = now+(k*1000);
            map.put(new Integer(k%2), new Long(l));
            l = now+((k+1)*1000);
            map.put(new Integer((k+1)%2), new Long(l));
            l = (now+(1000*200))+(k*1000);
            map.put(new Integer((k%2)+2), new Long(l));
            l = (now+(1000*200))+((k+1)*1000);
            map.put(new Integer(((k+1)%2)+2), new Long(l));
            System.out.println(map);
            addToFeatureVector(map, dataVec);
            k+=1;
        }

        System.out.println("\n\n\n");
        printARFFHeader(dataVec.get(0));
        printARFFData(dataVec);
    }

    public static void addToFeatureVector(ConcurrentHashMap<Integer, Long> map, Vector<Vector<Long>> dataVec){
        ArrayList<Integer> keyList = new ArrayList<Integer>(map.keySet());
        Vector<Long> featVec = new Vector<Long>();
        Object[] keyList2 = keyList.toArray();
        Arrays.sort(keyList2);
        keyList = new ArrayList(Arrays.asList(keyList2));
        for(int i=0; i<keyList.size(); i++){
            for(int j=0; j<keyList.size(); j++){
                if(i!=j){
                    long v = map.get(keyList.get(i)).longValue()- map.get(keyList.get(j)).longValue();
                    v = Math.abs(v);
                    featVec.add(new Long(v));
                } else {
                    featVec.add(new Long(0L));
                }
            }
            System.out.println(featVec + "," + keyList.get(i));
            featVec.add(new Integer(keyList.get(i)).longValue());
            Vector<Long> featVecCopy = new Vector<Long>(featVec);
            dataVec.add(featVecCopy);
            featVec.clear();
        }
    }

    public static void printARFFData(/*File f,*/ Vector<Vector<Long>> dataVector){
        //print it to the ARFF
        System.out.println("@DATA");
        for(int i=0; i<dataVector.size(); i++){
            Vector<Long> row = dataVector.get(i);
            for(int j=0; j<row.size(); j++){
                System.out.print(row.get(j));
                if(j<row.size()-1)
                    System.out.print(",");
                else{
                    if(j<10)
                        System.out.print(",Object_0"+row.get(j)+"\n");
                    else
                        System.out.print(",Object_"+row.get(j)+"\n");
                }
            }
        }
    }

    public static void printARFFHeader(/*File f,*/ Vector<Long> featureVector){
        String relStr = "@RELATION ObjectRefTimes";
        Vector<String> attributes = new Vector<String>();
        StringBuffer attrStr = new StringBuffer();
        for(int i=0; i<featureVector.size()-1; i++){
            if(i<10)
                attrStr.append("@ATTRIBUTE ObjTimeRefDiff_0").append(new Integer(i).toString()).append(" NUMERIC");
            else
                attrStr.append("@ATTRIBUTE ObjTimeRefDiff_").append(new Integer(i).toString()).append(" NUMERIC");
            attrStr.append("\n");
        }

        StringBuffer classStr =new StringBuffer().append("@ATTRIBUTE class {");
        for(int i=0; i<featureVector.size()-1; i++){
            if(i<10)
                classStr.append("Object_0").append(new Integer(i).toString());
            else
                classStr.append("Object_").append(new Integer(i).toString());
            if(i!=featureVector.size()-1)
                classStr.append(",");
        }
        classStr.append("}").append("\n");

        System.out.println(relStr);
        System.out.println(attrStr);
        System.out.println(classStr);
    }
         

}
