package com.example.WorkLogPlus.service.integration;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.annotation.Transactional;

import com.example.WorkLogPlus.dto.CommentDto;
import com.example.WorkLogPlus.entity.Comment;
import com.example.WorkLogPlus.entity.Report;
import com.example.WorkLogPlus.entity.Role;
import com.example.WorkLogPlus.entity.User;
import com.example.WorkLogPlus.factory.CommentFactory;
import com.example.WorkLogPlus.factory.ReportFactory;
import com.example.WorkLogPlus.factory.UserFactory;
import com.example.WorkLogPlus.form.CommentForm;
import com.example.WorkLogPlus.repository.CommentRepository;
import com.example.WorkLogPlus.service.CommentService;

@SpringBootTest
@Transactional
class CommentServiceIT {
	
	@Autowired
	private CommentService commentService;
	
	@Autowired
	private CommentRepository commentRepository;
	
	@Autowired
	private UserFactory userFactory;
	
	@Autowired
	private ReportFactory reportFactory;
	
	@Autowired
	private CommentFactory commentFactory;
	
	
	
	@Test
	void getComments_日報を本人が見た場合_DB上の未読コメントが自動で既読に更新されること() {
		
		User owner = userFactory.create("testUser");
		User admin = userFactory.create("testAdmin");
		Report report = reportFactory.create(owner);
		Comment comment = commentFactory.create(report, admin);
		
		//【実行】
		List<CommentDto> result = commentService.getComments(report.getId(), owner);
		
		
		//【検証】
		Comment savedComment = commentRepository.findById(comment.getId()).orElseThrow();
		//	save()を呼んでいなくても、DB側が既読(true)に書き換わっているか
		assertThat(savedComment.isRead()).as("データベース上のフラグが既読(true)に自動更新されていること").isTrue();
		assertThat(result).hasSize(1);
	}
	
	
	
	@Test
	void getComments_日報の持ち主以外が取得した場合はDB上の未読コメントが未読のまま維持されること() {
		
		User owner = userFactory.create("owner");
		User admin = userFactory.create("admin");
		Report targetReport = reportFactory.create(owner, LocalDate.of(2026, 11, 24));
		
		Comment unreadComment = commentFactory.create(targetReport, admin, false);
		commentRepository.save(unreadComment);
		
		//【実行】（管理者＝日報の持ち主以外が閲覧）
		List<CommentDto> result = commentService.getComments(targetReport.getId(), admin);
		
		//【検証】取得はできるが、DB上の isRead は false のまま変わらないこと
		assertThat(result).hasSize(1);
		Comment notUpdated = commentRepository.findById(unreadComment.getId()).orElseThrow();
		assertThat(notUpdated.isRead()).isFalse();
	}
	
	
	
	@Test
	void saveComment_他人の日報に対してコメントがDBに正常に保存されること() {
		
		User owner = userFactory.create("owner");
		User admin = userFactory.create("admin", Role.ADMIN);
		Report report = reportFactory.create(owner);
		Long reportId = report.getId();
		
		CommentForm form = new CommentForm();
		form.setBody("コメント保存テストの本文です");		
		
		//【実行】
		commentService.saveComment(form, reportId, admin.getId());
		
		
		//【検証】
		List<Comment> savedComment = commentRepository.findByReportIdOrderByCreatedAtAsc(reportId);
		assertThat(savedComment).hasSize(1);
		assertThat(savedComment.get(0).getBody()).isEqualTo("コメント保存テストの本文です");
		assertThat(savedComment.get(0).getAdmin().getId()).isEqualTo(admin.getId());
		assertThat(savedComment.get(0).isRead()).isFalse();
	}
	
	
	
	@Test
	void saveComment_自分の日報にコメントしようとした場合はAccessDeniedExceptionが投げられ保存されないこと() {
		
		User admin = userFactory.create("admin", Role.ADMIN);
		Report report = reportFactory.create(admin);
		Long reportId = report.getId();
		
		CommentForm form = new CommentForm();
		form.setBody("コメント保存テストの本文です");		
		
		
		//【実行＆検証】
		assertThatThrownBy(() -> commentService.saveComment(form, reportId, admin.getId()))
		.isInstanceOf(AccessDeniedException.class)
		.hasMessage("不正なリクエストです。自分の日報にフィードバックを送信することはできません。");
		
		List<Comment> comments = commentRepository.findByReportIdOrderByCreatedAtAsc(reportId);
		assertThat(comments).isEmpty();
	}
	
	
	
	@Test
	void deleteComment_コメントの作成者本人が削除した場合_正常に削除されること() {
		
		User reportOwner = userFactory.create("reportOwner");
		Report report = reportFactory.create(reportOwner);
		
		User admin = userFactory.create("admin");
		Comment comment = commentFactory.create(report, admin);
		
		//【実行】
		commentService.deleteComment(report.getId(), comment.getId(), admin.getId());
		
		
		//【検証】
		Optional<Comment> deleted = commentRepository.findById(comment.getId());
		assertThat(deleted).as("DB上からコメントが完全に削除されていること").isEmpty();
	}
	
	
	
	@Test
	void deleteComment_他者のコメントを削除しようとした場合_AccessDeniedExceptionが投げられ削除されないこと() {
		
		User reportOwner = userFactory.create("reportOwner");
		Report report = reportFactory.create(reportOwner);
		
		User admin = userFactory.create("admin");
		Comment comment = commentFactory.create(report, admin);
		
		User admin2 = userFactory.create("admin2");
		
		
		//【実行＆検証】
		assertThatThrownBy(() -> commentService.deleteComment(report.getId(), comment.getId(), admin2.getId()))
		.isInstanceOf(AccessDeniedException.class)
		.hasMessage("不正なリクエストです。他人のフィードバックを削除する権限がありません。");
		
		Optional<Comment> deleted = commentRepository.findById(comment.getId());
		assertThat(deleted).isPresent();
	}
	
	
	
//	@Test
//	void test() {
//		
//		
//		//【実行】
//		
//		
//		//【検証】
//		
//		
//		//【実行＆検証】
//	}
}
