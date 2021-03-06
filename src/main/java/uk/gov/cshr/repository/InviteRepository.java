package uk.gov.cshr.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.cshr.domain.Invite;
import uk.gov.cshr.domain.InviteStatus;

import java.util.Optional;

@Repository
public interface InviteRepository extends CrudRepository<Invite, Long> {

    Invite findByForEmail(String forEmail);

    Invite findByCode(String code);

    Optional<Invite> findByForEmailAndStatus(String email, InviteStatus status);

    boolean existsByCode(String code);

    boolean existsByForEmailAndStatus(String email, InviteStatus status);

    boolean existsByForEmailAndInviterIdIsNotNull(String email);
}