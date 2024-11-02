package com.rafikmoreira.github.todolist.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import java.time.LocalDateTime;
import org.springframework.web.bind.annotation.RestController;
import java.util.UUID;

import com.rafikmoreira.github.todolist.user.IUserRepository;
import com.rafikmoreira.github.todolist.utils.Utils;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/tasks")
public class TaskController {
  @Autowired
  private ITaskRepository taskRepository;
  @Autowired
  private IUserRepository userRepository;

  @PostMapping("")
  public ResponseEntity<?> create(@RequestBody TaskModel entity, HttpServletRequest request) {
    var user = this.userRepository.findById((UUID) request.getAttribute("idUser"));

    if (!user.isPresent()) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
    }

    entity.setIdUser(user.get().getId());

    var currentDate = LocalDateTime.now();

    if (currentDate.isAfter(entity.getStartAt())) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Start date must be greater than current date");
    }

    if (entity.getStartAt().isAfter(entity.getEndAt())) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("End date must be greater than start date");
    }

    var task = this.taskRepository.save(entity);

    return ResponseEntity.status(HttpStatus.CREATED).body(task);
  }

  @GetMapping("")
  public ResponseEntity<?> getMethodName(HttpServletRequest request) {
    var tasks = this.taskRepository.findByIdUser((UUID) request.getAttribute("idUser"));
    return ResponseEntity.status(HttpStatus.OK).body(tasks);
  }

  @PutMapping("/{id}")
  public ResponseEntity<?> update(@PathVariable String id, @RequestBody TaskModel taskModel,
      HttpServletRequest request) {
    var task = this.taskRepository.findById(UUID.fromString(id)).orElse(null);
    var userId = (UUID) request.getAttribute("idUser");

    if (task == null) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Task not found");
    }

    if (!task.getIdUser().equals(userId)) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You don't have permission to update this task");
    }

    taskModel.setIdUser(userId);
    taskModel.setId(UUID.fromString(id));

    Utils.copyNonNullProperties(taskModel, task);

    return ResponseEntity.status(HttpStatus.OK).body(task);
  }

}
