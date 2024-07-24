package controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dto.TaskDTO;
import entity.Task;
import exception.ServiceException;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mapper.TaskMapper;
import mapper.impl.TaskMapperImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.TaskService;
import service.impl.TaskServiceImpl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class TaskControllerTest {

    private TaskController taskController;
    private TaskService taskService;
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() throws ServletException {
        taskService = mock(TaskService.class);
        objectMapper = new ObjectMapper();
        taskController = new TaskController() {
            @Override
            public void init() {
                this.taskService = TaskControllerTest.this.taskService;
                this.objectMapper = TaskControllerTest.this.objectMapper;
            }
        };
        taskController.init();
    }

    @Test
    public void testDoGetAllTasks() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        List<TaskDTO> tasks = Arrays.asList(new TaskDTO(1L, "Task1", "Description1", 1L),
                new TaskDTO(2L, "Task2", "Description2", 2L));
        when(taskService.getAllTasks()).thenReturn(tasks);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ServletOutputStream servletOutputStream = new ServletOutputStream() {
            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setWriteListener(WriteListener writeListener) {

            }

            @Override
            public void write(int b) {
                outputStream.write(b);
            }
        };
        when(response.getOutputStream()).thenReturn(servletOutputStream);

        taskController.doGet(request, response);

        String jsonResponse = outputStream.toString();
        String expectedJson = objectMapper.writeValueAsString(tasks);
        assertEquals(expectedJson, jsonResponse);
    }

    @Test
    public void testDoGetTaskById() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        TaskDTO task = new TaskDTO(1L, "Task1", "Description1", 1L);

        when(request.getPathInfo()).thenReturn("/1");
        when(taskService.getTaskById(1L)).thenReturn(task);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ServletOutputStream servletOutputStream = new ServletOutputStream() {
            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setWriteListener(WriteListener writeListener) {

            }

            @Override
            public void write(int b) {
                outputStream.write(b);
            }
        };
        when(response.getOutputStream()).thenReturn(servletOutputStream);

        taskController.doGet(request, response);

        String jsonResponse = outputStream.toString();
        String expectedJson = objectMapper.writeValueAsString(task);
        assertEquals(expectedJson, jsonResponse);
    }

    @Test
    public void testDoPostCreateTask() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        TaskDTO taskDTO = new TaskDTO();
        taskDTO.setId(1L);
        taskDTO.setTitle("TaskTitle");
        String json = new ObjectMapper().writeValueAsString(taskDTO);
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(json.getBytes());

        ServletInputStream servletInputStream = new ServletInputStream() {
            @Override
            public int read() throws IOException {
                return byteArrayInputStream.read();
            }

            @Override
            public boolean isFinished() {
                return byteArrayInputStream.available() == 0;
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setReadListener(ReadListener readListener) {
                throw new UnsupportedOperationException();
            }
        };

        doReturn(servletInputStream).when(request).getInputStream();

        taskController.doPost(request, response);

        verify(taskService).createTask(any(TaskDTO.class));
        verify(response).setStatus(HttpServletResponse.SC_CREATED);
    }

    @Test
    void testDoPutUpdateTask() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        TaskDTO taskDTO = new TaskDTO();
        taskDTO.setId(1L);
        taskDTO.setTitle("UpdatedTaskTitle");
        String json = new ObjectMapper().writeValueAsString(taskDTO);
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(json.getBytes());

        ServletInputStream servletInputStream = new ServletInputStream() {
            @Override
            public boolean isFinished() {
                return byteArrayInputStream.available() == 0;
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setReadListener(ReadListener readListener) {
                throw new UnsupportedOperationException();
            }

            @Override
            public int read() throws IOException {
                return byteArrayInputStream.read();
            }
        };

        doReturn(servletInputStream).when(request).getInputStream();

        taskController.doPut(request, response);

        verify(taskService).updateTask(any(TaskDTO.class));
        verify(response).setStatus(HttpServletResponse.SC_NO_CONTENT);
    }

    @Test
    public void testDoDeleteTask() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        when(request.getPathInfo()).thenReturn("/1");

        taskController.doDelete(request, response);

        verify(taskService).deleteTask(1L);
        verify(response).setStatus(HttpServletResponse.SC_NO_CONTENT);
    }

    @Test
    void testTaskControllerConstructor() {
        TaskServiceImpl taskService = mock(TaskServiceImpl.class);
        ObjectMapper objectMapper = new ObjectMapper();
        TaskController taskController = new TaskController(taskService, objectMapper);

        assertNotNull(taskController);
        assertEquals(taskService, taskController.taskService);
        assertEquals(objectMapper, taskController.objectMapper);
    }

    @Test
    void testDoGetTaskByIdNotFound() throws ServletException, IOException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        when(request.getPathInfo()).thenReturn("/1");
        when(taskService.getTaskById(1L)).thenReturn(null);

        taskController.doGet(request, response);

        verify(response).sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    @Test
    void testDoGetInvalidTaskIdFormat() throws ServletException, IOException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        when(request.getPathInfo()).thenReturn("/invalid");

        taskController.doGet(request, response);

        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid task ID format");
    }

    @Test
    void testDoGetInvalidPathLength() throws ServletException, IOException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(request.getPathInfo()).thenReturn("");

        taskController.doGet(request, response);

        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST);

        String[] pathParts = when(request.getPathInfo()).thenReturn("").toString().split("/");

        if (pathParts.length < 1) {
            verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    @Test
    void testDoPostInvalidTaskDTO() throws ServletException, IOException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        TaskDTO taskDTO = new TaskDTO();
        taskDTO.setId(1L);
        taskDTO.setTitle("TaskTitle");
        taskDTO = null;
        String json = new ObjectMapper().writeValueAsString(taskDTO);
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(json.getBytes());

        ServletInputStream servletInputStream = new ServletInputStream() {
            @Override
            public boolean isFinished() {
                return byteArrayInputStream.available() == 0;
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setReadListener(ReadListener readListener) {
                throw new UnsupportedOperationException();
            }

            @Override
            public int read() throws IOException {
                return byteArrayInputStream.read();
            }
        };

        when(request.getInputStream()).thenReturn(servletInputStream);

        taskController.doPost(request, response);

        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid TaskDTO");
    }
    @Test
    void testDoPutInvalidTaskDTO() throws ServletException, IOException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        TaskDTO taskDTO = new TaskDTO();
        taskDTO.setId(1L);
        taskDTO.setTitle("TaskTitle");
        taskDTO = null;
        String json = new ObjectMapper().writeValueAsString(taskDTO);
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(json.getBytes());

        ServletInputStream servletInputStream = new ServletInputStream() {
            @Override
            public boolean isFinished() {
                return byteArrayInputStream.available() == 0;
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setReadListener(ReadListener readListener) {
                throw new UnsupportedOperationException();
            }

            @Override
            public int read() throws IOException {
                return byteArrayInputStream.read();
            }
        };

        when(request.getInputStream()).thenReturn(servletInputStream);

        taskController.doPut(request, response);

        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid TaskDTO");
    }

    @Test
    void testDoDeleteInvalidPath() throws ServletException, IOException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(request.getPathInfo()).thenReturn(null);

        taskController.doDelete(request, response);

        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid path");
    }

    @Test
    void testDoDeleteInvalidPathLength() throws ServletException, IOException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(request.getPathInfo()).thenReturn("");

        taskController.doDelete(request, response);

        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid path");

        String[] pathParts = when(request.getPathInfo()).thenReturn("").toString().split("/");

        if (pathParts.length < 1) {
            verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid path");
        }
    }

    @Test
    void testDoDeleteValidPath() throws ServletException, IOException {
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);

        when(req.getPathInfo()).thenReturn("/1");

        taskController.doDelete(req, resp);

        verify(taskService).deleteTask(1L);
        verify(resp).setStatus(HttpServletResponse.SC_NO_CONTENT);
    }

    @Test
    void testTaskConstructorAndToString() {
        Long id = 1L;
        String title = "Test Task";
        String description = "This is a test task";
        Long assignedUserId = 123L;

        Task task = new Task(id, title, description, assignedUserId);

        assertEquals(id, task.getId());
        assertEquals(title, task.getTitle());
        assertEquals(description, task.getDescription());
        assertEquals(assignedUserId, task.getAssignedUserId());

        String expectedToString = "Task{" +
                "id=" + id +
                ", name='" + title + '\'' +
                ", description='" + description + '\'' +
                ", assignedUserId=" + assignedUserId +
                '}';

        assertEquals(expectedToString, task.toString());
    }

    @Test
    void testTaskDTOConstructorAndToString() {
        Long id = 1L;
        String title = "Test Task";
        String description = "This is a test task";
        Long assignedUserId = 123L;

        TaskDTO taskDTO = new TaskDTO(id, title, description, assignedUserId);

        assertEquals(id, taskDTO.getId());
        assertEquals(title, taskDTO.getTitle());
        assertEquals(description, taskDTO.getDescription());
        assertEquals(assignedUserId, taskDTO.getAssignedUserId());

        String expectedToString = "TaskDTO{" +
                "id=" + id +
                ", name='" + title + '\'' +
                ", description='" + description + '\'' +
                ", assignedUserId=" + assignedUserId +
                '}';

        assertEquals(expectedToString, taskDTO.toString());

    }

    @Test
    void testTaskMapperPutNull() {
        TaskMapper taskMapper = new TaskMapperImpl();
        Task task = null;
        assertEquals(null, taskMapper.toDTO(task));

        Task task2 = new Task(2L, "", "", null);
        assertEquals(null, taskMapper.toDTO(task2));

        TaskDTO taskDTO = null;
        assertEquals(null, taskMapper.toEntity(taskDTO));

        TaskDTO taskDTO2 = new TaskDTO(2L, "", "", null);
        assertEquals(null, taskMapper.toEntity(taskDTO2));
    }

    @Test
    public void testHandleDeleteTask_Success() throws IOException {
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        when(req.getPathInfo()).thenReturn("/1");

        taskController.handleDeleteTask(req, resp);

        verify(taskService).deleteTask(1L);
        verify(resp).setStatus(HttpServletResponse.SC_NO_CONTENT);
    }

    @Test
    public void testHandleDeleteTask_InvalidPath() throws IOException {
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        when(req.getPathInfo()).thenReturn("/");

        taskController.handleDeleteTask(req, resp);

        verify(resp).sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid path");
    }

    @Test
    public void testHandleDeleteTask_InvalidIdFormat() throws IOException {
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        when(req.getPathInfo()).thenReturn("/invalid");

        taskController.handleDeleteTask(req, resp);

        verify(resp).sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid task ID format");
    }

    @Test
    public void testHandleDeleteTask_ServiceException() throws IOException {
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        when(req.getPathInfo()).thenReturn("/1");
        doThrow(new ServiceException("Service error")).when(taskService).deleteTask(1L);

        taskController.handleDeleteTask(req, resp);

        verify(resp).sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to delete task");
    }

    @Test
    public void testHandleGetTaskById_InvalidPath() throws IOException {
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        when(req.getPathInfo()).thenReturn("/");

        taskController.handleGetTaskById("/", resp);

        verify(resp).sendError(HttpServletResponse.SC_BAD_REQUEST);
    }

    @Test
    public void testHandleGetTaskById_InvalidIdFormat() throws IOException {
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        when(req.getPathInfo()).thenReturn("/invalid");

        taskController.handleGetTaskById("/invalid", resp);

        verify(resp).sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid task ID format");
    }

    @Test
    public void testHandleGetTaskById_TaskNotFound() throws IOException {
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        when(req.getPathInfo()).thenReturn("/1");
        when(taskService.getTaskById(1L)).thenReturn(null);

        taskController.handleGetTaskById("/1", resp);

        verify(resp).sendError(HttpServletResponse.SC_NOT_FOUND);
    }

}