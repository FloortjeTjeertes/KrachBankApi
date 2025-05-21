package com.krachbank.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.krachbank.api.models.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
     private final UserService userService;

    @GetMapping
    public List<UserResponse> getAllUsers(@RequestParam Map<String, String> params) {
        return userService.getAllUsers(params);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@RequestBody @Valid UserRequest userRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(userRequest));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long id, @RequestBody @Valid UserRequest userRequest) {
        return ResponseEntity.ok(userService.updateUser(id, userRequest));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<UserResponse> deleteUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.deactivateUser(id));
    }

    @PostMapping("/{id}/verify")
    public ResponseEntity<UserResponse> verifyUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.verifyUser(id));
    }

}
