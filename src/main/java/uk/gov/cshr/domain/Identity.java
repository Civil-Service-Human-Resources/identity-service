package uk.gov.cshr.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.*;
import javax.validation.constraints.Email;
import java.io.Serializable;
import java.time.Instant;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Slf4j
public class Identity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, length = 36)
    private String uid;

    @Column(unique = true, length = 150)
    @Email
    private String email;

    @Column(length = 100)
    private String password;

    private boolean active;

    private boolean locked;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "identity_role",
            joinColumns = @JoinColumn(name = "identity_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id")
    )
    private Set<Role> roles;

    private Instant lastLoggedIn;

    private boolean deletionNotificationSent;

    @Column
    private String agencyTokenUid;

    public Identity() {
    }

    public Identity(String uid, String email, String password, boolean active, boolean locked, Set<Role> roles, Instant lastLoggedIn, boolean deletionNotificationSent, boolean emailRecentlyUpdated) {
        this.uid = uid;
        this.email = email;
        this.password = password;
        this.active = active;
        this.roles = roles;
        this.locked = locked;
        this.lastLoggedIn = lastLoggedIn;
        this.deletionNotificationSent = deletionNotificationSent;
    }

    public Identity(String uid, String email, String password, boolean active, boolean locked, Set<Role> roles, Instant lastLoggedIn, boolean deletionNotificationSent, boolean emailRecentlyUpdated, String agencyTokenUid) {
        this.uid = uid;
        this.email = email;
        this.password = password;
        this.active = active;
        this.roles = roles;
        this.locked = locked;
        this.lastLoggedIn = lastLoggedIn;
        this.deletionNotificationSent = deletionNotificationSent;
        this.agencyTokenUid = agencyTokenUid;
    }

    @JsonIgnore
    public void removeRoles(Collection<String> roleNamesToRemove) {
        log.info(String.format("Removing roles: %s", roleNamesToRemove));
        Set<Role> newRoles = this.getRoles().stream().filter(role -> !roleNamesToRemove.contains(role.getName())).collect(Collectors.toSet());
        this.setRoles(newRoles);
    }

    @JsonIgnore
    public boolean hasAnyRole(Collection<String> rolesToCheck) {
        return this.roles.stream().anyMatch(r -> rolesToCheck.contains(r.getName()));
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUid() {
        return uid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public Instant getLastLoggedIn() {
        return lastLoggedIn;
    }

    public void setLastLoggedIn(Instant lastLoggedIn) {
        this.lastLoggedIn = lastLoggedIn;
    }

    public boolean isDeletionNotificationSent() {
        return deletionNotificationSent;
    }

    public void setDeletionNotificationSent(boolean deletionNotificationSent) {
        this.deletionNotificationSent = deletionNotificationSent;
    }

    public String getAgencyTokenUid() {
        return agencyTokenUid;
    }

    public void setAgencyTokenUid(String agencyTokenUid) {
        this.agencyTokenUid = agencyTokenUid;
    }

    @JsonIgnore
    public String getDomain() {
        return email.substring(email.indexOf('@') + 1);
    }

    @Override
    public String toString() {
        return "Identity{" +
                "id=" + id +
                ", uid='" + uid + '\'' +
                ", active=" + active +
                ", locked=" + locked +
                ", agencyTokenUid=" + agencyTokenUid +
                '}';
    }
}
