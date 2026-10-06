package com.example.CRUD.service;


import com.example.CRUD.DTO.UserRequestDTO;
import com.example.CRUD.DTO.UserResponseDTO;
import com.example.CRUD.Entity.User;
import com.example.CRUD.exception.ResourceNotFoundException;
import com.example.CRUD.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public UserResponseDTO createUser(UserRequestDTO dto){
        User user = new User();
        user.setName(dto.getName());
        user.setDesignation(dto.getDesignation());
        User saved = (User) userRepository.save(user);
        return toResponseDTO(saved);
    }

    public List<UserResponseDTO> getAllUsers(){
        return userRepository.findAll()
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }


    public UserResponseDTO getUserById(Long id) throws Throwable {
        return toResponseDTO(getUserEntityById(id));
    }

    private UserResponseDTO toResponseDTO(User user) {
        return new UserResponseDTO(
                user.getId(), user.getName(), user.getDesignation()
        );
    }

    public User getUserEntityById(Long id) throws Throwable {
        return userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User Not Found " + id));
    }

    public UserResponseDTO updateUser(Long id, UserRequestDTO updated) {
        User existing = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id " + id));
        existing.setName(updated.getName());
        existing.setDesignation(updated.getDesignation());
        return toResponseDTO(userRepository.save(existing));
    }

    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found with id " + id);
        }
        userRepository.deleteById(id);
    }
}
