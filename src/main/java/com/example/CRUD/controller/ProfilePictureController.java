package com.example.CRUD.controller;

import com.example.CRUD.DTO.ProfilePictureDownload;
import com.example.CRUD.DTO.ProfilePictureResponse;
import com.example.CRUD.service.ProfilePictureService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/persons/{id}/picture")
@RequiredArgsConstructor
public class ProfilePictureController {

    private final ProfilePictureService profilePictureService;

    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ProfilePictureResponse upload(@PathVariable Long id, @RequestParam("file") MultipartFile file,
                                         @AuthenticationPrincipal Jwt jwt) {
        return profilePictureService.save(id, file, Long.valueOf(jwt.getSubject()), jwt.getClaimAsString("role"));
    }

    @GetMapping
    public ResponseEntity<byte[]> get(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        ProfilePictureDownload p = profilePictureService.get(id, Long.valueOf(jwt.getSubject()), jwt.getClaimAsString("role"));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.inline().filename(p.fileName(), StandardCharsets.UTF_8).build().toString())
                .contentType(MediaType.parseMediaType(p.contentType()))
                .body(p.content());
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        profilePictureService.delete(id, Long.valueOf(jwt.getSubject()), jwt.getClaimAsString("role"));
    }
}