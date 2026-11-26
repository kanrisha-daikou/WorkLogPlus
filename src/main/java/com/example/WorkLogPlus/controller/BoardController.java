package com.example.WorkLogPlus.controller;

import java.util.List;
import java.util.Map;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.WorkLogPlus.config.LoginUser;
import com.example.WorkLogPlus.dto.ReportDto;
import com.example.WorkLogPlus.entity.TaskStatus;
import com.example.WorkLogPlus.entity.User;
import com.example.WorkLogPlus.service.ReportService;
import com.example.WorkLogPlus.service.TaskService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class BoardController {
	
	private final TaskService taskService;
	private final ReportService reportService;
	
	
	
//	ログイン後のトップ画面
	@GetMapping("/")
	public String userDashboard(@AuthenticationPrincipal LoginUser loginUser, Model model) {
		
		User user = loginUser .getUser();
		
		String realName = loginUser.getRealName();
		model.addAttribute("loginUserName", realName);
		
		Map<TaskStatus, Long> taskCounts = taskService.getStatusCounts(user);
		model.addAttribute("taskCounts", taskCounts);
		
		List<ReportDto> recentReports = reportService.getRecentReports(user, 3);
		model.addAttribute("recentReports", recentReports);
		
		boolean hasUnreadComments = reportService.hasUnreadComments(user);
		model.addAttribute("hasUnreadComments", hasUnreadComments);
		
		
		boolean isAdmin = loginUser.getAuthorities().stream().anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
		
		if(isAdmin) {
			return "admin/dashboard";
		}
		
		return "user/dashboard";	
	}
	
}

