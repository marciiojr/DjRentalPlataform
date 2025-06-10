package com.djrentals.controller;

import com.djrentals.model.User;
import com.djrentals.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    // Registro cliente
    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        return "users/register";
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("user") User user,
                               BindingResult result,
                               Model model) {
        if (result.hasErrors()) {
            return "users/register";
        }
        if (userService.findByEmail(user.getEmail()).isPresent()) {
            model.addAttribute("emailError", "Email já cadastrado");
            return "users/register";
        }
        user.setRole(User.Role.CLIENT);
        userService.saveUser(user);
        return "redirect:/login?registered";
    }

    // Listagem para admin
    @GetMapping
    public String listUsers(Model model) {
        model.addAttribute("users", userService.listAll());
        return "users/list";
    }
}
