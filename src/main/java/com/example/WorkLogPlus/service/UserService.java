package com.example.WorkLogPlus.service;

import java.util.List;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.WorkLogPlus.config.LoginUser;
import com.example.WorkLogPlus.dto.UserDto;
import com.example.WorkLogPlus.entity.Role;
import com.example.WorkLogPlus.entity.User;
import com.example.WorkLogPlus.form.UserForm;
import com.example.WorkLogPlus.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	
	
	
//	ユーザ登録
	@Transactional
	public User registerUser(UserForm form) {
		if(userRepository.findByEmail(form.getEmail()).isPresent()) {
			throw new RuntimeException("このメールアドレスは既に登録されています。");
		}
		
		User user = new User();
		
		user.setUsername(form.getUsername());;
		user.setEmail(form.getEmail());
		String encodedPassword = passwordEncoder.encode(form.getPassword());
		user.setPassword(encodedPassword);
		user.setRole(Role.GENERAL);
		
		return userRepository.save(user);
	}
	
	
	
//	全員ユーザーを取得
	public List<UserDto> getAllUsers(){
		
		List<User> users = userRepository.findAll();
		
		return users.stream().map(user -> new UserDto(user.getId(), user.getUsername())).toList();
	}



//※実装※　DBからメールアドレスでユーザーを検索
	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		
		User user = userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("ユーザーが見つかりません: " + email));
		
		return new LoginUser(user);
	}
}
