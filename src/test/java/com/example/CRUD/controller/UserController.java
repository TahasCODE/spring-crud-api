package com.example.CRUD.controller;



import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/users")
@Tag(name = "Users",description = "Create users and look up the books they have added")
public class UserController {


    private final UserService userService;
    private final BookService bookService;

    @PostMapping
    public ResponseEntity<UserResponseDTO> createUser(@Valid @RequestBody UserRequestDTO dto){
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(dto));
    }

    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> getAllUsers(){
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("{id}")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable Long id){
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @GetMapping("/{id}/books")
    public ResponseEntity<List<BookResponseDTO>> getBooksByUser(@PathVariable Long id){
        return ResponseEntity.ok(bookService.getBooksByUser(id));
    }


}
