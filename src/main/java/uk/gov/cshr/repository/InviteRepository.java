package uk.gov.cshr.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.cshr.domain.Invite;

@Repository
public interface InviteRepository extends CrudRepository<Invite, Long> {

    Invite findByForEmail(String forEmail);

    Invite findByCode(String code);

    boolean existsByCode(String code);

}