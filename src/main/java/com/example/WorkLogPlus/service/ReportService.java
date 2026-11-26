package com.example.WorkLogPlus.service;

import java.util.List;

import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.WorkLogPlus.dto.ReportDto;
import com.example.WorkLogPlus.entity.Report;
import com.example.WorkLogPlus.entity.User;
import com.example.WorkLogPlus.exception.DupeReportException;
import com.example.WorkLogPlus.form.ReportForm;
import com.example.WorkLogPlus.mapper.ReportMapper;
import com.example.WorkLogPlus.repository.CommentRepository;
import com.example.WorkLogPlus.repository.ReportRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReportService {

	private final ReportRepository reportRepository;
	private final ReportMapper reportMapper;
	private final CommentRepository commentRepository;
	
	
	
//	getUserReport() 任意のユーザの日報を全取得(日付が新しい順)
	public List<ReportDto> getUserReports(User loginUser){
		
		List<Report> entities = reportRepository.findByUserOrderByReportDateDesc(loginUser);
		
		return entities.stream().map(entity -> reportMapper.toDto(entity, loginUser)).toList();
	}
	
	
	
//	【ダッシュボード用】getRecentReports() ログインユーザーの直近N件の日報を取得するメソッド
	public List<ReportDto> getRecentReports(User loginUser, int limit){
		
		List<Report> entities = reportRepository.findByUserOrderByReportDateDesc(loginUser, Limit.of(limit));
		
		return entities.stream().map(entity -> reportMapper.toDto(entity, loginUser)).toList();
	}
	
	
	
//	【管理者用】全員の日報、または指定したユーザーIDで絞り込んだ日報を取得
	public List<ReportDto> getReportsBySearchUserId(Long searchUserId, User loginUser){
		
		List<Report> entities;
		
		if(searchUserId != null) {
			
			entities = reportRepository.findByUserIdOrderByReportDateDesc(searchUserId);
		
		} else {
			
			entities = reportRepository.findAllByOrderByReportDateDesc();
		}
		
		return entities.stream().map(entity -> reportMapper.toDto(entity, loginUser)).toList();
	}
	
	
	
//	getReportById() 【詳細画面用】指定の日報idの日報を1件取得
	public ReportDto getReportById(Long reportId, User loginUser) {
		
		Report report = reportRepository.findById(reportId).orElseThrow();
		
		return reportMapper.toDto(report, loginUser);
	}
	
	
	
//	getReportFormById() 【編集画面用】指定の日報idの日報を1件取得
	public ReportForm getReportFormById(Long reportId) {
		
		Report report = reportRepository.findById(reportId).orElseThrow();
		
		return reportMapper.toForm(report);
	}
	
	
	
//	saveReport() 日報を保存
	@Transactional
	public void saveReport(User loginUser, ReportForm form) {
		
		if(reportRepository.existsByUserAndReportDate(loginUser, form.getReportDate())) {
			throw new DupeReportException("この日付の日報は既に存在します");
		}
		
		Report report = reportMapper.toEntity(form, loginUser);
		reportRepository.save(report);
	}
	
	
	
//	updateReport() 指定の日報idの日報を更新
	@Transactional
	public void updateReport(Long reportId, User loginUser, ReportForm form) {
		
		Report report = reportRepository.findById(reportId).orElseThrow();
		
		if(reportRepository.existsByIdNotAndUserAndReportDate(reportId, loginUser, form.getReportDate())) {
			throw new DupeReportException("この日付の日報は既に存在します");
		}
		
		reportMapper.updateEntity(form, report);
	}
	
	
	
//	未読の管理者コメントがあるか調べるメソッド
	public boolean hasUnreadComments(User loginUser) {
		
		return commentRepository.existsByReportUserAndIsReadFalse(loginUser);
	}
	
}
