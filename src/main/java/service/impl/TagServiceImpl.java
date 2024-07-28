package service.impl;

import dao.TagDAO;
import dto.TagDTO;
import dto.TaskDTO;
import entity.Tag;
import entity.Task;
import mapper.TagMapper;
import mapper.TaskMapper;
import mapper.impl.TagMapperImpl;
import mapper.impl.TaskMapperImpl;
import service.TagService;

import java.util.List;

public class TagServiceImpl implements TagService {

    private final TagDAO tagDAO;
    private final TagMapper tagMapper = new TagMapperImpl();
    private final TaskMapper taskMapper = new TaskMapperImpl();

    public TagServiceImpl(TagDAO tagDAO) {
        this.tagDAO = tagDAO;
    }

    @Override
    public void createTag(TagDTO tagDTO) {
        if (tagDTO == null) {
            throw new IllegalArgumentException("TagDTO cannot be null");
        }
        Tag tag = tagMapper.toEntity(tagDTO);
        if (tag == null) {
            throw new IllegalStateException("Tag cannot be null");
        }
        tagDAO.create(tag);
    }

    @Override
    public TagDTO getTagById(Long id) {
        Tag tag = tagDAO.findById(id);
        return tagMapper.toDTO(tag);
    }

    @Override
    public List<TagDTO> getAllTags() {
        List<Tag> tags = tagDAO.findAll();
        return tags.stream()
                .map(tag -> {
                    List<Task> tasks = tagDAO.getTasksByTagId(tag.getId());
                    TagDTO tagDTO = tagMapper.toDTO(tag);

                    tagDTO.setTasks(tasks.stream()
                            .map(taskMapper::toDTO)
                            .toList());
                    return tagDTO;
                })
                .toList();
    }

    @Override
    public void updateTag(TagDTO tagDTO) {
        if (tagDTO.getId() == null) {
            throw new IllegalArgumentException("TagDTO ID cannot be null");
        }
        Tag tag = tagMapper.toEntity(tagDTO);
        if (tag == null) {
            throw new IllegalStateException("Tag cannot be null");
        }
        tagDAO.update(tag);
    }

    @Override
    public void deleteTag(Long id) {
        tagDAO.delete(id);
    }

    @Override
    public void assignTaskToTag(Long tagId, Long taskId) {
        tagDAO.assignTaskToTag(tagId, taskId);
    }

    @Override
    public List<TaskDTO> getTasksByTagId(Long tagId) {
        List<Task> tasks = tagDAO.getTasksByTagId(tagId);
        return tasks.stream()
                .map(taskMapper::toDTO)
                .toList();
    }
}
