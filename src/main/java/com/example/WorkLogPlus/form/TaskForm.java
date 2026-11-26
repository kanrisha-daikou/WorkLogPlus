package com.example.WorkLogPlus.form;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import org.springframework.format.annotation.DateTimeFormat;

import com.example.WorkLogPlus.entity.TaskStatus;

import lombok.Data;

@Data
public class TaskForm {

	@NotBlank(message = "タスク名は必須です")
	@Size(max = 100, message = "タスク名は１００文字以内で入力してください")
	private String title;
	
	@NotNull(message = "ステータスは必須です")
	private TaskStatus status;
	
	@DateTimeFormat(pattern = "yyyy-MM-dd") // タグのtype属性がdateのときvalue属性の値として認識してもらうため
	private LocalDate dueDate;
}
