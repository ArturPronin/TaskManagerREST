package mapper.impl;

import dto.TaskDTO;
import entity.Task;
import factory.Factory;
import factory.impl.TaskDTOFactoryImpl;
import factory.impl.TaskFactoryImpl;
import mapper.TaskMapper;

public class TaskMapperImpl implements TaskMapper {

    Factory<Task> taskFactory = new TaskFactoryImpl();
    Factory<TaskDTO> taskDTOFactory = new TaskDTOFactoryImpl();

    @Override
    public TaskDTO toDTO(Task task) {
        if (task == null) {
            return null;
        }

        if (task.getTitle() == null || task.getTitle().trim().isEmpty()) {
            return null;
        }
        TaskDTO taskDTO = taskDTOFactory.create();
        taskDTO.setId(task.getId());
        taskDTO.setTitle(task.getTitle());
        taskDTO.setDescription(task.getDescription());
        taskDTO.setAssignedUserId(task.getAssignedUserId());
        return taskDTO;
    }

    @Override
    public Task toEntity(dto.TaskDTO taskDTO) {
        if (taskDTO == null) {
            return null;
        }

        if (taskDTO.getTitle() == null || taskDTO.getTitle().trim().isEmpty()) {
            return null;
        }

        Task task = taskFactory.create();
        task.setId(taskDTO.getId());
        task.setTitle(taskDTO.getTitle());
        task.setDescription(taskDTO.getDescription());
        task.setAssignedUserId(taskDTO.getAssignedUserId());
        return task;
    }
}
