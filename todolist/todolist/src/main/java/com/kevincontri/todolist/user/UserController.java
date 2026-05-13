package com.kevincontri.todolist.user;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import at.favre.lib.crypto.bcrypt.BCrypt;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/users")
public class UserController {

  @Autowired
  private IUserRepository userRepository;

  @PostMapping("")
  public ResponseEntity createUser(@RequestBody UserModel user) {
    // Verificar se o usuário já existe pelo email
    var existingUser = this.userRepository.findByEmail(user.getEmail());
    if (existingUser != null) {
      // Mensagem de erro
      // Status code 400 Bad Request
      return ResponseEntity.status(400).body("Usuário já existe");
    }

    var passwordHashed = BCrypt.withDefaults().hashToString(12, user.getPassword().toCharArray());
    user.setPassword(passwordHashed);

    // Criar o usuário
    var userCreated = this.userRepository.save(user);
    return ResponseEntity.status(200).body(userCreated);
  }
}
