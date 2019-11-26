package org.arxing;

public enum ImportsType {
    IMPORT,
    EXPORT,
    PART,
    PART_OF;

    public String getOption() {
        switch (this) {
            case IMPORT:
                return "import";
            case EXPORT:
                return "export";
            case PART:
                return "part";
            case PART_OF:
                return "part of";
        }
        throw new IllegalStateException();
    }
}
