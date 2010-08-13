package slash.ot.server;

import slash.ot.OperationsOnContent;

import java.util.Collection;
import java.util.List;

public interface Server {
    Collection<Long> getContentIds();

    List<OperationsOnContent> processUpdates(long clientId, List<OperationsOnContent> operationsOnContents);
}
