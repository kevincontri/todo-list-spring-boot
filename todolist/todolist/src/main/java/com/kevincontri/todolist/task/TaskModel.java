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
@Entity(name = "tb_tasks")
public class TaskModel {
  @Id
  @GeneratedValue(generator = "UUID")
  private UUID id;

  @Column(name = "title", length = 50)
  private String title;
  @Column(name = "description", length = 255)
  private String description;
  @Column(name = "user_id")
  private UUID userId;
  @Column(name = "priority")
  private String priority;
  @Column(name = "author_name", length = 100)
  private String author_name;
  @Column(name = "start_at")
  private LocalDateTime startAt;
  @Column(name = "end_at")
  private LocalDateTime endAt;
  @Column(name = "time_span")
  private String timeSpan;

  public void setTitle(String title) throws IllegalArgumentException {
    if (title != null && title.length() > 50) {
      throw new IllegalArgumentException("O título deve ter no máximo 50 caracteres");
    }
    this.title = title;
  }

  @CreationTimestamp
  private LocalDateTime createdAt;
}
