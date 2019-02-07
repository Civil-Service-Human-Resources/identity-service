package uk.gov.cshr.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.cshr.domain.IdentityRole;
import uk.gov.cshr.domain.IdentityRoleIdentity;

@Repository
public interface IdentityRoleRepository extends CrudRepository<IdentityRole, IdentityRoleIdentity> {

    @Modifying
    @Query("DELETE FROM IdentityRole i WHERE i.id.identity_id = ?1")
    void deleteById(Long identityId);
}
