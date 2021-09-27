package greencity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class PageableWithUnreadenNotificationsDto<T> {
    private List<T> page;
    private long totalElements;
    private int currentPage;
    private int totalPages;
    private long unreadenNotification;
}
