package com.stories.stories.models;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Getter
@Setter
@ToString(exclude = {"password","profile"})
@NoArgsConstructor
@AllArgsConstructor
@Table(name="users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private boolean isAdmin;

    private String userName;
    @Column(unique = true)
    private String emailAddress;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;
    @OneToOne(
            cascade = CascadeType.ALL,fetch = FetchType.EAGER
    )
    @JoinColumn(name = "profile_id",referencedColumnName = "id")
    private Profile profile;

    private boolean accountVerified;
    private boolean isActivated;

    @JsonIgnore
    public String getPassword(){
        return password;
    }




}
