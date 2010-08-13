package slash.ot.server;

import slash.ot.Operation;
import slash.ot.Operations;
import slash.ot.OperationsOnContent;

import java.util.*;

public class ServerImpl implements Server {
    private final Object lock = new Object();
    private Map<Long, Content> contents = new HashMap<Long, Content>();

    public Collection<Long> getContentIds() {
        synchronized (lock) {
            return Collections.unmodifiableSet(contents.keySet());
        }
    }

    public List<OperationsOnContent> processUpdates(long clientId, List<OperationsOnContent> operationsOnContents) {
        List<OperationsOnContent> result = new ArrayList<OperationsOnContent>();
        for (OperationsOnContent operationOnContent : operationsOnContents) {
            Content content;
            synchronized (lock) {
                content = getContent(operationOnContent.getContentId());
            }

            Operations response = process(content, operationOnContent.getOperations());
            result.add(new OperationsOnContent(operationOnContent.getContentId(), response));
        }
        return result;
    }


    private Operations process(Content content, Operations operations) {
        List<Operations> allNewer = allNewer(content.getHistory(), operations.getVersion());
        Operations transformed = operations.transformToInclude(asSingleOperations(allNewer));
        if (transformed.size() > 0) {
            transformed.setVersion(content.getVersion() + 1);
            content.add(transformed);
        }

        List<Operation> responseOperations = new ArrayList<Operation>();
        for (Operations aNewer : allNewer) {
            for (Operation op : aNewer.getOperations())
                responseOperations.add(op);
        }

        Operations response = new Operations(-1, responseOperations);
        Operations transformedResponse = response.transformToInclude(operations);
        transformedResponse.setVersion(content.getVersion());
        return transformedResponse;
    }

    private Operations asSingleOperations(List<Operations> operationsList) {
        long version = 0;
        List<Operation> result = new ArrayList<Operation>();
        for (Operations operations : operationsList) {
            if (operations.getVersion() > version)
                version = operations.getVersion();

            for (Operation operation : operations.getOperations()) {
                result.add(operation);
            }
        }
        return new Operations(version, result);
    }

    private List<Operations> allNewer(List<Operations> operations, long version) {
        List<Operations> result = new ArrayList<Operations>();
        for (Operations ops : operations) {
            if (ops.getVersion() >= version)
                result.add(ops);
        }
        return result;
    }

    public /*for tests*/ Content getContent(long contentId) {
        synchronized (lock) {
            Content content;
            content = contents.get(contentId);
            if (content == null) {
                content = new Content();
                contents.put(contentId, content);
            }
            return content;
        }
    }
}
