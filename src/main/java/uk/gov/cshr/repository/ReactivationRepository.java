package uk.gov.cshr.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.cshr.domain.Reactivation;
import uk.gov.cshr.domain.ReactivationStatus;

import java.util.Date;
import java.util.Optional;

@Repository
public interface ReactivationRepository extends CrudRepository<Reactivation, Long> {

    Optional<Reactivation> findFirstByCodeAndReactivationStatusEquals(String code, ReactivationStatus reactivationStatus);

    boolean existsByCodeAndReactivationStatusEquals(String code, ReactivationStatus reactivationStatus);

    boolean existsByEmailAndReactivationStatusEqualsAndRequestedAtAfter(String email, ReactivationStatus reactivationStatus, Date requestedAt);

}
