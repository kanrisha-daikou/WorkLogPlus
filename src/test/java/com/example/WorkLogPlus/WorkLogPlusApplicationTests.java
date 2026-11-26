package com.example.WorkLogPlus;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.WorkLogPlus.repository.UserRepository;

@SpringBootTest
class WorkLogPlusApplicationTests {

	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	@Test
	void 既存ユーザーのパスワードをハッシュ化するテスト() {
		
		userRepository.findByEmail("nana@nana").ifPresent(user -> {
			
			String encodedPassword = passwordEncoder.encode("777777na");
			
			user.setPassword(encodedPassword);
			userRepository.save(user);
			
			System.out.println("【成功】Springが計算したハッシュ値: " + encodedPassword);
		});
	}

}
