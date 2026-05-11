package com.stories.stories.models;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@ToString()
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "profiles")
public class Profile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstName;

    private String lastName;

    private String profileDescription;

    @JsonIgnore
    @OneToOne(mappedBy = "profile", fetch = FetchType.EAGER)
    private User user;

    @OneToOne(cascade = CascadeType.ALL)
    private Image image;

}
