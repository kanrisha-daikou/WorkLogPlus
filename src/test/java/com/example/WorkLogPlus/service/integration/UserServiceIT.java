package com.example.WorkLogPlus.service.integration;

import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import com.example.WorkLogPlus.dto.UserDto;
import com.example.WorkLogPlus.entity.Role;
import com.example.WorkLogPlus.entity.User;
import com.example.WorkLogPlus.factory.UserFactory;
import com.example.WorkLogPlus.form.UserForm;
import com.example.WorkLogPlus.repository.UserRepository;
import com.example.WorkLogPlus.service.UserService;

@SpringBootTest
@Transactional
class UserServiceIT {
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	@Autowired
	private UserFactory userFactory;
	
	
	
	@Test
	void registerUser_ユーザーが正しくDBに保存され暗号化されていること() {
		
		UserForm form = new UserForm();
		form.setUsername("新規ユーザー");
		form.setEmail("newuser@example.com");
		form.setPassword("Password123");
		
		//【実行】
		User result = userService.registerUser(form);
		
		
		//【検証】
		assertThat(result.getId()).isNotNull()	; // IDが自動採番されているか
		assertThat(result.getRole()).isEqualTo(Role.GENERAL); // ロールがGENERALか
		
		//	DBからデータを引っ張ってきて、パスワードが暗号化されているか確かめる
		User savedUser = userRepository.findById(result.getId()).orElseThrow();		
		assertThat(passwordEncoder.matches("Password123", savedUser.getPassword())).isTrue();
	}
	
	
	
	@Test
	void getAllUsers_DBに新しく入れたユーザーが確実に含まれて取得できること() {
		
		userFactory.create("テスト専用ユーザ＝ボルバルザーク");
		
		//【実行】
		List<UserDto> dtoList = userService.getAllUsers();
		
		
		//【検証】
		boolean result = dtoList.stream().anyMatch(dto -> dto.username().equals("テスト専用ユーザ＝ボルバルザーク"));
		assertThat(result).isTrue(); // 確かに含まれていることを検証
	}
	
	
	
	@Test
	void loadUserByUsername_存在するメアドでLoginUserが正しく返ること() {
		
		User user = new User();
		user.setUsername("ログイン太郎");
		user.setEmail("login@example.com");
		user.setPassword("hash_pass");
		user.setRole(Role.GENERAL);
		userRepository.save(user);
		
		//【実行】
		UserDetails userDetails = userService.loadUserByUsername("login@example.com");
		
		
		//【検証】（Spring Security のルール通りに実装できていれば成功!?）
		//	返ってきた中身（LoginUser）が、DBのデータと一致しているか
		assertThat(userDetails).isNotNull();
		assertThat(userDetails.getUsername()).isEqualTo("login@example.com");
		assertThat(userDetails.getPassword()).isEqualTo("hash_pass");
	}
	
	
	
//	@Test
//	void test() {
//	
//	}
}
