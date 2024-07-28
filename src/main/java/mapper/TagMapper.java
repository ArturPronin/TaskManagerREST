package mapper;

import dto.TagDTO;
import entity.Tag;

public interface TagMapper {

    TagDTO toDTO(Tag tag);

    Tag toEntity(TagDTO tagDTO);
}
