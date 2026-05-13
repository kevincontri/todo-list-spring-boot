package com.kevincontri.todolist.filter;

import java.io.IOException;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.kevincontri.todolist.user.IUserRepository;
import at.favre.lib.crypto.bcrypt.BCrypt;

@Component
public class FilterTaskAuth extends OncePerRequestFilter {

  @Autowired
  private IUserRepository userRepository;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    // Verificar se a rota é /tasks
    var servletPath = request.getServletPath();
    if (servletPath.startsWith("/tasks")) {

      // Pegar a header Authorization da requisição e verificar se ela existe e se
      // começa com "Basic "
      var auth = request.getHeader("Authorization");
      if (!auth.startsWith("Basic ")) {
        response.sendError(401, "Authorization header inválida");
        return;
      }

      // Pegar os dados do usuário e senha da header Authorization
      var user_data_encoded = auth.substring("Basic".length()).trim();

      // Decodificar os dados do usuário e senha
      byte[] user_data_decoded = Base64.getDecoder().decode(user_data_encoded);

      // Converter os dados do usuário e senha para String
      String user_data_String = new String(user_data_decoded);

      // Separar os dados do usuário e senha pelo caractere ":"
      String[] user_data = user_data_String.split(":");

      // Verificar se o usuário existe no banco de dados
      var user = this.userRepository.findByEmail(user_data[0]);

      if (user == null) {
        response.sendError(401, "Usuário sem autorização");
      } else {
        // Se o usuário existir, verificar se a senha é válida
        var password_verify = BCrypt.verifyer().verify(user_data[1].toCharArray(), user.getPassword());

        if (!password_verify.verified) {
          response.sendError(401, "Senha inválida");
        } else {
          // Se a senha for válida, adicionar o id do usuário na requisição e continuar
          // com a requisição normalmente
          request.setAttribute("user_id", user.getId());
          request.setAttribute("author_name", user.getUsername());
          filterChain.doFilter(request, response);
        }
      }
    } else {
      // Se a rota não for /tasks, continuar com a requisição normalmente
      filterChain.doFilter(request, response);
    }
  }
}
