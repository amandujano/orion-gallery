package com.orionsgallery.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST API for the cat picture gallery.
 * Replace the stub data with real persistence (JPA, etc.) as needed.
 */
@RestController
@RequestMapping("/api/gallery")
public class GalleryController {

    public record CatPhoto(Long id, String title, String url, String description) {}

    @GetMapping("/photos")
    public List<CatPhoto> getPhotos() {
        // Stub data — wire up a real repository when ready
        return List.of(
            new CatPhoto(1L, "Orion napping", "/assets/images/orion-nap.jpg", "The classic loaf position"),
            new CatPhoto(2L, "Orion hunting", "/assets/images/orion-hunt.jpg", "Very serious business"),
            new CatPhoto(3L, "Orion in a box", "/assets/images/orion-box.jpg", "If it fits, I sits")
        );
    }
}
