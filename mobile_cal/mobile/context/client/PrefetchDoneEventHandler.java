package mobile.context.client;

import java.util.ArrayList;

import mobile.context.app.*;

public interface PrefetchDoneEventHandler{
    public void fetchDone(ArrayList<ApplicationObject> objects);
}
