package com.kevincontri.todolist.errors;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.http.ResponseEntity;

// @ControllerAdvice define classes globais no momento de tratamento de exceções
@ControllerAdvice
public class ExceptionHandlerController {

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException ex) {
    return ResponseEntity.status(400).body(ex.getMessage());
  }
}
