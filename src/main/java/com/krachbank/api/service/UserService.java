package com.krachbank.api.service;

import java.util.List;
import java.util.Map;

import com.krachbank.api.dto.DTO;
import com.krachbank.api.dto.UserDTO;
import com.krachbank.api.models.User;

public interface UserService extends Service<UserDTO, User> {
    List<UserDTO> getUsers();

    UserDTO getUserById(Long id);

    DTO verifyUser( Long id);

    UserDTO createUser(UserDTO userDTO);

    UserDTO updateUser(Long id, User user);

    UserDTO deactivateUser(Long id);

    List<UserDTO> getAllUsers(Map<String, String> params);

}
