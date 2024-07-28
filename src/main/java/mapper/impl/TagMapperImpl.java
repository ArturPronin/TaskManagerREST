package mapper.impl;

import dto.TagDTO;
import entity.Tag;
import factory.Factory;
import factory.impl.TagDTOFactoryImpl;
import factory.impl.TagFactoryImpl;
import mapper.TagMapper;

public class TagMapperImpl implements TagMapper {

    Factory<Tag> tagFactory = new TagFactoryImpl();
    Factory<TagDTO> tagDTOFactory = new TagDTOFactoryImpl();

    @Override
    public TagDTO toDTO(Tag tag) {
        if (tag == null) {
            return null;
        }
        if (tag.getName() == null || tag.getName().trim().isEmpty()) {
            return null;
        }
        TagDTO tagDTO = tagDTOFactory.create();
        tagDTO.setId(tag.getId());
        tagDTO.setName(tag.getName());
        return tagDTO;
    }

    @Override
    public Tag toEntity(TagDTO tagDTO) {
        if (tagDTO == null) {
            return null;
        }
        if (tagDTO.getName() == null || tagDTO.getName().trim().isEmpty()) {
            return null;
        }
        Tag tag = tagFactory.create();
        tag.setId(tagDTO.getId());
        tag.setName(tagDTO.getName());
        return tag;
    }


}
