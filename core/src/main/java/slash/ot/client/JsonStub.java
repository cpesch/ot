package slash.ot.client;

import slash.ot.OperationsOnContent;
import slash.ot.common.JsonHelper;
import slash.ot.server.JsonSkeleton;
import slash.ot.server.Server;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static slash.ot.common.JsonHelper.CLIENT_ID;
import static slash.ot.common.JsonHelper.CONTENT_IDS;
import static slash.ot.common.JsonHelper.OPERATIONS_ON_CONTENTS;

public class JsonStub implements Server {
    private JsonSkeleton skeleton;

    public JsonStub(JsonSkeleton skeleton) {
        this.skeleton = skeleton;
    }

    public Collection<Long> getContentIds() {
        String result = skeleton.getContentIds();

        Map<String, Object> results = (Map<String, Object>) JsonHelper.parse(result);
        return (List<Long>) results.get(CONTENT_IDS);
    }

    public List<OperationsOnContent> processUpdates(long clientId, List<OperationsOnContent> operationsOnContents) {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(CLIENT_ID, clientId);
        parameters.put(OPERATIONS_ON_CONTENTS, operationsOnContents);
        String clientIdPlusOperationOnContents = JsonHelper.format(parameters);

        String result = skeleton.processUpdates(clientIdPlusOperationOnContents);

        Map<String, Object> results = (Map<String, Object>) JsonHelper.parse(result);
        return (List<OperationsOnContent>) results.get(OPERATIONS_ON_CONTENTS);
    }
}
