package com.kevincontri.todolist.user;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import lombok.Data;

@Data
@Entity(name = "tb_users")
public class UserModel {

  @Id
  @GeneratedValue(generator = "UUID")
  private UUID id;

  @Column(name = "username", nullable = false)
  private String username;
  @Column(name = "email", nullable = false, unique = true)
  private String email;
  @Column(name = "password", nullable = false)
  private String password;

  @CreationTimestamp
  private LocalDateTime createdAt;
}
