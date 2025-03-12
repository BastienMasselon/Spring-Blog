package org.wild.myblog.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.wild.myblog.dto.image.ImageCreateDTO;
import org.wild.myblog.dto.image.ImageDTO;
import org.wild.myblog.model.Image;
import org.wild.myblog.service.ImageService;

import java.util.List;

@RestController
@RequestMapping("/images")
public class ImageController {

    private final ImageService imageService;

    public ImageController(ImageService imageService) {
        this.imageService = imageService;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'AUTHOR')")
    @GetMapping
    public ResponseEntity<List<ImageDTO>> getAllImages() {
        List<ImageDTO> images = imageService.getAllImages();
        if (images.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(images);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'AUTHOR')")
    @GetMapping("/{id}")
    public ResponseEntity<ImageDTO> getImageById(@PathVariable Long id) {
        ImageDTO image = imageService.getImageById(id);
        if (image  == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(image);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'AUTHOR')")
    @PostMapping
    public ResponseEntity<ImageDTO> createImage (@Valid @RequestBody ImageCreateDTO imageCreateDTO ) {
        ImageDTO savedImage = imageService.createImage(imageCreateDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedImage);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'AUTHOR')")
    @PutMapping("/{id}")
    public ResponseEntity<ImageDTO> updateImage(@PathVariable Long id, @RequestBody Image imageDetails) {
        ImageDTO image = imageService.updateImage(id, imageDetails);
        if (image == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(image);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'AUTHOR')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteImage(@PathVariable Long id) {
        if (imageService.deleteImage(id)) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}