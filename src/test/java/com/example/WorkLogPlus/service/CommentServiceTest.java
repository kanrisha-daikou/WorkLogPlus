package com.example.WorkLogPlus.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import com.example.WorkLogPlus.dto.CommentDto;
import com.example.WorkLogPlus.entity.Comment;
import com.example.WorkLogPlus.entity.Report;
import com.example.WorkLogPlus.entity.User;
import com.example.WorkLogPlus.form.CommentForm;
import com.example.WorkLogPlus.mapper.CommentMapper;
import com.example.WorkLogPlus.repository.CommentRepository;
import com.example.WorkLogPlus.repository.ReportRepository;
import com.example.WorkLogPlus.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {
	
	@Mock
	private ReportRepository reportRepository;
	
	@Mock
	private UserRepository userRepository;
	
	@Mock
	private CommentRepository commentRepository;
	
	@Mock
	private CommentMapper commentMapper;
	
	@Mock
	private ReportSecurityService reportSecurityService;
	
	@InjectMocks
	private CommentService commentService;
	
	
	
	@Test
	void getComments_自分の日報を自身が閲覧した場合_未読は既読になり既読はそのままでDtoのリストが返ること() {
		
		Long reportId = 99L;
		User loginUser = new User(); // 日報の持ち主
		
		Comment c1 = new Comment();
		c1.setRead(false); // 現状は未読
		
		Comment c2 = new Comment();
		c2.setRead(true); // 現状は既読
		
		List<Comment> mockList = List.of(c1, c2);
		
		when(commentRepository.findByReportIdOrderByCreatedAtAsc(reportId)).thenReturn(mockList);
		
		//	loginUserが日報の持ち主であるとして準備しておく
		when(reportSecurityService.isOwner(reportId, loginUser)).thenReturn(true);
		
		when(commentMapper.toDto(any(Comment.class), eq(loginUser))).thenReturn(new CommentDto(null, null, null, null, null, null, false));
		
		//【実行】
		List<CommentDto> result = commentService.getComments(reportId, loginUser);
		
		
		//【検証】
		//	戻り値のリストが正しく受け取れているか
		assertThat(result).hasSize(2);
		//	CommentエンティティのisReadが期待通りに true（既読）に書き換わること
		assertThat(c1.isRead()).as("未読だったコメントが既読に更新されていること").isTrue();
		assertThat(c2.isRead()).as("すでに既読のコメントは既読のままであること").isTrue();
	}
	
	
	
	@Test
	void getComments_日報を持ち主以外が閲覧した場合_未読コメントは未読のまま維持されてDtoのリストが返ること() {
		
		Long reportId = 99L;
		User loginUser = new User(); // 管理者や他者（持ち主ではない）
		
		Comment c1 = new Comment();
		c1.setRead(false); // 現状は未読
		
		List<Comment> mockList = List.of(c1);
		
		when(commentRepository.findByReportIdOrderByCreatedAtAsc(reportId)).thenReturn(mockList);
		
		//	loginUserは日報の持ち主ではない(false)として準備しておく
		when(reportSecurityService.isOwner(reportId, loginUser)).thenReturn(false);
		
		when(commentMapper.toDto(any(Comment.class), eq(loginUser))).thenReturn(new CommentDto(null, null, null, null, null, null, false));
		
		//【実行】
		List<CommentDto> result = commentService.getComments(reportId, loginUser);
		
		
		//【検証】
		assertThat(result).hasSize(1);
		//	持ち主ではないので、未読（false）のままであること
		assertThat(c1.isRead()).as("持ち主以外が読んだので、未読のまま維持されていること").isFalse();
	}
	
	
	
	@Test
	void saveComment_他人の日報にフィードバックした場合_コメント保存処理が呼ばれること() {
		
		Long reportId = 11L;
		Long adminId = 22L;
		
		Report report = new Report();
		User admin = new User();
		
		when(reportRepository.findById(reportId)).thenReturn(Optional.of(report));
		when(userRepository.findById(adminId)).thenReturn(Optional.of(admin));
		//	他人の日報なので、isOwnerの結果は false（持ち主ではない）を返すように設定
		when(reportSecurityService.isOwner(reportId, admin)).thenReturn(false);
		
		CommentForm form = new CommentForm();
		form.setBody("保存テストコメント");
		
		Comment comment = new Comment();
		
		when(commentMapper.toEntity(form, report, admin)).thenReturn(comment);
		
		//【実行】
		commentService.saveComment(form, reportId, adminId);
		
		
		//【検証】
		//	このメソッドは戻り値（void）がないため、Mockito の「verify」を使って、
		//	save メソッドが comment を引数にして1回実行されたことを検証します
		verify(commentRepository, times(1)).save(comment);
	}
	
	
	
	@Test
	void saveComment_指定された日報が存在しない場合はIllegalArgumentExceptionが投げられ保存されないこと() {
		
		Long reportId = 9999L;
		Long adminId = 99L;
		CommentForm form = new CommentForm();
		form.setBody("不正なテストコメント");
		
		when(reportRepository.findById(reportId)).thenReturn(Optional.empty());
		
		
		//【実行＆検証】
		assertThatThrownBy(() -> commentService.saveComment(form, reportId, adminId))
		.isInstanceOf(IllegalArgumentException.class)
		.hasMessage("指定された日報が見つかりません。ID:" + reportId);
		
		verify(commentRepository, never()).save(any());
	}
	
	
	
	@Test
	void saveComment_指定された管理者IDが存在しない場合はIllegalArgumentExceptionが投げられ保存されないこと() {
		
		Long reportId = 99L;
		Long adminId = 9999L;
		Report report = new Report();
		CommentForm form = new CommentForm();
		form.setBody("不正なテストコメント");
		
		when(reportRepository.findById(reportId)).thenReturn(Optional.of(report));
		when(userRepository.findById(adminId)).thenReturn(Optional.empty());
		
		
		//【実行＆検証】
		assertThatThrownBy(() -> commentService.saveComment(form, reportId, adminId))
		.isInstanceOf(IllegalArgumentException.class)
		.hasMessage("指定された管理者が見つかりません。ID:" + adminId);
		
		verify(commentRepository, never()).save(any());
	}
	
	
	
	@Test
	void saveComment_自分の日報にフィードバックしようとした場合_AccessDeniedExceptionが発生して保存されないこと() {
		
		Long reportId = 11L;
		Long adminId = 22L;
		
		Report report = new Report();
		User admin = new User();
		
		when(reportRepository.findById(reportId)).thenReturn(Optional.of(report));
		when(userRepository.findById(adminId)).thenReturn(Optional.of(admin));
		//	自分の日報への想定なので、 true（持ち主である）を返すように設定
		when(reportSecurityService.isOwner(reportId, admin)).thenReturn(true);
		
		CommentForm form = new CommentForm();
		form.setBody("不正なテストコメント");
		
		
		//【実行＆検証】
		assertThatThrownBy(() -> commentService.saveComment(form, reportId, adminId))
		.isInstanceOf(AccessDeniedException.class)
		.hasMessage("不正なリクエストです。自分の日報にフィードバックを送信することはできません。");
		
		//	このメソッドは戻り値（void）がないため、Mockito の「verify」を使って、
		//	save メソッドが一度も実行（保存）されていないことを検証します
		verify(commentRepository, never()).save(any());
	}
	
	
	
	@Test
	void deleteComment_正しい引数を渡した場合_deleteが呼ばれるか() {
		
		Long reportId = 11L;
		Long commentId = 22L;
		Long loginUserId = 33L;
		
		Report report = new Report();
		report.setId(reportId);
		
		User admin = new User();
		admin.setId(loginUserId);
		
		Comment comment = new Comment();
		comment.setId(commentId);
		comment.setReport(report);
		comment.setAdmin(admin);
		
		when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
		
		//【実行】
		commentService.deleteComment(reportId, commentId, loginUserId);
		
		
		//【検証】
		//	ここも戻り値がないため、delete が1回実行されたことを検証します
		verify(commentRepository, times(1)).delete(comment);
	}
	
	
	
	@Test
	void deleteComment_コメントが存在しない場合_IllegalArgumentExceptionがスローされるか() {
		
		Long reportId = 11L;
		Long commentId = 22L;
		Long loginUserId = 33L;
		
		when(commentRepository.findById(commentId)).thenReturn(Optional.empty());
		
		
		//【実行＆検証】例外の種類とそのメッセージ内容のチェック
		assertThatThrownBy(() -> commentService.deleteComment(reportId, commentId, loginUserId))
		.isInstanceOf(IllegalArgumentException.class)
		.hasMessage("指定されたコメントが見つかりません。ID:" + commentId);
		//	例外が出る想定なので delete が一度も実行されないことの確認
		verify(commentRepository, never()).delete(any());
	}
	
	
	
	@Test
	void deleteComment_コメントと日報の整合性が合わない場合_IllegalArgumentExceptionがスローされるか() {
		
		Long commentId = 11L;
		Long loginUserId = 22L;
		
		Report report = new Report();
		report.setId(33L);
		
		Comment comment = new Comment();
		comment.setId(commentId);
		comment.setReport(report);
		
		when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
		Long requestId = 999L;
		
		//【実行＆検証】
		assertThatThrownBy(() -> commentService.deleteComment(requestId, commentId, loginUserId))
		.isInstanceOf(IllegalArgumentException.class)
		.hasMessage("不正なリクエストです。日報とコメントの整合性が取れません。");
		
		verify(commentRepository, never()).delete(any());
	}
	
	
	
	@Test
	void deleteComment_削除権限がない場合_AccessDeniedExceptionがスローされるか() {
		
		Long reportId = 11L;
		Long commentId = 22L;
		
		Report report = new Report();
		report.setId(reportId);
		
		User otherAdmin = new User();
		otherAdmin.setId(33L);
		
		Comment comment = new Comment();
		comment.setReport(report);
		comment.setAdmin(otherAdmin);
		
		when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
		Long loginUserId = 999L;
		
		
		//【実行＆検証】
		assertThatThrownBy(() -> commentService.deleteComment(reportId, commentId, loginUserId))
		.isInstanceOf(AccessDeniedException.class)
		.hasMessage("不正なリクエストです。他人のフィードバックを削除する権限がありません。");
		
		verify(commentRepository, never()).delete(any());
	}
	
	
	
//	@Test
//	void test() {
//		
//	}
}
