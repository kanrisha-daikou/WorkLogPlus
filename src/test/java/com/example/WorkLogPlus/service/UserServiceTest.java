package com.example.WorkLogPlus.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.WorkLogPlus.entity.User;
import com.example.WorkLogPlus.form.UserForm;
import com.example.WorkLogPlus.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
	
	@Mock
	private UserRepository userRepository;

	@Mock
	private PasswordEncoder passwordEncoder;
	
	@InjectMocks
	private UserService userService;
	
	
	
	@Test
	void registerUser_メアドが重複の場合_RuntimeException発生で保存されないこと() {
		
		UserForm form = new UserForm();
		form.setUsername("テスト太郎");
		form.setEmail("already@example.com");
		form.setPassword("password");
		
		when(userRepository.findByEmail("already@example.com")).thenReturn(Optional.of(new User()));
		
		
		//【実行＆検証】
		assertThatThrownBy(() -> userService.registerUser(form))
		.isInstanceOf(RuntimeException.class)
		.hasMessage("このメールアドレスは既に登録されています。");
		
		//【重要な検証】保存処理（save）が絶対に呼び出されていないこと
		verify(userRepository, never()).save(any(User.class));
	}
	
	
	
	@Test
	void loadUserByUsername_ユーザが無い場合_UsernameNotFoundExceptionが発生すること() {
		
		String notFoundEmail = "notfound@example.com";
		
		when(userRepository.findByEmail(notFoundEmail)).thenReturn(Optional.empty());
		
		
		//【実行＆検証】assertThatThrownBy でエラーを捕まえる
		assertThatThrownBy(() -> userService.loadUserByUsername(notFoundEmail))
		// 発生したエラーの種類が UsernameNotFoundException であること
		.isInstanceOf(UsernameNotFoundException.class)
		// エラーメッセージが本番コードと一致していること
		.hasMessage("ユーザーが見つかりません: " + notFoundEmail);
	}
	
	
	
//	@Test
//	void test() {
//		
//	}
}
