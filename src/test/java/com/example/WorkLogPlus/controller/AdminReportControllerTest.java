package com.example.WorkLogPlus.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.WorkLogPlus.config.LoginUser;
import com.example.WorkLogPlus.dto.CommentDto;
import com.example.WorkLogPlus.dto.ReportDto;
import com.example.WorkLogPlus.entity.User;
import com.example.WorkLogPlus.exception.DupeReportException;
import com.example.WorkLogPlus.factory.UserFactory;
import com.example.WorkLogPlus.form.CommentForm;
import com.example.WorkLogPlus.form.ReportForm;
import com.example.WorkLogPlus.service.CommentService;
import com.example.WorkLogPlus.service.ReportService;
import com.example.WorkLogPlus.service.UserService;

@WebMvcTest(AdminReportController.class)
class AdminReportControllerTest {
	
	@Autowired
	private MockMvc mockMvc;
	
	@MockitoBean
	private ReportService reportService;
	
	@MockitoBean
	private CommentService commentService;
	
	@MockitoBean
	private UserService userService;
	
	private LoginUser mockAdmin;
	
	@BeforeEach
	void setUp() {
		
		UserFactory factory = new UserFactory();
		User admin = factory.createUnsaved("testAdmin");
		this.mockAdmin = new LoginUser(admin);
	}
	
	
	
	@Test
	void saveReport_正常系_正常な入力で日報登録処理が実行され一覧画面にリダイレクトされること() throws Exception {
		
		mockMvc.perform(post("/admin/reports/create")
				.param("reportDate", "2026-10-24")
				.param("content", "テストの日報本文です")
				.with(user(mockAdmin))
				.with(csrf()))
		.andExpect(status().isFound())
		.andExpect(view().name("redirect:/admin/reports"))
		.andExpect(flash().attribute("okMessage", "日報を登録しました"));	
		
		verify(reportService, times(1)).saveReport(any(User.class), any(ReportForm.class));
	}
	
	
	
	@Test
	void saveReport_異常系_入力エラーで日報保存処理が呼ばれず登録画面に戻ること() throws Exception {
		
		mockMvc.perform(post("/admin/reports/create")
				.param("reportDate", "")
				.param("content", "")
				.with(user(mockAdmin))
				.with(csrf()))
		.andExpect(status().isOk())
		.andExpect(view().name("admin/report/create"))
		.andExpect(model().attributeHasFieldErrorCode("reportForm", "reportDate", "NotNull"))
		.andExpect(model().attributeHasFieldErrorCode("reportForm", "content", "NotBlank"));
		
		verify(reportService, never()).saveReport(any(), any());
	}
	
	
	
	@Test
	void saveReport_異常系_DupeReportException発生時に登録画面に戻ること() throws Exception {
		
		doThrow(new DupeReportException("この日付の日報は既に存在します")).when(reportService).saveReport(any(), any());
		
		mockMvc.perform(post("/admin/reports/create")
				.param("reportDate", "2026-10-24")
				.param("content", "テストの日報本文です")
				.with(user(mockAdmin))
				.with(csrf()))
		.andExpect(status().isOk())
		.andExpect(view().name("admin/report/create"))
		.andExpect(model().attributeHasFieldErrorCode("reportForm", "reportDate", "error.dupe"));
	}
	
	
	
	@Test
	void updateReport_正常系_正常な入力で日報更新処理が実行され詳細画面にリダイレクトされること() throws Exception {
		
		Long reportId = 99L;
		
		mockMvc.perform(post("/admin/reports/{id}/update", reportId)
				.param("reportDate", "2026-10-27")
				.param("content", "テストの日報本文です")
				.with(user(mockAdmin))
				.with(csrf()))
		.andExpect(status().isFound())
		.andExpect(view().name("redirect:/admin/reports/" + reportId))
		.andExpect(flash().attribute("okMessage", "日報を更新しました"));
		
		verify(reportService, times(1)).updateReport(eq(reportId), any(User.class), any(ReportForm.class));
	}
	
	
	
	@Test
	void updateReport_異常系_入力エラーで更新処理が呼ばれずに編集画面に戻ること() throws Exception {

		Long reportId = 88L;
		
		mockMvc.perform(post("/admin/reports/{id}/update", reportId)
				.param("reportDate", "")
				.param("content", "")
				.with(user(mockAdmin))
				.with(csrf()))
		.andExpect(status().isOk())
		.andExpect(view().name("admin/report/edit"))
		.andExpect(model().attribute("reportId", reportId))
		.andExpect(model().attributeHasFieldErrorCode("reportForm", "reportDate", "NotNull"))
		.andExpect(model().attributeHasFieldErrorCode("reportForm", "content", "NotBlank"));
		
		verify(reportService, never()).updateReport(anyLong(), any(), any());
	}
	
	
	
	@Test
	void updateReport_異常系_DupeReportException発生でエラーメッセージを持って編集画面に戻ること() throws Exception {

		Long reportId = 77L;
		
		doThrow(new DupeReportException("この日付の日報は既に存在します")).when(reportService).updateReport(anyLong(), any(), any());
		
		mockMvc.perform(post("/admin/reports/{id}/update", reportId)
				.param("reportDate", "2026-10-27")
				.param("content", "日報更新テストの本文です")
				.with(user(mockAdmin))
				.with(csrf()))
		.andExpect(status().isOk())
		.andExpect(view().name("admin/report/edit"))
		.andExpect(model().attributeHasFieldErrorCode("reportForm", "reportDate", "error.dupe"))
		.andExpect(model().attribute("reportId", reportId));
	}
	
	
	
	@Test
	void saveComment_正常系_正常な入力でコメント保存処理が呼ばれ詳細画面にリダイレクトされること() throws Exception {
		
		Long reportId = 66L;

		mockAdmin.getUser().setId(111L);
		
		mockMvc.perform(post("/admin/reports/{id}/save-comment", reportId)
				.param("body", "コメント投稿テストの本文です")
				.with(user(mockAdmin))
				.with(csrf()))
		.andExpect(status().isFound())
		.andExpect(view().name("redirect:/admin/reports/" + reportId))
		.andExpect(flash().attribute("okMessage", "フィードバックを追加しました。"));
		
		verify(commentService, times(1)).saveComment(any(CommentForm.class), eq(reportId), eq(mockAdmin.getUser().getId()));
	}
	
	
	
	@Test
	void saveComment_異常系_バリデーションエラー時は詳細画面を再表示すること() throws Exception {
		
		Long reportId = 55L;
		ReportDto reportDto = mock(ReportDto.class);
		List<CommentDto> comments = new ArrayList<CommentDto>();
		
		when(reportService.getReportById(reportId, mockAdmin.getUser())).thenReturn(reportDto);
		
		when(commentService.getComments(reportId, mockAdmin.getUser())).thenReturn(comments);
		
		mockMvc.perform(post("/admin/reports/{id}/save-comment", reportId)
				.param("body", "")
				.with(user(mockAdmin))
				.with(csrf()))
		.andExpect(status().isOk())
		.andExpect(view().name("admin/report/detail"))
		.andExpect(model().attribute("reportDto", reportDto))
		.andExpect(model().attribute("comments", comments))
		.andExpect(model().attributeHasFieldErrorCode("commentForm", "body", "NotBlank"));
		
		verify(commentService, never()).saveComment(any(), anyLong(), anyLong());
	}
	
	
	
	@Test
	void deleteComment_正常系_コメント削除処理が呼ばれメッセージを持って日報詳細画面にリダイレクトされること() throws Exception {
		
		Long reportId = 123L;
		Long commentId = 456L;
		mockAdmin.getUser().setId(789L);
		
		mockMvc.perform(post("/admin/reports/{reportId}/delete-comment/{commentId}", reportId, commentId)
				.with(user(mockAdmin))
				.with(csrf()))
		.andExpect(status().isFound())
		.andExpect(view().name("redirect:/admin/reports/" + reportId))
		.andExpect(flash().attribute("okMessage", "コメントを削除しました。"));
		
		verify(commentService, times(1)).deleteComment(reportId, commentId, mockAdmin.getUser().getId());
	}
	
	
	
//	@Test
//	void test() {
//		
	
//	}
}
