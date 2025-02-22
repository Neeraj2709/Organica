package com.organica.services.impl;

import com.organica.config.JwtService;
import com.organica.entities.Cart;
import com.organica.entities.Role;
import com.organica.entities.TotalRoles;
import com.organica.entities.User;
import com.organica.payload.SignIn;
import com.organica.payload.UserDto;
import com.organica.repositories.UserRepo;
import com.organica.services.UserService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserServiceImpl implements UserService, UserDetailsService {

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public UserDto createUser(UserDto userDto) {
        if (userRepo.existsByEmail(userDto.getEmail())) {
            throw new RuntimeException("User already exists with this email: " + userDto.getEmail());
        }

        User user = this.modelMapper.map(userDto, User.class);

        // ✅ Fetch role from DB instead of hardcoding
        Role role = roleRepo.findByName(TotalRoles.USER.name())
                .orElseThrow(() -> new RuntimeException("Role not found"));
        user.setRole(List.of(role));

        // ✅ Encode password properly
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // ✅ Ensure cart is saved with the user
        Cart cart = new Cart();
        cart.setUser(user);
        user.setCart(cart);

        User savedUser = this.userRepo.save(user);
        System.out.println("User created successfully: " + savedUser);

        return this.modelMapper.map(savedUser, UserDto.class);
    }

    @Override
    public String signIn(SignIn signIn) {
        try {
            // Authenticate user
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(signIn.getEmail(), signIn.getPassword())
            );

            // Fetch user details
            User user = userRepo.findByEmail(signIn.getEmail())
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            // Generate and return JWT token
            if (!passwordEncoder.matches(signIn.getPassword(), user.getPassword())) {
                throw new BadCredentialsException("Invalid Password");
            }

            return jwtService.generateToken(user);
        } catch (BadCredentialsException e) {
            throw new RuntimeException("Invalid credentials!");
        } catch (Exception e) {
            throw new RuntimeException("Authentication failed: " + e.getMessage());
        }
    }


    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(), user.getPassword(), user.getAuthorities()
        );
    }
}
