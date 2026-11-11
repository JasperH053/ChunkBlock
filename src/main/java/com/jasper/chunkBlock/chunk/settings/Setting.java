package com.jasper.chunkBlock.chunk.settings;

public class Setting {

    private String id;
    private String displayName;
    private String material;
    private int guiSlot;
    private boolean defaultValue;

    public Setting(String id, String displayName, String material) {
        this.id = id;
        this.displayName = displayName;
        this.material = material;
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public String getMaterial() { return material; }
    public int getGuiSlot() { return guiSlot; }
    public boolean isDefaultValue() { return defaultValue; }
}
