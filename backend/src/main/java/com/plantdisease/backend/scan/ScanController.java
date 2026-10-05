package com.plantdisease.backend.scan;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.plantdisease.backend.scan.ScanResponses.PageResponse;
import com.plantdisease.backend.scan.ScanResponses.ScanDetail;
import com.plantdisease.backend.scan.ScanResponses.ScanSummary;

@RestController
@RequestMapping("/api/scans")
public class ScanController {

    private final ScanService service;

    public ScanController(ScanService service) {
        this.service = service;
    }

    /** POST /api/scans with a "file" field: analyzes the photo and saves the result. */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ScanDetail analyze(@RequestParam("file") MultipartFile file) {
        return service.analyze(file);
    }

    /** GET /api/scans?page=0&size=10 : history, most recent first. */
    @GetMapping
    public PageResponse<ScanSummary> history(@RequestParam(defaultValue = "0") int page,
                                             @RequestParam(defaultValue = "10") int size) {
        return service.history(page, size);
    }

    @GetMapping("/{id}")
    public ScanDetail get(@PathVariable Long id) {
        return service.get(id);
    }

    @GetMapping("/{id}/image")
    public ResponseEntity<byte[]> image(@PathVariable Long id) {
        return toResponse(service.image(id, false));
    }

    @GetMapping("/{id}/heatmap")
    public ResponseEntity<byte[]> heatmap(@PathVariable Long id) {
        return toResponse(service.image(id, true));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    private static ResponseEntity<byte[]> toResponse(ScanService.StoredImage image) {
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(image.contentType()))
                .body(image.bytes());
    }
}
