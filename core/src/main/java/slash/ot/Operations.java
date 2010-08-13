package slash.ot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Operations {
    private long version;
    private List<Operation> operations = new ArrayList<Operation>();

    public Operations() {
        this(0);
    }

    public Operations(long version) {
        this.version = version;
    }

    public Operations(long version, Operation... operations) {
        this(version);
        for (Operation operation : operations)
            add(operation);
    }

    public Operations(long version, List<Operation> operations) {
        this(version, operations.toArray(new Operation[operations.size()]));
    }

    public Operations(Operations operations) {
        this(operations.getVersion(), operations.getOperations());
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public List<Operation> getOperations() {
        return new ArrayList<Operation>(operations);
    }

    public /*for JSON*/ void setOperations(List<Operation> operations) {
        this.operations = operations;
    }

    public void add(Operation operation) {
        operations.add(operation);
    }

    public Operation get(int index) {
        return operations.get(index);
    }

    public int size() {
        return operations.size();
    }

    public void clear() {
        operations.clear();
    }

    public Operations transformToInclude(Operations operations) {
        List<Operation> result = transform(getOperations(), operations.getOperations());
        return new Operations(-1, result);
    }

    private List<Operation> transformInner(List<Operation> toBeTransformed, List<Operation> base) {
        List<Operation> result = new ArrayList<Operation>();
        for (Operation operation : toBeTransformed) {
            if (base.size() == 0) {
                result.add(operation);
            } else {
                List<Operation> transformed = operation.transformToInclude(base.get(0));
                result.addAll(transformInner(transformed, base.subList(1, base.size())));
            }
        }
        return result;
    }

    private List<Operation> transform(Operation transform, List<Operation> base) {
        return transformInner(Arrays.asList(transform), base);
    }

    private List<Operation> transform(List<Operation> toBeTransformed, List<Operation> base) {
        if (toBeTransformed.size() == 0)
            return new ArrayList<Operation>();

        Operation firstLeft = toBeTransformed.get(0);
        List<Operation> tailLeft = toBeTransformed.subList(1, toBeTransformed.size());

        // transform first element against base
        List<Operation> firstProcessed = transform(firstLeft, base);

        List<Operation> result = new ArrayList<Operation>();
        result.addAll(firstProcessed);

        if (tailLeft.size() > 0) {
            // transform the rest of the base against the result
            List<Operation> restBase = transform(base, firstProcessed);

            // transform tail against base
            result.addAll(transform(tailLeft, restBase));
        }
        return result;
    }

    public Operations reduce() {
        return new Operations(getVersion(), reduceInner(new ArrayList<Operation>(), getOperations()));
    }

    private List<Operation> reduceInner(List<Operation> transformed, List<Operation> toBeTransformed) {
        if (toBeTransformed.size() == 0)
            return transformed;

        Operation firstToBeTransformed = toBeTransformed.get(0);
        List<Operation> tailtoBeTransformed = toBeTransformed.subList(1, toBeTransformed.size());

        List<Operation> headTransformed;
        List<Operation> merged;
        if (transformed.size() == 0) {
            headTransformed = Arrays.asList(firstToBeTransformed);
            merged = Collections.EMPTY_LIST;
        } else {
            headTransformed = transformed.subList(0, transformed.size() - 1);
            Operation lastTransformed = transformed.get(transformed.size() - 1);
            merged = lastTransformed.mergeWith(firstToBeTransformed);
        }

        List<Operation> headAlreadyTransformed = new ArrayList<Operation>();
        headAlreadyTransformed.addAll(headTransformed);
        headAlreadyTransformed.addAll(merged);

        return reduceInner(headAlreadyTransformed, tailtoBeTransformed);
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer("Operations[version=");
        buffer.append(version).append(",operations=[");
        for (Operation operation : operations)
            buffer.append(operation.toString()).append(",");
        buffer.append("]]");
        return buffer.toString();
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Operations that = (Operations) o;

        return version == that.version &&
                !(operations != null ? !operations.equals(that.operations) : that.operations != null);
    }

    public int hashCode() {
        int result = (int) (version ^ (version >>> 32));
        result = 31 * result + (operations != null ? operations.hashCode() : 0);
        return result;
    }
}
