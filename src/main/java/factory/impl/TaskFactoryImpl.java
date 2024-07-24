package factory.impl;

import entity.Task;
import factory.Factory;

public class TaskFactoryImpl implements Factory<Task> {

    @Override
    public Task create() {
        return new Task();
    }
}
