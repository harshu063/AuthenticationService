package dtos;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.bind.annotation.ResponseStatus;

@Getter
@Setter
public class LoginResponseDto {
   private RequestStatus requestStatus;


}
