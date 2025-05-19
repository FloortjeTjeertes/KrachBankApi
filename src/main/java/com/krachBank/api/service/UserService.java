package com.krachbank.api.service;

import java.util.List;

import com.krachbank.api.dto.UserDTO;
import com.krachbank.api.models.User;

public interface UserService {
    public List<UserDTO> getUsers();

    public UserDTO getUserById(Long id);

    public UserDTO createUser(User userDTO);

    public UserDTO updateUser(Long id, User userDTO);

    public void removeUser(Long id);
}
