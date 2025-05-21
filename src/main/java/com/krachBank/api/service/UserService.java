package com.krachbank.api.service;

import java.util.List;
import java.util.Map;

import com.krachbank.api.dto.DTO;
import com.krachbank.api.dto.UserDTO;

import com.krachbank.api.models.User;

public interface UserService {

    public UserDTO getUserById(Long id);
    public UserDTO createUser(UserDTO userDTO);
    public UserDTO updateUser(Long id, UserDTO userDTO);
    public UserDTO deactivateUser(Long id);
    public UserDTO verifyUser(Long id);

    List<UserDTO> getAllUsers(Map<String, String> params);
}

