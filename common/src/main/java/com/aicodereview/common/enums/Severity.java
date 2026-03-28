package com.aicodereview.common.enums;

public enum Severity {
    HIGH, MEDIUM, LOW;

    public String getLabel() {
        return this.name().toLowerCase();
    }
}