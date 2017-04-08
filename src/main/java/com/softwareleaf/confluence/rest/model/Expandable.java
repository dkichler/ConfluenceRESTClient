package com.softwareleaf.confluence.rest.model;

/**
 *
 */
public enum Expandable {

    SPACE("space"),
    BODY("body"),
    STORAGE("storage"),
    VIEW("view"),
    DESCRIPTION("description"),
    COTNAINER("container"),
    ANCESTORS("ancestors"),
    VERSION("version"),
    HISTORY("history"),
    PREVIOUS_VERSION("previousVersion"),
    NEXT_VERSION("nextVersion"),
    LAST_UPDATE("lastUpdate");

    private final String value;

    Expandable(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }

}
