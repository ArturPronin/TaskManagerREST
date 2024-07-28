package controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dto.TagDTO;
import dto.TaskDTO;
import entity.Tag;
import entity.Task;
import exception.ServiceException;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mapper.TaskMapper;
import mapper.impl.TaskMapperImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import service.TaskService;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class TaskControllerTest {

    private TaskService taskService;
    private ObjectMapper objectMapper;
    private TaskController taskController;
    private Logger mockLogger;

    @BeforeEach
    public void setUp() {
        taskService = Mockito.mock(TaskService.class);
        objectMapper = new ObjectMapper();
        taskController = new TaskController(taskService, objectMapper);
        mockLogger = mock(Logger.class);
    }

    @Test
    public void testDoGetAllTasks() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        List<TaskDTO> tasks = Arrays.asList(new TaskDTO(1L, "Task1", "Description1", 1L, null),
                new TaskDTO(2L, "Task2", "Description2", 2L, null));
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
        TaskDTO task = new TaskDTO(1L, "Task1", "Description1", 1L, null);

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
        List<TagDTO> tagsDTO = new ArrayList<>();

        TaskDTO taskDTO = new TaskDTO();
        taskDTO.setId(1L);
        taskDTO.setTitle("TaskTitle");
        taskDTO.setTags(tagsDTO);
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

        when(request.getPathInfo()).thenReturn("/");
        when(request.getInputStream()).thenReturn(servletInputStream);
        doNothing().when(taskService).createTask(any(TaskDTO.class));
        doNothing().when(response).setStatus(HttpServletResponse.SC_CREATED);

        taskController.doPost(request, response);

        verify(request).getInputStream();
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

        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid path get");
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

        when(request.getPathInfo()).thenReturn("/");
        when(request.getInputStream()).thenReturn(servletInputStream);
        doNothing().when(taskService).createTask(any(TaskDTO.class));
        doNothing().when(response).setStatus(HttpServletResponse.SC_CREATED);

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
        List<Tag> tags = new ArrayList<>();
        Tag tag = new Tag(1L, "Tag1", null);
        tags.add(tag);

        Task task = new Task(id, title, description, assignedUserId, tags);

        assertEquals(id, task.getId());
        assertEquals(title, task.getTitle());
        assertEquals(description, task.getDescription());
        assertEquals(assignedUserId, task.getAssignedUserId());
        assertEquals(tags, task.getTags());

        String expectedToString = "Task{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", assignedUserId=" + assignedUserId +
                ", tags=" + tags +
                '}';

        assertEquals(expectedToString, task.toString());
    }

    @Test
    void testTaskDTOConstructorAndToString() {
        Long id = 1L;
        String title = "Test Task";
        String description = "This is a test task";
        Long assignedUserId = 123L;
        List<TagDTO> tagsDTO = new ArrayList<>();
        TagDTO tagDTO = new TagDTO(1L, "Tag1", null);
        TagDTO tagDTO2 = new TagDTO(2L, "Tag2", null);
        tagsDTO.add(tagDTO);
        tagsDTO.add(tagDTO2);

        TaskDTO taskDTO = new TaskDTO(id, title, description, assignedUserId, tagsDTO);

        assertEquals(id, taskDTO.getId());
        assertEquals(title, taskDTO.getTitle());
        assertEquals(description, taskDTO.getDescription());
        assertEquals(assignedUserId, taskDTO.getAssignedUserId());
        assertEquals(tagsDTO, taskDTO.getTags());

        String expectedToString = "TaskDTO{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", assignedUserId=" + assignedUserId +
                ", tags=" + tagsDTO +
                '}';

        assertEquals(expectedToString, taskDTO.toString());

    }

    @Test
    void testTaskMapperPutNull() {
        TaskMapper taskMapper = new TaskMapperImpl();
        Task task = null;
        assertNull(taskMapper.toDTO(task));

        Task task2 = new Task(2L, "", "", null, null);
        assertNull(taskMapper.toDTO(task2));

        TaskDTO taskDTO = null;
        assertNull(taskMapper.toEntity(taskDTO));

        TaskDTO taskDTO2 = new TaskDTO(2L, "", "", null, null);
        assertNull(taskMapper.toEntity(taskDTO2));
    }

    @Test
    public void testHandleDeleteTaskSuccess() throws IOException {
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        when(req.getPathInfo()).thenReturn("/1");

        taskController.handleDeleteTask(req, resp);

        verify(taskService).deleteTask(1L);
        verify(resp).setStatus(HttpServletResponse.SC_NO_CONTENT);
    }

    @Test
    public void testHandleDeleteTaskInvalidPath() throws IOException {
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        when(req.getPathInfo()).thenReturn("/");

        taskController.handleDeleteTask(req, resp);

        verify(resp).sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid path");
    }

    @Test
    public void testHandleDeleteTaskInvalidIdFormat() throws IOException {
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        when(req.getPathInfo()).thenReturn("/invalid");

        taskController.handleDeleteTask(req, resp);

        verify(resp).sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid task ID format");
    }

    @Test
    public void testHandleDeleteTaskServiceException() throws IOException {
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        when(req.getPathInfo()).thenReturn("/1");
        doThrow(new ServiceException("Service error")).when(taskService).deleteTask(1L);

        taskController.handleDeleteTask(req, resp);

        verify(resp).sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to delete task");
    }

    @Test
    public void testHandleGetTaskByIdInvalidPath() throws IOException {
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        when(req.getPathInfo()).thenReturn("/");

        taskController.handleGetTaskById("/", resp);

        verify(resp).sendError(HttpServletResponse.SC_BAD_REQUEST);
    }

    @Test
    void testHandleGetTaskByIdInvalidIdFormat() throws IOException {
        HttpServletResponse resp = mock(HttpServletResponse.class);
        when(resp.getOutputStream()).thenThrow(new IOException("OutputStream error"));

        taskController.handleGetTaskById("/invalid", resp);

        verify(resp).sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid task ID format");
    }

    @Test
    void testHandleGetTaskByIdTaskNotFound() throws IOException {
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);

        when(req.getPathInfo()).thenReturn("/1");
        when(taskService.getTaskById(1L)).thenReturn(null);

        taskController.handleGetTaskById("/1", resp);

        verify(resp).sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    @Test
    void testHandleIOException() throws IOException {
        HttpServletResponse resp = mock(HttpServletResponse.class);
        IOException ioException = new IOException("Test IOException");

        taskController.handleIOException(ioException, resp);

        verify(resp).sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An internal error occurred");
    }

    @Test
    void testDoPostIOException() throws IOException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        when(request.getPathInfo()).thenReturn("/invalid/path");

        taskController.doPost(request, response);

        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid Path");
    }

    @Test
    public void testDoPostAssignTagsToTask() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        when(request.getPathInfo()).thenReturn("/1/tags/2");

        taskController.doPost(request, response);

        verify(taskService).assignTagsToTask(1L, 2L);
        verify(response).setStatus(HttpServletResponse.SC_NO_CONTENT);
    }
}
