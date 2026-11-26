package com.example.WorkLogPlus.mapper;

import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Component;

import com.example.WorkLogPlus.dto.ReportDto;
import com.example.WorkLogPlus.entity.Report;
import com.example.WorkLogPlus.entity.User;
import com.example.WorkLogPlus.form.ReportForm;

@Component
public class ReportMapper {

	private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy/MM/dd(E)");
	private static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");
	
	
//	【新規用】ReportFormをDB保存用のReportエンティティへ移すメソッド
	public Report toEntity(ReportForm form, User user) {
		if(form == null) {
			return null;
		}
		
		Report report = new Report();
		report.setUser(user);
		report.setReportDate(form.getReportDate());
		report.setContent(form.getContent());
		report.setImpressions(form.getImpressions());
		report.setIssues(form.getIssues());
		
		return report;
	}
	
	
	
//	【更新用】ReportFormを既存のReportエンティティへ移すメソッド
	public void updateEntity(ReportForm form, Report entity) {
		if(form == null || entity == null) {
			return;
		}
		
		entity.setReportDate(form.getReportDate());
		entity.setContent(form.getContent());
		entity.setImpressions(form.getImpressions());
		entity.setIssues(form.getIssues());
	}
	
	
	
//	【閲覧用】Reportエンティティをブラウザへ渡す専用のReportDtoへ移すメソッド
	public ReportDto toDto(Report entity, User loginUser) {
		if(entity == null) {
			return null;
		}
		
		String formattedReportDate = (entity.getReportDate() == null) ? "" : entity.getReportDate().format(DATE_FORMAT);
		String formattedCreatedAt = (entity.getCreatedAt() == null) ? "" : entity.getCreatedAt().format(DATETIME_FORMAT);
		
		int commentCount = 0;
		if(entity.getComments() != null) {
			commentCount = entity.getComments().size();
		}
		
		//	未読コメントが１件でも有ればここが true になる
		boolean unRead = false;
		if(entity.getComments() != null) {
			unRead = entity.getComments().stream().anyMatch(comment -> !comment.isRead());
		}
		
		//	本物のログインユーザーが存在する場合のみIDを比較する
		boolean isOwn = false;
		if(entity.getUser() != null && loginUser != null) {
			isOwn = entity.getUser().getId().equals(loginUser.getId());
		}
				
		return new ReportDto(
				entity.getId(),
				entity.getUser().getId(),
				entity.getUser().getUsername(),
				formattedReportDate,
				entity.getContent(),
				entity.getImpressions(),
				entity.getIssues(),
				formattedCreatedAt,
				commentCount,
				unRead,
				isOwn);
	}
	
	
	
//	【編集画面用】Reportエンティティを編集画面用のReportFormへ移すメソッド
	public ReportForm toForm(Report entity) {
		if(entity == null) {
			return null;
		}
		
		ReportForm form = new ReportForm();
		form.setReportDate(entity.getReportDate());
		form.setContent(entity.getContent());
		form.setImpressions(entity.getImpressions());
		form.setIssues(entity.getIssues());
		
		return form;
	}
}
