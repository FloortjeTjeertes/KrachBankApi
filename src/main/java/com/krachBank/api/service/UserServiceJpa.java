package com.krachbank.api.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.krachbank.api.dto.UserDTO;
import com.krachbank.api.models.User;
import com.krachbank.api.repository.UserRepository;

@Service
public class UserServiceJpa implements UserService {
    private final UserRepository userRepository;

    public UserServiceJpa(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public List<UserDTO> getUsers() {
        return userRepository.findAll().stream()
                .map(user -> new UserDTO(
                        user.getId(),
                        String.valueOf(user.getDailyLimit()),
                        user.getCreatedAt().toString(),
                        user.isVerified(),
                        user.isActive(),
                        user.getFirstName(),
                        user.getLastName(),
                        user.getEmail(),
                        user.getPhoneNumber(),
                        user.getBsn()))
                .collect(Collectors.toList());
    }

    @Override
    public UserDTO getUserById(Long id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getUserById'");
    }

    @Override
    public UserDTO createUser(User userDTO) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'createUser'");
    }

    @Override
    public UserDTO updateUser(Long id, User userDTO) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updateUser'");
    }

    @Override
    public void removeUser(Long id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'removeUser'");
    }

}
