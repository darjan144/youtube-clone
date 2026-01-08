package isa.vezbe1.spring_boot_example.dto;

import isa.vezbe1.spring_boot_example.model.VideoTag;

public class VideoTagDTO {

    private Long id;
    private String name;

    public VideoTagDTO() {
    }

    public VideoTagDTO(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public VideoTagDTO(VideoTag tag) {
        this.id = tag.getId();
        this.name = tag.getName();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}