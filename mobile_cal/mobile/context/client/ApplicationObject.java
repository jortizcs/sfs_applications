/**
 * Represents the basic unit of operation for the application.  ApplicationObjects represent 
 * an object in the application that can be mutated by applied an Operation object on it.
 */
public abstract class ApplicationObject{

    private ObjectName name = null;

    public Applicationobject(ObjectName n){
        name = n;
    }
   
    /**
     * Returns the name of the object as represented by the ObjectName.
     */
    public ObjectName getName(){}

    /**
     * Returns of copy of this object.  Used to keep a history of the object before and after the application of an operation.
     */
    public ApplicationObject copyObject();

}
