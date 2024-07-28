package controller;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dto.TagDTO;
import dto.TaskDTO;
import exception.ServiceException;
import factory.impl.TagControllerFactory;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.TagService;

import java.io.IOException;
import java.util.List;

@WebServlet("/tags/*")
public class TagController extends HttpServlet {
    private final transient TagService tagService;
    private final ObjectMapper objectMapper;
    private static final Logger LOGGER = LoggerFactory.getLogger(TagController.class);

    public TagController(TagService tagService, ObjectMapper objectMapper) {
        this.tagService = tagService;
        this.objectMapper = objectMapper;
    }

    public TagController() {
        TagController controller = TagControllerFactory.createTagController();
        this.tagService = controller.tagService;
        this.objectMapper = controller.objectMapper;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        String pathInfo = req.getPathInfo();
        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                handleGetAllTags(resp);
            } else if (pathInfo.matches("/\\d+")) {
                handleGetTagById(pathInfo, resp);
            } else {
                sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid path");
            }
        } catch (JsonGenerationException | JsonMappingException e) {
            handleJsonException(e, resp);
        } catch (IOException e) {
            handleIOException(e, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        String pathInfo = req.getPathInfo();
        if ("/".equals(pathInfo)) {
            TagDTO tagDTO;
            try {
                tagDTO = objectMapper.readValue(req.getInputStream(), TagDTO.class);
            } catch (IOException e) {
                handleSendErrorException(HttpServletResponse.SC_BAD_REQUEST, "Invalid TagDTO", resp);
                return;
            }

            if (tagDTO == null) {
                handleSendErrorException(HttpServletResponse.SC_BAD_REQUEST, "TagDTO cannot be null", resp);
                return;
            }
            tagService.createTag(tagDTO);
            resp.setStatus(HttpServletResponse.SC_CREATED);
        } else if (pathInfo.matches("/\\d+/tasks/\\d+")) {
            try {
                String[] pathParts = pathInfo.split("/");
                Long tagId = Long.parseLong(pathParts[1]);
                Long taskId = Long.parseLong(pathParts[3]);
                tagService.assignTaskToTag(tagId, taskId);
                resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
            } catch (NumberFormatException e) {
                handleSendErrorException(HttpServletResponse.SC_BAD_REQUEST, "Invalid tag or task ID format", resp);
            }
        } else {
            handleSendErrorException(HttpServletResponse.SC_BAD_REQUEST, "Invalid Path", resp);
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) {
        TagDTO tagDTO;
        try {
            tagDTO = objectMapper.readValue(req.getInputStream(), TagDTO.class);
        } catch (IOException e) {
            handleError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid TagDTO");
            return;
        }
        if (tagDTO == null) {
            handleError(resp, HttpServletResponse.SC_BAD_REQUEST, "TagDTO cannot be null");
            return;
        }
        if (tagService.getTagById(tagDTO.getId()) == null) {
            handleError(resp, HttpServletResponse.SC_NOT_FOUND, "Tag not found");
            return;
        }
        try {
            tagService.updateTag(tagDTO);
            resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
        } catch (Exception e) {
            handleError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to update tag");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) {
        String pathInfo = req.getPathInfo();
        if (pathInfo != null && !pathInfo.equals("/")) {
            String[] pathParts = pathInfo.split("/");
            if (pathParts.length > 1) {
                try {
                    Long id = Long.parseLong(pathParts[1]);
                    tagService.deleteTag(id);
                    resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
                } catch (NumberFormatException e) {
                    handleSendErrorException(HttpServletResponse.SC_BAD_REQUEST, "Invalid tag ID format", resp);
                } catch (ServiceException e) {
                    handleSendErrorException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to delete tag", resp);
                }
            } else {
                handleSendErrorException(HttpServletResponse.SC_BAD_REQUEST, "Invalid path length", resp);
            }
        } else {
            handleSendErrorException(HttpServletResponse.SC_BAD_REQUEST, "Path cannot be null or empty", resp);
        }
    }

    private void sendError(HttpServletResponse resp, int statusCode, String message) throws IOException {
        try {
            resp.sendError(statusCode, message);
        } catch (IOException e) {
            LOGGER.error("Error send error response", e);
        }
    }

    void handleError(HttpServletResponse resp, int statusCode, String message) {
        try {
            resp.sendError(statusCode, message);
        } catch (IOException e) {
            LOGGER.error("Error handle error response", e);
            try {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An unexpected error occurred");
            } catch (IOException ex) {
                LOGGER.error("Error sending internal server error response", ex);
            }
        }
    }

    void handleSendErrorException(int statusCode, String message, HttpServletResponse resp) {
        try {
            resp.sendError(statusCode, message);
        } catch (IOException e) {
            LOGGER.error("Error sending error response", e);
        }
    }

    void handleGetAllTags(HttpServletResponse resp) throws IOException {
        try {
            List<TagDTO> tags = tagService.getAllTags();
            resp.setContentType("application/json");
            objectMapper.writeValue(resp.getOutputStream(), tags);
        } catch (IOException e) {
            handleIOException(e, resp);
        }
    }

    void handleGetTagById(String pathInfo, HttpServletResponse resp) throws IOException {
        try {
            String[] pathParts = pathInfo.split("/");
            if (pathParts.length > 1) {
                Long id = Long.parseLong(pathParts[1]);
                TagDTO tag = tagService.getTagById(id);
                if (tag != null) {
                    List<TaskDTO> tasks = tagService.getTasksByTagId(id);
                    tag.setTasks(tasks);
                    resp.setContentType("application/json");
                    objectMapper.writeValue(resp.getOutputStream(), tag);
                } else {
                    sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Tag not found");
                }
            } else {
                sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid path length");
            }
        } catch (NumberFormatException e) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid tag ID format");
        } catch (IOException e) {

            handleIOException(e, resp);
        }
    }

    void handleJsonException(Exception e, HttpServletResponse resp) {
        try {
            LOGGER.error("Error processing JSON", e);
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error processing JSON");
        } catch (IOException ioException) {
            LOGGER.error("Error handle json error response", ioException);

        }
    }

    void handleIOException(IOException e, HttpServletResponse resp) {
        try {
            LOGGER.error("Error handling request", e);
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error handling request");
        } catch (IOException ioException) {
            LOGGER.error("Error IOException error response", ioException);

        }
    }

}
