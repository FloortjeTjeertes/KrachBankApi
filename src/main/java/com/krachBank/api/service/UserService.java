package com.krachbank.api.service;

import java.util.List;

import com.krachbank.api.dto.DTO;
import com.krachbank.api.dto.UserDTO;

import com.krachbank.api.models.User;

public interface UserService extends Service<UserDTO, User> {
    public List<UserDTO> getUsers();

    public UserDTO getUserById(Long id);

    public DTO verifyUser(User user);

    public UserDTO updateUser(Long id, User userDTO);

    public void removeUser(Long id);

}
