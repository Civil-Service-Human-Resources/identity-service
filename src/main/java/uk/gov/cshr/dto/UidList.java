package uk.gov.cshr.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
@AllArgsConstructor
public class UidList {
    @NotEmpty
    private List<String> uids;
}
