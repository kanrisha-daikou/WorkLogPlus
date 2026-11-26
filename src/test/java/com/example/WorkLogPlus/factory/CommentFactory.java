package com.example.WorkLogPlus.factory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.WorkLogPlus.entity.Comment;
import com.example.WorkLogPlus.entity.Report;
import com.example.WorkLogPlus.entity.User;
import com.example.WorkLogPlus.repository.CommentRepository;

@Component
public class CommentFactory {
	
	@Autowired
	private CommentRepository commentRepository;
	
	
	
	public Comment create(Report report, User admin) {
		
		Comment comment = new Comment();
		comment.setReport(report);
		comment.setAdmin(admin);
		comment.setBody("テスト用フィードバック本文です。");
		comment.setRead(false);
		
		return commentRepository.save(comment);
	}
	
	
	
	public Comment create(Report report, User admin, boolean isRead) {
		
		Comment comment = new Comment();
		comment.setReport(report);
		comment.setAdmin(admin);
		comment.setBody("テスト用フィードバック本文です。");
		comment.setRead(isRead);
		
		return commentRepository.save(comment);
	}
}
