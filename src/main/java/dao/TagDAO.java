package dao;

import entity.Tag;
import entity.Task;

import java.util.List;

public interface TagDAO {

    void create(Tag tag);

    Tag findById(Long id);

    List<Tag> findAll();

    void update(Tag tag);

    void delete(Long id);

    void assignTaskToTag(Long tagId, Long taskId);

    List<Task> getTasksByTagId(Long tagId);
}
