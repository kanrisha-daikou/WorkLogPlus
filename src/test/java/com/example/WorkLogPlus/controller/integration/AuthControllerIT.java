package com.example.WorkLogPlus.controller.integration;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;


@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthControllerIT {
	
	@Autowired
	private MockMvc mockMvc;
	

	
	@Test
	void ログイン画面_正常系_パラメータなしで通常のログイン画面が表示されること() throws Exception {
		
		mockMvc.perform(get("/login"))
		.andExpect(status().isOk())
		.andExpect(view().name("login"))
		.andExpect(content().string(not(containsString("メールアドレスまたはパスワードが正しくありません。"))))
		.andExpect(content().string(not(containsString("ログアウトしました。"))));
	}
	
	
	
	@Test
	void ログイン画面_異常系_ログインエラー時はエラーメッセージが表示されること() throws Exception {
		
		mockMvc.perform(get("/login")
				.param("error", ""))
		.andExpect(status().isOk())
		.andExpect(view().name("login"))
		.andExpect(content().string(containsString("メールアドレスまたはパスワードが正しくありません。")))
		.andExpect(content().string(not(containsString("ログアウトしました。"))));
	}
	
	
	
	@Test
	void ログイン画面_正常系_ログアウト時はその旨と共にログイン画面が表示されること() throws Exception {
		
		mockMvc.perform(get("/login")
				.param("logout", ""))
		.andExpect(status().isOk())
		.andExpect(view().name("login"))
		.andExpect(content().string(containsString("ログアウトしました。")))
		.andExpect(content().string(not(containsString("メールアドレスまたはパスワードが正しくありません。"))));
	}
}
