package com.example.WorkLogPlus.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.WorkLogPlus.dto.TaskDto;
import com.example.WorkLogPlus.entity.Task;
import com.example.WorkLogPlus.entity.TaskStatus;
import com.example.WorkLogPlus.entity.User;
import com.example.WorkLogPlus.form.TaskForm;
import com.example.WorkLogPlus.mapper.TaskMapper;
import com.example.WorkLogPlus.repository.TaskRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TaskService {

	private final TaskRepository taskRepository;
	private final TaskMapper taskMapper;
	
	
//	getUserTasks() 任意のユーザのタスクを全取得
	public List<TaskDto> getUserTasks(User user, String sort){
		Sort sortOrder = Sort.by(Sort.Order.asc("dueDate").with(Sort.NullHandling.NULLS_LAST));
		
		if("desc".equals(sort)) {
			sortOrder = Sort.by(Sort.Order.desc("dueDate").with(Sort.NullHandling.NULLS_FIRST));
		}
		
		Page<Task> taskPage = taskRepository.findByUserIdCustom(user, Pageable.unpaged(sortOrder));
		
		return taskPage.getContent().stream().map(taskMapper::toDto).toList();
	}
	
	
	
//【管理者用】	getAllTasks() 全ユーザーのタスクを全取得
	public List<TaskDto> getAllTasks(String sort){
		Sort sortOrder = Sort.by(Sort.Order.asc("dueDate").with(Sort.NullHandling.NULLS_LAST));
		
		if("desc".equals(sort)) {
			sortOrder = Sort.by(Sort.Order.desc("dueDate").with(Sort.NullHandling.NULLS_FIRST));
		}
		
		Page<Task> taskPage = taskRepository.findAllCustom(Pageable.unpaged(sortOrder));
		
		return taskPage.getContent().stream().map(taskMapper::toDto).toList();
	}
	
	
	
//	saveTask() タスクを保存
	@Transactional
	public void saveTask(TaskForm form, User user) {
		Task task = taskMapper.toEntity(form, user);
		
		taskRepository.save(task);
	}
	
	
	
//	updateTaskTitle() タイトルのみを更新
	@Transactional
	public void updateTaskTitle(Long id, String title) {
		Task task = taskRepository.findById(id).orElseThrow();
//		@Transactionalにより、リポジトリ.saveしなくてもsetだけで済む
		task.setTitle(title);
	}
	
	
	
// updateTaskStatus() ステータスのみを更新
	@Transactional
	public void updateTaskStatus(Long id, String statusStr) {
		Task task = taskRepository.findById(id).orElseThrow();
		
		task.setStatus(TaskStatus.valueOf(statusStr));
	}
	
	
	
// updateTaskDueDate() 期限のみを更新
	@Transactional
	public void updateTaskDueDate(Long id, LocalDate dueDate) {
		Task task = taskRepository.findById(id).orElseThrow();
		
		task.setDueDate(dueDate);
	}
	
	
	
// deleteTask() タスクを削除する
	@Transactional
	public void deleteTask(Long id) {
		
		taskRepository.deleteById(id);
	}
	
	
	
//	ログインユーザのタスク件数をステータスごとに集計して取得
	public Map<TaskStatus, Long> getStatusCounts(User user){
		
		return Map.of(
				TaskStatus.TODO, taskRepository.countByUserAndStatus(user, TaskStatus.TODO), 
				TaskStatus.DOING, taskRepository.countByUserAndStatus(user, TaskStatus.DOING), 
				TaskStatus.DONE, taskRepository.countByUserAndStatus(user, TaskStatus.DONE)
				);
	}
}
