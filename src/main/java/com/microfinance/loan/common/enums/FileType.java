package com.microfinance.loan.common.enums;

import java.util.Set;

public enum FileType {

    PDF(Set.of("application/pdf"), Set.of("pdf")),

    IMAGE(Set.of("image/jpeg", "image/png", "image/jpg"),
            Set.of("jpg", "jpeg", "png"));

    private final Set<String> contentTypes;
    private final Set<String> extensions;

    FileType(Set<String> contentTypes, Set<String> extensions) {
        this.contentTypes = contentTypes;
        this.extensions = extensions;
    }

    public static boolean isValid(String contentType, String extension) {

        if (contentType == null || extension == null) {
            return false;
        }

        for (FileType type : FileType.values()) {
            if (type.matches(contentType, extension)) {
                return true;
            }
        }

        return false;
    }

    public boolean matches(String contentType, String extension) {
        return contentTypes.contains(contentType.toLowerCase()) &&
                extensions.contains(extension.toLowerCase());
    }
}