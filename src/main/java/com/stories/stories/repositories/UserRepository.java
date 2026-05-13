package com.stories.stories.repositories;

import com.stories.stories.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User,Long> {
    User findByEmailAddress(String email);

    java.util.Optional<User> findByUserNameIgnoreCase(String userName);
}
