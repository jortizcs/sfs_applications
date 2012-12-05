package mobile.context.client;

import java.util.*;
import java.util.concurrent.*;

public class Prefetcher{
    private static Prefetcher prefetcher = null;
    private static ArrayList<PrefetchDoneEventHandler> eventHandlers = null;
    private static long T = -1L; //period (T)
    private static Timer t = null;

    private Prefetcher(){
        eventHandlers = new ArrayList<PrefetchDoneEventHandler>();
        t = new Timer();
    }

    public static Prefetcher getInstance(){
        if(prefetcher==null)
            prefetcher = new Prefetcher();
        return prefetcher;
    }

    public void setPeriod(long period){
        try {
            if(t!=null)
                t.cancel();
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    public int setPrefetcherDoneEventHandler(PrefetchDoneEventHandler handler){
        return 0;
    }

    public class PrefetchTask extends TimerTask{
        public PrefetchTask(){}

        public void run(){
            //prefetch algorithm runs here
        }
    }
}
