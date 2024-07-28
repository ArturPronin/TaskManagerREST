package dao.impl;

import dao.TagDAO;
import entity.Tag;
import entity.Task;
import entity.User;
import exception.DatabaseOperationException;
import exception.TaskAssignmentException;
import exception.TaskRetrievalException;
import exception.UserNotFoundException;
import factory.Factory;
import factory.impl.TagFactoryImpl;
import factory.impl.TaskFactoryImpl;
import factory.impl.UserFactoryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TagDAOImpl implements TagDAO {
    private final Connection connection;
    private final Factory<Tag> tagsFactory = new TagFactoryImpl();
    private final Factory<Task> taskFactory = new TaskFactoryImpl();
    private static final Logger LOGGER = LoggerFactory.getLogger(UserDAOImpl.class);

    public TagDAOImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void create(Tag tag) {
        String sql = "INSERT INTO tags (name) VALUES (?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, tag.getName());
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new DatabaseOperationException("Creating tag failed, no rows affected.");
            }
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    tag.setId(generatedKeys.getLong(1));
                } else {
                    throw new DatabaseOperationException("Creating tag failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Database error while creating tag", e);
        }
    }

    @Override
    public Tag findById(Long id) {

        String sql = "SELECT id, name FROM tags WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    Tag tag = tagsFactory.create();

                    tag.setId(resultSet.getLong("id"));
                    tag.setName(resultSet.getString("name"));

                    List<Task> tasks = getTasksByTagId(tag.getId());
                    tag.setTasks(tasks);
                    return tag;
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Error find tag by ID", e);
        }
        return null;
    }

    @Override
    public List<Tag> findAll() {
        List<Tag> tags = new ArrayList<>();
        String sql = "SELECT id, name FROM tags";
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                Tag tag = tagsFactory.create();
                tag.setId(resultSet.getLong("id"));
                tag.setName(resultSet.getString("name"));
                List<Task> tasks = getTasksByTagId(tag.getId());
                tag.setTasks(tasks);
                tags.add(tag);
            }
        } catch (SQLException e) {
            LOGGER.error("Error find all tags", e);
        }
        return tags;
    }

    @Override
    public void update(Tag tag) {
        String sql = "UPDATE tags SET name = ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, tag.getName());
            statement.setLong(2, tag.getId());
            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                throw new UserNotFoundException("Tag not found with ID: " + tag.getId());
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Database error while updating tag", e);
        }
    }

    @Override
    public void delete(Long id) {
        String sql = "DELETE FROM tags WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                throw new UserNotFoundException("Tag not found with ID: " + id);
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Database error while deleting tag", e);
        }
    }
    @Override
    public void assignTaskToTag(Long tagId, Long taskId) {
        String checkSql = "SELECT COUNT(*) FROM task_tag WHERE tag_id = ? AND task_id = ?";
        try (PreparedStatement checkStmt = connection.prepareStatement(checkSql)) {
            checkStmt.setLong(1, taskId);
            checkStmt.setLong(2, taskId);
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    return;
                }
            }
        } catch (SQLException e) {
            throw new TaskAssignmentException("Error checking task assignment", e);
        }

        String insertSql = "INSERT INTO task_tag (tag_id, task_id) VALUES (?, ?)";
        try (PreparedStatement insertStmt = connection.prepareStatement(insertSql)) {
            insertStmt.setLong(1, tagId);
            insertStmt.setLong(2, taskId);
            insertStmt.executeUpdate();
        } catch (SQLException e) {
            throw new TaskAssignmentException("Error assigning task to tag", e);
        }
    }

    @Override
    public List<Task> getTasksByTagId(Long tagId) {
        List<Task> tasks = new ArrayList<>();
        String sql = "SELECT t.id, t.title, t.description, t.assigned_user_id FROM tasks t JOIN task_tag ut ON t.id = ut.task_id WHERE ut.tag_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, tagId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    TaskDAOImpl.tasksSetList(resultSet, tasks, taskFactory);
                }
            }
        } catch (SQLException e) {
            throw new TaskRetrievalException("Error retrieving tasks for tags ID: " + tagId, e);
        }
        return tasks;
    }

    static void tagsSetList(ResultSet resultSet, List<Tag> tags, Factory<Tag> tagsFactory) throws SQLException {
        Tag tag = tagsFactory.create();
        tag.setId(resultSet.getLong("id"));
        tag.setName(resultSet.getString("name"));
        tags.add(tag);
    }
}
