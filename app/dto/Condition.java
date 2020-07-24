package dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 用于排序选择的 form
 */
public class Condition {

    @Getter
    @Setter
    private String sort;

    @Getter
    @Setter
    private boolean ascending;
}
