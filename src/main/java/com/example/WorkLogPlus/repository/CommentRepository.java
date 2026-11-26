package com.example.WorkLogPlus.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.WorkLogPlus.entity.Comment;
import com.example.WorkLogPlus.entity.User;

public interface CommentRepository extends JpaRepository<Comment, Long> {

	// 指定された日報（ID）付いた全コメントを、古い順で取得
	List<Comment> findByReportIdOrderByCreatedAtAsc(Long reportId);
	
	// 【未読チェック】特定ユーザの日報に付いたコメントの内、未読（既読がfalse）のものが１件でもあるかを判定
	boolean existsByReportUserAndIsReadFalse(User user);
}
