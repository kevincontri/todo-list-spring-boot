package com.kevincontri.todolist.task;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import lombok.Data;

@Data
public class TaskModel {
  @Id
  @GeneratedValue(generator = "UUID")
  private UUID id;
  @Column(name = "title", nullable = false)
  private String title;
  @Column(name = "description", nullable = true)
  private String description;
  @CreationTimestamp
  private LocalDateTime createdAt;
}
