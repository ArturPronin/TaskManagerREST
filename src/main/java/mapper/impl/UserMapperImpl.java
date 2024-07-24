package mapper.impl;

import dto.UserDTO;
import entity.User;
import factory.Factory;
import factory.impl.UserDTOFactoryImpl;
import factory.impl.UserFactoryImpl;
import mapper.UserMapper;

public class UserMapperImpl implements UserMapper {

    Factory<User> userFactory = new UserFactoryImpl();
    Factory<UserDTO> userDTOFactory = new UserDTOFactoryImpl();

    @Override
    public UserDTO toDTO(User user) {
        if (user == null) {
            return null;
        }
        if (user.getName() == null || user.getName().trim().isEmpty()) {
            return null;
        }
        UserDTO userDTO = userDTOFactory.create();
        userDTO.setId(user.getId());
        userDTO.setName(user.getName());
        return userDTO;
    }

    @Override
    public User toEntity(dto.UserDTO userDTO) {
        if (userDTO == null) {
            return null;
        }
        if (userDTO.getName() == null || userDTO.getName().trim().isEmpty()) {
            return null;
        }

        User user = userFactory.create();
        user.setId(userDTO.getId());
        user.setName(userDTO.getName());
        return user;
    }
}
