package dao;

import entity.Task;
import entity.User;

import java.util.List;

public interface UserDAO {
    void create(User user);

    User findById(Long id);

    List<User> findAll();

    void update(User user);

    void delete(Long id);

    void assignTaskToUser(Long userId, Long taskId);

    List<Task> getTasksByUserId(Long userId);
}