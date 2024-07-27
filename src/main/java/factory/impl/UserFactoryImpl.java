package factory.impl;

import entity.User;
import factory.Factory;

public class UserFactoryImpl implements Factory<User> {

    @Override
    public User create() {
        return new User();

    }
}
