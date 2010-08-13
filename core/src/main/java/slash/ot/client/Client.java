package slash.ot.client;

import slash.ot.Operations;
import slash.ot.OperationsOnContent;
import slash.ot.server.Server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class Client {
    private final Object lock = new Object();
    private long clientId;
    private List<ClientListener> clientListeners = new CopyOnWriteArrayList<ClientListener>();
    private Map<Long, ProxyContent> contents = new HashMap<Long, ProxyContent>();

    private Server server;

    public boolean isConnected() {
        return server != null;
    }

    public long getClientId() {
        return clientId;
    }

    public void connect(Server server, long clientId) {
        this.server = server;
        this.clientId = clientId;
        update();
    }

    public void disconnect() {
        this.server = null;
        this.clientId = -1;
        contents = new HashMap<Long, ProxyContent>();
    }

    public void addClientListener(ClientListener listener) {
        clientListeners.add(listener);
    }

    public void removeClientListener(ClientListener listener) {
        clientListeners.remove(listener);
    }

    public ProxyContent getContent(Long contentId) {
        synchronized (lock) {
            ProxyContent content = contents.get(contentId);
            if (content == null) {
                content = new ProxyContent();
                contents.put(contentId, content);
            }
            return content;
        }
    }

    public void insert(long contentId, String text, int index) {
        ProxyContent content = getContent(contentId);
        content.insert(text, index, clientId);
    }

    public void delete(long contentId, int startIndex, int endIndex) {
        ProxyContent content = getContent(contentId);
        content.delete(startIndex, endIndex, clientId);
    }

    public void watch(Long... contentIds) {
        List<OperationsOnContent> queued = collectQueuedOperations();
        for (Long contentId : contentIds) {
            queued.add(new OperationsOnContent(contentId, new Operations()));
        }
        System.out.println(System.currentTimeMillis() + " client " + clientId + " queued " + queued);
        List<OperationsOnContent> updates = server.processUpdates(clientId, queued);
        System.out.println(System.currentTimeMillis() + " client " + clientId + " updates " + updates);
        processUpdates(updates);
        processNewContents();
    }

    public void update() {
        watch();
    }

    private List<OperationsOnContent> collectQueuedOperations() {
        List<OperationsOnContent> modify = new ArrayList<OperationsOnContent>();
        synchronized (lock) {
            for (Map.Entry<Long, ProxyContent> entry : contents.entrySet()) {
                ProxyContent content = entry.getValue();
                Long contentId = entry.getKey();

                Operations queue = content.getQueue();
                Operations send = new Operations(queue.getVersion() + 1, queue.getOperations()).reduce();
                entry.getValue().clear();

                modify.add(new OperationsOnContent(contentId, send));
            }
        }
        return modify;
    }

    private void processUpdates(List<OperationsOnContent> results) {
        for (OperationsOnContent result : results) {
            ProxyContent content = getContent(result.getContentId());
            Operations operations = result.getOperations();
            content.processUpdates(operations);

            for (ClientListener listener : clientListeners) {
                listener.contentUpdated(result.getContentId(), operations.getVersion(), operations.getOperations());
            }
        }
    }

    private void processNewContents() {
        Collection<Long> knownContentIds;
        Collection<Long> allContentIds;
        synchronized (lock) {
            knownContentIds = contents.keySet();
            allContentIds = server.getContentIds();
        }

        Collection<Long> newContentIds = new HashSet<Long>();
        for (Long contentId : allContentIds) {
            if (!knownContentIds.contains(contentId))
                newContentIds.add(contentId);
        }

        synchronized (lock) {
            for (Long newContentId : newContentIds)
                contents.put(newContentId, getContent(newContentId));
        }

        if (newContentIds.size() > 0) {
            for (ClientListener listener : clientListeners) {
                listener.contentsAdded(newContentIds);
            }
        }
    }
}
