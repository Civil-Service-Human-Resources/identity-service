package uk.gov.cshr.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BatchProcessResponse {
    private List<String> successfulIds = Collections.emptyList();
    private List<String> failedIds = Collections.emptyList();
}
