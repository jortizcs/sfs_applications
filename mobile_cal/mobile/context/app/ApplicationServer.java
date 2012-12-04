package mobile.context.app;

import mobile.context.client.*;

public interface ApplicationServer{
    public ApplicationObject doRead(ObjectName objectName) throws Exception;

    public ApplicationObject[] doWrite(Operation op) throws Exception;

    public ApplicationObject[] doWriteExpession(Expression exp) throws Exception;

    public byte[] doQuery(String queryString) throws Exception;

    public boolean isUp();
}
