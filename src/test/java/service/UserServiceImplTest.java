package service;

import dao.UserDAO;
import dto.TaskDTO;
import dto.UserDTO;
import entity.Task;
import entity.User;
import mapper.TaskMapper;
import mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import service.impl.UserServiceImpl;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

public class UserServiceImplTest {

    @Mock
    private UserDAO userDAO;

    @Mock
    private UserMapper userMapper;

    @Mock
    private TaskMapper taskMapper;

    @InjectMocks
    private UserServiceImpl userService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testCreateUser() {

        UserDTO userDTO = new UserDTO();
        userDTO.setName("User 1");

        User user = new User();
        user.setName("User 1");
        when(userMapper.toEntity(userDTO)).thenReturn(user);
        userService.createUser(userDTO);
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userDAO, times(1)).create(userCaptor.capture());
        User capturedUser = userCaptor.getValue();
        assertNotNull(capturedUser);
        assertEquals("User 1", capturedUser.getName());
    }

    @Test
    public void testUpdateUser() {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(1L);
        userDTO.setName("Updated Name");

        User user = new User();
        user.setId(1L);
        user.setName("Updated Name");

        when(userMapper.toEntity(userDTO)).thenReturn(user);

        userService.updateUser(userDTO);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userDAO, times(1)).update(userCaptor.capture());
        assertEquals(1L, userCaptor.getValue().getId());
        assertEquals("Updated Name", userCaptor.getValue().getName());
    }

    @Test
    public void testGetUserById() {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setName("User 1");
        UserDTO userDTO = new UserDTO();
        userDTO.setId(userId);
        userDTO.setName("User 1");

        when(userDAO.findById(userId)).thenReturn(user);

        UserDTO result = userService.getUserById(userId);

        assertNotNull(result);
        assertEquals(userId, result.getId());
        assertEquals("User 1", result.getName());
        verify(userDAO, times(1)).findById(userId);
    }

    @Test
    public void testGetAllUsers() {
        User user1 = new User();
        user1.setId(1L);
        user1.setName("User 1");

        User user2 = new User();
        user2.setId(2L);
        user2.setName("User 2");

        Task task1 = new Task();
        task1.setId(1L);
        task1.setTitle("Task 1");

        Task task2 = new Task();
        task2.setId(2L);
        task2.setTitle("Task 2");

        UserDTO userDTO1 = new UserDTO();
        userDTO1.setId(1L);
        userDTO1.setName("User 1");

        UserDTO userDTO2 = new UserDTO();
        userDTO2.setId(2L);
        userDTO2.setName("User 2");

        TaskDTO taskDTO1 = new TaskDTO();
        taskDTO1.setId(1L);
        taskDTO1.setTitle("Task 1");

        TaskDTO taskDTO2 = new TaskDTO();
        taskDTO2.setId(2L);
        taskDTO2.setTitle("Task 2");

        when(userDAO.findAll()).thenReturn(Arrays.asList(user1, user2));
        when(userDAO.getTasksByUserId(1L)).thenReturn(Arrays.asList(task1, task2));
        when(userDAO.getTasksByUserId(2L)).thenReturn(Arrays.asList(task2));

        List<UserDTO> result = userService.getAllUsers();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(2, result.get(0).getTasks().size());
        assertEquals(1, result.get(1).getTasks().size());

        verify(userDAO, times(1)).findAll();
        verify(userDAO, times(1)).getTasksByUserId(1L);
        verify(userDAO, times(1)).getTasksByUserId(2L);
    }

    @Test
    public void testDeleteUser() {
        Long userId = 1L;
        doNothing().when(userDAO).delete(userId);

        userService.deleteUser(userId);

        verify(userDAO, times(1)).delete(userId);
    }

    @Test
    public void testAssignTaskToUser() {
        Long userId = 1L;
        Long taskId = 1L;
        doNothing().when(userDAO).assignTaskToUser(userId, taskId);

        userService.assignTaskToUser(userId, taskId);

        verify(userDAO, times(1)).assignTaskToUser(userId, taskId);
    }

    @Test
    public void testGetTasksByUserId() {
        Long userId = 1L;
        Task task1 = new Task();
        task1.setId(1L);
        task1.setTitle("Task 1");

        Task task2 = new Task();
        task2.setId(2L);
        task2.setTitle("Task 2");

        TaskDTO taskDTO1 = new TaskDTO();
        taskDTO1.setId(1L);
        taskDTO1.setTitle("Task 1");

        TaskDTO taskDTO2 = new TaskDTO();
        taskDTO2.setId(2L);
        taskDTO2.setTitle("Task 2");

        when(userDAO.getTasksByUserId(userId)).thenReturn(Arrays.asList(task1, task2));

        List<TaskDTO> result = userService.getTasksByUserId(userId);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Task 1", result.get(0).getTitle());
        assertEquals("Task 2", result.get(1).getTitle());

        verify(userDAO, times(1)).getTasksByUserId(userId);
    }

    @Test
    public void testCreateUserWithNullDTO() {

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            userService.createUser(null);
        });
        assertEquals("UserDTO cannot be null", thrown.getMessage());
    }

    @Test
    public void testCreateUserWithNullUser() {
        UserDTO userDTO = new UserDTO();
        when(userMapper.toEntity(userDTO)).thenReturn(null);

        IllegalStateException thrown = assertThrows(IllegalStateException.class, () -> {
            userService.createUser(userDTO);
        });
        assertEquals("User cannot be null", thrown.getMessage());
    }

    @Test
    public void testUpdateUserWithNullId() {
        UserDTO userDTO = new UserDTO();
        userDTO.setName("new name");

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            userService.updateUser(userDTO);
        });
        assertEquals("UserDTO ID cannot be null", thrown.getMessage());
    }

    @Test
    public void testUpdateUserWithNullUser() {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(1L);
        when(userMapper.toEntity(userDTO)).thenReturn(null);

        IllegalStateException thrown = assertThrows(IllegalStateException.class, () -> {
            userService.updateUser(userDTO);
        });
        assertEquals("User cannot be null", thrown.getMessage());
    }

    @Test
    public void testGetUserByIdWithNullUser() {
        Long userId = 1L;
        when(userDAO.findById(userId)).thenReturn(null);

        UserDTO result = userService.getUserById(userId);

        assertNull(result);
    }

    @Test
    public void testDeleteUserWithNonExistingId() {
        Long userId = 1L;
        doNothing().when(userDAO).delete(userId);

        userService.deleteUser(userId);

        verify(userDAO, times(1)).delete(userId);
    }

}
