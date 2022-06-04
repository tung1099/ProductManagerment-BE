package com.example.productmanagement.controller;

import com.example.productmanagement.model.JwtResponse;
import com.example.productmanagement.model.Role;
import com.example.productmanagement.model.User;
import com.example.productmanagement.service.jwt.JwtService;
import com.example.productmanagement.service.user.IUserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@CrossOrigin("*")
public class AuthController {
    @Autowired
    Environment env;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private IUserService userService;

    @GetMapping("/list")
    public ResponseEntity<Iterable<User>> getAllUsers() {
        List<User> users = (List<User>) userService.findAll();
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    @PostMapping("/register")
    public ResponseEntity<User> saveUser(@RequestParam("file") MultipartFile file, @RequestParam("newUser") String user) {
        String file1 = file.getOriginalFilename();
        Role role = null;
        try {
            User user1 = new ObjectMapper().readValue(user, User.class);
            user1.setAvatar(file1);
            userService.save(user1);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        String fileUpload = env.getProperty("upload.path");
        try {
            FileCopyUtils.copy(file.getBytes(), new File(fileUpload + file1));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User user) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = jwtService.generateTokenLogin(authentication);
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User currentUser = userService.findByUsername(user.getUsername()).get();
        return ResponseEntity.ok(new JwtResponse(currentUser.getId(), jwt, userDetails.getUsername(), userDetails.getAuthorities()));
    }

    @GetMapping("/info/{id}")
    public ResponseEntity<User> findUserById(@PathVariable Long id) {
        Optional<User> userOptional = userService.findById(id);
        return new ResponseEntity<>(userOptional.get(), HttpStatus.OK);
    }

    @PutMapping("/info/{id}")
    public ResponseEntity<User> update(@PathVariable Long id,@RequestPart("file")MultipartFile file,@RequestPart ("newUser") String user){
        Optional<User> customerOptional = userService.findById(id);
        if (!customerOptional.isPresent()){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }else {
            MultipartFile multipartFile = file;
            String file1 = multipartFile.getOriginalFilename();
            try {
                User user1 = new ObjectMapper().readValue(user,User.class);
                user1.setId(customerOptional.get().getId());
                user1.setAvatar(file1);
                userService.save(user1);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            String fileUpLoad = env.getProperty("upload.path");
            try {
                FileCopyUtils.copy(multipartFile.getBytes(),new File(fileUpLoad + file1));
            } catch (IOException e) {
                e.printStackTrace();
            }
            return new ResponseEntity<>(HttpStatus.OK);
        }
    }
}
