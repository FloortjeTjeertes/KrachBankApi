package com.krachbank.api.service;

import java.util.List;

import com.krachbank.api.dto.DTO;
import com.krachbank.api.dto.UserDTOResponse;

import com.krachbank.api.models.User;

public interface UserService extends Service<UserDTOResponse, User> {
    public List<UserDTOResponse> getUsers();

    public UserDTOResponse getUserById(Long id);

    public DTO verifyUser(User user);

    public UserDTOResponse updateUser(Long id, User userDTO);

    public void removeUser(Long id);

}
