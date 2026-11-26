package com.example.WorkLogPlus.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.WorkLogPlus.config.LoginUser;
import com.example.WorkLogPlus.dto.TaskDto;
import com.example.WorkLogPlus.form.TaskForm;
import com.example.WorkLogPlus.form.TaskTitleForm;
import com.example.WorkLogPlus.service.TaskService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/tasks")
@RequiredArgsConstructor
public class TaskController {
	
	private final TaskService taskService;

	
	
//	本人のタスク一覧画面
	@GetMapping
	public String showTaskList(
			@AuthenticationPrincipal LoginUser loginUser, 
			@RequestParam(defaultValue = "asc") String sort, 
			Model model) {
		
		List<TaskDto> tasks = taskService.getUserTasks(loginUser.getUser(), sort);
		model.addAttribute("tasks", tasks);
		
		model.addAttribute("currentSort", sort);
		
		if(!model.containsAttribute("taskForm")) {
			model.addAttribute("taskForm", new TaskForm());
		}
		
		return "task_list";
	}
	
	
	
//	【管理者用】全ユーザーのタスク一覧画面を表示する
	@GetMapping("/admin")
	@PreAuthorize("hasRole('ADMIN')") //URLが「/tasks」から始まりコンフィグをすり抜けるので
	public String showAdminTaskList(
			@RequestParam(defaultValue = "asc") String sort, 
			Model model) {
		
		List<TaskDto> allTasks = taskService.getAllTasks(sort);
		model.addAttribute("tasks", allTasks);
		
		model.addAttribute("currentSort", sort);
		
		return "admin/all_tasks";
	}
	


//新規タスク登録リクエストを処理して一覧画面を返す
	@PostMapping("/create")
	public String createTask(
			@AuthenticationPrincipal LoginUser loginUser, 
			@Validated @ModelAttribute TaskForm taskForm, 
			BindingResult result, 
			RedirectAttributes redirectAttributes, 
			Model model) {
		
		if(result.hasErrors()) {
			
			redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.taskForm", result);
			redirectAttributes.addFlashAttribute("taskForm", taskForm);
			
			return "redirect:/tasks";
		}
		
		taskService.saveTask(taskForm, loginUser.getUser());
		redirectAttributes.addFlashAttribute("okMessage", "タスクを登録しました");
		
		return "redirect:/tasks";
	}
	
	
	
// タイトル更新リクエストを処理してJSへ結果を返す（非同期）
	@PostMapping("/{id}/update-title")
	@ResponseBody
	// 🛡️ 裏側のAOPが「ログインユーザー」と「引数のid」を自動で検証します
	@PreAuthorize("@taskSecurity.isOwner(#id, principal)")
	public ResponseEntity<?> updateTitle(
			@PathVariable Long id, 
			@Validated TaskTitleForm taskTitleForm, 
			BindingResult result){
		
		if(result.hasErrors()) {
			String errorMessage = result.getFieldError("title").getDefaultMessage();
			return ResponseEntity.badRequest().body(Map.of("detail", errorMessage));
		}
		
		taskService.updateTaskTitle(id, taskTitleForm.getTitle());
		return ResponseEntity.ok().build();
	}
	
	
	
//	ステータス更新リクエストを処理してJSへ結果を返す（非同期）
	@PostMapping("/{id}/update-status")
	@ResponseBody
	@PreAuthorize("@taskSecurity.isOwner(#id, principal)")
	public ResponseEntity<Void> updateStatus(@PathVariable Long id, @RequestParam String status){
		
		taskService.updateTaskStatus(id, status);
		return ResponseEntity.ok().build();
	}
	
	
	
//	期限更新リクエストを処理してJSへ結果を返す（非同期）
	@PostMapping("/{id}/update-duedate")
	@ResponseBody
	@PreAuthorize("@taskSecurity.isOwner(#id, principal)")
	public ResponseEntity<Void> updateDueDate(
			@PathVariable Long id, 
			@RequestParam 
			@DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dueDate){
		
		taskService.updateTaskDueDate(id, dueDate);
		return ResponseEntity.ok().build();
	}
	
	
	
//	削除リクエストを処理してJSへ結果を返す（非同期）
	@PostMapping("/{id}/delete")
	@ResponseBody
	@PreAuthorize("@taskSecurity.isOwner(#id, principal)")
	public ResponseEntity<Void> deleteTask(@PathVariable Long id){
		
		taskService.deleteTask(id);
		return ResponseEntity.ok().build();
	}
}
