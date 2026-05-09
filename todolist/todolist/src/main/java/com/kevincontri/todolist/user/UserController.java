package com.kevincontri.todolist.user;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/users")
public class UserController {

  @PostMapping("")
  public String createUser(@RequestBody UserModel user) {
    System.out.println(user.getUsername());
    System.out.println(user.getEmail());
    System.out.println(user.getPassword());
    return ("{\"username\": \"" + user.getUsername() + "\", \"email\": \"" + user.getEmail() + "\", \"password\": \""
        + user.getPassword() + "\"}");
  }
}
