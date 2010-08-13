package slash.ot;

import junit.framework.TestCase;
import slash.ot.client.Client;
import slash.ot.client.JsonStub;
import slash.ot.server.JsonSkeleton;
import slash.ot.server.ServerImpl;

import java.util.Arrays;

public class JsonConnectTest extends TestCase {
    private Client client = new Client();
    private ServerImpl server = new ServerImpl();
    private JsonStub stub = new JsonStub(new JsonSkeleton(server));

    public void testGetContentIds() {
        server.processUpdates(1, Arrays.asList(new OperationsOnContent(1, new Operations()), new OperationsOnContent(2, new Operations())));
        client.connect(stub, 1);
        assertEquals(Arrays.asList(1L, 2L), stub.getContentIds());
    }

    public void testProcessUpdates() {
        client.connect(stub, 1);
        client.insert(1, "a", 0);
        client.insert(2, "b", 0);
        client.update();
        assertEquals(Arrays.asList(1L, 2L), stub.getContentIds());
    }
}

