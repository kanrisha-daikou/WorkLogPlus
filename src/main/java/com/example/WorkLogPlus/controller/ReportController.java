package com.example.WorkLogPlus.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.WorkLogPlus.config.LoginUser;
import com.example.WorkLogPlus.dto.CommentDto;
import com.example.WorkLogPlus.dto.ReportDto;
import com.example.WorkLogPlus.entity.User;
import com.example.WorkLogPlus.exception.DupeReportException;
import com.example.WorkLogPlus.form.ReportForm;
import com.example.WorkLogPlus.service.CommentService;
import com.example.WorkLogPlus.service.ReportService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {

	private final ReportService reportService;
	private final CommentService commentService;
	
	
	
//	日報管理（一覧）画面
	@GetMapping
	public String showReportList(@AuthenticationPrincipal LoginUser loginUser, Model model) {
		
		User user = loginUser.getUser();
		
		List<ReportDto> reports = reportService.getUserReports(user);
		model.addAttribute("reports", reports);
		
		return "user/report/mylist";
	}
	
	
	
//	新規作成画面
	@GetMapping("/new")
	public String showReportCreate(Model model) {
		
		model.addAttribute("reportForm", new ReportForm());
		
		return "user/report/mycreate";
	}
	
	
	
//	詳細画面
	@GetMapping("/{id}")
	@PreAuthorize("@reportSecurity.isOwner(#reportId, principal)")
	public String showReportDetail(
			@AuthenticationPrincipal LoginUser loginUser, 
			@PathVariable("id") Long reportId, 
			Model model) {
		
		ReportDto reportDto = reportService.getReportById(reportId, loginUser.getUser());
		model.addAttribute("reportDto", reportDto);
		
		List<CommentDto> comments = commentService.getComments(reportId, loginUser.getUser());
		model.addAttribute("comments", comments);
		
		return "user/report/mydetail";
	}
	
	
	
//	編集画面
	@GetMapping("/{id}/edit")
	@PreAuthorize("@reportSecurity.isOwner(#reportId, principal)")
	public String showReportEdit(@PathVariable("id") Long reportId, Model model) {
		
		ReportForm reportForm = reportService.getReportFormById(reportId);
		
		model.addAttribute("reportForm", reportForm);
		model.addAttribute("reportId", reportId);
		
		return "user/report/myedit";
	}
	
	
	
//	新規登録リクエストを受け保存し一覧画面を返す
	@PostMapping("/create")
	public String createReport(
			@AuthenticationPrincipal LoginUser loginUser, 
			@Validated @ModelAttribute ReportForm reportForm, 
			BindingResult result, 
			RedirectAttributes redirectAttributes, 
			Model model) {
		
		if(result.hasErrors()) {
			return "user/report/mycreate";
		}
		
		try {
			reportService.saveReport(loginUser.getUser(), reportForm);
			
		} catch(DupeReportException e) {
			result.rejectValue("reportDate", "error.dupe", e.getMessage());
			
			return "user/report/mycreate";
		}
		
		redirectAttributes.addFlashAttribute("okMessage", "日報を登録しました");
		
		return "redirect:/reports";
	}
	
	
	
//	日報更新リクエストを処理して、成功で詳細画面を返す
	@PostMapping("/{id}/update")
	@PreAuthorize("@reportSecurity.isOwner(#reportId, principal)")
	public String updateReport(
			@AuthenticationPrincipal LoginUser loginUser, 
			@PathVariable("id") Long reportId, 
			@Validated @ModelAttribute ReportForm reportForm, 
			BindingResult result, 
			RedirectAttributes redirectAttributes, 
			Model model) {
		
		if(result.hasErrors()) {
			model.addAttribute("reportId", reportId); 
			
			return "user/report/myedit";
		}
		
		try {
			reportService.updateReport(reportId, loginUser.getUser(), reportForm);
			
		} catch(DupeReportException e) {
			result.rejectValue("reportDate", "error.dupe", e.getMessage());
			model.addAttribute("reportId", reportId); 
			
			return "user/report/myedit";
		}
		
		redirectAttributes.addFlashAttribute("okMessage", "日報を更新しました");
		
		return "redirect:/reports/" + reportId;
	}
}
