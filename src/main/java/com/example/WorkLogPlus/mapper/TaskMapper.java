package com.example.WorkLogPlus.mapper;

import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Component;

import com.example.WorkLogPlus.dto.TaskDto;
import com.example.WorkLogPlus.entity.Task;
import com.example.WorkLogPlus.entity.User;
import com.example.WorkLogPlus.form.TaskForm;

@Component
public class TaskMapper {

	private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy/MM/dd");
	private static final DateTimeFormatter INPUT_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	private static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");


	
//	【新規用】TaskFormをDB保存用のReportエンティティへ移すメソッド
	public Task toEntity(TaskForm form, User user) {
		if(form == null) {
			return null;
		}
		
		Task task = new Task();
		task.setUser(user);
		task.setTitle(form.getTitle());
		task.setStatus(form.getStatus());
		task.setDueDate(form.getDueDate());
		
		return task;
	}
	
	
	
//	【画面用】Taskエンティティをブラウザへ渡す専用のTaskDtoへ移すメソッド
	public TaskDto toDto(Task entity) {
		if(entity == null) {
			return null;
		}
		
		String formattedDueDate = (entity.getDueDate() == null) ? "未設定" : entity.getDueDate().format(DATE_FORMAT);
		String inputDueDate = (entity.getDueDate() == null) ? "" : entity.getDueDate().format(INPUT_FORMAT);
		String formattedCreatedAt = (entity.getCreatedAt() == null) ? "" : entity.getCreatedAt().format(DATETIME_FORMAT);
		String formattedUpdatedAt = (entity.getUpdatedAt() == null) ? "" : entity.getUpdatedAt().format(DATETIME_FORMAT);
		
		return new TaskDto(
				entity.getId(),
				entity.getUser().getId(),
				entity.getUser().getUsername(),
				entity.getTitle(),
				entity.getStatus().name(),
				entity.getStatus().getLabel(),
				formattedDueDate,// ① 一覧画面のテキスト表示用（スラッシュ）
				inputDueDate,// ② 編集画面のカレンダー初期表示用（ハイフン）
				formattedCreatedAt,
				formattedUpdatedAt
				);
	}
}
