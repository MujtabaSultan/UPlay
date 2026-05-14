package com.stories.stories.security;
import com.stories.stories.models.User;
import com.stories.stories.services.CircleSolver;
import com.stories.stories.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class MyUserDetailsService implements UserDetailsService {
    private CircleSolver userService;
    @Autowired
    public void setCircleSolver(CircleSolver userService){
        this.userService=userService;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        User user = userService.loadUsingEmail(email);
        return new MyUserDetails(user);

    }

}