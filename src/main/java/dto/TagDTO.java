package dto;

import java.util.ArrayList;
import java.util.List;

public class TagDTO {

    private Long id;
    private String name;
    private List<TaskDTO> tasks = new ArrayList<>();

    public TagDTO() {
    }

    public TagDTO(Long id, String name, List<TaskDTO> tasks) {
        this.id = id;
        this.name = name;
        this.tasks = tasks;
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

    public List<TaskDTO> getTasks() {
        return tasks;
    }

    public void setTasks(List<TaskDTO> tasks) {
        this.tasks = tasks;
    }

    @Override
    public String toString() {
        return "TagDTO{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", tasks=" + tasks +
                '}';
    }
}
