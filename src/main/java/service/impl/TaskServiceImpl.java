package service.impl;

import dao.TaskDAO;
import dto.TaskDTO;
import entity.Task;
import mapper.TaskMapper;
import mapper.impl.TaskMapperImpl;
import service.TaskService;

import java.util.List;

public class TaskServiceImpl implements TaskService {

    private final TaskDAO taskDAO;
    private final TaskMapper taskMapper = new TaskMapperImpl();

    public TaskServiceImpl(TaskDAO taskDAO) {
        this.taskDAO = taskDAO;
    }

    @Override
    public void createTask(TaskDTO taskDTO) {
        if (taskDTO == null) {
            throw new IllegalArgumentException("TaskDTO cannot be null");
        }
        Task task = taskMapper.toEntity(taskDTO);
        if (task == null) {
            throw new IllegalStateException("Task cannot be null");
        }
        taskDAO.create(task);
    }

    @Override
    public TaskDTO getTaskById(Long id) {
        Task task = taskDAO.findById(id);
        return taskMapper.toDTO(task);
    }

    @Override
    public List<TaskDTO> getAllTasks() {
        List<Task> tasks = taskDAO.findAll();
        return tasks.stream()
                .map(taskMapper::toDTO)
                .toList();
    }

    public void updateTask(TaskDTO taskDTO) {
        if (taskDTO.getId() == null) {
            throw new IllegalArgumentException("TaskDTO ID cannot be null");
        }
        Task task = taskMapper.toEntity(taskDTO);
        if (task == null) {
            throw new IllegalStateException("Task cannot be null");
        }
        taskDAO.update(task);
    }

    @Override
    public void deleteTask(Long id) {
        taskDAO.delete(id);
    }
}