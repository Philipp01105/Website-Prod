package com.example.demo.Controller;

import com.example.demo.Entities.User;
import com.example.demo.Services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public User registerUser(
            @RequestParam("username") String username,
            @RequestParam("password") String password) {

        return userService.registerUser(username, password, "ROLE_USER");
    }
}