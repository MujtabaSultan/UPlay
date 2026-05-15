package com.stories.stories.config;

import com.stories.stories.models.Profile;
import com.stories.stories.models.User;
import com.stories.stories.repositories.UserRepository;
import com.stories.stories.security.Hasher;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final Hasher hasher;

    public DataSeeder(UserRepository userRepository, Hasher hasher) {
        this.userRepository = userRepository;
        this.hasher = hasher;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return;
        }

        User user1 = new User();
        user1.setUserName("sara");
        user1.setEmailAddress("sara@example.com");
        user1.setPassword(hasher.passwordEncoder().encode("password123"));
        user1.setAccountVerified(true);
        user1.setActivated(true);
        user1.setAdmin(true);

        Profile profile1 = new Profile();
        profile1.setFirstName("Sara");
        profile1.setLastName("Noor");
        profile1.setProfileDescription("Tournament organizer.");
        user1.setProfile(profile1);

        User user2 = new User();
        user2.setUserName("omar");
        user2.setEmailAddress("omar@example.com");
        user2.setPassword(hasher.passwordEncoder().encode("password123"));
        user2.setAccountVerified(true);
        user2.setActivated(true);

        Profile profile2 = new Profile();
        profile2.setFirstName("Omar");
        profile2.setLastName("Ali");
        profile2.setProfileDescription("Competitive ping pong player.");
        user2.setProfile(profile2);

        User user3 = new User();
        user3.setUserName("lina");
        user3.setEmailAddress("lina@example.com");
        user3.setPassword(hasher.passwordEncoder().encode("password123"));
        user3.setAccountVerified(true);
        user3.setActivated(true);

        Profile profile3 = new Profile();
        profile3.setFirstName("Lina");
        profile3.setLastName("Khaled");
        profile3.setProfileDescription("Weekend bracket player.");
        user3.setProfile(profile3);

        User user4 = new User();
        user4.setUserName("khalid");
        user4.setEmailAddress("khalid@example.com");
        user4.setPassword(hasher.passwordEncoder().encode("password123"));
        user4.setAccountVerified(true);
        user4.setActivated(true);

        Profile profile4 = new Profile();
        profile4.setFirstName("Khalid");
        profile4.setLastName("Hassan");
        profile4.setProfileDescription("Player 4");
        user4.setProfile(profile4);

        User user5 = new User();
        user5.setUserName("nadia");
        user5.setEmailAddress("nadia@example.com");
        user5.setPassword(hasher.passwordEncoder().encode("password123"));
        user5.setAccountVerified(true);
        user5.setActivated(true);

        Profile profile5 = new Profile();
        profile5.setFirstName("Nadia");
        profile5.setLastName("Karim");
        profile5.setProfileDescription("Player 5");
        user5.setProfile(profile5);

        User user6 = new User();
        user6.setUserName("yusuf");
        user6.setEmailAddress("yusuf@example.com");
        user6.setPassword(hasher.passwordEncoder().encode("password123"));
        user6.setAccountVerified(true);
        user6.setActivated(true);

        Profile profile6 = new Profile();
        profile6.setFirstName("Yusuf");
        profile6.setLastName("Malik");
        profile6.setProfileDescription("Player 6");
        user6.setProfile(profile6);

        User user7 = new User();
        user7.setUserName("rania");
        user7.setEmailAddress("rania@example.com");
        user7.setPassword(hasher.passwordEncoder().encode("password123"));
        user7.setAccountVerified(true);
        user7.setActivated(true);

        Profile profile7 = new Profile();
        profile7.setFirstName("Rania");
        profile7.setLastName("Saad");
        profile7.setProfileDescription("Player 7");
        user7.setProfile(profile7);

        User user8 = new User();
        user8.setUserName("tarek");
        user8.setEmailAddress("tarek@example.com");
        user8.setPassword(hasher.passwordEncoder().encode("password123"));
        user8.setAccountVerified(true);
        user8.setActivated(true);

        Profile profile8 = new Profile();
        profile8.setFirstName("Tarek");
        profile8.setLastName("Fawzi");
        profile8.setProfileDescription("Player 8");
        user8.setProfile(profile8);

        userRepository.save(user1);
        userRepository.save(user2);
        userRepository.save(user3);
        userRepository.save(user4);
        userRepository.save(user5);
        userRepository.save(user6);
        userRepository.save(user7);
        userRepository.save(user8);
    }
}
