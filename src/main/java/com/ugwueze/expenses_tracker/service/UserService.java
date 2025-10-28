package com.ugwueze.expenses_tracker.service;

import com.ugwueze.expenses_tracker.dto.UserDto;

import java.util.List;

public interface UserService {

    UserDto createUser(UserDto userDto);

    UserDto updateUser(Long id, UserDto userDto);

    void deleteUser(Long id);

    UserDto getUserById(Long id);

    UserDto getUserByUsername(String username);

    List<UserDto> getAllUsers();

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}