package uk.gov.cshr.domain;

import javax.persistence.Entity;
import javax.persistence.OneToOne;

@Entity
public class IdentityRole {

    @OneToOne(optional = false)
    private Identity identity;

    @OneToOne(optional = false)
    private Role role;

    public Identity getIdentity() {
        return identity;
    }

    public void setIdentity(Identity identity) {
        this.identity = identity;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
