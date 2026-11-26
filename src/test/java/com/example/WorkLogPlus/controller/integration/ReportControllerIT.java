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
import com.example.WorkLogPlus.entity.Report;
import com.example.WorkLogPlus.entity.User;
import com.example.WorkLogPlus.factory.ReportFactory;
import com.example.WorkLogPlus.factory.UserFactory;
import com.example.WorkLogPlus.repository.ReportRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ReportControllerIT {
	
	@Autowired
	private MockMvc mockMvc;
	
	@Autowired
	private UserFactory userFactory;
	
	@Autowired
	private ReportFactory reportFactory;
	
	@Autowired
	private ReportRepository reportRepository;
	
	private User mockUser;
	
	private LoginUser mockLogin;
	
	@BeforeEach
	void setUp() {
		
		this.mockUser = userFactory.create("testUser");
		this.mockLogin = new LoginUser(mockUser);
	}
	
	// AssertJ (assertThat)ではないJUnit5 (assertEquals)を試しに使ってみました。
	
	
	
	@Test
	void showReportList_正常系_日報一覧画面が表示されること() throws Exception {
		
		mockMvc.perform(get("/reports")
				.with(user(mockLogin)))
		.andExpect(status().isOk())
		.andExpect(view().name("user/report/mylist"))
		.andExpect(model().attributeExists("reports"));
		
	}
	
	
	
	@Test
	void showReportCreate_正常系_新規日報作成画面が表示されること() throws Exception {
		
		mockMvc.perform(get("/reports/new")
				.with(user(mockLogin)))
		.andExpect(status().isOk())
		.andExpect(view().name("user/report/mycreate"))
		.andExpect(model().attributeExists("reportForm"));
	}
	
	
	
	@Test
	void showReportDetail_正常系_本人のアクセスで詳細画面が表示されること() throws Exception {
		
		Report report = reportFactory.create(mockUser);
		Long reportId = report.getId();
		
		mockMvc.perform(get("/reports/{id}", reportId)
				.with(user(mockLogin)))
		.andExpect(status().isOk())
		.andExpect(view().name("user/report/mydetail"))
		.andExpect(model().attributeExists("reportDto"))
		.andExpect(model().attributeExists("comments"));
	}
	
	
	
	@Test
	void showReportDetail_異常系_本人以外のアクセスで403エラーになること() throws Exception {
		
		User otherUser = userFactory.create("others");
		Report report = reportFactory.create(otherUser);
		Long reportId = report.getId();
		
		mockMvc.perform(get("/reports/{id}", reportId)
				.with(user(mockLogin)))
		.andExpect(status().isForbidden())
		.andExpect(view().name("error/403"))
		.andExpect(model().attributeDoesNotExist("reportDto"))
		.andExpect(model().attributeDoesNotExist("comments"));
	}
	
	
	
	@Test
	void showReportDetail_異常系_存在しない日報IDを指定した場合はPreAuthorizeが先に働き403エラー画面になること() throws Exception {
		
		Long reportId = 9999L;
		
		mockMvc.perform(get("/reports/{id}", reportId)
				.with(user(mockLogin)))
		.andExpect(status().isForbidden())
		.andExpect(view().name("error/403"))
		.andExpect(model().attributeDoesNotExist("reportDto"))
		.andExpect(model().attributeDoesNotExist("comments"));
	}
	
	
	
	@Test
	void showReportEdit_正常系_本人のアクセスで編集画面が表示されること() throws Exception {
		
		Report report = reportFactory.create(mockUser);
		Long reportId = report.getId();
		
		mockMvc.perform(get("/reports/{id}/edit", reportId)
				.with(user(mockLogin)))
		.andExpect(status().isOk())
		.andExpect(view().name("user/report/myedit"))
		.andExpect(model().attributeExists("reportForm"))
		.andExpect(model().attribute("reportId", reportId));
	}
	
	
	
	@Test
	void showReportEdit_異常系_本人以外のアクセスで403エラーになること() throws Exception {
		
		User otherUser = userFactory.create("others");
		Report report = reportFactory.create(otherUser);
		Long reportId = report.getId();
		
		mockMvc.perform(get("/reports/{id}/edit", reportId)
				.with(user(mockLogin)))
		.andExpect(status().isForbidden())
		.andExpect(view().name("error/403"))
		.andExpect(model().attributeDoesNotExist("reportForm"))
		.andExpect(model().attributeDoesNotExist("reportId"));
	}
	
	
	
	@Test
	void showReportEdit_異常系_存在しない日報IDを指定した場合はPreAuthorizeが先に働き403エラー画面になること() throws Exception {
		
		Long reportId = 999L;
		
		mockMvc.perform(get("/reports/{id}/edit", reportId)
				.with(user(mockLogin)))
		.andExpect(status().isForbidden())
		.andExpect(view().name("error/403"))
		.andExpect(model().attributeDoesNotExist("reportForm"))
		.andExpect(model().attributeDoesNotExist("reportId"));
	}
	
	
	
	@Test
	void createReport_正常系_正しい入力で日報が保存され一覧画面にリダイレクトされること() throws Exception {
		
		long beforeCount = reportRepository.count();
		
		mockMvc.perform(post("/reports/create")
				.param("reportDate", "2026-11-06")
				.param("content", "日報登録テストの本文です")
				.with(user(mockLogin))
				.with(csrf()))
		.andExpect(status().isFound())
		.andExpect(redirectedUrl("/reports"))
		.andExpect(flash().attribute("okMessage", "日報を登録しました"));
		
		long afterCount = reportRepository.count();
		assertEquals(beforeCount +1, afterCount);
	}
	
	
	
	@Test
	void createReport_異常系_同じ日付の日報が存在するとDupeReportExceptionが出て保存されずに登録画面が再表示されること() throws Exception {
		
		LocalDate targetDate = LocalDate.of(2026, 11, 6);
		reportFactory.create(mockUser, targetDate);
		
		long beforeCount = reportRepository.count();
		
		mockMvc.perform(post("/reports/create")
				.param("reportDate", targetDate.toString())
				.param("content", "日報登録テストの本文です")
				.with(user(mockLogin))
				.with(csrf()))
		.andExpect(status().isOk())
		.andExpect(view().name("user/report/mycreate"))
		.andExpect(model().attributeHasFieldErrorCode("reportForm", "reportDate", "error.dupe"));
		
		long afterCount = reportRepository.count();
		assertEquals(beforeCount, afterCount);
	}
	
	
	
	@Test
	void updateReport_正常系_自身の日報が正しく更新され詳細画面にリダイレクトされること() throws Exception {
		
		Report report = reportFactory.create(mockUser, LocalDate.of(1999, 1, 10));
		Long reportId = report.getId();
		
		mockMvc.perform(post("/reports/{id}/update", reportId)
				.param("reportDate", "2026-11-06")
				.param("content", "日報更新テストの本文です")
				.with(user(mockLogin))
				.with(csrf()))
		.andExpect(status().isFound())
		.andExpect(redirectedUrl("/reports/" + reportId))
		.andExpect(flash().attribute("okMessage", "日報を更新しました"));
		
		Report dbReport = reportRepository.findById(reportId).orElseThrow();
		assertEquals(LocalDate.of(2026, 11, 6), dbReport.getReportDate());
		assertEquals("日報更新テストの本文です", dbReport.getContent());
	}
	
	
	
	@Test
	void updateReport_異常系_同じ日付の日報が存在するとDupeReportExceptionが出て更新されずに編集画面が再表示されること() throws Exception {
		
		LocalDate targetDate = LocalDate.of(2026, 11, 6);
		reportFactory.create(mockUser, targetDate); // 既存の日報
		
		LocalDate beforeDate = LocalDate.of(1999, 12, 31);
		Report report = reportFactory.create(mockUser, beforeDate);
		String beforeContent = report.getContent();
		Long reportId = report.getId(); // 更新する日報
		
		mockMvc.perform(post("/reports/{id}/update", reportId)
				.param("reportDate", targetDate.toString())
				.param("content", "日報更新テストの本文です")
				.with(user(mockLogin))
				.with(csrf()))
		.andExpect(status().isOk())
		.andExpect(view().name("user/report/myedit"))
		.andExpect(model().attributeHasFieldErrorCode("reportForm", "reportDate", "error.dupe"))
		.andExpect(model().attribute("reportId", reportId));
		
		Report dbReport = reportRepository.findById(reportId).orElseThrow();
		assertEquals(beforeDate, dbReport.getReportDate());
		assertEquals(beforeContent, dbReport.getContent()); // 第一引数はReportFactoryのデフォ値
	}
	
	
	
	@Test
	void updateReport_異常系_本人以外のアクセスで403エラーになること() throws Exception {
		
		User otherUser = userFactory.create("otherUser");
		Report otherReport = reportFactory.create(otherUser);
		
		mockMvc.perform(post("/reports/{id}/update", otherReport.getId())
				.param("reportDate", "2026-11-06")
				.param("content", "日報更新テストの本文です")
				.with(user(mockLogin))
				.with(csrf()))
		.andExpect(status().isForbidden())
		.andExpect(view().name("error/403"))
		.andExpect(model().attributeDoesNotExist("reportForm"))
		.andExpect(model().attributeDoesNotExist("reportId"));
	}
	
	
	
//	@Test
//	void test() {
//		
//		
//	}
}
