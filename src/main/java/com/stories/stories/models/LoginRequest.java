package com.stories.stories.models;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class LoginRequest {
    String email;
    String password ;
    String username;
}
