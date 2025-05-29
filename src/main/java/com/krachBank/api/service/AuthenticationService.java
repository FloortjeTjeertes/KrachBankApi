package com.krachBank.api.service;

import com.krachBank.api.models.User;

public interface AuthenticationService {

    public UserDTO Login(String username, String password);

    public User Register(User user);
}
