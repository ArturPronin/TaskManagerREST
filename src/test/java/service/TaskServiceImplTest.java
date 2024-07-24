package service;

import dao.TaskDAO;
import dto.TaskDTO;
import entity.Task;
import mapper.TaskMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import service.impl.TaskServiceImpl;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

public class TaskServiceImplTest {

    @Mock
    private TaskDAO taskDAO;

    @Mock
    private TaskMapper taskMapper;

    @InjectMocks
    private TaskServiceImpl taskService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testCreateTask() {
        TaskDTO taskDTO = new TaskDTO();
        taskDTO.setTitle("Task 1");
        Task task = new Task();
        task.setTitle("Task 1");

        when(taskMapper.toEntity(taskDTO)).thenReturn(task);

        taskService.createTask(taskDTO);

        ArgumentCaptor<Task> taskCaptor = ArgumentCaptor.forClass(Task.class);
        verify(taskDAO, times(1)).create(taskCaptor.capture());
        assertEquals("Task 1", taskCaptor.getValue().getTitle());
    }

    @Test
    public void testUpdateTask() {
        TaskDTO taskDTO = new TaskDTO();
        taskDTO.setId(1L);
        taskDTO.setTitle("Updated Title");

        Task task = new Task();
        task.setId(1L);
        task.setTitle("Updated Title");

        when(taskMapper.toEntity(taskDTO)).thenReturn(task);

        taskService.updateTask(taskDTO);

        ArgumentCaptor<Task> taskCaptor = ArgumentCaptor.forClass(Task.class);
        verify(taskDAO, times(1)).update(taskCaptor.capture());
        assertEquals(1L, taskCaptor.getValue().getId());
        assertEquals("Updated Title", taskCaptor.getValue().getTitle());
    }

    @Test
    public void testGetTaskById() {
        Long taskId = 1L;
        Task task = new Task();
        task.setId(taskId);
        task.setTitle("Task 1");
        TaskDTO taskDTO = new TaskDTO();
        taskDTO.setId(taskId);
        taskDTO.setTitle("Task 1");

        when(taskDAO.findById(taskId)).thenReturn(task);
        when(taskMapper.toDTO(task)).thenReturn(taskDTO);

        TaskDTO result = taskService.getTaskById(taskId);

        assertNotNull(result);
        assertEquals(taskId, result.getId());
        assertEquals("Task 1", result.getTitle());
        verify(taskDAO, times(1)).findById(taskId);
    }

    @Test
    public void testGetAllTasks() {
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

        when(taskDAO.findAll()).thenReturn(Arrays.asList(task1, task2));
        when(taskMapper.toDTO(task1)).thenReturn(taskDTO1);
        when(taskMapper.toDTO(task2)).thenReturn(taskDTO2);

        List<TaskDTO> result = taskService.getAllTasks();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Task 1", result.get(0).getTitle());
        assertEquals("Task 2", result.get(1).getTitle());

        verify(taskDAO, times(1)).findAll();
    }

    @Test
    public void testDeleteTask() {
        Long taskId = 1L;
        doNothing().when(taskDAO).delete(taskId);

        taskService.deleteTask(taskId);

        verify(taskDAO, times(1)).delete(taskId);
    }

    @Test
    public void testCreateTaskWithNullDTO() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            taskService.createTask(null);
        });
        assertEquals("TaskDTO cannot be null", thrown.getMessage());
    }

    @Test
    public void testCreateTaskWithNullTask() {
        TaskDTO taskDTO = new TaskDTO();
        when(taskMapper.toEntity(taskDTO)).thenReturn(null);

        IllegalStateException thrown = assertThrows(IllegalStateException.class, () -> {
            taskService.createTask(taskDTO);
        });
        assertEquals("Task cannot be null", thrown.getMessage());
    }

    @Test
    public void testUpdateTaskWithNullId() {
        TaskDTO taskDTO = new TaskDTO();
        taskDTO.setTitle("Updated Title");

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            taskService.updateTask(taskDTO);
        });
        assertEquals("TaskDTO ID cannot be null", thrown.getMessage());
    }

    @Test
    public void testUpdateTaskWithNullTask() {
        TaskDTO taskDTO = new TaskDTO();
        taskDTO.setId(1L);
        when(taskMapper.toEntity(taskDTO)).thenReturn(null);

        IllegalStateException thrown = assertThrows(IllegalStateException.class, () -> {
            taskService.updateTask(taskDTO);
        });
        assertEquals("Task cannot be null", thrown.getMessage());
    }

    @Test
    public void testGetTaskByIdWithNullTask() {
        Long taskId = 1L;
        when(taskDAO.findById(taskId)).thenReturn(null);

        TaskDTO result = taskService.getTaskById(taskId);

        assertNull(result);
    }

    @Test
    public void testDeleteTaskWithNonExistingId() {
        Long taskId = 1L;
        doNothing().when(taskDAO).delete(taskId);

        taskService.deleteTask(taskId);

        verify(taskDAO, times(1)).delete(taskId);
    }
}
