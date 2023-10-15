package com.example.todolist.task;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.apache.catalina.connector.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.todolist.utils.Utils;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


@RestController
@RequestMapping("/tasks")
public class TaskController {

    @Autowired
    private ItaskRepository taskRepository;

    @PostMapping("/create")
    public ResponseEntity create(@RequestBody TaskModel taskModel, HttpServletRequest request){
        var userId = request.getAttribute("idUser");
        
        taskModel.setIdUser( (UUID) userId);
        
        var currentDate = LocalDateTime.now();
        if(currentDate.isAfter(taskModel.getStartDate()) || currentDate.isAfter(taskModel.getEndDate())){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Initial/end date can't be later than " + taskModel.getEndDate());
        }

        if(taskModel.getStartDate().isAfter((taskModel.getEndDate()))){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Initial date can't be later than " + taskModel.getEndDate());
        }
        var task = this.taskRepository.save(taskModel);
        return ResponseEntity.status(HttpStatus.OK).body(task);
    }

    
   
    @GetMapping("/")
    public List<TaskModel> list(HttpServletRequest request){
        var userId = request.getAttribute("idUser");
        var tasks = this.taskRepository.findByIdUser((UUID) userId);
        return tasks;
    }

    @PutMapping("/{id}")
    public ResponseEntity update(@RequestBody TaskModel taskModel, HttpServletRequest request, @PathVariable UUID id){

        var task = this.taskRepository.findById(id).orElse(null);

        if(task == null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Task not found");
        }

        var userId = request.getAttribute("idUser");

        if(!task.getIdUser().equals(userId)){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You don't have permission to update this task");
        }

        Utils.copyNonNullProperties(taskModel, task);

        var taskUpdated = this.taskRepository.save(task);

        return ResponseEntity.status(HttpStatus.OK).body(taskUpdated);
    }
}
