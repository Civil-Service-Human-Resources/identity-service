package uk.gov.cshr.domain;

import lombok.Data;

import javax.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Data
@Entity
public class EmailUpdate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String code = UUID.randomUUID().toString();

    private String email;

    @ManyToOne
    private Identity identity;
    private Instant timestamp;
}
