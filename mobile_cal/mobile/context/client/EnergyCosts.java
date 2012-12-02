package mobile.context.client;

import mobile.context.app.*;

import java.util.*;
import java.util.concurrent.*;

//import android.telephony.TelephonyManager;
//import android.net.wifi.WifiInfo;

public class EnergyCosts {

    private static ConcurrentHashMap<ObjectName, Double> fetchSizeMap = null;
    private static ConcurrentHashMap<String, Integer> netAvail = null;
    private static ConcurrentHashMap<String, Double> bwAvail = null;
    private static int netAvailTotal = 0;

    enum ConnectionType{
        WIFI, GPRS, EDGE, HSDPA, LTE
    }

    public static final double WIFI_BW = 7077888.0; //54 Mbps
    public static final double GPRS_BW = 16384.0; //128 Kbps
    public static final double EDGE_BW = 30310.0; //236.8 Kbps
    public static final double THREEG_BW = 262144.0; //2 Mbps
    public static final double HDSPA_BW = 943718.0; //7.2 Mbps

    //approximated using PowerTutor_1.4 and Network Stats
    public static final double WIFI_COST = 0.00000180869267; //joules per byte
    public static final double MOBILE_COST = 0.0000429153442; //joules per byte

    private static final double alpha = 0.8; //how much weight to give to the latest reading

    public String[] netTypeStrs = {"wifi", "gprs", "edge", "hdspa", "lte"};

    private EnergyCosts(){
        fetchSizeMap = new ConcurrentHashMap<ObjectName, Double>();
        netAvail = new ConcurrentHashMap<String, Integer>();

        //init the netAvail map
        for(int i=0; i< netTypeStrs.length; i++)
            netAvail.put(netTypeStrs[i], new Integer(1));

        //init the bandwidth map
        bwAvail.put("wifi", new Double(WIFI_BW));
        bwAvail.put("gprs", new Double(GPRS_BW));
        bwAvail.put("edge", new Double(EDGE_BW));
        bwAvail.put("hdspa", new Double(THREEG_BW));
        bwAvail.put("lte", new Double(HDSPA_BW));
    }

    public double getCost(ObjectName name, int size_in_bytes, ConnectionType type){
        if(type == ConnectionType.WIFI)
            return WIFI_COST * size_in_bytes * getTimeToFetch(name, size_in_bytes, type);
        return MOBILE_COST * size_in_bytes * getTimeToFetch(name, size_in_bytes, type);
    }

    public double getTimeToFetch(ObjectName name, int size, ConnectionType type){
        double fetch_time = 0;
        for(int i=0; i<netTypeStrs.length; i++){
            String typeStr = getTypeString(type);
            fetch_time += (netAvail.get(typeStr).doubleValue()/(double)netAvailTotal)*(size/bwAvail.get(typeStr).doubleValue());
        }
        return fetch_time;
    }

    public void newSample(ObjectName object, int size, ConnectionType type,
            double xfer_time/*seconds*/){
        if(fetchSizeMap.containsKey(object)){
            double avgsize = (1-alpha)*fetchSizeMap.get(object).doubleValue() + alpha*size;
            fetchSizeMap.replace(object, new Double(avgsize));
        } else {
            double avgsize = alpha*size;
            fetchSizeMap.put(object, new Double(avgsize));
        }

        String typeStr = getTypeString(type);
        

        if(netAvail.containsKey(typeStr)){
            int cnt = netAvail.get(typeStr).intValue() +1;
            netAvailTotal +=1;
            netAvail.replace(typeStr, new Integer(cnt));
        } else {
            netAvail.put(typeStr, new Integer(2));
        }
    }


    public String getTypeString(ConnectionType type){
        String typeStr = null;
        switch(type){
            case WIFI:
                typeStr = "wifi";
                break;
            case GPRS:
                typeStr = "gprs";
                break;
            case EDGE:
                typeStr = "edge";
                break;
            case HSDPA:
                typeStr = "hdspa";
                break;
            case LTE:
                typeStr = "lte";
                break;
            default:
                typeStr = "gprs";
                break;
        }
        return typeStr;
    }

}
