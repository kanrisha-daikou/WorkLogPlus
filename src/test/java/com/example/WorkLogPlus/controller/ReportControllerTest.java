package com.example.WorkLogPlus.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.WorkLogPlus.config.LoginUser;
import com.example.WorkLogPlus.entity.User;
import com.example.WorkLogPlus.exception.DupeReportException;
import com.example.WorkLogPlus.factory.UserFactory;
import com.example.WorkLogPlus.form.ReportForm;
import com.example.WorkLogPlus.service.CommentService;
import com.example.WorkLogPlus.service.ReportService;

@WebMvcTest(ReportController.class)
class ReportControllerTest {
	
	@Autowired
	private MockMvc mockMvc;
	
	@MockitoBean
	private ReportService reportService;
	
	@MockitoBean
	private CommentService commentService;
	
	private User mockUser;
	
	private LoginUser mockLogin;
	
	@BeforeEach
	void setUp() {
		
		UserFactory factory = new UserFactory();
		this.mockUser = factory.createUnsaved("testUser");
		this.mockLogin = new LoginUser(mockUser);
	}
	
	
	
	@Test
	void createReport_正常系_入力が正常な場合は登録処理が呼ばれ一覧画面にリダイレクトされること() throws Exception {
		
		mockMvc.perform(post("/reports/create")
				.param("reportDate", "2026-11-05")
				.param("content", "日報登録テストの本文です")
				.with(user(mockLogin))
				.with(csrf()))
		.andExpect(status().isFound())
		.andExpect(view().name("redirect:/reports"))
		.andExpect(flash().attribute("okMessage", "日報を登録しました"));
		
		verify(reportService, times(1)).saveReport(eq(mockUser), any(ReportForm.class));
	}
	
	
	
	@Test
	void createReport_異常系_入力エラーの場合は登録処理を呼び出さず新規作成画面に戻ること() throws Exception {
		
		mockMvc.perform(post("/reports/create")
				.param("reportDate", "")
				.param("content", "")
				.with(user(mockLogin))
				.with(csrf()))
		.andExpect(status().isOk())
		.andExpect(view().name("user/report/mycreate"))
		.andExpect(model().attributeHasFieldErrorCode("reportForm", "reportDate", "NotNull"))
		.andExpect(model().attributeHasFieldErrorCode("reportForm", "content", "NotBlank"));
		
		verify(reportService, never()).saveReport(any(), any());
	}
	
	
	
	@Test
	void createReport_異常系_DupeReportException発生時はエラーメッセージを伴って新規作成画面に戻ること() throws Exception {
		
		doThrow(new DupeReportException("この日付の日報は既に存在します"))
		.when(reportService).saveReport(eq(mockUser), any(ReportForm.class));
		
		mockMvc.perform(post("/reports/create")
				.param("reportDate", "2026-12-05")
				.param("content", "日報登録テストの本文です")
				.with(user(mockLogin))
				.with(csrf()))
		.andExpect(status().isOk())
		.andExpect(view().name("user/report/mycreate"))
		.andExpect(model().attributeHasFieldErrorCode("reportForm", "reportDate", "error.dupe"));
	}
	
	
	
	@Test
	void updateReport_正常系_入力が正常な場合は更新処理が呼ばれ詳細画面にリダイレクトされること() throws Exception {
		
		Long reportId = 111L;
		
		mockMvc.perform(post("/reports/{id}/update", reportId)
				.param("reportDate", "2026-11-05")
				.param("content", "日報更新テストの本文です")
				.with(user(mockLogin))
				.with(csrf()))
		.andExpect(status().isFound())
		.andExpect(view().name("redirect:/reports/" + reportId))
		.andExpect(flash().attribute("okMessage", "日報を更新しました"));
		
		verify(reportService, times(1)).updateReport(eq(reportId), eq(mockUser), any(ReportForm.class));
	}
	
	
	
	@Test
	void updateReport_異常系_入力エラーの場合は更新処理を呼び出さず編集画面に戻ること() throws Exception {
		
		Long reportId = 222L;
		
		mockMvc.perform(post("/reports/{id}/update", reportId)
				.param("reportDate", "")
				.param("content", "")
				.with(user(mockLogin))
				.with(csrf()))
		.andExpect(status().isOk())
		.andExpect(view().name("user/report/myedit"))
		.andExpect(model().attributeHasFieldErrorCode("reportForm", "reportDate", "NotNull"))
		.andExpect(model().attributeHasFieldErrorCode("reportForm", "content", "NotBlank"))
		.andExpect(model().attribute("reportId", reportId));
		
		verify(reportService, never()).updateReport(anyLong(), any(), any());
	}
	
	
	
	@Test
	void updateReport_異常系_DupeReportException発生時はエラーメッセージを伴って編集画面に戻ること() throws Exception {
		
		Long reportId = 333L;
		
		doThrow(new DupeReportException("この日付の日報は既に存在します"))
		.when(reportService).updateReport(eq(reportId), eq(mockUser), any(ReportForm.class));
		
		mockMvc.perform(post("/reports/{id}/update", reportId)
				.param("reportDate", "2026-11-05")
				.param("content", "日報更新テストの本文です")
				.with(user(mockLogin))
				.with(csrf()))
		.andExpect(status().isOk())
		.andExpect(view().name("user/report/myedit"))
		.andExpect(model().attributeHasFieldErrorCode("reportForm", "reportDate", "error.dupe"))
		.andExpect(model().attribute("reportId", reportId));
	}
	
	
	
//	@Test
//	void test() {
//		
//		
//	}
}
