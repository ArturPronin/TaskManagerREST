package factory.impl;

import dto.UserDTO;
import factory.Factory;

public class UserDTOFactoryImpl implements Factory<UserDTO> {

    @Override
    public UserDTO create() {
        return new UserDTO();
    }
}
