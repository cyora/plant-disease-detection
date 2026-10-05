package com.plantdisease.backend.stats;

/** A label with a count, e.g. ("Tomato - Late blight", 12). Used by the dashboard charts. */
public record CountItem(String label, Long count) {
}
