package com.kevincontri.todolist.task;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kevincontri.todolist.utils.Utils;

import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.Period;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/tasks")
public class TaskController {
  @Autowired
  private ITaskRepository taskRepository;

  @PostMapping("")
  public ResponseEntity<?> createTask(@RequestBody TaskModel task, HttpServletRequest request) {
    // Pegar o user_id do request e setar no task (coluna user_id)
    task.setUserId((UUID) request.getAttribute("user_id"));
    task.setAuthor_name((String) request.getAttribute("author_name"));

    // Verificar se a data de início e término estão no futuro e se a data de início
    // é antes da data de término
    var currentDate = LocalDateTime.now();
    if (currentDate.isAfter(task.getStartAt()) || currentDate.isAfter(task.getEndAt())) {
      return ResponseEntity.status(400).body("A data de início e término devem ser no futuro");
    } else if (task.getStartAt().isAfter(task.getEndAt())) {
      return ResponseEntity.status(400).body("A data de início deve ser antes da data de término");
    }

    // Calcular o tempo para executar as tarefas
    var startDate = task.getStartAt().toLocalDate();
    var endDate = task.getEndAt().toLocalDate();
    var period = Period.between(startDate, endDate);
    task.setTimeSpan(period.getYears() + " anos, " + period.getMonths() + " meses e " + period.getDays() + " dias");

    var savedTask = this.taskRepository.save(task);
    return ResponseEntity.status(200).body(savedTask);
  }

  @GetMapping("")
  public List<TaskModel> listTasks(HttpServletRequest request) {
    var user_id = request.getAttribute("user_id");
    var tasksFound = this.taskRepository.findByUserId((UUID) user_id);
    return tasksFound;
  }

  @GetMapping("/{id}")
  public ResponseEntity<?> getTaskById(HttpServletRequest request, @PathVariable UUID id) {
    var task = this.taskRepository.findById(id).orElse(null);
    if (task == null) {
      return ResponseEntity.status(400).body("Tarefa não encontrada");
    }
    if (!task.getUserId().equals(request.getAttribute("user_id"))) {
      return ResponseEntity.status(403).body("Acesso não autorizado");
    }

    return ResponseEntity.status(200).body(task);
  }

  @PutMapping("/{id}")
  public ResponseEntity<?> updateTask(@RequestBody TaskModel updateTaskModel, HttpServletRequest request,
      @PathVariable UUID id) {
    var task = this.taskRepository.findById(id).orElse(null);
    if (task == null) {
      return ResponseEntity.status(400).body("Tarefa não encontrada");
    }

    if (!task.getUserId().equals(request.getAttribute("user_id"))) {
      return ResponseEntity.status(403).body("Usuário não tem permissão");
    }

    Utils.copyNonNullProperties(updateTaskModel, task);
    var taskUpdated = this.taskRepository.save(task);
    return ResponseEntity.status(200).body(taskUpdated);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<?> deleteTask(HttpServletRequest request, @PathVariable UUID id) {
    var task = this.taskRepository.findById(id).orElse(null);
    if (task == null) {
      return ResponseEntity.status(400).body("Tarefa não encontrada");
    }

    if (!task.getUserId().equals(request.getAttribute("user_id"))) {
      return ResponseEntity.status(403).body("Usuário não tem permissão");
    }

    this.taskRepository.delete(task);
    return ResponseEntity.status(204).body("");
  }
}
