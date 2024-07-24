package controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dto.TaskDTO;
import dto.UserDTO;
import entity.Task;
import entity.User;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mapper.UserMapper;
import mapper.impl.UserMapperImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.com.fasterxml.jackson.core.JsonGenerator;
import service.UserService;
import service.impl.UserServiceImpl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

public class UserControllerTest {

    private UserController userController;
    private UserService userService;
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() throws ServletException {

        userService = mock(UserService.class);
        objectMapper = new ObjectMapper();
        userController = new UserController() {
            @Override
            public void init() {
                this.userService = UserControllerTest.this.userService;
                this.objectMapper = UserControllerTest.this.objectMapper;
            }
        };
        userController.init();
    }
    @Test
    public void testDoGetAllUsers() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        List<UserDTO> users = Arrays.asList(new UserDTO(1L, "User1", null), new UserDTO(2L, "User2", null));
        when(userService.getAllUsers()).thenReturn(users);

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

        userController.doGet(request, response);

        String jsonResponse = outputStream.toString();
        String expectedJson = objectMapper.writeValueAsString(users);
        assertEquals(expectedJson, jsonResponse);
    }

    @Test
    public void testDoGetUserById() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        UserDTO user = new UserDTO(1L, "User1", Collections.emptyList());

        when(request.getPathInfo()).thenReturn("/1");
        when(userService.getUserById(1L)).thenReturn(user);
        when(userService.getTasksByUserId(1L)).thenReturn(Collections.emptyList());

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

        userController.doGet(request, response);

        String jsonResponse = outputStream.toString();
        String expectedJson = objectMapper.writeValueAsString(user);
        assertEquals(expectedJson, jsonResponse);
    }

    @Test
    public void testDoPostAssignTaskToUser() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        when(request.getPathInfo()).thenReturn("/1/tasks/2");

        userController.doPost(request, response);

        verify(userService).assignTaskToUser(1L, 2L);
        verify(response).setStatus(HttpServletResponse.SC_NO_CONTENT);
    }

    @Test
    void testDoPostCreateUser() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        UserDTO userDTO = new UserDTO();
        userDTO.setId(1L);
        userDTO.setName("SetUsername");
        String json = new ObjectMapper().writeValueAsString(userDTO);
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
        doNothing().when(userService).createUser(any(UserDTO.class));
        doNothing().when(response).setStatus(HttpServletResponse.SC_CREATED);

        userController.doPost(request, response);

        verify(request).getInputStream();
        verify(userService).createUser(any(UserDTO.class));
        verify(response).setStatus(HttpServletResponse.SC_CREATED);
    }
    @Test
    void testDoPutUpdateUser() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        UserDTO userDTO = new UserDTO();
        userDTO.setId(1L);
        userDTO.setName("SetUsername");
        String json = new ObjectMapper().writeValueAsString(userDTO);
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
        when(userService.getUserById(1L)).thenReturn(userDTO);
        doNothing().when(userService).updateUser(any(UserDTO.class));
        doNothing().when(response).setStatus(HttpServletResponse.SC_NO_CONTENT);

        userController.doPut(request, response);

        verify(request).getInputStream();
        verify(userService).getUserById(1L);
        verify(userService).updateUser(any(UserDTO.class));
        verify(response).setStatus(HttpServletResponse.SC_NO_CONTENT);
    }
    @Test
    public void testDoDeleteUser() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        when(request.getPathInfo()).thenReturn("/1");

        userController.doDelete(request, response);

        verify(userService).deleteUser(1L);
        verify(response).setStatus(HttpServletResponse.SC_NO_CONTENT);
    }
    @Test
    public void testDoGetInvalidPath() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        when(request.getPathInfo()).thenReturn("/invalid/path");

        userController.doGet(request, response);

        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid path");
    }

    @Test
    void testDoPostInvalidPath() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        when(request.getPathInfo()).thenReturn("/invalid/path");

        userController.doPost(request, response);

        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid Path");
    }

    @Test
    void testDoDeleteInvalidPathLength() throws ServletException, IOException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        when(request.getPathInfo()).thenReturn("");

        userController.doDelete(request, response);

        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid path length");
    }

    @Test
    void testDoPostUserDTONull() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        UserDTO userDTO = null;
        String json = new ObjectMapper().writeValueAsString(userDTO);
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(json.getBytes());
        when(request.getPathInfo()).thenReturn("/");

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

        when(request.getInputStream()).thenReturn(servletInputStream);

        userController.doPost(request, response);

        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "UserDTO cannot be null");

    }

    @Test
    void testDoPutUserDTONull() throws ServletException, IOException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        UserDTO userDTO = null;
        String json = new ObjectMapper().writeValueAsString(userDTO);
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(json.getBytes());
        when(request.getPathInfo()).thenReturn("/");

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

        when(request.getInputStream()).thenReturn(servletInputStream);

        userController.doPut(request, response);

        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "UserDTO cannot be null");
    }

    @Test
    void testUserControllerConstructor() {
        UserServiceImpl userService = mock(UserServiceImpl.class);
        ObjectMapper objectMapper = new ObjectMapper();

        UserController userController = new UserController(userService, objectMapper);

        assertNotNull(userController);
        assertEquals(userService, userController.userService);
        assertEquals(objectMapper, userController.objectMapper);
    }

    @Test
    void testDoPostInvalidJSON() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        when(request.getPathInfo()).thenReturn("/");

        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream("invalid json".getBytes());
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
        when(request.getInputStream()).thenReturn(servletInputStream);

        userController.doPost(request, response);

        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid UserDTO");
    }

    @Test
    void testDoPostAssignTaskInvalidPath() throws Exception {

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        when(request.getPathInfo()).thenReturn("/1/tasks/");

        userController.doPost(request, response);

        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid Path");
    }
    @Test
    public void testDoPutInvalidUserDTO() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        when(request.getInputStream()).thenThrow(new IOException("Invalid Input"));

        userController.doPut(request, response);

        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid UserDTO");
    }

    @Test
    public void testDoDeleteInvalidPath() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        when(request.getPathInfo()).thenReturn("/");

        userController.doDelete(request, response);

        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Path cannot be null or empty");
    }

    @Test
    public void testInitWithSQLException() throws Exception {
        UserController userController = new UserController() {
            @Override
            public void init() throws ServletException {
                try {
                    throw new SQLException("Database error");
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        };

        assertThrows(RuntimeException.class, () -> userController.init());
    }

    @Test
    public void testDoGetUserNotFound() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        when(request.getPathInfo()).thenReturn("/999");
        when(userService.getUserById(999L)).thenReturn(null);

        userController.doGet(request, response);

        verify(response).sendError(HttpServletResponse.SC_NOT_FOUND, "User not found");
    }

    @Test
    void testDoPutUpdateNonExistentUser() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        UserDTO userDTO = new UserDTO();
        userDTO.setId(1L);
        userDTO.setName("SetUsername");
        String json = new ObjectMapper().writeValueAsString(userDTO);
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
        when(userService.getUserById(anyLong())).thenReturn(null);

        userController.doPut(request, response);

        verify(response).sendError(HttpServletResponse.SC_NOT_FOUND, "User not found");
    }

    @Test
    void testUserConstructorAndToString() {
        Long id = 1L;
        String name = "Test User";
        Task task1 = new Task(1L, "Task1", "Description1", 1L);
        Task task2 = new Task(2L, "Task2", "Description2", 2L);
        List<Task> tasks = Arrays.asList(task1, task2);

        User user = new User(id, name, tasks);

        assertEquals(id, user.getId());
        assertEquals(name, user.getName());
        assertEquals(tasks, user.getTasks());

        String expectedToString = "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", tasks=" + tasks +
                '}';

        assertEquals(expectedToString, user.toString());
    }

    @Test
    void testUserDTOConstructorAndToString() {
        Long id = 1L;
        String name = "Test User";
        TaskDTO task1 = new TaskDTO(1L, "Task1", "Description1", 1L);
        TaskDTO task2 = new TaskDTO(2L, "Task2", "Description2", 2L);
        List<TaskDTO> tasks = Arrays.asList(task1, task2);

        UserDTO userDTO = new UserDTO(id, name, tasks);

        assertEquals(id, userDTO.getId());
        assertEquals(name, userDTO.getName());
        assertEquals(tasks, userDTO.getTasks());

        String expectedToString = "UserDTO{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", tasks=" + tasks +
                '}';

        assertEquals(expectedToString, userDTO.toString());
    }

    @Test
    void testUserMapperPutNull() {
        UserMapper userMapper = new UserMapperImpl();
        User user = null;
        assertNull(userMapper.toDTO(user));
        User user2 = new User(2L, "", null);
        assertNull(userMapper.toDTO(user2));

        UserDTO userDTO = null;
        assertNull(userMapper.toEntity(userDTO));
        UserDTO userDTO2 = new UserDTO(2L, "", null);
        assertNull(userMapper.toEntity(userDTO2));
    }

    @Test
    public void testHandleErrorSendsError() throws IOException {
        HttpServletResponse resp = mock(HttpServletResponse.class);

        userController.handleError(resp, HttpServletResponse.SC_BAD_REQUEST, "Bad Request");

        verify(resp).sendError(HttpServletResponse.SC_BAD_REQUEST, "Bad Request");
    }

    @Test
    public void testHandleErrorSendsInternalServerErrorOnIOException() throws IOException {
        HttpServletResponse resp = mock(HttpServletResponse.class);
        doThrow(new IOException("Network error")).when(resp).sendError(HttpServletResponse.SC_BAD_REQUEST, "Bad Request");

        userController.handleError(resp, HttpServletResponse.SC_BAD_REQUEST, "Bad Request");

        verify(resp).sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An unexpected error occurred");
    }

    @Test
    public void testHandleSendErrorExceptionSendsError() throws IOException {
        HttpServletResponse resp = mock(HttpServletResponse.class);

        userController.handleSendErrorException(HttpServletResponse.SC_NOT_FOUND, "Not Found", resp);

        verify(resp).sendError(HttpServletResponse.SC_NOT_FOUND, "Not Found");
    }

    @Test
    public void testHandleSendErrorExceptionOnIOException() throws IOException {
        HttpServletResponse resp = mock(HttpServletResponse.class);
        doThrow(new IOException("Network error")).when(resp).sendError(HttpServletResponse.SC_NOT_FOUND, "Not Found");

        userController.handleSendErrorException(HttpServletResponse.SC_NOT_FOUND, "Not Found", resp);

        verify(resp).sendError(HttpServletResponse.SC_NOT_FOUND, "Not Found");
    }



    @Test
    public void testHandleGetUserById_UserNotFound() throws IOException {
        HttpServletResponse resp = mock(HttpServletResponse.class);
        when(userService.getUserById(1L)).thenReturn(null);

        userController.handleGetUserById("/1", resp);

        verify(resp).sendError(HttpServletResponse.SC_NOT_FOUND, "User not found");
    }

    @Test
    public void testHandleGetUserById_InvalidPath() throws IOException {
        HttpServletResponse resp = mock(HttpServletResponse.class);

        userController.handleGetUserById("/", resp);

        verify(resp).sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid path length");
    }

    @Test
    public void testHandleGetUserById_InvalidIdFormat() throws IOException {
        HttpServletResponse resp = mock(HttpServletResponse.class);

        userController.handleGetUserById("/invalid", resp);

        verify(resp).sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid user ID format");
    }

}