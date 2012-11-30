package mobile.context.app;

import mobile.context.client.*;

public interface ApplicationServer{
    public ApplicationObject doRead(ObjectName objectName);

    public ApplicationObject doWrite(Operation op);

    public ApplicationObject[] doWriteExpession(Expression exp);

    public byte[] doQuery(String queryString);
}
