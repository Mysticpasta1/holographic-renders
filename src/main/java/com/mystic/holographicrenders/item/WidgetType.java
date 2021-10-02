package com.mystic.holographicrenders.item;

public enum WidgetType {
    Blank("Blank"),
    Clock("Clock");

    private final String text;

    WidgetType(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }

    public static WidgetType fromId(int id) {
        return values()[id];
    }
}
