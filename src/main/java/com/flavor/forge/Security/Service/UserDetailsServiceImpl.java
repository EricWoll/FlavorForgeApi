package com.flavor.forge.Security.Service;

import com.flavor.forge.Model.ERole;
import com.flavor.forge.Model.User;
import com.flavor.forge.Repo.UserRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepo userRepo;

    private Logger logger = LoggerFactory.getLogger(UserDetailsServiceImpl.class);

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        User user = userRepo.findByUsername(username)
                .orElseThrow(()-> {
                    logger.info("Username not Found!");
                    return new UsernameNotFoundException("Username Not Found!");
                });

        System.out.println("Authenticated user: " + user.getUsername());
        System.out.println("Authenticated user: " + user.getRole());
        System.out.println("Authorities: " + user.getAuthorities());

        return user;
    }
}
