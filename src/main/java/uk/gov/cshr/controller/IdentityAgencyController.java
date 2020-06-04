package uk.gov.cshr.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.cshr.domain.Identity;
import uk.gov.cshr.dto.IdentityAgencyDTO;
import uk.gov.cshr.repository.IdentityRepository;

import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/agencyToken")
public class IdentityAgencyController {

    private IdentityRepository identityRepository;

    @Autowired
    public IdentityAgencyController(IdentityRepository identityRepository) {
        this.identityRepository = identityRepository;
    }

    @GetMapping(value = "/identity", params = "uid")
    public ResponseEntity<IdentityAgencyDTO> findByUid(@RequestParam String uid) {
        log.info("Getting agency token uid for user with uid " + uid);
        Optional<Identity> identity = identityRepository.findFirstByUid(uid);
        return identity
                .map(i -> ResponseEntity.ok(new IdentityAgencyDTO(i.getUid(), i.getAgencyTokenUid())))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

}
