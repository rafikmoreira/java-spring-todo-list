package com.rafikmoreira.github.todolist.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import at.favre.lib.crypto.bcrypt.BCrypt;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@RequestMapping("/users")
public class UserController {
  @Autowired
  private IUserRepository userRepository;

  @PostMapping("")
  public ResponseEntity<?> create(@RequestBody UserModel entity) {

    var user = this.userRepository.findByUsername(entity.getUsername());

    if (user != null) {
      return ResponseEntity.status(HttpStatus.CONFLICT).body("Username already exists");
    }

    var passwordHashred = BCrypt.withDefaults().hashToString(12, entity.getPassword().toCharArray());

    entity.setPassword(passwordHashred);

    var userCreated = this.userRepository.save(entity);

    return ResponseEntity.status(HttpStatus.CREATED).body(userCreated);
  }
}
