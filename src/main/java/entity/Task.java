package entity;

import java.util.ArrayList;
import java.util.List;

public class Task {
    private java.lang.Long id;
    private String title;
    private String description;
    private Long assignedUserId;

    private List<Tag> tags = new ArrayList<>();

    public Task() {
    }

    public Task(Long id, String title, String description, Long assignedUserId, List<Tag> tags) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.assignedUserId = assignedUserId;
        this.tags = tags;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getAssignedUserId() {
        return assignedUserId;
    }

    public void setAssignedUserId(Long assignedUserId) {
        this.assignedUserId = assignedUserId;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }

    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", assignedUserId=" + assignedUserId +
                ", tags=" + tags +
                '}';
    }
}
