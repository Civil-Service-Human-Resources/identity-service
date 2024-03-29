package uk.gov.cshr.dto;

import uk.gov.cshr.domain.Identity;
import uk.gov.cshr.domain.Role;

import java.util.HashSet;
import java.util.Set;

public class IdentityDTO {
    private String username;
    private String uid;
    private Set<String> roles = new HashSet<>();

    public IdentityDTO(Identity identity) {
        this.username = identity.getEmail();
        this.uid = identity.getUid();
        Set<Role> identityRoles = identity.getRoles();
        if (identityRoles != null) {
            identityRoles.forEach(role -> this.roles.add(role.getName()));
        }
    }

    public IdentityDTO(String username, String uid, Set<Role> roles) {
        this.username = username;
        this.uid = uid;
        roles.forEach(role -> this.roles.add(role.getName()));
    }

    public IdentityDTO(String username, String uid) {
        this.username = username;
        this.uid = uid;
    }

    public String getUsername() {
        return username;
    }

    public String getUid() {
        return uid;
    }

    public Set<String> getRoles() {
        return roles;
    }
}
