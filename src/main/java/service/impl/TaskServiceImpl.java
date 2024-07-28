package service.impl;

import dao.TaskDAO;
import dto.TagDTO;
import dto.TaskDTO;
import entity.Tag;
import entity.Task;
import mapper.TagMapper;
import mapper.TaskMapper;
import mapper.impl.TagMapperImpl;
import mapper.impl.TaskMapperImpl;
import service.TaskService;

import java.util.List;

public class TaskServiceImpl implements TaskService {

    private final TaskDAO taskDAO;
    private final TaskMapper taskMapper = new TaskMapperImpl();

    private final TagMapper tagMapper = new TagMapperImpl();

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
                .map(task -> {
                    List<Tag> tags = taskDAO.getTagsByTaskId(task.getId());
                    TaskDTO taskDTO = taskMapper.toDTO(task);

                    taskDTO.setTags(tags.stream()
                            .map(tagMapper::toDTO)
                            .toList());
                    return taskDTO;
                })
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

    @Override
    public void assignTagsToTask(Long taskId, Long tagId) {
        taskDAO.assignTagToTask(taskId, tagId);
    }

    @Override
    public List<TagDTO> getTagsByTaskId(Long taskId) {
        List<Tag> tags = taskDAO.getTagsByTaskId(taskId);
        return tags.stream()
                .map(tagMapper::toDTO)
                .toList();
    }
}