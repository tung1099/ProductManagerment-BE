package com.example.productmanagement.service.user;

import com.example.productmanagement.model.User;
import com.example.productmanagement.service.IGeneralService;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.Optional;

public interface IUserService extends IGeneralService<User>, UserDetailsService {
    Optional<User> findByUsername(String username);
}