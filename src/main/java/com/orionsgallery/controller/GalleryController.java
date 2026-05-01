package com.orionsgallery.controller;

import com.orionsgallery.model.CatPhoto;
import com.orionsgallery.service.VercelBlobService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/gallery")
public class GalleryController {

    private final VercelBlobService blobService;

    public GalleryController(VercelBlobService blobService) {
        this.blobService = blobService;
    }

    // ── GET /api/gallery/photos ───────────────────────────────────────────────

    /**
     * Returns all photos from the gallery/photos.json metadata stored in Vercel Blob.
     */
    @GetMapping("/photos")
    public ResponseEntity<List<CatPhoto>> getPhotos() {
        try {
            return ResponseEntity.ok(blobService.loadMetadata());
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // ── POST /api/gallery/upload ──────────────────────────────────────────────

    /**
     * Upload flow — everything goes to Vercel Blob:
     *
     *  1. Validate the file is a real image
     *  2. Upload the image file → Vercel Blob  →  get public image URL
     *  3. Load existing gallery metadata  (gallery/photos.json) from Vercel Blob
     *  4. Append the new CatPhoto (url points to the uploaded image blob)
     *  5. Save updated metadata back to Vercel Blob  (overwrites gallery/photos.json)
     *  6. Return the new CatPhoto to the client
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadPhoto(
            @RequestParam("file")        MultipartFile file,
            @RequestParam("title")       String title,
            @RequestParam("description") String description) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("No file provided.");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return ResponseEntity.badRequest().body("Only image files are accepted.");
        }

        try {
            // Step 1 — image → Vercel Blob
            String imageUrl = blobService.uploadImage(file);

            // Step 2 — load existing metadata
            List<CatPhoto> photos = blobService.loadMetadata();

            // Step 3 — build new entry
            long newId = photos.isEmpty() ? 1L : photos.getLast().id() + 1;
            CatPhoto newPhoto = new CatPhoto(newId, title.trim(), imageUrl, description.trim());

            // Step 4 — append and persist
            photos.add(newPhoto);
            blobService.saveMetadata(photos);

            return ResponseEntity.ok(newPhoto);

        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                    .body("Upload failed: " + e.getMessage());
        }
    }
}
