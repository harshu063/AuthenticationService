package models;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;
import tools.jackson.databind.annotation.JsonDeserialize;


@Entity
@Getter
@Setter
@JsonDeserialize(as = Role.class)
public class Role extends BaseModel {
    private String role;
}
