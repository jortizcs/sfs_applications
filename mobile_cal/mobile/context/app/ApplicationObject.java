import java.util.*;

/**
 * Represents the basic unit of operation for the application.  ApplicationObjects represent 
 * an object in the application that can be mutated by applied an Operation object on it.
 */
public abstract class ApplicationObject{

    private ObjectName name = null;

    public ApplicationObject(ObjectName n){
        name = n;
    }
   
    /**
     * Returns the name of the object as represented by the ObjectName.
     */
    public ObjectName getName(){
        return name;
    }

    /**
     * Returns of copy of this object.  Used to keep a history of the object before and after the application of an operation.
     */
    public ApplicationObject clone(){
        return (ApplicationObject)this.clone();
    }

    /**
     * Returns a byte array representation of this application object.
     */
    public abstract byte[] getBytes();

    public boolean equals(ApplicationObject appObj){
        if(appObj==null)
            return false;
        if(Arrays.equals(appObj.getName().getByteName(), name.getByteName()) && 
                Arrays.equals(appObj.getBytes(), getBytes()))
            return true;
        return false;
    }

}
