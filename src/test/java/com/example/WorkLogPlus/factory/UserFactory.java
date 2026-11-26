package com.example.WorkLogPlus.factory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.WorkLogPlus.entity.Role;
import com.example.WorkLogPlus.entity.User;
import com.example.WorkLogPlus.repository.UserRepository;

@Component
public class UserFactory {

	@Autowired
	private UserRepository userRepository;
	
	
	
	public User create(String username) {
		
		return create(username, Role.GENERAL);
	}
	
	
	
	public User create(String username, Role role) {
		
		User user = new User();
		user.setUsername(username);
		user.setEmail(username + "@example.com");
		user.setPassword("password");
		user.setRole(role);
		
		return userRepository.save(user);
	}
	
	
	
	public User createUnsaved(String username) {
		
		return createUnsaved(username, Role.GENERAL);
	}
	
	
	
	public User createUnsaved(String username, Role role) {
		
		User user = new User();
		user.setUsername(username);
		user.setEmail(username + "@example.com");
		user.setPassword("password");
		user.setRole(role);
		
		return user;
	}
}
