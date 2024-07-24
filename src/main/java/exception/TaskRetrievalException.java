package exception;

public class TaskRetrievalException extends RuntimeException {
    public TaskRetrievalException(String message) {
        super(message);
    }

    public TaskRetrievalException(String message, Throwable cause) {
        super(message, cause);
    }
}
