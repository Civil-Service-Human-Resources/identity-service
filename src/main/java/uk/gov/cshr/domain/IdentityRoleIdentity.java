package uk.gov.cshr.domain;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class IdentityRoleIdentity implements Serializable {

    @NotNull
    @Column(nullable = false)
    private Long identity_id;

    @NotNull
    @Column(nullable = false)
    private Long role_id;

    public IdentityRoleIdentity() {
    }

    public IdentityRoleIdentity(@NotNull Long identity_id, @NotNull Long role_id) {
        this.identity_id = identity_id;
        this.role_id = role_id;
    }

    public Long getIdentity_id() {
        return identity_id;
    }

    public void setIdentity_id(Long identity_id) {
        this.identity_id = identity_id;
    }

    public Long getRole_id() {
        return role_id;
    }

    public void setRole_id(Long role_id) {
        this.role_id = role_id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IdentityRoleIdentity that = (IdentityRoleIdentity) o;
        return Objects.equals(identity_id, that.identity_id) &&
                Objects.equals(role_id, that.role_id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(identity_id, role_id);
    }
}
