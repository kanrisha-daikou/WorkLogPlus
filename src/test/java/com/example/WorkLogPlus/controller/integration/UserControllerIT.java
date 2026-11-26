package com.example.WorkLogPlus.controller.integration;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.example.WorkLogPlus.factory.UserFactory;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UserControllerIT {
	
	@Autowired
	private MockMvc mockMvc;
	
	@Autowired
	private UserFactory userFactory;
	
	
	
	@Test
	void ユーザー登録画面が正常に表示されること() throws Exception {
		
		mockMvc.perform(get("/register"))
		.andExpect(status().isOk())
		.andExpect(view().name("register"))
		.andExpect(model().attributeExists("userForm"));
	}
	
	
	
	@Test
	void ユーザー登録_正しい入力値の場合は正常にDBに保存されログイン画面にリダイレクトされること() throws Exception {
		
		mockMvc.perform(post("/register")
				.param("username", "新規ゆで太郎")
				.param("email", "new-yudetarou@example.com")
				.param("password", "Pass1234")
				.with(csrf()))
		.andExpect(status().is3xxRedirection())
		.andExpect(redirectedUrl("/login"))
		.andExpect(flash().attributeExists("okMessage"));
	}
	
	
	
	@Test
	void ユーザー登録_入力エラーがある場合は登録画面に戻ること() throws Exception {
		
		mockMvc.perform(post("/register")
				.param("username", "") // ユーザー名を空っぽ
				.param("email", "invalid-email") // メール形式エラー
				.param("password", "abc") // 8文字未満＆数字なしエラー
				.with(csrf()))
		.andExpect(status().isOk())
		.andExpect(view().name("register"))
		.andExpect(model().hasErrors())
		.andExpect(model().attributeHasFieldErrorCode("userForm", "username", "NotBlank"))
		.andExpect(model().attributeHasFieldErrorCode("userForm", "email", "Email"))
		.andExpect(content().string(containsString("パスワードは８文字以上で入力してください")))
		.andExpect(content().string(containsString("半角英数字の混在で入力してください")));
	}
	
	
	
	@Test
	void ユーザー登録_サービスで例外が発生した場合はエラーメッセージが表示されること() throws Exception {
		
		// userFactoryでメアドが (username + "@example.com") になるようにしてる
		userFactory.create("testUser"); // ※既存ユーザーとしての準備
		
		mockMvc.perform(post("/register")
				.param("username", "テスト太郎")
				.param("email", "testUser@example.com") // メアドを既存ユーザーと重複させる
				.param("password", "Pass1234")
				.with(csrf()))
		.andExpect(status().isOk())
		.andExpect(view().name("register"))
		.andExpect(model().attributeExists("errorMessage"));
	}
	
	
	
//	@Test
//	void t() {
//		
//		
//	}
}
