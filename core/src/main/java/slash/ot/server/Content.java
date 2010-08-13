package slash.ot.server;

import slash.ot.Operation;
import slash.ot.Operations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Content {
    private List<Operations> history = new ArrayList<Operations>();

    public synchronized List<Operations> getHistory() {
        return Collections.unmodifiableList(history);
    }

    public synchronized void add(Operations operations) {
        history.add(operations);
    }

    public synchronized long getVersion() {
        return history.size() > 0 ? history.get(history.size() - 1).getVersion() : 0;
    }

    public synchronized String getText() {
        StringBuffer buffer = new StringBuffer();
        for (Operations operations : history) {
            for (Operation operation : operations.getOperations()) {
                operation.mutate(buffer);
            }
        }
        return buffer.toString();
    }
}
