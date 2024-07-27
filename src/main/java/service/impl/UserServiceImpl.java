package service.impl;

import dao.UserDAO;
import dto.TaskDTO;
import dto.UserDTO;
import entity.Task;
import entity.User;
import mapper.TaskMapper;
import mapper.UserMapper;
import mapper.impl.TaskMapperImpl;
import mapper.impl.UserMapperImpl;
import service.UserService;

import java.util.List;

public class UserServiceImpl implements UserService {

    private final UserDAO userDAO;
    private final UserMapper userMapper = new UserMapperImpl();
    private final TaskMapper taskMapper = new TaskMapperImpl();

    public UserServiceImpl(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    @Override
    public void createUser(UserDTO userDTO) {
        if (userDTO == null) {
            throw new IllegalArgumentException("UserDTO cannot be null");
        }
        User user = userMapper.toEntity(userDTO);
        if (user == null) {
            throw new IllegalStateException("User cannot be null");
        }
        userDAO.create(user);
    }

    @Override
    public dto.UserDTO getUserById(Long id) {
        User user = userDAO.findById(id);
        return userMapper.toDTO(user);
    }

    @Override
    public List<UserDTO> getAllUsers() {
        List<User> users = userDAO.findAll();
        return users.stream()
                .map(user -> {
                    List<Task> tasks = userDAO.getTasksByUserId(user.getId());
                    UserDTO userDTO = userMapper.toDTO(user);

                    userDTO.setTasks(tasks.stream()
                            .map(taskMapper::toDTO)
                            .toList());
                    return userDTO;
                })
                .toList();
    }

    @Override
    public void updateUser(UserDTO userDTO) {
        if (userDTO.getId() == null) {
            throw new IllegalArgumentException("UserDTO ID cannot be null");
        }
        User user = userMapper.toEntity(userDTO);
        if (user == null) {
            throw new IllegalStateException("User cannot be null");
        }
        userDAO.update(user);
    }

    @Override
    public void deleteUser(Long id) {
        userDAO.delete(id);
    }

    public void assignTaskToUser(Long userId, Long taskId) {
        userDAO.assignTaskToUser(userId, taskId);
    }

    public List<TaskDTO> getTasksByUserId(Long userId) {
        List<Task> tasks = userDAO.getTasksByUserId(userId);
        return tasks.stream()
                .map(taskMapper::toDTO)
                .toList();
    }
}
