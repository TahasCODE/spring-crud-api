package com.example.CRUD.service.impl;

import com.example.CRUD.DTO.ProfilePictureDownload;
import com.example.CRUD.DTO.ProfilePictureResponse;
import com.example.CRUD.Entity.ProfilePicture;
import com.example.CRUD.exception.BadRequestException;
import com.example.CRUD.exception.ResourceNotFoundException;
import com.example.CRUD.repo.PersonRepository;
import com.example.CRUD.repo.ProfilePictureRepository;
import com.example.CRUD.service.ProfilePictureService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ProfilePictureServiceImpl implements ProfilePictureService {

    private static final Set<String> ALLOWED_TYPES =
            Set.of("image/jpeg", "image/png", "image/webp", "image/gif");

    private final ProfilePictureRepository repository;
    private final PersonRepository personRepository;

    @Override
    @Transactional
    public ProfilePictureResponse save(Long personId, MultipartFile file) {
        if (!personRepository.existsById(personId)) {
            throw new ResourceNotFoundException("Customer or employee not found with id " + personId);
        }
        if (file.isEmpty()) {
            throw new BadRequestException("File is empty");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType.toLowerCase())) {
            throw new BadRequestException("Only JPEG, PNG, WebP or GIF images are allowed");
        }

        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            throw new IllegalStateException("Could not read the uploaded picture", e);
        }

        String name = StringUtils.getFilename(file.getOriginalFilename());
        if (name == null || name.isBlank()) {
            name = "picture";
        }

        removeByPersonId(personId);   // one picture per person: a new upload replaces the old one

        ProfilePicture p = new ProfilePicture();
        p.setPersonId(personId);
        p.setFileName(name);
        p.setContentType(contentType.toLowerCase());
        p.setSize(bytes.length);
        p.setContent(bytes);
        return toResponse(repository.save(p));
    }

    @Override
    @Transactional(readOnly = true)   // a large object can only be read inside a transaction
    public ProfilePictureDownload get(Long personId) {
        ProfilePicture p = repository.findByPersonId(personId)
                .orElseThrow(() -> new ResourceNotFoundException("No profile picture for id " + personId));
        return new ProfilePictureDownload(p.getContent(), p.getFileName(), p.getContentType());
    }

    @Override
    @Transactional
    public void delete(Long personId) {
        if (removeByPersonId(personId) == 0) {
            throw new ResourceNotFoundException("No profile picture for id " + personId);
        }
    }

    @Override
    @Transactional
    public void deleteIfExists(Long personId) {
        removeByPersonId(personId);
    }

    // unlink the large object first, then delete the row
    private int removeByPersonId(Long personId) {
        repository.unlinkContentByPersonId(personId);
        return repository.deleteByPersonId(personId);
    }

    private ProfilePictureResponse toResponse(ProfilePicture p) {
        return new ProfilePictureResponse(p.getPersonId(), p.getFileName(), p.getContentType(),
                p.getSize(), p.getUploadedAt());
    }
}