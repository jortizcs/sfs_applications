package mobile.context.client;

import mobile.context.app.*;
import java.util.*;
import java.util.concurrent.*;

public class ObjRefClusterer {
    private int objCnt = 0;

    private ConcurrentHashMap<Integer, ObjectName> idToObjname = null;
    private ConcurrentHashMap<ObjectName, Integer> objnameToId = null;

    private Vector<Long> lastRefTime = null;

    private static double alpha = 0.8;
    
    private ArrayList<ArrayList<Double>> absRefDiffMatrix = null;
    
    public ObjRefClusterer() {
        absRefDiffMatrix = new ArrayList<ArrayList<Double>>();
        idToObjname = new ConcurrentHashMap<Integer, ObjectName>();
        objnameToId = new ConcurrentHashMap<ObjectName, Integer>();
        lastRefTime = new Vector<Long>();
    }

    public synchronized void recordRef(ObjectName name){
        long now = System.currentTimeMillis();
        if(objnameToId.contains(name)){
            //update the reference time for this object
            Integer idxInt = objnameToId.get(name);
            lastRefTime.set(idxInt.intValue(), new Long(now));

            //update all the values in the matrix to reflect the new time differences
            int sz = absRefDiffMatrix.size();
            ArrayList<Double> newRow = new ArrayList<Double>(sz);
            for(int i=0; i<sz; i++){
                double diffDouble = new Long(Math.abs(lastRefTime.elementAt(i).longValue()-now)).doubleValue();
                double oldDiffDouble = absRefDiffMatrix.get(i).get(idxInt.intValue()).doubleValue();
                Double avgDiff = new Double(alpha*diffDouble+(1.0-alpha)*oldDiffDouble);
                absRefDiffMatrix.get(i).set(idxInt.intValue(), avgDiff);
            }
        } else {
            //create a new index and associate it with this object
            Integer oid = new Integer(objCnt);
            objCnt +=1;
            objnameToId.put(name, oid);
            idToObjname.put(oid, name);
            lastRefTime.add(new Long(now));

            //create a new row and update the associated value in the matrix
            int sz = absRefDiffMatrix.size()+1;
            ArrayList<Double> newRow = new ArrayList<Double>(sz);
            for(int i=0; i<sz; i++){
                long diff = Math.abs(lastRefTime.elementAt(i).longValue()-now);
                Double avgDiff = new Double(new Long(diff).doubleValue());
                newRow.add(i, avgDiff);
                if(i<sz-1)
                    absRefDiffMatrix.get(i).add(avgDiff);
            }
            absRefDiffMatrix.add(newRow);
        }
    }

    public ArrayList<ObjectName> topK(int sizeInBytes){
        return null;
    }
}
