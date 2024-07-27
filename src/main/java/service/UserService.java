package service;

import dto.TaskDTO;
import dto.UserDTO;

import java.util.List;

public interface UserService {
    void createUser(UserDTO userDTO);

    UserDTO getUserById(Long id);

    List<UserDTO> getAllUsers();

    void updateUser(UserDTO userDTO);

    void deleteUser(Long id);

    void assignTaskToUser(Long userId, Long taskId);

    List<TaskDTO> getTasksByUserId(Long userId);

}