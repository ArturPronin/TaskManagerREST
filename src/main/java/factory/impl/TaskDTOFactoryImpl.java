package factory.impl;

import dto.TaskDTO;
import factory.Factory;

public class TaskDTOFactoryImpl implements Factory<TaskDTO> {

    @Override
    public TaskDTO create() {
        return new TaskDTO();
    }
}
