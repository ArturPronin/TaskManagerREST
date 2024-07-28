package dao;

import entity.Tag;
import entity.Task;

import java.util.List;

public interface TaskDAO {
    void create(Task task);

    Task findById(Long id);

    List<Task> findAll();

    void update(Task task);

    void delete(Long id);

    void assignTagToTask(Long taskId, Long tagId);

    List<Tag> getTagsByTaskId(Long taskId);
}