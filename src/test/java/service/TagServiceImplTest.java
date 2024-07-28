package service;

import dao.TagDAO;
import dto.TagDTO;
import dto.TaskDTO;
import entity.Tag;
import entity.Task;
import mapper.TagMapper;
import mapper.TaskMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import service.impl.TagServiceImpl;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TagServiceImplTest {

    @Mock
    private TagDAO tagDAO;

    @Mock
    private TagMapper tagMapper;

    @Mock
    private TaskMapper taskMapper;

    @InjectMocks
    private TagServiceImpl tagService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testCreateUser() {
        TagDTO tagDTO = new TagDTO();
        tagDTO.setName("Tag1");

        Tag tag = new Tag();
        tag.setName("Tag1");
        when(tagMapper.toEntity(tagDTO)).thenReturn(tag);
        tagService.createTag(tagDTO);
        ArgumentCaptor<Tag> tagCaptor = ArgumentCaptor.forClass(Tag.class);
        verify(tagDAO, times(1)).create(tagCaptor.capture());
        Tag capturedTag = tagCaptor.getValue();
        assertNotNull(capturedTag);
        assertEquals("Tag1", capturedTag.getName());
    }

    @Test
    public void testUpdateTag() {
        TagDTO tagDTO = new TagDTO();
        tagDTO.setId(1L);
        tagDTO.setName("Tag1");

        Tag tag = new Tag();
        tag.setId(1L);
        tag.setName("Tag1");

        when(tagMapper.toEntity(tagDTO)).thenReturn(tag);

        tagService.updateTag(tagDTO);

        ArgumentCaptor<Tag> tagCaptor = ArgumentCaptor.forClass(Tag.class);
        verify(tagDAO, times(1)).update(tagCaptor.capture());
        assertEquals(1L, tagCaptor.getValue().getId());
        assertEquals("Tag1", tagCaptor.getValue().getName());
    }

    @Test
    public void testGetTagById() {
        Long tagId = 1L;
        Tag tag = new Tag();
        tag.setId(tagId);
        tag.setName("Tag1");
        TagDTO tagDTO = new TagDTO();
        tagDTO.setId(tagId);
        tagDTO.setName("Tag1");

        when(tagDAO.findById(tagId)).thenReturn(tag);

        TagDTO result = tagService.getTagById(tagId);

        assertNotNull(result);
        assertEquals(tagId, result.getId());
        assertEquals("Tag1", result.getName());
        verify(tagDAO, times(1)).findById(tagId);
    }

    @Test
    public void testGetAllTags() {
        Tag tag1 = new Tag();
        tag1.setId(1L);
        tag1.setName("Tag1");

        Tag tag2 = new Tag();
        tag2.setId(2L);
        tag2.setName("Tag2");

        Task task1 = new Task();
        task1.setId(1L);
        task1.setTitle("Task 1");

        Task task2 = new Task();
        task2.setId(2L);
        task2.setTitle("Task 2");

        TagDTO tagDTO1 = new TagDTO();
        tagDTO1.setId(1L);
        tagDTO1.setName("Tag1");

        TagDTO tagDTO2 = new TagDTO();
        tagDTO2.setId(1L);
        tagDTO2.setName("Tag2");

        TaskDTO taskDTO1 = new TaskDTO();
        taskDTO1.setId(1L);
        taskDTO1.setTitle("Task 1");

        TaskDTO taskDTO2 = new TaskDTO();
        taskDTO2.setId(2L);
        taskDTO2.setTitle("Task 2");

        when(tagDAO.findAll()).thenReturn(Arrays.asList(tag1, tag2));
        when(tagDAO.getTasksByTagId(1L)).thenReturn(Arrays.asList(task1, task2));
        when(tagDAO.getTasksByTagId(2L)).thenReturn(Arrays.asList(task2));

        List<TagDTO> result = tagService.getAllTags();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(2, result.get(0).getTasks().size());
        assertEquals(1, result.get(1).getTasks().size());

        verify(tagDAO, times(1)).findAll();
        verify(tagDAO, times(1)).getTasksByTagId(1L);
        verify(tagDAO, times(1)).getTasksByTagId(2L);
    }

    @Test
    public void testDeleteTag() {
        Long tagId = 1L;
        doNothing().when(tagDAO).delete(tagId);

        tagService.deleteTag(tagId);

        verify(tagDAO, times(1)).delete(tagId);
    }

    @Test
    public void testAssignTaskToTag() {
        Long tagId = 1L;
        Long taskId = 1L;
        doNothing().when(tagDAO).assignTaskToTag(tagId, taskId);

        tagService.assignTaskToTag(tagId, taskId);

        verify(tagDAO, times(1)).assignTaskToTag(tagId, taskId);
    }

    @Test
    public void testGetTasksByTagId() {
        Long tagId = 1L;
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

        when(tagDAO.getTasksByTagId(tagId)).thenReturn(Arrays.asList(task1, task2));

        List<TaskDTO> result = tagService.getTasksByTagId(tagId);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Task 1", result.get(0).getTitle());
        assertEquals("Task 2", result.get(1).getTitle());

        verify(tagDAO, times(1)).getTasksByTagId(tagId);
    }

    @Test
    public void testCreateTagWithNullDTO() {

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            tagService.createTag(null);
        });
        assertEquals("TagDTO cannot be null", thrown.getMessage());
    }

    @Test
    public void testCreateUserWithNullTag() {
        TagDTO tagDTO = new TagDTO();
        when(tagMapper.toEntity(tagDTO)).thenReturn(null);

        IllegalStateException thrown = assertThrows(IllegalStateException.class, () -> {
            tagService.createTag(tagDTO);
        });
        assertEquals("Tag cannot be null", thrown.getMessage());
    }

    @Test
    public void testUpdateTagWithNullId() {
        TagDTO tagDTO = new TagDTO();
        tagDTO.setName("TagNew");

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            tagService.updateTag(tagDTO);
        });
        assertEquals("TagDTO ID cannot be null", thrown.getMessage());
    }

    @Test
    public void testUpdateTagWithNullTag() {
        TagDTO tagDTO = new TagDTO();
        tagDTO.setId(1L);
        when(tagMapper.toEntity(tagDTO)).thenReturn(null);

        IllegalStateException thrown = assertThrows(IllegalStateException.class, () -> {
            tagService.updateTag(tagDTO);
        });
        assertEquals("Tag cannot be null", thrown.getMessage());
    }

    @Test
    public void testGetTagByIdWithNullTag() {
        Long tagId = 1L;
        when(tagDAO.findById(tagId)).thenReturn(null);

        TagDTO result = tagService.getTagById(tagId);

        assertNull(result);
    }

    @Test
    public void testDeleteTagWithNonExistingId() {
        Long tagId = 1L;
        doNothing().when(tagDAO).delete(tagId);

        tagService.deleteTag(tagId);

        verify(tagDAO, times(1)).delete(tagId);
    }
}
