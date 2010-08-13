package slash.ot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Delete extends Operation {
    private int startIndex, endIndex;

    public /*for JSON*/ Delete() {
    }

    public Delete(int startIndex, int endIndex, long clientId) {
        super(clientId);
        this.startIndex = startIndex;
        this.endIndex = endIndex;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public /*for JSON*/ void setStartIndex(int startIndex) {
        this.startIndex = startIndex;
    }

    public int getEndIndex() {
        return endIndex;
    }

    public /*for JSON*/ void setEndIndex(int endIndex) {
        this.endIndex = endIndex;
    }

    public int getLength() {
        return getEndIndex() - getStartIndex();
    }


    private List<Operation> asList(int startIndex, int endIndex) {
        List<Operation> result = new ArrayList<Operation>();
        result.add(new Delete(startIndex, endIndex, clientId));
        return result;
    }

    protected List<Operation> transformToInclude(Operation operation) {
        if (operation instanceof Delete) {
            Delete delete = (Delete) operation;
            // other operation is completely behind this operation
            if (delete.getStartIndex() >= getEndIndex())
                return asList(getStartIndex(), getEndIndex());
            // other operation is completely before this operation
            if (delete.getEndIndex() <= getStartIndex())
                return asList(getStartIndex() - delete.getLength(), getEndIndex() - delete.getLength());

            // other operation completely covers this operation
            if (delete.getStartIndex() <= getStartIndex() && delete.getEndIndex() >= getEndIndex())
                return new ArrayList<Operation>();
            // other operation is a subset of this operation
            if (delete.getStartIndex() >= getStartIndex() && delete.getEndIndex() <= getEndIndex())
                return asList(getStartIndex(), getEndIndex() - delete.getLength());

            // other operation overlaps with the start of this operation
            if (delete.getStartIndex() <= getStartIndex() && delete.getEndIndex() <= getEndIndex())
                return asList(delete.getStartIndex(), delete.getStartIndex() + (getEndIndex() - delete.getEndIndex()));
            // other operation overlaps with the end of this operation
            if (delete.getStartIndex() >= getStartIndex() && delete.getEndIndex() >= getEndIndex())
                return asList(getStartIndex(), getStartIndex() + (delete.getStartIndex() - getStartIndex()));

            throw new InternalError("uncovered Delete on Delete case");

        } else if (operation instanceof Insert) {
            Insert insert = (Insert) operation;
            // other operation is completely behind this operation
            if (insert.getStartIndex() >= getEndIndex())
                return asList(getStartIndex(), getEndIndex());
            // other operation is completely before this operation
            if (insert.getEndIndex() <= getStartIndex())
                return asList(getStartIndex() + insert.getLength(), getEndIndex() + insert.getLength());

            // other operation overlaps with the start of this operation
            if (insert.getStartIndex() <= getStartIndex() && insert.getEndIndex() >= getEndIndex())
                return asList(getStartIndex() + insert.getLength(), getEndIndex() + insert.getLength());
            // other operation overlaps with the end of this operation
            if (insert.getStartIndex() >= getStartIndex() && insert.getEndIndex() <= getEndIndex()) {
                List<Operation> result = new ArrayList<Operation>();
                result.add(new Delete(getStartIndex(), insert.getStartIndex(), clientId));
                result.add(new Delete(getStartIndex() + insert.getLength(),
                        getEndIndex() - (insert.getStartIndex() - getStartIndex()) + insert.getLength(), clientId));
                return result;
            }

            throw new InternalError("uncovered Delete on Insert case");
        } else
            throw new UnsupportedOperationException("Operation " + operation + " is not supported");
    }

    protected List<Operation> mergeWith(Operation operation) {
        if (operation instanceof Delete) {
            Delete delete = (Delete) operation;
            if (delete.getEndIndex() == getStartIndex())
                return asList(delete.getStartIndex(), getEndIndex());
            if (delete.getStartIndex() == getEndIndex())
                return asList(getStartIndex(), delete.getEndIndex());
        }
        return Arrays.asList(this, operation);
    }

    public void mutate(StringBuffer text) {
        text.delete(startIndex, endIndex);
    }


    public String toString() {
        return "Delete[start=" + getStartIndex() + ",end=" + getEndIndex() + "]";
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        Delete that = (Delete) o;

        return endIndex == that.endIndex &&
                startIndex == that.startIndex;
    }

    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + startIndex;
        result = 31 * result + endIndex;
        return result;
    }
}
