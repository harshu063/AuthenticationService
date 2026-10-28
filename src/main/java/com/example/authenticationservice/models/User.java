package com.example.authenticationservice.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Column;
import lombok.Getter;
import lombok.Setter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@JsonDeserialize(as = User.class)
public class User extends BaseModel {
    @Column(nullable = false, unique = true, length = 320)
    private String email;
    @Column(nullable = false)
    private String password;
    @ManyToMany(fetch = FetchType.EAGER)
    @JsonIgnore
    private Set<Role> roles = new HashSet<>();

}
