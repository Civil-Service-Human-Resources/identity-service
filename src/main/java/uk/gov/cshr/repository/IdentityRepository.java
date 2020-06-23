package uk.gov.cshr.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import uk.gov.cshr.domain.Identity;
import uk.gov.cshr.dto.IdentityDTO;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Repository
public interface IdentityRepository extends JpaRepository<Identity, Long> {

        Identity findFirstByActiveTrueAndEmailEquals(String email);

        Identity findFirstByEmailEquals(String email);

        Optional<Identity> findFirstByActiveFalseAndEmailEquals(String email);

        boolean existsByEmail(String email);

        Optional<Identity> findFirstByUid(String uid);

        @Query("select new uk.gov.cshr.dto.IdentityDTO(i.email, i.uid) " +
                "from Identity i")
        List<IdentityDTO> findAllNormalised();

        Long countByAgencyTokenUid(String uid);

        @Transactional
        @Modifying(flushAutomatically = true, clearAutomatically = true)
        @Query("UPDATE Identity SET agencyTokenUid = null, active = false WHERE agencyTokenUid IS NOT NULL AND agencyTokenUid = :agencyTokenUid")
        void removeAgencyToken(String agencyTokenUid);
}
