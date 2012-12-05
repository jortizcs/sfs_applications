package mobile.context.app;

import mobile.context.client.*;

public interface ApplicationServer{
    public ApplicationObject doRead(ObjectName objectName) throws Exception;

    public ApplicationObject[] doWrite(Operation op) throws Exception;

    public ApplicationObject[] doWriteExpression(Expression exp) throws Exception;

    public ApplicationObject doQuery(String queryString) throws Exception;

    public boolean isUp();
}
