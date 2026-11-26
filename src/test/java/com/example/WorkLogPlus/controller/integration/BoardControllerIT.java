package com.example.WorkLogPlus.controller.integration;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.example.WorkLogPlus.config.LoginUser;
import com.example.WorkLogPlus.entity.Role;
import com.example.WorkLogPlus.factory.UserFactory;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class BoardControllerIT {
	
	@Autowired
	private MockMvc mockMvc;
	
	@Autowired
	private UserFactory userFactory;
	
	
	
	@Test
	void トップ画面_正常系_一般ユーザーは一般ユーザー用ダッシュボードが表示されること() throws Exception {
		
		// springsecurityの@AuthenticationPrincipalを使っているのでLoginUserにしないといけない
		LoginUser mockUser = new LoginUser(userFactory.create("testUser"));
		
		mockMvc.perform(get("/").with(user(mockUser)))
		.andExpect(status().isOk())
		.andExpect(view().name("user/dashboard"))
		.andExpect(model().attributeExists("loginUserName"))
		.andExpect(model().attributeExists("taskCounts"))
		.andExpect(model().attributeExists("recentReports"))
		.andExpect(model().attributeExists("hasUnreadComments"));
	}
	
	
	
	@Test
	void トップ画面_正常系_管理者は管理者用ダッシュボードが表示されること() throws Exception {
		
		LoginUser mockAdmin = new LoginUser(userFactory.create("testAdmin", Role.ADMIN));
		
		mockMvc.perform(get("/").with(user(mockAdmin)))
		.andExpect(status().isOk())
		.andExpect(view().name("admin/dashboard")) // ★ここ重要！
		.andExpect(model().attributeExists("loginUserName"))
		.andExpect(model().attributeExists("taskCounts"))
		.andExpect(model().attributeExists("recentReports"))
		.andExpect(model().attributeExists("hasUnreadComments"));
	}
	
	
	
//	@Test
//	void test() {
//		
//	}

}
