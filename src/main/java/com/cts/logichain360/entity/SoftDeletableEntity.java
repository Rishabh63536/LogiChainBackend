package com.cts.logichain360.entity;


import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

@MappedSuperclass
@Getter @Setter
public abstract class SoftDeletableEntity {

    @Column(name = "is_deleted", nullable = false)
    protected boolean isDeleted = false;
}