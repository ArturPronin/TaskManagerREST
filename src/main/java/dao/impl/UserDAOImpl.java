package dao.impl;

import dao.UserDAO;
import entity.Task;
import entity.User;
import exception.DatabaseOperationException;
import exception.TaskAssignmentException;
import exception.TaskRetrievalException;
import exception.UserNotFoundException;
import factory.Factory;
import factory.impl.TaskFactoryImpl;
import factory.impl.UserFactoryImpl;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAOImpl implements UserDAO {

    private Connection connection;
    private Factory<User> userFactory = new UserFactoryImpl();
    private Factory<Task> taskFactory = new TaskFactoryImpl();

    public UserDAOImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void create(User user) {
        String sql = "INSERT INTO users (name) VALUES (?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, user.getName());
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new DatabaseOperationException("Creating user failed, no rows affected.");
            }
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    user.setId(generatedKeys.getLong(1));
                } else {
                    throw new DatabaseOperationException("Creating user failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Database error while creating user", e);
        }
    }

    @Override
    public User findById(Long id) {

        String sql = "SELECT id, name FROM users WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    User user = userFactory.create();

                    user.setId(resultSet.getLong("id"));
                    user.setName(resultSet.getString("name"));

                    List<Task> tasks = getTasksByUserId(user.getId());
                    user.setTasks(tasks);
                    return user;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<User> findAll() {
        List<User> users = new ArrayList<>();

        String sql = "SELECT id, name FROM users";
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                User user = userFactory.create();

                user.setId(resultSet.getLong("id"));
                user.setName(resultSet.getString("name"));

                List<Task> tasks = getTasksByUserId(user.getId());
                user.setTasks(tasks);
                users.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    @Override
    public void update(User user) {
        String sql = "UPDATE users SET name = ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, user.getName());
            statement.setLong(2, user.getId());
            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                throw new UserNotFoundException("User not found with ID: " + user.getId());
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Database error while updating user", e);
        }
    }

    @Override
    public void delete(Long id) {
        String sql = "DELETE FROM users WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                throw new UserNotFoundException("User not found with ID: " + id);
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Database error while deleting user", e);
        }
    }

    public void assignTaskToUser(Long userId, Long taskId) {
        String checkSql = "SELECT COUNT(*) FROM user_tasks WHERE user_id = ? AND task_id = ?";
        try (PreparedStatement checkStmt = connection.prepareStatement(checkSql)) {
            checkStmt.setLong(1, userId);
            checkStmt.setLong(2, taskId);
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    return;
                }
            }
        } catch (SQLException e) {
            throw new TaskAssignmentException("Error checking task assignment", e);
        }

        String insertSql = "INSERT INTO user_tasks (user_id, task_id) VALUES (?, ?)";
        try (PreparedStatement insertStmt = connection.prepareStatement(insertSql)) {
            insertStmt.setLong(1, userId);
            insertStmt.setLong(2, taskId);
            insertStmt.executeUpdate();
        } catch (SQLException e) {
            throw new TaskAssignmentException("Error assigning task to user", e);
        }
    }
    public List<Task> getTasksByUserId(Long userId) {
        List<Task> tasks = new ArrayList<>();
        String sql = "SELECT t.id, t.title, t.description, t.assigned_user_id FROM tasks t JOIN user_tasks ut ON t.id = ut.task_id WHERE ut.user_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    TaskDAOImpl.tasksSetList(resultSet, tasks, taskFactory);
                }
            }
        } catch (SQLException e) {

            throw new TaskRetrievalException("Error retrieving tasks for user ID: " + userId, e);
        }
        return tasks;
    }
}