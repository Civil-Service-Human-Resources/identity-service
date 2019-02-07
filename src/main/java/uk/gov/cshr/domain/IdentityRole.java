package uk.gov.cshr.domain;

import org.codehaus.jackson.annotate.JsonIgnore;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import java.util.Objects;

@Entity
public class IdentityRole {

    @JsonIgnore
    @EmbeddedId
    private IdentityRoleIdentity id;

    public IdentityRole(Long identityId, Long roleId) {
        this.id = new IdentityRoleIdentity(identityId, roleId);
    }

    public IdentityRoleIdentity getId() {
        return id;
    }

    public void setId(IdentityRoleIdentity id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IdentityRole that = (IdentityRole) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "IdentityRole{" +
                "id=" + id +
                '}';
    }
}
