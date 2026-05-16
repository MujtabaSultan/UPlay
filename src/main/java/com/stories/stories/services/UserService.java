package com.stories.stories.services;

import com.cloudinary.Cloudinary;
import com.stories.stories.mailing.AccountPasswordResetEmailContext;
import com.stories.stories.mailing.AccountVerificationEmailContext;
import com.stories.stories.mailing.EmailService;
import com.stories.stories.models.*;
import com.stories.stories.repositories.ImageRepository;
import com.stories.stories.repositories.ProfileRepository;
import com.stories.stories.repositories.UserRepository;
import com.stories.stories.security.JWTUtils;
import com.stories.stories.security.MyUserDetails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.Map;

@Service
public class UserService {

    @Value("${site.base.url.https}")
    private String baseurl;
    private UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private ProfileRepository profileRepository;
    private final SecureTokenService secureTokenService;
    private EmailService emailService;
    private final AuthenticationManager authenticationManager;
    private final JWTUtils jwtUtils;
    private final Cloudinary cloudinary;
    private final ImageRepository imageRepository;



    public UserService(UserRepository userRepository
            ,PasswordEncoder passwordEncoder,SecureTokenService secureTokenService
            ,EmailService emailService,Cloudinary cloudinary,
                       ImageRepository imageRepository,
                       JWTUtils jwtUtils,
                       ProfileRepository profileRepository,
                       AuthenticationManager authenticationManager){
        this.passwordEncoder=passwordEncoder;
        this.emailService=emailService;
        this.profileRepository=profileRepository;
        this.userRepository=userRepository;
        this.secureTokenService=secureTokenService;
        this.authenticationManager=authenticationManager;
        this.jwtUtils=jwtUtils;
        this.imageRepository=imageRepository;
        this.cloudinary=cloudinary;
    }

    public void sendConfirmationEmail(User user) {
        SecureToken secureToken = secureTokenService.createToken();
        secureToken.setUser(user);
        secureTokenService.saveSecureToken(secureToken);
        AccountVerificationEmailContext context = new AccountVerificationEmailContext();
        context.init(user);
        context.setToken(secureToken.getToken());
        context.buildVerificationUrl(baseurl, secureToken.getToken());

        System.out.println("sending email to " + user.getEmailAddress());
        emailService.sendMail(context);
    }

    public User createUser(UserDto userObj){

        User existingUser = userRepository.findByEmailAddress(userObj.getEmailAddress());


        User newUser = new User();
        String hashedPass = passwordEncoder.encode(userObj.getPassword());
        newUser.setPassword(hashedPass);
        newUser.setEmailAddress(userObj.getEmailAddress());
        newUser.setUserName(userObj.getUserName());


        Profile inputProfile = new Profile();
        inputProfile.setFirstName(userObj.getFirstName());
        inputProfile.setProfileDescription(userObj.getProfileDescription());
        inputProfile.setLastName(userObj.getLastName());

        newUser.setProfile(inputProfile);
        newUser.setActivated(true);
        userRepository.save(newUser);
        sendConfirmationEmail(newUser);

        return newUser;
    }

    public void validate(String token) {
        SecureToken secureToken = secureTokenService.findByToken(token);
        User user = secureToken.getUser();
        user.setAccountVerified(true);
        userRepository.save(user);
    }

    public User findUserByEmail(String email){
        User user = userRepository.findByEmailAddress(email);
        return user;
    }
    public void resetPassword(String email) {
        System.out.println("service got this oooooooooo" +email);
        SecureToken secureToken = secureTokenService.createToken();
        User user = userRepository.findByEmailAddress(email);
        System.out.println(user);
        System.out.println("service found user ====> " + user.getUserName());
        secureToken.setUser(user);
        secureTokenService.saveSecureToken(secureToken);
        AccountPasswordResetEmailContext context = new AccountPasswordResetEmailContext();
        context.init(user);
        context.setToken(secureToken.getToken());
        context.buildResetUrl(baseurl, secureToken.getToken());

        System.out.println("sending email to " + user.getEmailAddress());
        emailService.sendMail(context);
    }

    public void changePassword(String oldPass, String newPass) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();
        MyUserDetails myUserDetails = (MyUserDetails) authentication.getPrincipal();
        System.out.println("the useeeer");
        User user = myUserDetails.getUser();
        System.out.println(user);
        if (newPass == null) {
            System.out.println("no pass was provided");
        }
        try {
            if (passwordEncoder.matches(oldPass, user.getPassword())) {

                user.setPassword(passwordEncoder.encode(newPass));
                userRepository.save(user);
            }
            else {
                System.out.println("wrong old password" + oldPass + "old is " + user.getPassword());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public ResponseEntity<?> loginUser(LoginRequest loginRequest) {
        try {
            System.out.println("this persooooooooon" + loginRequest.getEmail());
            Authentication authentication = authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(loginRequest
                            .getEmail(), loginRequest.getPassword()));
            System.out.println(authentication);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            MyUserDetails myUserDetails = (MyUserDetails) authentication.getPrincipal();
            final String JWT = jwtUtils.generateJwtToken(myUserDetails);
            return ResponseEntity.ok(JWT);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok("password username/email incorrect");
        }
    }

    //self soft delete
    public void softDelete() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        System.out.println(authentication.getPrincipal());
        MyUserDetails myUserDetails = (MyUserDetails) authentication.getPrincipal();
        User user = myUserDetails.getUser();
        User currentUser = getUser();
        if(user.getId().equals(currentUser.getId())||currentUser.isAdmin()){

            user.setActivated(false);
            //user.setAccountVerified(false);
            userRepository.save(user);
        }
        else{
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "cant delete the user");

        }
    }


    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }
    public void otherSoftDelete(Long id) {
        User user = getUserOrThrow(id);
        User currentUser = getUser();
        if(user.getId().equals(currentUser.getId())||currentUser.isAdmin()){

            user.setActivated(false);
            //user.setAccountVerified(false);
            userRepository.save(user);
        }
        else{
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "cant delete the user");

        }
    }

    public void resetPasswordActivator(String token, ForgotPasswordForm form) {
        System.out.println("currently goin through the reset validator iiiiiiiiiiiiiiii");
        SecureToken secureToken = secureTokenService.findByToken(token);
        System.out.println("zzzzzzzzzzzzzzzzz");
        System.out.println(secureToken);
        User user = secureToken.getUser();
        System.out.println(user);
        System.out.println("bbbbbbbbbbbbb");
        System.out.println(form);
        user.setPassword(passwordEncoder.encode(form.getPassword()));
        userRepository.save(user);

    }
    public User getUser() {
        return ((MyUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUser();
    }

    public User updateProfile(String firstName, String lastName, String profileDescription) {
        User user = getUser();
        Profile profile = user.getProfile();

        if (firstName != null) profile.setFirstName(firstName);
        if (lastName != null) profile.setLastName(lastName);
        if (profileDescription != null) profile.setProfileDescription(profileDescription);

        profileRepository.save(profile);
        return user;
    }

    public Image uploadProfileImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Image file is required");
        }

        User user = getUser();
        Profile profile = user.getProfile();
        if (profile == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User profile does not exist");
        }

        try {
            Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), Map.of(
                    "folder", "tournaments/profile-images",
                    "resource_type", "image"
            ));

            String publicId = String.valueOf(uploadResult.get("public_id"));
            String secureUrl = String.valueOf(uploadResult.get("secure_url"));

            Image image = profile.getImage();
            if (image == null) {
                image = new Image();
                image.setProfile(profile);
            }

            image.setName(publicId);
            image.setUrl(secureUrl);
            image = imageRepository.save(image);
            profile.setImage(image);
            userRepository.save(user);
            return image;
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Failed to upload image to Cloudinary");
        }
    }

}
