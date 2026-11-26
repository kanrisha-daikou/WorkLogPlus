package com.example.WorkLogPlus.controller.integration;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.example.WorkLogPlus.config.LoginUser;
import com.example.WorkLogPlus.entity.Comment;
import com.example.WorkLogPlus.entity.Report;
import com.example.WorkLogPlus.entity.Role;
import com.example.WorkLogPlus.entity.User;
import com.example.WorkLogPlus.factory.CommentFactory;
import com.example.WorkLogPlus.factory.ReportFactory;
import com.example.WorkLogPlus.factory.UserFactory;
import com.example.WorkLogPlus.form.ReportForm;
import com.example.WorkLogPlus.repository.CommentRepository;
import com.example.WorkLogPlus.repository.ReportRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AdminReportControllerIT {

	@Autowired
	private MockMvc mockMvc;
	
	@Autowired
	private UserFactory userFactory;
	
	@Autowired
	private ReportFactory reportFactory;
	
	@Autowired
	private CommentFactory commentFactory;
	
	@Autowired
	private ReportRepository reportRepository;
	
	@Autowired
	private CommentRepository commentRepository;
	
	private User admin;
	
	private LoginUser mockAdmin;
	
	@BeforeEach
	void setUp() {
		this.admin = userFactory.create("testAdmin", Role.ADMIN);
		this.mockAdmin = new LoginUser(admin);
	}
	
	// AssertJ (assertThat)ではないJUnit5 (assertEqualsなど)を試しに使ってみました。
	
	
	
	@Test
	void 日報管理画面_正常系_検索条件なしで全ユーザーの日報一覧が表示されること() throws Exception {
		
		mockMvc.perform(get("/admin/reports").with(user(mockAdmin)))
		.andExpect(status().isOk())
		.andExpect(view().name("admin/report/list"))
		.andExpect(model().attributeExists("reports"))
		.andExpect(model().attributeExists("users"))
		.andExpect(model().attributeDoesNotExist("searchUserId"));
	}
	
	
	
	@Test
	void 日報管理画面_正常系_ユーザーIDを指定して日報一覧の絞り込みができること() throws Exception {
		
		User target = userFactory.create("targetUser");
		Long targetId = target.getId();
		
		mockMvc.perform(get("/admin/reports")
				.param("searchUserId", String.valueOf(targetId))
				.with(user(mockAdmin)))
		.andExpect(status().isOk())
		.andExpect(view().name("admin/report/list"))
		.andExpect(model().attributeExists("reports"))
		.andExpect(model().attributeExists("users"))
		.andExpect(model().attribute("searchUserId", targetId));
	}
	
	
	
	@Test
	void adminReportCreate_正常系_日報作成画面が表示されること() throws Exception {
		
		mockMvc.perform(get("/admin/reports/new")
				.with(user(mockAdmin)))
		.andExpect(status().isOk())
		.andExpect(view().name("admin/report/create"))
		.andExpect(model().attributeExists("reportForm"));
	}
	
	
	
	@Test
	void 詳細画面_正常系_指定したIDの日報とコメント一覧が正しく表示されること() throws Exception {
		
		User mockUser = userFactory.create("testUser");
		Report report = reportFactory.create(mockUser);
		
		mockMvc.perform(get("/admin/reports/{id}", report.getId())
				.with(user(mockAdmin)))
		.andExpect(status().isOk())
		.andExpect(view().name("admin/report/detail"))
		.andExpect(model().attributeExists("reportDto"))
		.andExpect(model().attributeExists("comments"))
		.andExpect(model().attributeExists("commentForm"));
	}
	
	
	
	@Test
	void 詳細画面_異常系_存在しない日報IDを指定した場合は500エラー画面になること() throws Exception {
		
		Long reportId = 9999L; // 存在しない日報ID
		
		mockMvc.perform(get("/admin/reports/{id}", reportId)
				.with(user(mockAdmin)))
		.andExpect(status().isInternalServerError())
		.andExpect(view().name("error/500"))
		.andExpect(model().attributeDoesNotExist("reportDto"))
		.andExpect(model().attributeDoesNotExist("comments"))
		.andExpect(model().attributeDoesNotExist("commentForm"));
	}
	
	
	
	@Test
	void 編集画面_正常系_日報の所有者本人がアクセスした場合は画面が正しく表示されること() throws Exception {
		
		Report report = reportFactory.create(admin);
		Long reportId = report.getId();
		
		ReportForm targetForm = new ReportForm();
		targetForm.setReportDate(report.getReportDate());
		targetForm.setContent(report.getContent());
		
		mockMvc.perform(get("/admin/reports/{id}/edit", reportId)
				.with(user(mockAdmin)))
		.andExpect(status().isOk())
		.andExpect(view().name("admin/report/edit"))
		.andExpect(model().attribute("reportForm", targetForm))
		.andExpect(model().attribute("reportId", reportId));
	}
	
	
	
	@Test
	void 編集画面_異常系_日報の所有者ではない他人がアクセスした場合は403エラー画面になること() throws Exception {
		
		User otherUser = userFactory.create("others");
		Report report = reportFactory.create(otherUser);
		Long reportId = report.getId();
		
		mockMvc.perform(get("/admin/reports/{id}/edit", reportId)
				.with(user(mockAdmin)))
		.andExpect(status().isForbidden())
		.andExpect(view().name("error/403"))
		.andExpect(model().attributeDoesNotExist("reportForm"))
		.andExpect(model().attributeDoesNotExist("reportId"));
	}
	
	
	
	@Test
	void 編集画面_異常系_存在しない日報IDを指定した場合はPreAuthorizeが先に働き403エラー画面になること() throws Exception {
		
		Long reportId = 999L;
		
		mockMvc.perform(get("/admin/reports/{id}/edit", reportId)
				.with(user(mockAdmin)))
		.andExpect(status().isForbidden())
		.andExpect(view().name("error/403"))
		.andExpect(model().attributeDoesNotExist("reportForm"))
		.andExpect(model().attributeDoesNotExist("reportId"));
	}
	
	
	
	@Test
	void saveReport_正常系_日報が正しく保存され一覧画面にリダイレクトされること() throws Exception {
		
		long beforeCount = reportRepository.count();
		
		mockMvc.perform(post("/admin/reports/create")
				.param("reportDate", "2026-10-31")
				.param("content", "日報登録のテストです")
				.with(user(mockAdmin))
				.with(csrf()))
		.andExpect(status().isFound())
		.andExpect(redirectedUrl("/admin/reports"))
		.andExpect(flash().attribute("okMessage", "日報を登録しました"));
		
		long afterCount = reportRepository.count();
		assertEquals(beforeCount + 1, afterCount);
	}
	
	
	
	@Test
	void saveReport_異常系_同じ日付の日報が存在するとDupeReportExceptionが出て保存されずに登録画面が再表示されること() throws Exception {
		
		LocalDate targetDate = LocalDate.of(2026, 10, 31);
		reportFactory.create(admin, targetDate);
		
		long beforeCount = reportRepository.count();
		
		mockMvc.perform(post("/admin/reports/create")
				.param("reportDate", targetDate.toString()) // 日付を被らせた新規登録
				.param("content", "日報登録のテストです")
				.with(user(mockAdmin))
				.with(csrf()))
		.andExpect(status().isOk())
		.andExpect(view().name("admin/report/create"))
		.andExpect(model().attributeHasFieldErrorCode("reportForm", "reportDate", "error.dupe"));
		
		long afterCount = reportRepository.count();
		assertEquals(beforeCount, afterCount);
	}
	
	
	
	@Test
	void updateReport_正常系_自身の日報が正しく更新され詳細画面にリダイレクトされること() throws Exception {
		
		Report report = reportFactory.create(admin);
		Long reportId = report.getId();
		
		mockMvc.perform(post("/admin/reports/{id}/update", reportId)
				.param("reportDate", "2026-11-02")
				.param("content", "日報更新テストの本文です")
				.with(user(mockAdmin))
				.with(csrf()))
		.andExpect(status().isFound())
		.andExpect(redirectedUrl("/admin/reports/" + reportId))
		.andExpect(flash().attribute("okMessage", "日報を更新しました"));
		
		// 【DB検証】本当にDBの中身が「新しいタイトル」に書き換わったか確認
		Report dbReport = reportRepository.findById(reportId).orElseThrow();
		assertEquals("日報更新テストの本文です", dbReport.getContent());
	}
	
	
	
	@Test
	void updateReport_異常系_同じ日付の日報が存在するとDupeReportExceptionが出て更新されずに編集画面が再表示されること() throws Exception {
		
		LocalDate targetDate = LocalDate.of(2026, 11, 2);
		reportFactory.create(admin, targetDate); // 既存の日報を準備しておく
		
		LocalDate beforeDate = LocalDate.of(2099, 1, 10);
		Report report = reportFactory.create(admin, beforeDate);
		String beforeContent = report.getContent();
		Long reportId = report.getId(); // もう一つ用意したこれを更新して被らせに行く
		
		mockMvc.perform(post("/admin/reports/{id}/update", reportId)
				.param("reportDate", targetDate.toString()) // 既存の日報の日付と重複させる
				.param("content", "日報更新テストの本文です")
				.with(user(mockAdmin))
				.with(csrf()))
		.andExpect(status().isOk())
		.andExpect(view().name("admin/report/edit"))
		.andExpect(model().attributeHasFieldErrorCode("reportForm", "reportDate", "error.dupe"))
		.andExpect(model().attribute("reportId", reportId));
		
		Report dbReport = reportRepository.findById(reportId).orElseThrow();
		assertEquals(beforeDate, dbReport.getReportDate());
		assertEquals(beforeContent, dbReport.getContent());
	}
	
	
	
	@Test
	void updateReport_異常系_他人の日報を更新しようとした場合は認可エラー403が発生すること() throws Exception {
		
		User otherUser = userFactory.create("testUser");
		Report otherReport= reportFactory.create(otherUser);
		
		mockMvc.perform(post("/admin/reports/{id}/update", otherReport.getId())
				.param("reportDate", "2099-10-10")
				.param("content", "日報更新テストの本文です")
				.with(user(mockAdmin))
				.with(csrf()))
		.andExpect(status().isForbidden())
		.andExpect(view().name("error/403"))
		.andExpect(model().attributeDoesNotExist("reportForm"))
		.andExpect(model().attributeDoesNotExist("reportId"));
	}
	
	
	
	@Test
	void saveComment_正常系_コメントが正しく保存され詳細画面にリダイレクトされること() throws Exception {
		
		User user = userFactory.create("testUser");
		Report report = reportFactory.create(user);
		Long reportId = report.getId();
		
		long beforeCount = commentRepository.count();
		
		mockMvc.perform(post("/admin/reports/{id}/save-comment", reportId)
				.param("body", "コメント投稿のテストです")
				.with(user(mockAdmin))
				.with(csrf()))
		.andExpect(status().isFound())
		.andExpect(redirectedUrl("/admin/reports/" + reportId))
		.andExpect(flash().attribute("okMessage", "フィードバックを追加しました。"));
		
		long afterCount = commentRepository.count();
		assertEquals(beforeCount + 1, afterCount);
	}
	
	
	
	@Test
	void saveComment_異常系_入力エラーがある場合に詳細画面を再表示すること() throws Exception {
		
		User user = userFactory.create("testUsr");
		Report report = reportFactory.create(user);
		Long reportId =report.getId();
		
		long beforeCount = commentRepository.count();
		
		mockMvc.perform(post("/admin/reports/{id}/save-comment", reportId)
				.param("body", "")
				.with(user(mockAdmin))
				.with(csrf()))
		.andExpect(status().isOk())
		.andExpect(view().name("admin/report/detail"))
		.andExpect(model().attributeExists("reportDto"))
		.andExpect(model().attributeExists("comments"))
		.andExpect(model().attributeHasFieldErrorCode("commentForm", "body", "NotBlank"));
		
		long afterCount = commentRepository.count();
		assertEquals(beforeCount, afterCount);
	}
	
	
	
	@Test
	void saveComment_異常系_自身の日報にコメントは出来ずに403エラーになること() throws Exception {
		
		Report report = reportFactory.create(admin);
		Long reportId = report.getId();
		
		long beforeCount = commentRepository.count();
		
		mockMvc.perform(post("/admin/reports/{id}/save-comment", reportId)
				.param("body", "コメント投稿のテストです")
				.with(user(mockAdmin))
				.with(csrf()))
		.andExpect(status().isForbidden())
		.andExpect(view().name("error/403"))
		.andExpect(model().attributeDoesNotExist("reportDto"))
		.andExpect(model().attributeDoesNotExist("comments"));
		
		long afterCount = commentRepository.count();
		assertEquals(beforeCount, afterCount);
	}
	
	
	
	@Test
	void saveComment_異常系_存在しない日報にコメントは出来ずに500エラーになること() throws Exception {
		
		Long reportId = 999L;
		
		long beforeCount = commentRepository.count();
		
		mockMvc.perform(post("/admin/reports/{id}/save-comment", reportId)
				.param("body", "コメント投稿のテストです")
				.with(user(mockAdmin))
				.with(csrf()))
		.andExpect(status().isInternalServerError())
		.andExpect(view().name("error/500"))
		.andExpect(model().attributeDoesNotExist("reportDto"))
		.andExpect(model().attributeDoesNotExist("comments"));
		
		long afterCount = commentRepository.count();
		assertEquals(beforeCount, afterCount);
	}
	
	
	
	@Test
	void deleteComment_正常系_コメントが削除され詳細画面にリダイレクトされること() throws Exception {
		
		User otherUser = userFactory.create("otherUser");
		Report report = reportFactory.create(otherUser);
		Long reportId = report.getId();
		
		Comment comment = commentFactory.create(report, admin);
		Long commentId = comment.getId();
		
		assertTrue(commentRepository.existsById(commentId), "削除前はコメントが存在すること");
		
		mockMvc.perform(post("/admin/reports/{reportId}/delete-comment/{commentId}", reportId, commentId)
				.with(user(mockAdmin))
				.with(csrf()))
		.andExpect(status().isFound())
		.andExpect(redirectedUrl("/admin/reports/" + reportId))
		.andExpect(flash().attribute("okMessage", "コメントを削除しました。"));
		
		assertFalse(commentRepository.existsById(commentId), "削除後はコメントが消えていること");
	}
	
	
	
	@Test
	void deleteComment_異常系_コメント作者本人でないと削除できないこと() throws Exception {
		
		User otherUser = userFactory.create("otherUser");
		Report report = reportFactory.create(otherUser);
		Long reportId = report.getId();
		
		User otherAdmin = userFactory.create("otherAdmin", Role.ADMIN);
		Comment comment = commentFactory.create(report, otherAdmin);
		Long commentId = comment.getId();
		
		mockMvc.perform(post("/admin/reports/{reportId}/delete-comment/{commentId}", reportId, commentId)
				.with(user(mockAdmin))
				.with(csrf()))
		.andExpect(status().isForbidden())
		.andExpect(view().name("error/403"));
		
		assertTrue(commentRepository.existsById(commentId));
	}
	
	
	
//	@Test
//	void test() {
//		
	
//	}
}
