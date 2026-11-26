package com.example.WorkLogPlus.service;

import org.springframework.stereotype.Component;

import com.example.WorkLogPlus.config.LoginUser;
import com.example.WorkLogPlus.entity.Report;
import com.example.WorkLogPlus.entity.User;
import com.example.WorkLogPlus.repository.ReportRepository;

import lombok.RequiredArgsConstructor;

@Component("reportSecurity") // @PreAuthorize("... reportSecurity ...") で呼べるようにします
@RequiredArgsConstructor
public class ReportSecurityService {

	private final ReportRepository reportRepository;
	
	
	
// 指定された日報が、ログイン中のユーザーのモノか判定する（アノテーション用）	
	public boolean isOwner(Long reportId, LoginUser loginUser) {
		if (loginUser == null) return false;
		
		// 日報の作成者IDと、ログインユーザーのIDが一致するかチェック
	    return isOwner(reportId, loginUser.getUser()); // ★下のと共通化
	}
	
	
	
// ～が、指定されたユーザのモノか判定する（Service等の手動呼び出し用）
	public boolean isOwner(Long reportId, User user) {
		if(user == null) return false;
		
		Report report = reportRepository.findById(reportId).orElse(null);
		if(report == null) {
			return false;
		}
		
		return report.getUser().getId().equals(user.getId());
	}
}
