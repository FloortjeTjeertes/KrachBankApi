package com.krachBank.api.service;

import java.util.List;
import java.util.Map;

import com.krachBank.api.dto.UserDTO;

public interface UserService {

    public UserDTO getUserById(Long id);
    public UserDTO createUser(UserDTO userDTO);
    public UserDTO updateUser(Long id, UserDTO userDTO);
    public UserDTO deactivateUser(Long id);
    public UserDTO verifyUser(Long id);

    List<UserDTO> getAllUsers(Map<String, String> params);
}

