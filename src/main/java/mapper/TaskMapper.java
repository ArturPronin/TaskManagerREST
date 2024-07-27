package mapper;

import dto.TaskDTO;
import entity.Task;
import org.mapstruct.Mapper;

@Mapper
public interface TaskMapper {
    TaskDTO toDTO(Task task);

    Task toEntity(TaskDTO taskDTO);
}
