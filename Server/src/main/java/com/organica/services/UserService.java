package com.organica.services;

import com.organica.payload.SignIn;
import com.organica.payload.UserDto;

public interface UserService {
    UserDto createUser(UserDto userDto);
    String signIn(SignIn signIn); // Change return type to String (JWT token)
}
