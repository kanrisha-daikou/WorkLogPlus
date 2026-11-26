package com.example.WorkLogPlus.service.integration;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.example.WorkLogPlus.dto.ReportDto;
import com.example.WorkLogPlus.entity.Report;
import com.example.WorkLogPlus.entity.User;
import com.example.WorkLogPlus.exception.DupeReportException;
import com.example.WorkLogPlus.factory.CommentFactory;
import com.example.WorkLogPlus.factory.ReportFactory;
import com.example.WorkLogPlus.factory.UserFactory;
import com.example.WorkLogPlus.form.ReportForm;
import com.example.WorkLogPlus.repository.ReportRepository;
import com.example.WorkLogPlus.service.ReportService;

@SpringBootTest
@Transactional
class ReportServiceIT {
	
	@Autowired
	private ReportService reportService;
	
	@Autowired
	private ReportRepository reportRepository;
	
	@Autowired
	private ReportFactory reportFactory;
	
	@Autowired
	private UserFactory userFactory;
	
	@Autowired
	private CommentFactory commentFactory;
	
	
	
	@Test
	void getUserReports_日付の新しい順で任意のユーザの日報だけを取得できること() {
		
		User user1 = userFactory.create("user1");
		reportFactory.create(user1, LocalDate.of(2026, 10, 1));
		reportFactory.create(user1, LocalDate.of(2026, 11, 23));
		reportFactory.create(user1, LocalDate.of(2026, 9, 13));
		
		User user2 = userFactory.create("user2");
		reportFactory.create(user2, LocalDate.of(2026, 10, 10));
		
		//【実行】
		List<ReportDto> result = reportService.getUserReports(user1);
		
		
		//【検証】
		assertThat(result).hasSize(3);
		assertThat(result.get(0).reportDate()).isEqualTo("2026/11/23(月)");
		assertThat(result.get(1).reportDate()).isEqualTo("2026/10/01(木)");
		assertThat(result.get(2).reportDate()).isEqualTo("2026/09/13(日)");
	}
	
	
	
	@Test
	void getRecentReports_指定した件数制限の通りに最新の日報だけが取得できること() {
		
		User user = userFactory.create("testUser");
		
		reportFactory.create(user, LocalDate.of(2026, 11, 1));
		reportFactory.create(user, LocalDate.of(2026, 9, 9));
		reportFactory.create(user, LocalDate.of(2026, 12, 12));
		reportFactory.create(user, LocalDate.of(2026, 8, 18));
		reportFactory.create(user, LocalDate.of(2027, 9, 9));
		reportFactory.create(user, LocalDate.of(2021, 11, 1));
		
		//【実行】
		List<ReportDto> result = reportService.getRecentReports(user, 3);
		
		
		//【検証】
		assertThat(result).hasSize(3);
		assertThat(result.get(0).reportDate()).isEqualTo("2027/09/09(木)");
		assertThat(result.get(1).reportDate()).isEqualTo("2026/12/12(土)");
		assertThat(result.get(2).reportDate()).isEqualTo("2026/11/01(日)");
	}
	
	
	
	@Test
	void getReportsBySearchUserId_検索IDがある場合_任意のユーザの日報だけが日付の新しい順で取得できること() {
		
		User userA = userFactory.create("testUser");
		reportFactory.create(userA, LocalDate.of(2026, 10, 1));
		reportFactory.create(userA, LocalDate.of(2026, 11, 23));
		reportFactory.create(userA, LocalDate.of(2026, 9, 13));
		
		User userB = userFactory.create("user2");
		reportFactory.create(userB, LocalDate.of(2026, 10, 10));
		
		//【実行】
		List<ReportDto> result = reportService.getReportsBySearchUserId(userA.getId(), userA);
		
		
		//【検証】
		assertThat(result).hasSize(3);
		assertThat(result.get(0).reportDate()).isEqualTo("2026/11/23(月)");
		assertThat(result.get(1).reportDate()).isEqualTo("2026/10/01(木)");
		assertThat(result.get(2).reportDate()).isEqualTo("2026/09/13(日)");
	}
	
	
	
	@Test
	void getReportsBySearchUserId_検索IDがNullの場合_全員の全日報が日付の新しい順で取得できること() {
		
		User userA = userFactory.create("userA");
		reportFactory.create(userA, LocalDate.of(2026, 10, 1));
		reportFactory.create(userA, LocalDate.of(2026,  11, 23));
		reportFactory.create(userA, LocalDate.of(2026, 9, 13));
		
		User userB = userFactory.create("userB");
		reportFactory.create(userB, LocalDate.of(2026, 10, 10));
		
		//【実行】
		List<ReportDto> result = reportService.getReportsBySearchUserId(null, userA);
		
		
		//【検証】
		assertThat(result).isNotEmpty();
		assertThat(result)
		.extracting(ReportDto::reportDate)
		.containsSubsequence("2026/11/23(月)", "2026/10/10(土)", "2026/10/01(木)", "2026/09/13(日)");
	}
	
	
	
	@Test
	void getReportById_指定した日報IDの日報が1件だけ正しく取得できること() {
		
		User user = userFactory.create("testUser");
		Report report = reportFactory.create(user, LocalDate.of(2026, 10, 15));
		
		//【実行】
		ReportDto dto = reportService.getReportById(report.getId(), user);
		
		
		//【検証】
		assertThat(dto).isNotNull();
		assertThat(dto.reportDate()).isEqualTo("2026/10/15(木)");
	}
	
	
	
	//	getReportFormByIdはgetReportByIdと似たようなもんなので省略
	
	
	
	@Test
	void saveReport_同じ日付の日報がない場合_正常にDBに保存されること() {
		
		User user = userFactory.create("testUser");
		
		ReportForm form = new ReportForm();
		form.setReportDate(LocalDate.of(2030, 3, 30));
		form.setContent("新規保存のテストです");
		
		//【実行】
		reportService.saveReport(user, form);
		
		
		//【検証】
		List<ReportDto> result = reportService.getUserReports(user);
		assertThat(result).hasSize(1);
		assertThat(result.get(0).reportDate()).isEqualTo("2030/03/30(土)");
		assertThat(result.get(0).content()).isEqualTo("新規保存のテストです");
	}
	
	
	
	@Test
	void saveReport_同じ日付の日報があった場合_DupeReportExceptionが発生して保存されないこと() {
		
		User user = userFactory.create("testUser");
		LocalDate targetDate = LocalDate.of(2026, 10, 15);
		
		reportFactory.create(user, targetDate);
		
		long beforeCount = reportRepository.count();
		
		ReportForm form = new ReportForm();
		form.setReportDate(targetDate);
		form.setContent("重複を狙った新しい日報です");
		
		
		//【実行＆検証】
		assertThatThrownBy(() -> reportService.saveReport(user, form))
		.isInstanceOf(DupeReportException.class)
		.hasMessage("この日付の日報は既に存在します");
		
		long afterCount = reportRepository.count();
		assertThat(afterCount).isEqualTo(beforeCount);
	}
	
	
	
	@Test
	void updateReport_日付を変更せずに内容のみ更新できること() {
		
		LocalDate sameDate = LocalDate.of(2026, 11, 24);
		User user = userFactory.create("testUser");
		Report report = reportFactory.create(user, sameDate);
		
		ReportForm form = new ReportForm();
		form.setReportDate(sameDate);
		form.setContent("更新後の内容です");
		form.setImpressions("更新後の所感");
		form.setIssues("更新後の課題");
		
		//【実行】
		reportService.updateReport(report.getId(), user, form);
		
		
		//【検証】
		List<ReportDto> result = reportService.getUserReports(user);
		assertThat(result).hasSize(1);
		assertThat(result.get(0).reportDate()).isEqualTo("2026/11/24(火)");
		assertThat(result.get(0).content()).isEqualTo("更新後の内容です");
		assertThat(result.get(0).impressions()).isEqualTo("更新後の所感");
		assertThat(result.get(0).issues()).isEqualTo("更新後の課題");
	}
	
	
	
	@Test
	void updateReport_重複がない場合_既存の日報が正常に更新されること() {
		
		User user = userFactory.create("testUser");
		Report report = reportFactory.create(user, LocalDate.of(2000, 1, 10));
		
		ReportForm form = new ReportForm();
		form.setReportDate(LocalDate.of(2060, 2, 20));
		form.setContent("更新後の内容です");
		form.setImpressions("更新後の所感");
		form.setIssues("更新後の課題");
		
		//【実行】
		reportService.updateReport(report.getId(), user, form);
		
		
		//【検証】
		List<ReportDto> result = reportService.getUserReports(user);
		assertThat(result).hasSize(1);
		assertThat(result.get(0).reportDate()).isEqualTo("2060/02/20(金)");
		assertThat(result.get(0).content()).isEqualTo("更新後の内容です");
		assertThat(result.get(0).impressions()).isEqualTo("更新後の所感");
		assertThat(result.get(0).issues()).isEqualTo("更新後の課題");
	}
	
	
	
	@Test
	void updateReport_自分の他の日報と日付が重複する場合_DupeReportExceptionが発生すること() {
		
		User user = userFactory.create("testUser");
		Report report = reportFactory.create(user, LocalDate.of(2026, 10, 1));
		
		LocalDate targetDate = LocalDate.of(2026, 10, 10);
		reportFactory.create(user, targetDate); // 競合相手
		
		ReportForm form = new ReportForm();
		form.setReportDate(targetDate);
		form.setContent("日付を重複させようとする更新");
		
		
		//【実行＆検証】
		assertThatThrownBy(() -> reportService.updateReport(report.getId(), user, form))
		.isInstanceOf(DupeReportException.class)
		.hasMessage("この日付の日報は既に存在します");
	}
	
	
	
	@Test
	void hasUnreadComments_未読コメントが存在する場合_trueが返ること() {
		
		User user = userFactory.create("testUser");
		Report report = reportFactory.create(user);
		
		User admin = userFactory.create("testAdmin");
		commentFactory.create(report, admin, false);
		
		//【実行】
		boolean result = reportService.hasUnreadComments(user);
		
		
		//【検証】
		assertThat(result).isTrue();
	}
	
	
	
	@Test
	void hasUnreadComments_既読コメントのみ存在する場合_falseが返ること() {
		
		User user = userFactory.create("testUser");
		Report report = reportFactory.create(user);
		
		User admin = userFactory.create("testAdmin");
		commentFactory.create(report, admin, true);
		
		//【実行】
		boolean result = reportService.hasUnreadComments(user);
		
		
		//【検証】
		assertThat(result).isFalse();
	}
	
	
	
//	@Test
//	void test() {
//		
//		
//		
//		//【実行】
//		
//		
//		
//		//【検証】
//	}
}
