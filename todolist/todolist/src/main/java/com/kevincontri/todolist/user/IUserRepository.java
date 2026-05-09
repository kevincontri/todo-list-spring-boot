package com.kevincontri.todolist.user;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface IUserRepository extends JpaRepository<UserModel, UUID> {
  UserModel findByEmail(String email); 
  // Método para encontrar um usuário pelo email, UserModel é a entidade e String é o tipo do email
}
