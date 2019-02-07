package uk.gov.cshr.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.cshr.domain.Identity;
import uk.gov.cshr.domain.IdentityRole;

@Repository
public interface IdentityRoleRepository extends CrudRepository<IdentityRole, Long> {
    void deleteByIdentity(Identity identity);
}
