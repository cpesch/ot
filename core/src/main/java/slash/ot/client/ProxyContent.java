package slash.ot.client;

import slash.ot.Delete;
import slash.ot.Insert;
import slash.ot.Operation;
import slash.ot.Operations;

public class ProxyContent {
    private StringBuffer buffer = new StringBuffer("");
    private Operations queue = new Operations();

    public synchronized Operations getQueue() {
        return new Operations(queue);
    }

    public synchronized long getVersion() {
        return queue.getVersion();
    }

    public synchronized String getText() {
        return buffer.toString();
    }

    private void checkIndex(int index) {
        if (index < 0 || index > buffer.length())
            throw new IllegalArgumentException("Index " + index + " does not exist");
    }

    public synchronized void insert(String text, int index, long clientId) {
        checkIndex(index);
        buffer.insert(index, text);
        queue.add(new Insert(index, text, clientId));
    }

    public synchronized void delete(int startIndex, int endIndex, long clientId) {
        checkIndex(startIndex);
        checkIndex(endIndex);
        buffer.delete(startIndex, endIndex);
        queue.add(new Delete(startIndex, endIndex, clientId));
    }

    public synchronized void processUpdates(Operations operations) {
        for (Operation operation : operations.getOperations()) {
            operation.mutate(buffer);
        }
        queue.setVersion(operations.getVersion());
    }

    public synchronized void clear() {
        queue.clear();
    }
}
