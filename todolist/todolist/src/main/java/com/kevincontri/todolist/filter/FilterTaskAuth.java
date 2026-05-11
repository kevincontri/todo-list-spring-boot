package com.kevincontri.todolist.filter;

import java.io.IOException;
import java.util.Base64;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class FilterTaskAuth extends OncePerRequestFilter {

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    // Pegar a requisição
    var auth = request.getHeader("Authorization");

    var user_data_encoded = auth.substring("Basic".length()).trim();

    byte[] user_data_decoded = Base64.getDecoder().decode(user_data_encoded);

    String user_data_String = new String(user_data_decoded);

    String[] user_data = user_data_String.split(":");

    System.out.println("User: " + user_data[0]);
    System.out.println("Password: " + user_data[1]);

    // Validar Usuário

    // Validar Senha

    // Permitir o acesso
    filterChain.doFilter(request, response);
  }

}
