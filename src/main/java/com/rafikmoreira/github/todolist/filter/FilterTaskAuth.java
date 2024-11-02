package com.rafikmoreira.github.todolist.filter;

import java.io.IOException;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.rafikmoreira.github.todolist.user.IUserRepository;

import at.favre.lib.crypto.bcrypt.BCrypt;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class FilterTaskAuth extends OncePerRequestFilter {
  @Autowired
  private IUserRepository userRepository;

  @Override
  protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain) throws ServletException, IOException {

    var servletPath = request.getServletPath();

    if (!servletPath.startsWith("/tasks")) {
      filterChain.doFilter(request, response);
      return;
    }

    var authorization = request.getHeader("Authorization");

    var password = authorization.substring("Basic".length()).trim();

    byte[] decodedPassword = Base64.getDecoder().decode(password);

    String authString = new String(decodedPassword);

    String[] credentials = authString.split(":");

    String username = credentials[0];
    String passwordString = credentials[1];

    var user = this.userRepository.findByUsername(username);

    if (user == null) {
      response.sendError(401);

    } else {

      var passwordVerify = BCrypt.verifyer().verify(passwordString.toCharArray(), user.getPassword().toCharArray());

      if (!passwordVerify.verified) {
        response.sendError(401);
        return;
      }

      request.setAttribute("idUser", user.getId());

      filterChain.doFilter(request, response);

    }
  }

}
