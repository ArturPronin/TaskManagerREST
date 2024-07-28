package service;

import dto.TagDTO;
import dto.TaskDTO;

import java.util.List;

public interface TagService {
    void createTag(TagDTO tagDTO);

    TagDTO getTagById(Long id);

    List<TagDTO> getAllTags();

    void updateTag(TagDTO tagDTO);

    void deleteTag(Long id);

    void assignTaskToTag(Long tagId, Long taskId);

    List<TaskDTO> getTasksByTagId(Long tagId);
}
