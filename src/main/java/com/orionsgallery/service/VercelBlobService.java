package com.orionsgallery.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.orionsgallery.model.CatPhoto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * All storage goes through Vercel Blob:
 *
 *   • Images      → photos/<uuid>.<ext>          (random suffix, public)
 *   • Metadata    → gallery/photos.json           (fixed pathname, overwritten on each save)
 *
 * Vercel Blob REST API reference:
 *   https://vercel.com/docs/storage/vercel-blob/using-blob-sdk#technical-details
 *
 * Required property:  vercel.blob.token  (a vercel_blob_rw_... token)
 */
@Service
public class VercelBlobService {

    private static final String BASE_URL    = "https://blob.vercel-storage.com";
    private static final String API_VERSION = "7";
    private static final String METADATA_PATHNAME = "gallery/photos.json";

    @Value("${vercel.blob.token}")
    private String token;

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public VercelBlobService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.restClient   = RestClient.builder().baseUrl(BASE_URL).build();
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Image upload
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Uploads an image to Vercel Blob and returns its public URL.
     * A random UUID prefix is used so every photo gets a unique, stable URL.
     */
    public String uploadImage(MultipartFile file) throws IOException {
        String pathname    = "photos/" + UUID.randomUUID() + extension(file.getOriginalFilename());
        String contentType = file.getContentType() != null ? file.getContentType() : "application/octet-stream";

        BlobUploadResponse response = restClient.put()
                .uri("/{pathname}?access=public", pathname)
                .headers(h -> {
                    h.set("Authorization",  "Bearer " + token);
                    h.set("x-api-version",  API_VERSION);
                })
                .contentType(MediaType.parseMediaType(contentType))
                .body(file.getBytes())
                .retrieve()
                .body(BlobUploadResponse.class);

        if (response == null || response.url() == null) {
            throw new IOException("Vercel Blob returned an empty response for image upload");
        }
        return response.url();
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Gallery metadata  (photos.json)
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Reads gallery metadata from Vercel Blob.
     *
     * Flow:
     *   1. List blobs whose pathname starts with "gallery/photos.json"
     *   2. If none found → return empty list (first run)
     *   3. Fetch the blob's public URL and deserialise the JSON array
     */
    public List<CatPhoto> loadMetadata() throws IOException {
        BlobListResponse listing = listBlobs(METADATA_PATHNAME);

        if (listing.blobs() == null || listing.blobs().isEmpty()) {
            return new ArrayList<>();
        }

        // Fetch the actual JSON content from the public blob URL
        String jsonText = restClient.get()
                .uri(listing.blobs().getFirst().url())
                .retrieve()
                .body(String.class);

        if (jsonText == null || jsonText.isBlank()) {
            return new ArrayList<>();
        }
        return objectMapper.readValue(jsonText, new TypeReference<List<CatPhoto>>() {});
    }

    /**
     * Persists the gallery metadata to Vercel Blob.
     *
     * Flow:
     *   1. List any existing "gallery/photos.json" blobs
     *   2. Delete them all  (Vercel Blob doesn't overwrite — each PUT is a new object)
     *   3. PUT the new JSON  (addRandomSuffix=false → predictable pathname)
     */
    public void saveMetadata(List<CatPhoto> photos) throws IOException {
        // 1. Collect URLs of any existing metadata blobs
        BlobListResponse listing = listBlobs(METADATA_PATHNAME);
        if (listing.blobs() != null && !listing.blobs().isEmpty()) {
            List<String> urls = listing.blobs().stream().map(BlobItem::url).toList();
            //deleteBlobs(urls);
        }

        // 2. Serialise and upload new metadata
        byte[] json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(photos);

        restClient.put()
                // addRandomSuffix=false → consistent pathname each time
                .uri("/{pathname}?access=public&addRandomSuffix=false", METADATA_PATHNAME)
                .headers(h -> {
                    h.set("Authorization",  "Bearer " + token);
                    h.set("x-api-version",  API_VERSION);
                })
                .contentType(MediaType.APPLICATION_JSON)
                .body(json)
                .retrieve()
                .toBodilessEntity();
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Vercel Blob REST helpers
    // ══════════════════════════════════════════════════════════════════════════

    /** Lists blobs whose pathname starts with {@code prefix}. */
    private BlobListResponse listBlobs(String prefix) {
        BlobListResponse response = restClient.get()
                .uri("?prefix={prefix}&limit=100", prefix)
                .headers(h -> {
                    h.set("Authorization", "Bearer " + token);
                    h.set("x-api-version", API_VERSION);
                })
                .retrieve()
                .body(BlobListResponse.class);

        return response != null ? response : new BlobListResponse(List.of(), false, null);
    }

//    /**
//     * Deletes blobs by their public URLs.
//     * DELETE https://blob.vercel-storage.com  body: {"urls": [...]}
//     */
//    private void deleteBlobs(List<String> urls) {
//        restClient.delete()
//                .uri("/")
//                .headers(h -> {
//                    h.set("Authorization",  "Bearer " + token);
//                    h.set("x-api-version",  API_VERSION);
//                })
//                .body(new BlobDeleteRequest(urls))
//                .retrieve()
//                .toBodilessEntity();
//    }

    // ══════════════════════════════════════════════════════════════════════════
    // Internal records (Vercel Blob API shapes)
    // ══════════════════════════════════════════════════════════════════════════

    private record BlobUploadResponse(String url, String downloadUrl, String pathname, String contentType) {}

    private record BlobItem(String url, String downloadUrl, String pathname, long size) {}

    private record BlobListResponse(List<BlobItem> blobs, boolean hasMore, String cursor) {}

    private record BlobDeleteRequest(List<String> urls) {}

    // ══════════════════════════════════════════════════════════════════════════
    // Utility
    // ══════════════════════════════════════════════════════════════════════════

    private static String extension(String filename) {
        if (filename == null || !filename.contains(".")) return "";
        return filename.substring(filename.lastIndexOf('.'));
    }
}
