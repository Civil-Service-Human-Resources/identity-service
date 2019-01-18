package uk.gov.cshr.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.cshr.domain.EmailUpdate;

import java.util.Optional;

public interface EmailUpdateRepository extends JpaRepository<EmailUpdate, Long> {

    Optional<EmailUpdate> findByCode(String code);
}
