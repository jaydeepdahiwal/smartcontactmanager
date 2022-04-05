package com.smart.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.smart.entities.User;
import com.smart.repository.UserRepository;

public class UserDetailsServiceImpl implements UserDetailsService{

	@Autowired
	private UserRepository userRepository;
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		//fetching user from database
		User user=userRepository.getUserByUserName(username);
		//checking whether user is null or not
		if(user==null) {
			throw new UsernameNotFoundException("User not Found");
		}
		//sending fetched details to CustomUserDetails for validations
		CustomUserDetails customUserDetails= new CustomUserDetails(user);
		return customUserDetails;
	}

}
