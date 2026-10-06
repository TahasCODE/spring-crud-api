package com.example.CRUD.controller;



import com.example.CRUD.DTO.UserRequestDTO;
import com.example.CRUD.DTO.UserResponseDTO;
import com.example.CRUD.Entity.User;
import com.example.CRUD.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/users")
public class UserController {


    private final UserService userService;


    @PostMapping
    public ResponseEntity<UserResponseDTO> createUser(@Valid @RequestBody UserRequestDTO dto){
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(dto));
    }

    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> getAllUsers(){
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("{id}")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable Long id) throws Throwable {
        return ResponseEntity.ok(userService.getUserById(id));
    }


    @PutMapping("{id}")
    public User updateUser(@PathVariable Long id, @RequestBody User user){
        return userService.updateUser(id,user);
    }



    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id){
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

}
