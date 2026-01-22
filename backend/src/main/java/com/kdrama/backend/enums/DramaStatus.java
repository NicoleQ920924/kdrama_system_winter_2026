package com.kdrama.backend.enums;

// Suggested by ChatGPT to enhance data consistency
public enum DramaStatus implements HasDisplayName {
    NOT_AIRED("即將播出"),
    ONGOING("跟播中"),
    COMPLETED("已完結");

    private final String displayName;

    DramaStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
