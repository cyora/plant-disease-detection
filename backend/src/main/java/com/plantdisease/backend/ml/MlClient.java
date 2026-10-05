package com.plantdisease.backend.ml;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import com.plantdisease.backend.common.InvalidImageException;
import com.plantdisease.backend.common.MlServiceUnavailableException;

/** Sends the photo to the FastAPI service and returns its prediction. */
@Component
public class MlClient {

    private final RestClient restClient;

    public MlClient(@Value("${ml.service.url}") String baseUrl,
                    @Value("${ml.service.timeout-seconds}") int timeoutSeconds) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(5));
        factory.setReadTimeout(Duration.ofSeconds(timeoutSeconds));
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(factory)
                .build();
    }

    public MlPrediction predict(byte[] image, String filename, String contentType) {
        // The file part of the multipart request, with its name and type
        ByteArrayResource resource = new ByteArrayResource(image) {
            @Override
            public String getFilename() {
                return filename != null ? filename : "image.jpg";
            }
        };
        HttpHeaders partHeaders = new HttpHeaders();
        partHeaders.setContentType(contentType != null
                ? MediaType.parseMediaType(contentType)
                : MediaType.APPLICATION_OCTET_STREAM);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new HttpEntity<>(resource, partHeaders));

        try {
            return restClient.post()
                    .uri("/predict?gradcam=true")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(body)
                    .retrieve()
                    .body(MlPrediction.class);
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().is4xxClientError()) {
                // FastAPI refused the file (unreadable image, too large...)
                throw new InvalidImageException("The image could not be read. Use a JPG or PNG photo.");
            }
            throw new MlServiceUnavailableException("The analysis service returned an error.", e);
        } catch (ResourceAccessException e) {
            // Connection refused or timeout: FastAPI is not running
            throw new MlServiceUnavailableException("The analysis service is not reachable. Is FastAPI running?", e);
        }
    }
}
