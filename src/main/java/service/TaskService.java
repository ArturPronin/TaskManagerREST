package service;


import dto.TaskDTO;

import java.util.List;

public interface TaskService {
    void createTask(TaskDTO taskDTO);

    TaskDTO getTaskById(Long id);

    List<TaskDTO> getAllTasks();

    void updateTask(TaskDTO taskDTO);

    void deleteTask(Long id);
}