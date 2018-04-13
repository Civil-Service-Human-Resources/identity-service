package uk.gov.cshr.service.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import uk.gov.cshr.domain.Identity;
import uk.gov.cshr.repository.IdentityRepository;

@Service
public class IdentityService implements UserDetailsService {

    private IdentityRepository identityRepository;

    @Autowired
    public IdentityService(IdentityRepository identityRepository) {
        this.identityRepository = identityRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Identity identity = identityRepository.findFirstByActiveTrueAndEmailEquals(username);
        if (identity == null) {
            throw new UsernameNotFoundException("No user found with email address " + username);
        }
        return new IdentityDetails(identity);
    }
}