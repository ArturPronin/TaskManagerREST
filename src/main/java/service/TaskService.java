package service;

import dto.TagDTO;
import dto.TaskDTO;

import java.util.List;

public interface TaskService {
    void createTask(TaskDTO taskDTO);

    TaskDTO getTaskById(Long id);

    List<TaskDTO> getAllTasks();

    void updateTask(TaskDTO taskDTO);

    void deleteTask(Long id);

    void assignTagsToTask(Long taskId, Long tagId);

    List<TagDTO> getTagsByTaskId(Long taskId);
}