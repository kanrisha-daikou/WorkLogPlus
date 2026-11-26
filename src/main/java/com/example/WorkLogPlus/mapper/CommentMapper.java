package com.example.WorkLogPlus.mapper;

import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Component;

import com.example.WorkLogPlus.dto.CommentDto;
import com.example.WorkLogPlus.entity.Comment;
import com.example.WorkLogPlus.entity.Report;
import com.example.WorkLogPlus.entity.User;
import com.example.WorkLogPlus.form.CommentForm;

@Component
public class CommentMapper {

	private static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");
	
	
	
//	Commentエンティティをブラウザへ渡す専用のCommentDtoへ移すメソッド
	public CommentDto toDto(Comment entity, User loginUser) {
		if(entity == null) {
			return null;
		}
		
		String formattedCreatedAt = (entity.getCreatedAt() == null) ? "" : entity.getCreatedAt().format(DATETIME_FORMAT);
		
		boolean isOwn = false;
		if(entity.getAdmin() != null && loginUser != null) {
			isOwn = entity.getAdmin().getId().equals(loginUser.getId());
		}
		
		return new CommentDto(
				entity.getId(),
				entity.getReport().getId(),
				entity.getAdmin().getId(),
				entity.getAdmin().getUsername(),
				entity.getBody(),
				formattedCreatedAt,
				isOwn);
	}
	
	
	
//	CommentFormをDB保存用のCommentエンティティへ移すメソッド
	public Comment toEntity(CommentForm form, Report report, User admin) {
		if(form == null) {
			return null;
		}
		
		Comment comment = new Comment();
		comment.setReport(report);
		comment.setAdmin(admin);
		comment.setBody(form.getBody());
		
		return comment;
	}
}
