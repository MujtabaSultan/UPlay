package com.stories.stories.models;

import lombok.Data;

@Data
public class UserDto {

    private String userName;
    private String emailAddress;
    private String password;

    private Profile profile;

    private boolean accountVerified;
    private boolean isActivated;

    private String firstName;

    private String lastName;

    private String profileDescription;
    private User user;
    private Image image;
}
