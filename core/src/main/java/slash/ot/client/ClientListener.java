package slash.ot.client;

import slash.ot.Operation;

import java.util.Collection;
import java.util.List;

public interface ClientListener {
    void contentsAdded(Collection<Long> contentIds);

    void contentUpdated(long contentId, long version, List<Operation> operations);
}
