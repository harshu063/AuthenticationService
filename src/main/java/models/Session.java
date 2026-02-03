package models;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class Session extends BaseModel {
    private String token;
    @ManyToOne
    private User user;
    private Date expiringAt;
    @Enumerated(EnumType.ORDINAL)
    private String SessionStatus;

}
