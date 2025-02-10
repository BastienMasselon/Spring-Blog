package org.wild.myblog.service;

import org.springframework.stereotype.Service;
import org.wild.myblog.dto.ImageDTO;
import org.wild.myblog.mapper.ImageMapper;
import org.wild.myblog.model.Image;
import org.wild.myblog.repository.ImageRepository;

import java.util.List;

@Service
public class ImageService {

    private final ImageMapper imageMapper;
    private final ImageRepository imageRepository;

    public ImageService(ImageMapper imageMapper, ImageRepository imageRepository) {
        this.imageMapper = imageMapper;
        this.imageRepository = imageRepository;
    }

    public List<ImageDTO> getAllImages() {
        List<Image> images = imageRepository.findAll();
        return images.stream().map(imageMapper::convertToDTO).toList();
    }

    public ImageDTO getImageById(Long id) {
        Image image = imageRepository.findById(id).orElse(null);
        if (image  == null) {
            return null;
        }
        return imageMapper.convertToDTO(image);
    }

    public ImageDTO createImage (Image image ) {
        Image savedImage = imageRepository.save(image);
        return imageMapper.convertToDTO(savedImage);
    }

    public ImageDTO updateImage(Long id, Image imageDetails) {
        Image image = imageRepository.findById(id).orElse(null);
        if (image == null) {
            return null;
        }
        image.setUrl(imageDetails.getUrl());
        Image updatedImage = imageRepository.save(image);
        return imageMapper.convertToDTO(updatedImage);
    }

    public boolean deleteImage(Long id) {
        Image image = imageRepository.findById(id).orElse(null);
        if (image == null) {
            return false;
        }
        imageRepository.delete(image);
        return true;
    }
}
