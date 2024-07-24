package exception;

public class SQLExceptionWrapper extends RuntimeException {
    public SQLExceptionWrapper(String message, Throwable cause) {
        super(message, cause);
    }
}
