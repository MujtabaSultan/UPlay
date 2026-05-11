package com.stories.stories.models;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class ForgotPasswordDto {
    private String emailAddress;
}
