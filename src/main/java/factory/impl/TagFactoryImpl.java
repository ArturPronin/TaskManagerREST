package factory.impl;

import entity.Tag;
import factory.Factory;

public class TagFactoryImpl implements Factory<Tag> {
    @Override
    public Tag create() {
        return new Tag();

    }

}
