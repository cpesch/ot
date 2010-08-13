package slash.ot;

import junit.framework.TestCase;
import slash.ot.client.Client;
import slash.ot.server.ServerImpl;

import java.util.Random;

public class UpdateTest extends TestCase {
    private Client client1 = new Client();
    private Client client2 = new Client();
    private ServerImpl server = new ServerImpl();
    private final long contentId = new Random().nextLong();

    protected void setUp() throws Exception {
        client1.connect(server, 1);
        client2.connect(server, 2);
    }

    public void testInsert() {
        client1.insert(contentId, "a", 0);
        assertEquals("a", client1.getContent(contentId).getText());
        assertEquals(new Operations(0, new Insert(0, "a", 1)), client1.getContent(contentId).getQueue());
        assertEquals("", server.getContent(contentId).getText());

        client1.update();
        assertEquals("a", server.getContent(contentId).getText());
        assertEquals(1, client1.getContent(contentId).getVersion());
        assertEquals(1, server.getContent(contentId).getVersion());
        assertEquals("a", client1.getContent(contentId).getText());

        client2.watch(contentId);
        assertEquals("a", client2.getContent(contentId).getText());
        assertEquals(1, client2.getContent(contentId).getVersion());
    }

    public void testInsertUpdateThenDelete() {
        client1.insert(contentId, "a", 0);
        client1.update();
        client1.delete(contentId, 0, 1);
        assertEquals(new Operations(1, new Delete(0, 1, 1)), client1.getContent(contentId).getQueue());

        client1.update();
        assertEquals("", client1.getContent(contentId).getText());
        assertEquals(2, client1.getContent(contentId).getVersion());
        assertEquals("", server.getContent(contentId).getText());
        assertEquals(2, server.getContent(contentId).getVersion());

        client2.watch(contentId);
        assertEquals("", client2.getContent(contentId).getText());
        assertEquals(2, client2.getContent(contentId).getVersion());
    }

    public void testInsertDeleteThenUpdate() {
        client1.insert(contentId, "a", 0);
        client1.delete(contentId, 0, 1);
        assertEquals(new Operations(0, new Insert(0, "a", 1), new Delete(0, 1, 1)), client1.getContent(contentId).getQueue());

        client1.update();
        assertEquals("", client1.getContent(contentId).getText());
        assertEquals(1, client1.getContent(contentId).getVersion());
        assertEquals("", server.getContent(contentId).getText());
        assertEquals(1, server.getContent(contentId).getVersion());

        client2.watch(contentId);
        assertEquals("", client2.getContent(contentId).getText());
        assertEquals(1, client2.getContent(contentId).getVersion());
    }

    public void testIllegalIndex() {
        try {
            client1.insert(4711, "a", 1);
            assertTrue(false);
        }
        catch (IllegalArgumentException e) {
        }

        try {
            client1.insert(4711, "a", -1);
            assertTrue(false);
        }
        catch (IllegalArgumentException e) {
        }
    }

    public void testUpdateWithTwoClients() {
        client1.insert(contentId, "a", 0);
        assertEquals("", client2.getContent(contentId).getText());
        client1.update();
        client2.update();
        assertEquals("a", client2.getContent(contentId).getText());
    }

    public void testInsertAndUpdateTwiceWithSameClient() {
        client1.insert(contentId, "a", 0);
        client1.update();
        client2.watch(contentId);
        assertEquals("a", client1.getContent(contentId).getText());
        assertEquals("a", client2.getContent(contentId).getText());

        client1.insert(contentId, "b", 1);
        client1.update();
        client2.update();
        assertEquals("ab", client1.getContent(contentId).getText());
        assertEquals("ab", client2.getContent(contentId).getText());
    }

    public void testInsertAndUpdateTwiceWithDifferenceClients() {
        client1.insert(contentId, "a", 0);
        client1.update();
        client2.watch(contentId);
        assertEquals("a", client1.getContent(contentId).getText());
        assertEquals("a", client2.getContent(contentId).getText());

        client2.insert(contentId, "b", 1);
        client2.update();
        client1.update();
        assertEquals("ab", client1.getContent(contentId).getText());
        assertEquals("ab", client2.getContent(contentId).getText());
    }

    public void testUpdateTwoContents() {
        client1.insert(contentId, "a", 0);
        client1.update();
        assertTrue(server.getContentIds().contains(contentId));
        client1.insert(contentId + 1, "b", 0);
        client1.update();
        assertEquals(2, server.getContentIds().size());
        assertTrue(server.getContentIds().contains(contentId + 1));
    }
}