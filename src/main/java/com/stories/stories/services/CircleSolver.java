package com.stories.stories.services;

import com.stories.stories.models.User;
import com.stories.stories.repositories.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class CircleSolver {

   final  UserRepository userRepository ;
   public CircleSolver (UserRepository userRepository){
       this.userRepository=userRepository;
   }
   public User loadUsingEmail (String email){
       return userRepository.findByEmailAddress(email);
   }
}
