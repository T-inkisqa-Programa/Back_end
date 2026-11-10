package com.tinkisqa.iam.infrastructure.persistence.jpa;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

import com.tinkisqa.iam.domain.model.AccessStatus;
import com.tinkisqa.iam.domain.model.AccountStatus;
import com.tinkisqa.iam.domain.model.GenderIdentity;
import com.tinkisqa.iam.domain.model.IdentityStatus;
import com.tinkisqa.iam.domain.model.Role;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "accounts")
public class AccountJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "nombre_completo", nullable = false)
    private String nombreCompleto;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_status", nullable = false)
    private AccountStatus accountStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "identity_status", nullable = false)
    private IdentityStatus identityStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "access_status", nullable = false)
    private AccessStatus accessStatus;

    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified;

    @Column(name = "normas_aceptadas", nullable = false)
    private boolean normasAceptadas;

    @Column(name = "bio")
    private String bio;

    @Enumerated(EnumType.STRING)
    @Column(name = "genero")
    private GenderIdentity genero;

    @Column(name = "genero_personalizado")
    private String generoPersonalizado;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "account_interests", joinColumns = @JoinColumn(name = "account_id"))
    @Column(name = "interest")
    private Set<String> intereses = new LinkedHashSet<>();

    @Column(name = "avatar_uri")
    private String avatarUri;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public AccountStatus getAccountStatus() {
        return accountStatus;
    }

    public void setAccountStatus(AccountStatus accountStatus) {
        this.accountStatus = accountStatus;
    }

    public IdentityStatus getIdentityStatus() {
        return identityStatus;
    }

    public void setIdentityStatus(IdentityStatus identityStatus) {
        this.identityStatus = identityStatus;
    }

    public AccessStatus getAccessStatus() {
        return accessStatus;
    }

    public void setAccessStatus(AccessStatus accessStatus) {
        this.accessStatus = accessStatus;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public boolean isNormasAceptadas() {
        return normasAceptadas;
    }

    public void setNormasAceptadas(boolean normasAceptadas) {
        this.normasAceptadas = normasAceptadas;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public GenderIdentity getGenero() {
        return genero;
    }

    public void setGenero(GenderIdentity genero) {
        this.genero = genero;
    }

    public String getGeneroPersonalizado() {
        return generoPersonalizado;
    }

    public void setGeneroPersonalizado(String generoPersonalizado) {
        this.generoPersonalizado = generoPersonalizado;
    }

    public Set<String> getIntereses() {
        return intereses;
    }

    public void setIntereses(Set<String> intereses) {
        this.intereses = intereses == null ? new LinkedHashSet<>() : intereses;
    }

    public String getAvatarUri() {
        return avatarUri;
    }

    public void setAvatarUri(String avatarUri) {
        this.avatarUri = avatarUri;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
