package exception;

public class TaskAssignmentException extends DatabaseOperationException {
    public TaskAssignmentException(String message) {
        super(message);
    }

    public TaskAssignmentException(String message, Throwable cause) {
        super(message, cause);
    }
}