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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.WorkLogPlus.config.LoginUser;
import com.example.WorkLogPlus.dto.CommentDto;
import com.example.WorkLogPlus.dto.ReportDto;
import com.example.WorkLogPlus.dto.UserDto;
import com.example.WorkLogPlus.entity.User;
import com.example.WorkLogPlus.exception.DupeReportException;
import com.example.WorkLogPlus.form.CommentForm;
import com.example.WorkLogPlus.form.ReportForm;
import com.example.WorkLogPlus.service.CommentService;
import com.example.WorkLogPlus.service.ReportService;
import com.example.WorkLogPlus.service.UserService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/admin/reports")
@RequiredArgsConstructor
public class AdminReportController {

	private final ReportService reportService;
	private final CommentService commentService;
	private final UserService userService;
	
	
	
//	日報管理（全ユーザ一覧）画面	
	@GetMapping
	public String adminReportList(
			@AuthenticationPrincipal LoginUser loginUser, 
			@RequestParam(required = false) Long searchUserId, 
			Model model) {
		
		List<ReportDto> allReports = reportService.getReportsBySearchUserId(searchUserId, loginUser.getUser());
		model.addAttribute("reports", allReports);
		
		List<UserDto> allUsers = userService.getAllUsers();
		model.addAttribute("users", allUsers);
		model.addAttribute("searchUserId", searchUserId);
		
		return "admin/report/list";
	}
	
	
	
//	新規作成画面
	@GetMapping("/new")
	public String adminReportCreate(Model model) {
		
		model.addAttribute("reportForm", new ReportForm());
		
		return "admin/report/create";
	}
	
	
	
//	詳細画面
	@GetMapping("/{id}")
	public String adminReportDetail(
			@AuthenticationPrincipal LoginUser loginUser, 
			@PathVariable("id") Long reportId, 
			Model model) {
		
		User currentAdmin = loginUser.getUser();
		
		ReportDto reportDto = reportService.getReportById(reportId, currentAdmin);
		model.addAttribute("reportDto", reportDto);
		
		List<CommentDto> comments = commentService.getComments(reportId, currentAdmin);
		model.addAttribute("comments", comments);
		
		model.addAttribute("commentForm", new CommentForm());
		
		return "admin/report/detail";
	}
	
	
	
//	編集画面
	@GetMapping("/{id}/edit")
	@PreAuthorize("@reportSecurity.isOwner(#reportId, principal)")
	public String showReportEdit(@PathVariable("id") Long reportId, Model model) {
		
		ReportForm reportForm = reportService.getReportFormById(reportId);
		
		model.addAttribute("reportForm", reportForm);
		model.addAttribute("reportId", reportId);
		
		return "admin/report/edit";
	}
	
	
	
//	新規日報リクエストを受け保存し一覧画面を返す
	@PostMapping("/create")
	public String saveReport(
			@AuthenticationPrincipal LoginUser loginUser, 
			@Validated @ModelAttribute ReportForm reportForm, 
			BindingResult result, 
			RedirectAttributes redirectAttributes, 
			Model model) {
		
		if(result.hasErrors()) {
			return "admin/report/create";
		}
		
		try {
			reportService.saveReport(loginUser.getUser(), reportForm);
			
		} catch(DupeReportException e) {
			result.rejectValue("reportDate", "error.dupe", e.getMessage());
			
			return "admin/report/create";
		}
		
		redirectAttributes.addFlashAttribute("okMessage", "日報を登録しました");
		
		return "redirect:/admin/reports";
	}
	
	
	
//	日報更新リクエストを受取る窓口（成功で詳細画面を返す）
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
			
			return "admin/report/edit";
		}
		
		try {
			reportService.updateReport(reportId, loginUser.getUser(), reportForm);
			
		} catch(DupeReportException e) {
			result.rejectValue("reportDate", "error.dupe", e.getMessage());
			model.addAttribute("reportId", reportId);
			
			return "admin/report/edit";
		}
		
		redirectAttributes.addFlashAttribute("okMessage", "日報を更新しました");
		
		return "redirect:/admin/reports/" + reportId;
	}
	
	
	
//	コメントを保存して詳細画面を返す
	@PostMapping("/{id}/save-comment")
	public String saveComment(
			@AuthenticationPrincipal LoginUser loginUser, 
			@PathVariable("id") Long reportId, 
			@Validated @ModelAttribute CommentForm commentForm, 
			BindingResult result, 
			RedirectAttributes redirectAttributes, 
			Model model) {
		
		User currentAdmin = loginUser.getUser();
		
		if(result.hasErrors()) {
			
			ReportDto reportDto = reportService.getReportById(reportId, currentAdmin);
			model.addAttribute("reportDto", reportDto);
			
			List<CommentDto> comments = commentService.getComments(reportId, currentAdmin);
			model.addAttribute("comments", comments);
			
			return "admin/report/detail";
		}
		
		commentService.saveComment(commentForm, reportId, currentAdmin.getId());
		redirectAttributes.addFlashAttribute("okMessage", "フィードバックを追加しました。");
		
		return "redirect:/admin/reports/" + reportId;
	}
	
	
	
//	コメントを削除して詳細画面を返す
	@PostMapping("/{reportId}/delete-comment/{commentId}")
	public String deleteComment(
			@AuthenticationPrincipal LoginUser loginUser, 
			@PathVariable Long reportId, 
			@PathVariable Long commentId, 
			RedirectAttributes redirectAttributes) {
		
		commentService.deleteComment(reportId, commentId, loginUser.getUser().getId());
		redirectAttributes.addFlashAttribute("okMessage", "コメントを削除しました。");
		
		return "redirect:/admin/reports/" + reportId;
	}
}
