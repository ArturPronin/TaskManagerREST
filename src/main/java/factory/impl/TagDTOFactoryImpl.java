package factory.impl;

import dto.TagDTO;
import factory.Factory;

public class TagDTOFactoryImpl implements Factory<TagDTO> {

    @Override
    public TagDTO create() {
        return new TagDTO();
    }
}
