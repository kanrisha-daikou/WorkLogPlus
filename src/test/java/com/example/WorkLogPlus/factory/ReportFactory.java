package com.example.WorkLogPlus.factory;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.WorkLogPlus.entity.Report;
import com.example.WorkLogPlus.entity.User;
import com.example.WorkLogPlus.repository.ReportRepository;

@Component
public class ReportFactory {
	
	@Autowired
	private ReportRepository reportRepository;
	
	
	
	public Report create(User user) {
		
		Report report = new Report();
		report.setUser(user);
		report.setReportDate(LocalDate.now());
		report.setContent("テスト用の日報内容です。");
		
		return reportRepository.save(report);
	}
	
	
	
	public Report create(User user, LocalDate date) {
		
		Report report = new Report();
		report.setUser(user);
		report.setReportDate(date);
		report.setContent("テスト用の日報内容です。");
		
		return reportRepository.save(report);
	}
}
