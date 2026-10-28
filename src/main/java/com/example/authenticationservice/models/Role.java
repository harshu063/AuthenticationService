package com.example.authenticationservice.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Column;
import lombok.Getter;
import lombok.Setter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;


@Entity
@Getter
@Setter
@JsonDeserialize(as = Role.class)
public class Role extends BaseModel {
    @Column(nullable = false, unique = true)
    private String role;
}
