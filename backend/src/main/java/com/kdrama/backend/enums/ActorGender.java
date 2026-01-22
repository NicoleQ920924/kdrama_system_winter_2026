package com.kdrama.backend.enums;

public enum ActorGender implements HasDisplayName {
    MALE("男"),
    FEMALE("女");

    private final String displayName;

    ActorGender(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
