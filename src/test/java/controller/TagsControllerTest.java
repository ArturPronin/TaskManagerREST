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
import mapper.TagMapper;
import mapper.impl.TagMapperImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import service.TagService;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

public class TagsControllerTest {
    private TagService tagService;
    private ObjectMapper objectMapper;
    private TagController tagController;
    private HttpServletResponse resp;

    @BeforeEach
    public void setUp() {
        tagService = Mockito.mock(TagService.class);
        objectMapper = new ObjectMapper();
        tagController = new TagController(tagService, objectMapper);
        resp = Mockito.mock(HttpServletResponse.class);
    }

    @Test
    public void testDoGetAllTags() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        List<TagDTO> tags = Arrays.asList(new TagDTO(1L, "Tag1", null), new TagDTO(2L, "Tag2", null));
        when(tagService.getAllTags()).thenReturn(tags);

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

        tagController.doGet(request, response);

        String jsonResponse = outputStream.toString();
        String expectedJson = objectMapper.writeValueAsString(tags);
        assertEquals(expectedJson, jsonResponse);
    }

    @Test
    public void testDoGetTagById() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        TagDTO tag = new TagDTO(1L, "Tag1", Collections.emptyList());

        when(request.getPathInfo()).thenReturn("/1");
        when(tagService.getTagById(1L)).thenReturn(tag);
        when(tagService.getTasksByTagId(1L)).thenReturn(Collections.emptyList());

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

        tagController.doGet(request, response);

        String jsonResponse = outputStream.toString();
        String expectedJson = objectMapper.writeValueAsString(tag);
        assertEquals(expectedJson, jsonResponse);
    }

    @Test
    public void testDoPostAssignTaskToTag() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        when(request.getPathInfo()).thenReturn("/1/tasks/2");

        tagController.doPost(request, response);

        verify(tagService).assignTaskToTag(1L, 2L);
        verify(response).setStatus(HttpServletResponse.SC_NO_CONTENT);
    }

    @Test
    void testDoPostCreateTag() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        TagDTO tagDTO = new TagDTO();
        tagDTO.setId(1L);
        tagDTO.setName("SetTagName");
        String json = new ObjectMapper().writeValueAsString(tagDTO);
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
        doNothing().when(tagService).createTag(any(TagDTO.class));
        doNothing().when(response).setStatus(HttpServletResponse.SC_CREATED);

        tagController.doPost(request, response);

        verify(request).getInputStream();
        verify(tagService).createTag(any(TagDTO.class));
        verify(response).setStatus(HttpServletResponse.SC_CREATED);
    }

    @Test
    void testDoPutUpdateTag() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        TagDTO tagDTO = new TagDTO();
        tagDTO.setId(1L);
        tagDTO.setName("SetTagName");
        String json = new ObjectMapper().writeValueAsString(tagDTO);
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
        when(tagService.getTagById(1L)).thenReturn(tagDTO);
        doNothing().when(tagService).updateTag(any(TagDTO.class));
        doNothing().when(response).setStatus(HttpServletResponse.SC_NO_CONTENT);

        tagController.doPut(request, response);

        verify(request).getInputStream();
        verify(tagService).getTagById(1L);
        verify(tagService).updateTag(any(TagDTO.class));
        verify(response).setStatus(HttpServletResponse.SC_NO_CONTENT);
    }

    @Test
    public void testDoDeleteTag() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        when(request.getPathInfo()).thenReturn("/1");

        tagController.doDelete(request, response);

        verify(tagService).deleteTag(1L);
        verify(response).setStatus(HttpServletResponse.SC_NO_CONTENT);
    }

    @Test
    public void testDoGetInvalidPath() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        when(request.getPathInfo()).thenReturn("/invalid/path");

        tagController.doGet(request, response);

        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid path");
    }

    @Test
    void testDoPostInvalidPath() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        when(request.getPathInfo()).thenReturn("/invalid/path");

        tagController.doPost(request, response);

        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid Path");
    }

    @Test
    void testDoDeleteInvalidPathLength() throws ServletException, IOException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        when(request.getPathInfo()).thenReturn("");

        tagController.doDelete(request, response);

        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid path length");
    }

    @Test
    void testDoPostTagDTONull() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        TagDTO tagDTO = null;
        String json = new ObjectMapper().writeValueAsString(tagDTO);
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

        tagController.doPost(request, response);

        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "TagDTO cannot be null");

    }

    @Test
    void testDoPutTagDTONull() throws ServletException, IOException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        TagDTO tagDTO = null;
        String json = new ObjectMapper().writeValueAsString(tagDTO);
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

        tagController.doPut(request, response);

        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "TagDTO cannot be null");
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

        tagController.doPost(request, response);

        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid TagDTO");
    }

    @Test
    void testDoPostAssignTaskInvalidPath() throws Exception {

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        when(request.getPathInfo()).thenReturn("/1/tasks/");

        tagController.doPost(request, response);

        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid Path");
    }

    @Test
    public void testDoPutInvalidTagDTO() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        when(request.getInputStream()).thenThrow(new IOException("Invalid Input"));

        tagController.doPut(request, response);

        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid TagDTO");
    }

    @Test
    public void testDoDeleteInvalidPath() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        when(request.getPathInfo()).thenReturn("/");

        tagController.doDelete(request, response);

        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Path cannot be null or empty");
    }

    @Test
    public void testDoGetTagNotFound() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        when(request.getPathInfo()).thenReturn("/999");
        when(tagService.getTagById(999L)).thenReturn(null);

        tagController.doGet(request, response);

        verify(response).sendError(HttpServletResponse.SC_NOT_FOUND, "Tag not found");
    }

    @Test
    void testDoPutUpdateNonExistentTag() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        TagDTO tagDTO = new TagDTO();
        tagDTO.setId(1L);
        tagDTO.setName("SetTagName");
        String json = new ObjectMapper().writeValueAsString(tagDTO);
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
        when(tagService.getTagById(anyLong())).thenReturn(null);

        tagController.doPut(request, response);

        verify(response).sendError(HttpServletResponse.SC_NOT_FOUND, "Tag not found");
    }

    @Test
    void testTagConstructorAndToString() {
        Long id = 1L;
        String name = "Test Tag";
        Task task1 = new Task(1L, "Task1", "Description1", 1L, null);
        Task task2 = new Task(2L, "Task2", "Description2", 2L, null);
        List<Task> tasks = Arrays.asList(task1, task2);

        Tag tag = new Tag(id, name, tasks);

        assertEquals(id, tag.getId());
        assertEquals(name, tag.getName());
        assertEquals(tasks, tag.getTasks());

        String expectedToString = "Tag{" + "id=" + id + ", name='" + name + '\'' + ", tasks=" + tasks + '}';

        assertEquals(expectedToString, tag.toString());
    }

    @Test
    void testTagDTOConstructorAndToString() {
        Long id = 1L;
        String name = "Test Tag";
        TaskDTO task1 = new TaskDTO(1L, "Task1", "Description1", 1L, null);
        TaskDTO task2 = new TaskDTO(2L, "Task2", "Description2", 2L, null);
        List<TaskDTO> tasks = Arrays.asList(task1, task2);

        TagDTO tagDTO = new TagDTO(id, name, tasks);

        assertEquals(id, tagDTO.getId());
        assertEquals(name, tagDTO.getName());
        assertEquals(tasks, tagDTO.getTasks());

        String expectedToString = "TagDTO{" + "id=" + id + ", name='" + name + '\'' + ", tasks=" + tasks + '}';

        assertEquals(expectedToString, tagDTO.toString());
    }

    @Test
    void testTagMapperPutNull() {
        TagMapper tagMapper = new TagMapperImpl();
        Tag tag = null;
        assertNull(tagMapper.toDTO(tag));
        Tag tag2 = new Tag(2L, "", null);
        assertNull(tagMapper.toDTO(tag2));

        TagDTO tagDTO = null;
        assertNull(tagMapper.toEntity(tagDTO));
        TagDTO tagDTO2 = new TagDTO(2L, "", null);
        assertNull(tagMapper.toEntity(tagDTO2));
    }

    @Test
    public void testHandleErrorSendsError() throws IOException {
        HttpServletResponse resp = mock(HttpServletResponse.class);

        tagController.handleError(resp, HttpServletResponse.SC_BAD_REQUEST, "Bad Request");

        verify(resp).sendError(HttpServletResponse.SC_BAD_REQUEST, "Bad Request");
    }

    @Test
    public void testHandleErrorSendsInternalServerErrorOnIOException() throws IOException {
        HttpServletResponse resp = mock(HttpServletResponse.class);
        doThrow(new IOException("Network error")).when(resp).sendError(HttpServletResponse.SC_BAD_REQUEST, "Bad Request");

        tagController.handleError(resp, HttpServletResponse.SC_BAD_REQUEST, "Bad Request");

        verify(resp).sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An unexpected error occurred");
    }

    @Test
    public void testHandleSendErrorExceptionSendsError() throws IOException {
        HttpServletResponse resp = mock(HttpServletResponse.class);

        tagController.handleSendErrorException(HttpServletResponse.SC_NOT_FOUND, "Not Found", resp);

        verify(resp).sendError(HttpServletResponse.SC_NOT_FOUND, "Not Found");
    }

    @Test
    public void testHandleSendErrorExceptionOnIOException() throws IOException {
        HttpServletResponse resp = mock(HttpServletResponse.class);
        doThrow(new IOException("Network error")).when(resp).sendError(HttpServletResponse.SC_NOT_FOUND, "Not Found");

        tagController.handleSendErrorException(HttpServletResponse.SC_NOT_FOUND, "Not Found", resp);

        verify(resp).sendError(HttpServletResponse.SC_NOT_FOUND, "Not Found");
    }

    @Test
    public void testHandleGetTagByIdTagNotFound() throws IOException {
        HttpServletResponse resp = mock(HttpServletResponse.class);
        when(tagService.getTagById(1L)).thenReturn(null);

        tagController.handleGetTagById("/1", resp);

        verify(resp).sendError(HttpServletResponse.SC_NOT_FOUND, "Tag not found");
    }

    @Test
    public void testHandleGetTagByIdInvalidPath() throws IOException {
        HttpServletResponse resp = mock(HttpServletResponse.class);

        tagController.handleGetTagById("/", resp);

        verify(resp).sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid path length");
    }

    @Test
    public void testHandleGetTagByIdInvalidIdFormat() throws IOException {
        HttpServletResponse resp = mock(HttpServletResponse.class);

        tagController.handleGetTagById("/invalid", resp);

        verify(resp).sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid tag ID format");
    }

    @Test
    void testHandleJsonException() throws IOException {
        Exception exception = new RuntimeException("Test exception");
        tagController.handleJsonException(exception, resp);
        verify(resp).sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error processing JSON");
    }

    @Test
    void testHandleIOException() throws IOException {
        IOException ioException = new IOException("Test IO exception");
        tagController.handleIOException(ioException, resp);
        verify(resp).sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error handling request");
    }

    @Test
    void testDoDeleteWithInvalidUserIdFormat() throws IOException, ServletException {

        HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse resp = Mockito.mock(HttpServletResponse.class);

        when(req.getPathInfo()).thenReturn("/invalid-id");
        tagController.doDelete(req, resp);
        verify(resp).sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid tag ID format");
    }

    @Test
    void testDoDeleteWithServiceException() throws IOException, ServletException {

        HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse resp = Mockito.mock(HttpServletResponse.class);

        when(req.getPathInfo()).thenReturn("/1");
        doThrow(new ServiceException("Service error")).when(tagService).deleteTag(anyLong());
        tagController.doDelete(req, resp);
        verify(resp).sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to delete tag");
    }
}
