package com.example.WorkLogPlus.service;

import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.WorkLogPlus.dto.CommentDto;
import com.example.WorkLogPlus.entity.Comment;
import com.example.WorkLogPlus.entity.Report;
import com.example.WorkLogPlus.entity.User;
import com.example.WorkLogPlus.form.CommentForm;
import com.example.WorkLogPlus.mapper.CommentMapper;
import com.example.WorkLogPlus.repository.CommentRepository;
import com.example.WorkLogPlus.repository.ReportRepository;
import com.example.WorkLogPlus.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommentService {

	private final CommentRepository commentRepository;
	private final ReportRepository reportRepository;
	private final UserRepository userRepository;
	private final CommentMapper commentMapper;
	private final ReportSecurityService reportSecurityService;
	
	
	
//	日報idに紐づいたコメントを全取得
	@Transactional // ←既読（UPDATE）処理を行うために付与
	public List<CommentDto> getComments(Long reportId, User loginUser){
		
		List<Comment> entities = commentRepository.findByReportIdOrderByCreatedAtAsc(reportId);
		
		boolean isReportOwner = reportSecurityService.isOwner(reportId, loginUser);
		
		for(Comment comment : entities) {
			// コメントが未読、かつ ログインユーザーが「日報の持ち主」である
			if(!comment.isRead() && isReportOwner) {
				comment.setRead(true);
			}
		}
		
		return entities.stream().map(entity -> commentMapper.toDto(entity, loginUser)).toList();
	}
	
	
	
//	コメントを保存
	@Transactional
	public void saveComment(CommentForm form, Long reportId, Long adminId) {
		
		Report report = reportRepository.findById(reportId).orElseThrow(() -> new IllegalArgumentException("指定された日報が見つかりません。ID:" + reportId));
		
		User admin = userRepository.findById(adminId).orElseThrow(() -> new IllegalArgumentException("指定された管理者が見つかりません。ID:" + adminId));
		
		if(reportSecurityService.isOwner(reportId, admin)) {
			throw new AccessDeniedException("不正なリクエストです。自分の日報にフィードバックを送信することはできません。");
		}
		
		Comment comment = commentMapper.toEntity(form, report, admin);
		
		commentRepository.save(comment);
	}
	
	
	
//	削除
	@Transactional
	public void deleteComment(Long reportId, Long commentId, Long loginUserId) {
		
		Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new IllegalArgumentException("指定されたコメントが見つかりません。ID:" + commentId));
		
		if(!comment.getReport().getId().equals(reportId)) {
			throw new IllegalArgumentException("不正なリクエストです。日報とコメントの整合性が取れません。");
		}
		
		// コメントの作成者と、ログイン中の管理者が、一致するかIDで判定して本人確認（403エラー画面へ）
		// ここだけの為に@PreAuthorizeとそれ用のsecurityクラスを増やすのを嫌ってこうしました。
		if(!comment.getAdmin().getId().equals(loginUserId)) {
			throw new AccessDeniedException("不正なリクエストです。他人のフィードバックを削除する権限がありません。");
		}
		
		commentRepository.delete(comment);
	}
	
}
