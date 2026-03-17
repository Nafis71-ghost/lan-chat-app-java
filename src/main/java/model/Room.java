package model;

public class Room {
    private String name;
    private String createdBy;

    public Room() {
    }

    public Room(String name, String createdBy) {
        this.name = name;
        this.createdBy = createdBy;
    }

    public String getName() {
        return name;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
}