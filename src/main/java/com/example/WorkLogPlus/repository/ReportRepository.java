package com.example.WorkLogPlus.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.WorkLogPlus.entity.Report;
import com.example.WorkLogPlus.entity.User;

public interface ReportRepository extends JpaRepository<Report, Long> {

	// 特定のユーザーの日報をすべて取得する（日付が新しい順）
	List<Report> findByUserOrderByReportDateDesc(User user);
	// 上記の内、直近Ｎ件（limit）だけを取得する
	List<Report> findByUserOrderByReportDateDesc(User user, Limit limit);
	
	// 特定のユーザーID（数字）から日報をすべて取得する（日付が新しい順）
	List<Report> findByUserIdOrderByReportDateDesc(Long userId);
	
	// 全日報を新しい順で取得する
	List<Report> findAllByOrderByReportDateDesc();
	
	// 【登録用重複チェック】指定したユーザーと日付の日報が在るか判定
	boolean existsByUserAndReportDate(User user, LocalDate reportDate);

	// 【編集用重複チェック】指定した日報ID（編集中の日報）以外で登録用のように判定
	boolean existsByIdNotAndUserAndReportDate(Long id, User user, LocalDate reportDate);
	
}
