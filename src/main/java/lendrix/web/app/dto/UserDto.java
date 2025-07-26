package lendrix.web.app.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class UserDto {
    private String firstname;
    private String lastname;

    private String username;

    private LocalDate dob; //date of Birth
    private String email;
    private String password;



}
