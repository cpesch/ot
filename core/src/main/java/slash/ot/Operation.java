package slash.ot;

import java.util.List;

public abstract class Operation {
    protected long clientId;

    protected /*for JSON*/ Operation() {
    }

    public Operation(long clientId) {
        this.clientId = clientId;
    }

    public long getClientId() {
        return clientId;
    }

    public /*for JSON*/ void setClientId(long clientId) {
        this.clientId = clientId;
    }

    public /*for JSON*/ void setType(String type) {
    }


    protected abstract List<Operation> transformToInclude(Operation operation);

    protected abstract List<Operation> mergeWith(Operation operation);

    public abstract void mutate(StringBuffer text);

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Operation operation = (Operation) o;

        return clientId == operation.clientId;
    }

    public int hashCode() {
        return (int) (clientId ^ (clientId >>> 32));
    }
}
