package com.krachbank.api.service;

import org.springframework.stereotype.Service;
import com.krachbank.api.repository.UserRepository;

@Service
public class UserServiceJpa implements UserService {
    private final UserRepository userRepository;
   

}
