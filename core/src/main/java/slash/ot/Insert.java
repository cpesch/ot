package slash.ot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Insert extends Operation {
    private int startIndex;
    private String delta;

    public /*for JSON*/ Insert() {
    }

    public Insert(int startIndex, String delta, long clientId) {
        super(clientId);
        this.startIndex = startIndex;
        this.delta = delta;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public /*for JSON*/ void setStartIndex(int startIndex) {
        this.startIndex = startIndex;
    }

    public int getEndIndex() {
        return getStartIndex() + getLength();
    }

    public int getLength() {
        return delta.length();
    }

    public String getDelta() {
        return delta;
    }

    public /*for JSON*/ void setDelta(String delta) {
        this.delta = delta;
    }


    private List<Operation> asList(int index) {
        List<Operation> result = new ArrayList<Operation>();
        result.add(new Insert(index, delta, clientId));
        return result;
    }

    protected List<Operation> transformToInclude(Operation operation) {
        if (operation instanceof Insert) {
            Insert insert = (Insert) operation;
            if (insert.getStartIndex() < getStartIndex() ||
                    insert.getStartIndex() == getStartIndex() && insert.getClientId() < getClientId())
                return asList(startIndex + insert.getLength());
            else
                return asList(startIndex);

        } else if (operation instanceof Delete) {
            Delete delete = (Delete) operation;
            if (delete.getStartIndex() > getEndIndex())
                return asList(getStartIndex());
            if (delete.getEndIndex() < getStartIndex())
                return asList(getStartIndex() - delete.getLength());

            return asList(delete.getStartIndex());
        } else
            throw new UnsupportedOperationException("Operation " + operation + " is not supported");
    }

    private List<Operation> asList(int index, String delta) {
        List<Operation> result = new ArrayList<Operation>();
        result.add(new Insert(index, delta, clientId));
        return result;
    }

    protected List<Operation> mergeWith(Operation operation) {
        if (operation instanceof Insert) {
            Insert insert = (Insert) operation;
            if (insert.getEndIndex() == getStartIndex())
                return asList(insert.getStartIndex(), insert.getDelta() + getDelta());
            if (insert.getStartIndex() == getEndIndex())
                return asList(getStartIndex(), getDelta() + insert.getDelta());
        }
        return Arrays.asList(this, operation);
    }

    public void mutate(StringBuffer text) {
        text.insert(startIndex, delta);
    }


    public String toString() {
        return "Insert[start=" + getStartIndex() + ",delta=" + getDelta() + "]";
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        Insert insert = (Insert) o;

        return startIndex == insert.startIndex &&
                delta.equals(insert.delta);
    }

    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + startIndex;
        result = 31 * result + delta.hashCode();
        return result;
    }
}