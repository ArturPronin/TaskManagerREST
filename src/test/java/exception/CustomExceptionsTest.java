package exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CustomExceptionsTest {

    @Test
    public void testConfigurationExceptionMessage() {
        ConfigurationException exception = new ConfigurationException("Configuration error");
        assertEquals("Configuration error", exception.getMessage());
    }

    @Test
    public void testConfigurationExceptionWithCause() {
        Throwable cause = new RuntimeException("Root cause");
        ConfigurationException exception = new ConfigurationException("Configuration error", cause);
        assertEquals("Configuration error", exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    public void testServiceExceptionMessage() {
        ServiceException exception = new ServiceException("Service error");
        assertEquals("Service error", exception.getMessage());
    }

    @Test
    public void testServiceExceptionWithCause() {
        Throwable cause = new RuntimeException("Root cause");
        ServiceException exception = new ServiceException("Service error", cause);
        assertEquals("Service error", exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    public void testTaskAssignmentExceptionMessage() {
        TaskAssignmentException exception = new TaskAssignmentException("Task assignment error");
        assertEquals("Task assignment error", exception.getMessage());
    }

    @Test
    public void testTaskAssignmentExceptionWithCause() {
        Throwable cause = new RuntimeException("Root cause");
        TaskAssignmentException exception = new TaskAssignmentException("Task assignment error", cause);
        assertEquals("Task assignment error", exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    public void testInitializationExceptionMessage() {
        InitializationException exception = new InitializationException("Initialization error");
        assertEquals("Initialization error", exception.getMessage());
    }

    @Test
    public void testInitializationExceptionWithCause() {
        Throwable cause = new RuntimeException("Root cause");
        InitializationException exception = new InitializationException("Initialization error", cause);
        assertEquals("Initialization error", exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    public void testTaskRetrievalExceptionMessage() {
        TaskRetrievalException exception = new TaskRetrievalException("Task retrieval error");
        assertEquals("Task retrieval error", exception.getMessage());
    }

    @Test
    public void testTaskRetrievalExceptionWithCause() {
        Throwable cause = new RuntimeException("Root cause");
        TaskRetrievalException exception = new TaskRetrievalException("Task retrieval error", cause);
        assertEquals("Task retrieval error", exception.getMessage());
        assertEquals(cause, exception.getCause());
    }
}
