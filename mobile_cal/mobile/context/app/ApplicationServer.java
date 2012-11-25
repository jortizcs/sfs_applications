package mobile.context.app;

import mobile.context.client.*;

public interface ApplicationServer{
    public ApplicationObject doRead(Operation op);

    public ApplicationObject doWrite(Operation op);

    public byte[] doQuery(Operation op);
}
