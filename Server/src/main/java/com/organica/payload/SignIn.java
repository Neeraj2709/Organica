package com.organica.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@Data
@ToString
public class SignIn {

    private String email;   // Changed from Email to email
    private String password; // Changed from Password to password
//    private String jwt;

}
