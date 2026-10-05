package com.plantdisease.backend.scan;

import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Locale;

import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.plantdisease.backend.common.InvalidImageException;
import com.plantdisease.backend.disease.Disease;
import com.plantdisease.backend.disease.DiseaseRepository;
import com.plantdisease.backend.ml.MlClient;
import com.plantdisease.backend.ml.MlPrediction;
import com.plantdisease.backend.scan.ScanResponses.PageResponse;
import com.plantdisease.backend.scan.ScanResponses.ScanDetail;
import com.plantdisease.backend.scan.ScanResponses.ScanSummary;
import com.plantdisease.backend.storage.ImageStorageService;

@Service
public class ScanService {

    private static final List<String> ALLOWED_EXTENSIONS = List.of(".jpg", ".jpeg", ".png");

    private final ScanRepository scanRepository;
    private final DiseaseRepository diseaseRepository;
    private final MlClient mlClient;
    private final ImageStorageService storage;

    public ScanService(ScanRepository scanRepository, DiseaseRepository diseaseRepository,
                       MlClient mlClient, ImageStorageService storage) {
        this.scanRepository = scanRepository;
        this.diseaseRepository = diseaseRepository;
        this.mlClient = mlClient;
        this.storage = storage;
    }

    /** Full flow: validate, ask the ML service, store the images, save the scan. */
    @Transactional
    public ScanDetail analyze(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidImageException("No image was sent.");
        }
        String contentType = file.getContentType();
        if (contentType != null && !contentType.startsWith("image/")) {
            throw new InvalidImageException("The file must be an image (JPG or PNG).");
        }

        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            throw new InvalidImageException("The image could not be read.");
        }

        MlPrediction prediction = mlClient.predict(bytes, file.getOriginalFilename(), contentType);

        String imagePath = storage.save(bytes, extensionOf(file.getOriginalFilename()));
        String heatmapPath = prediction.heatmapBase64() == null ? null
                : storage.save(Base64.getDecoder().decode(prediction.heatmapBase64()), ".png");

        Scan scan = new Scan(imagePath, heatmapPath,
                findDisease(prediction.topClass().className()),
                prediction.topClass().confidence(), prediction.uncertain(), prediction.threshold(),
                prediction.inferenceMs(), prediction.gradcamMs());

        List<MlPrediction.MlClass> top3 = prediction.top3();
        for (int i = 0; i < top3.size(); i++) {
            scan.addPrediction(findDisease(top3.get(i).className()), i + 1, top3.get(i).confidence());
        }

        scanRepository.save(scan);
        return ScanDetail.from(scan);
    }

    @Transactional(readOnly = true)
    public PageResponse<ScanSummary> history(int page, int size) {
        PageRequest request = PageRequest.of(Math.max(page, 0), Math.clamp(size, 1, 50));
        return PageResponse.from(scanRepository.findAllByOrderByCreatedAtDesc(request).map(ScanSummary::from));
    }

    @Transactional(readOnly = true)
    public ScanDetail get(Long id) {
        return ScanDetail.from(scanRepository.findWithDetailsById(id).orElseThrow(() -> notFound(id)));
    }

    @Transactional(readOnly = true)
    public StoredImage image(Long id, boolean heatmap) {
        Scan scan = scanRepository.findById(id).orElseThrow(() -> notFound(id));
        String path = heatmap ? scan.getHeatmapPath() : scan.getImagePath();
        if (path == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No heatmap for scan " + id);
        }
        return new StoredImage(storage.load(path), path.toLowerCase(Locale.ROOT).endsWith(".png")
                ? "image/png" : "image/jpeg");
    }

    @Transactional
    public void delete(Long id) {
        Scan scan = scanRepository.findById(id).orElseThrow(() -> notFound(id));
        scanRepository.delete(scan);
        storage.delete(scan.getImagePath());
        storage.delete(scan.getHeatmapPath());
    }

    public record StoredImage(byte[] bytes, String contentType) {
    }

    private Disease findDisease(String className) {
        return diseaseRepository.findByClassName(className).orElseThrow(() -> new IllegalStateException(
                "Class '" + className + "' returned by the model is missing from the disease table"));
    }

    private static String extensionOf(String filename) {
        if (filename != null && filename.contains(".")) {
            String ext = filename.substring(filename.lastIndexOf('.')).toLowerCase(Locale.ROOT);
            if (ALLOWED_EXTENSIONS.contains(ext)) {
                return ext;
            }
        }
        return ".jpg";
    }

    private static ResponseStatusException notFound(Long id) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Scan " + id + " not found");
    }
}
