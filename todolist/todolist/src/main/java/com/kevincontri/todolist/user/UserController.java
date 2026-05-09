package com.kevincontri.todolist.user;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/users")
public class UserController {

  @Autowired
  private IUserRepository userRepository;

  @PostMapping("")
  public UserModel createUser(@RequestBody UserModel user) {
    // Verificar se o usuário já existe pelo email
    var existingUser = this.userRepository.findByEmail(user.getEmail());
    if (existingUser != null) {
      throw new RuntimeException("User already exists");
    }

    // Criar o usuário
    var userCreated = this.userRepository.save(user);
    return userCreated;
  }
}
