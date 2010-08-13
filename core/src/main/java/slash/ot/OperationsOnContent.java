package slash.ot;

public class OperationsOnContent {
    private long contentId;
    private Operations operations;

    public /*for JSON*/ OperationsOnContent() {
    }

    public OperationsOnContent(long contentId, Operations operations) {
        this.contentId = contentId;
        this.operations = operations;
    }

    public long getContentId() {
        return contentId;
    }

    public /*for JSON*/ void setContentId(long contentId) {
        this.contentId = contentId;
    }

    public Operations getOperations() {
        return operations;
    }

    public /*for JSON*/ void setOperations(Operations operations) {
        this.operations = operations;
    }

    public String toString() {
        return "OperationsOnContent[contentId=" + getContentId() + ",operations=" + getOperations() + "]";
    }
}
