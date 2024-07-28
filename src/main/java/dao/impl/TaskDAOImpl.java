package dao.impl;

import dao.TaskDAO;
import entity.Task;
import exception.DatabaseOperationException;
import exception.SQLExceptionWrapper;
import factory.Factory;
import factory.impl.TaskFactoryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TaskDAOImpl implements TaskDAO {

    private static final String DATABASE_ERROR_MESSAGE = "Database error";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_TITLE = "title";
    private static final String COLUMN_DESCRIPTION = "description";
    private static final String COLUMN_ASSIGNED_USER_ID = "assigned_user_id";
    private final Connection connection;
    private final Factory<Task> taskFactory = new TaskFactoryImpl();
    private static final Logger LOGGER = LoggerFactory.getLogger(TaskDAOImpl.class);

    public TaskDAOImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void create(Task task) {
        String insertTaskSQL = "INSERT INTO tasks (title, description, assigned_user_id) VALUES (?, ?, ?) RETURNING id";
        try (PreparedStatement insertTaskStmt = connection.prepareStatement(insertTaskSQL)) {
            insertTaskStmt.setString(1, task.getTitle());
            insertTaskStmt.setString(2, task.getDescription());
            insertTaskStmt.setLong(3, task.getAssignedUserId());

            try (ResultSet generatedKeys = insertTaskStmt.executeQuery()) {
                if (generatedKeys.next()) {
                    Long taskId = generatedKeys.getLong(1);
                    task.setId(taskId);
                }
                String insertUserTaskSQL = "INSERT INTO user_tasks (user_id, task_id) VALUES (?, ?)";
                try (PreparedStatement insertUserTaskStmt = connection.prepareStatement(insertUserTaskSQL)) {
                    insertUserTaskStmt.setLong(1, task.getAssignedUserId());
                    insertUserTaskStmt.setLong(2, task.getId());
                    insertUserTaskStmt.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new SQLExceptionWrapper("Error creating task", e);
        }
    }

    @Override
    public Task findById(Long id) {
        String sql = "SELECT id, title, description, assigned_user_id FROM tasks WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    Task task = taskFactory.create();
                    task.setId(resultSet.getLong(COLUMN_ID));
                    task.setTitle(resultSet.getString(COLUMN_TITLE));
                    task.setDescription(resultSet.getString(COLUMN_DESCRIPTION));
                    task.setAssignedUserId(resultSet.getLong(COLUMN_ASSIGNED_USER_ID));
                    return task;
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException(DATABASE_ERROR_MESSAGE, e);
        }
        return null;
    }

    @Override
    public List<Task> findAll() {
        List<Task> tasks = new ArrayList<>();
        String sql = "SELECT id, title, description, assigned_user_id FROM tasks";
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                tasksSetList(resultSet, tasks, taskFactory);
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException(DATABASE_ERROR_MESSAGE, e);
        }
        return tasks;
    }

    @Override
    public void update(Task taskDTO) {
        Connection conn = null;
        PreparedStatement updateTaskStmt = null;
        PreparedStatement updateUserTasksStmt = null;
        try {
            conn = connection;
            conn.setAutoCommit(false);
            String selectSql = "SELECT * FROM tasks WHERE id = ?";
            Task existingTask = null;
            try (PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {
                selectStmt.setLong(1, taskDTO.getId());
                try (ResultSet resultSet = selectStmt.executeQuery()) {
                    existingTask = getExsistingTask(resultSet, existingTask);
                }
            }
            existingTaskNotNull(taskDTO, existingTask);
            StringBuilder updateSql = new StringBuilder("UPDATE tasks SET ");
            List<Object> parameters = new ArrayList<>();

            getTitleAndDescriptionQuery(taskDTO.getTitle(), existingTask.getTitle(), updateSql, "title = ?, ", parameters);
            getTitleAndDescriptionQuery(taskDTO.getDescription(), existingTask.getDescription(), updateSql, "description = ?, ", parameters);
            getAssignedUserIdQuery(taskDTO, existingTask, updateSql, parameters);

            if (updateSql.length() > 0) {
                updateSql.setLength(updateSql.length() - 2);
                updateSql.append(" WHERE id = ?");
                parameters.add(taskDTO.getId());

                updateTaskStmt = conn.prepareStatement(updateSql.toString());
                for (int i = 0; i < parameters.size(); i++) {
                    updateTaskStmt.setObject(i + 1, parameters.get(i));
                }
                updateTaskStmt.executeUpdate();
            }
            if (taskDTO.getAssignedUserId() != null) {
                String deleteOldUserTasksSql = "DELETE FROM user_tasks WHERE task_id = ?";
                try (PreparedStatement deleteStmt = conn.prepareStatement(deleteOldUserTasksSql)) {
                    deleteStmt.setLong(1, taskDTO.getId());
                    deleteStmt.executeUpdate();
                }

                String insertUserTasksSql = "INSERT INTO user_tasks (user_id, task_id) VALUES (?, ?)";
                updateUserTasksStmt = conn.prepareStatement(insertUserTasksSql);
                updateUserTasksStmt.setLong(1, taskDTO.getAssignedUserId());
                updateUserTasksStmt.setLong(2, taskDTO.getId());
                updateUserTasksStmt.executeUpdate();
            }

            conn.commit();
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            throw new DatabaseOperationException("Database exception");
        } finally {
            try {
                if (updateTaskStmt != null) updateTaskStmt.close();
                if (updateUserTasksStmt != null) updateUserTasksStmt.close();
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void delete(Long id) {
        String deleteUserTasksSQL = "DELETE FROM user_tasks WHERE task_id = ?";
        String deleteTaskSQL = "DELETE FROM tasks WHERE id = ?";
        boolean commitSuccessful = false;
        try {
            connection.setAutoCommit(false);
            try (PreparedStatement deleteUserTasksStmt = connection.prepareStatement(deleteUserTasksSQL)) {
                deleteUserTasksStmt.setLong(1, id);
                deleteUserTasksStmt.executeUpdate();
            }
            try (PreparedStatement deleteTaskStmt = connection.prepareStatement(deleteTaskSQL)) {
                deleteTaskStmt.setLong(1, id);
                deleteTaskStmt.executeUpdate();
            }
            connection.commit();
            commitSuccessful = true;
        } catch (SQLException e) {
            handleDatabaseError(e);
        } finally {
            restoreAutoCommitState(commitSuccessful);
        }
    }

    static void tasksSetList(ResultSet resultSet, List<Task> tasks, Factory<Task> taskFactory) throws SQLException {
        Task task = taskFactory.create();
        task.setId(resultSet.getLong(COLUMN_ID));
        task.setTitle(resultSet.getString(COLUMN_TITLE));
        task.setDescription(resultSet.getString(COLUMN_DESCRIPTION));
        task.setAssignedUserId(resultSet.getLong(COLUMN_ASSIGNED_USER_ID));
        tasks.add(task);
    }

    public void handleDatabaseError(SQLException e) {
        try {
            if (connection != null) {
                connection.rollback();
            }
        } catch (SQLException ex) {
            LOGGER.error("Error rollback task", e);
        }
        LOGGER.error("Error handle database", e);
        throw new DatabaseOperationException("Database error occurred", e);
    }

    public void restoreAutoCommitState(boolean commitSuccessful) {
        try {
            if (!commitSuccessful) {
                connection.rollback();
            }
            connection.setAutoCommit(true);
        } catch (SQLException e) {
            LOGGER.error("Error restore auto commit", e);

        }
    }

    public static void getAssignedUserIdQuery(Task taskDTO, Task existingTask, StringBuilder updateSql, List<Object> parameters) {
        if (taskDTO.getAssignedUserId() != null && !taskDTO.getAssignedUserId().equals(existingTask.getAssignedUserId())) {
            updateSql.append("assigned_user_id = ?, ");
            parameters.add(taskDTO.getAssignedUserId());
        }
    }

    private static void getTitleAndDescriptionQuery(String taskDTO, String existingTask, StringBuilder updateSql, String str, List<Object> parameters) {
        if (taskDTO != null && !taskDTO.equals(existingTask)) {
            updateSql.append(str);
            parameters.add(taskDTO);
        }
    }

    private static void existingTaskNotNull(Task taskDTO, Task existingTask) throws SQLException {
        if (existingTask == null) {
            throw new SQLException("Task not found with ID: " + taskDTO.getId());
        }
    }

    private Task getExsistingTask(ResultSet resultSet, Task existingTask) throws SQLException {
        if (resultSet.next()) {
            existingTask = taskFactory.create();
            existingTask.setId(resultSet.getLong(COLUMN_ID));
            existingTask.setTitle(resultSet.getString(COLUMN_TITLE));
            existingTask.setDescription(resultSet.getString(COLUMN_DESCRIPTION));
            existingTask.setAssignedUserId(resultSet.getLong(COLUMN_ASSIGNED_USER_ID));
        }
        return existingTask;
    }

}