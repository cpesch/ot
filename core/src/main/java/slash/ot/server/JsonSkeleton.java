package slash.ot.server;

import slash.ot.OperationsOnContent;
import slash.ot.common.JsonHelper;
import static slash.ot.common.JsonHelper.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonSkeleton {
    private Server server;

    public JsonSkeleton(Server server) {
        this.server = server;
    }

    public String getContentIds() {
        Collection<Long> contentIds = server.getContentIds();

        Map<String, Object> result = new HashMap<String, Object>();
        result.put(CONTENT_IDS, contentIds);
        return JsonHelper.format(result);
    }

    public String processUpdates(String clientIdPlusOperationOnContents) {
        Map<String, Object> map = (Map<String, Object>) JsonHelper.parse(clientIdPlusOperationOnContents);
        Long clientId = (Long) map.get(CLIENT_ID);
        List<OperationsOnContent> operationOnContents = (List<OperationsOnContent>) map.get(OPERATIONS_ON_CONTENTS);

        List<OperationsOnContent> updates = server.processUpdates(clientId, operationOnContents);

        Map<String, Object> result = new HashMap<String, Object>();
        result.put(OPERATIONS_ON_CONTENTS, updates);
        return JsonHelper.format(result);
    }
}
