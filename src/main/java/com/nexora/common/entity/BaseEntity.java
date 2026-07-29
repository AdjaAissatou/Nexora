package com.nexora.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Superclasse technique portant les colonnes d'audit communes a toutes les
 * entites metier (tracabilite, soft-delete). Correspond a la classe
 * {@code BaseEntity} du diagramme de classes.
 *
 * <p>Chaque entite concrete definit sa propre cle primaire (idUtilisateur,
 * idEspace, ...) ; cette classe n'apporte que les champs transverses afin de
 * respecter le principe DRY sans imposer de strategie d'heritage JPA.</p>
 */
@Getter
@Setter
@MappedSuperclass
public abstract class BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /** Soft-delete : {@code false} = archive / desactive. */
    @Column(name = "actif", nullable = false)
    private boolean actif = true;

    @Column(name = "created_by", updatable = false, length = 120)
    private String createdBy;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
