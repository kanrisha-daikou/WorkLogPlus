package com.example.WorkLogPlus.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.WorkLogPlus.entity.Task;
import com.example.WorkLogPlus.entity.TaskStatus;
import com.example.WorkLogPlus.entity.User;
import com.example.WorkLogPlus.form.TaskForm;
import com.example.WorkLogPlus.mapper.TaskMapper;
import com.example.WorkLogPlus.repository.TaskRepository;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {
	
	@Mock
	private TaskRepository taskRepository;
	
	@Mock
	private TaskMapper taskMapper;
	
	@InjectMocks
	private TaskService taskService;
	
	
	
	@Test
	void saveTask_エンティティに変換されリポジトリが呼び出されること() {
		
		TaskForm form = new TaskForm();
		form.setTitle("新規保存タスク");
		
		User user = new User();
		
		Task task = new Task();
		task.setTitle("新規保存タスク");
		when(taskMapper.toEntity(form, user)).thenReturn(task);
		
		//【実行】
		taskService.saveTask(form, user);
		
		
		//【検証】
		//	捕獲の準備
		ArgumentCaptor<Task> taskCaptor = ArgumentCaptor.forClass(Task.class);
		//	リポジトリのsaveが確実に呼び出されたことの検証と、その時の引数（task）の捕獲
		verify(taskRepository).save(taskCaptor.capture());
		
		Task capturedTask = taskCaptor.getValue();
		assertThat(capturedTask).isNotNull();
		assertThat(capturedTask.getTitle()).isEqualTo("新規保存タスク");
	}
	
	
	
	@Test
	void updateTaskTitle_異常系_存在しないタスクIDの場合はNoSuchElementExceptionが投げられること() {
		
		when(taskRepository.findById(999L)).thenReturn(Optional.empty());
		
		//【実行＆検証】
		assertThatThrownBy(() -> taskService.updateTaskTitle(999L, "新タイトル")).isInstanceOf(NoSuchElementException.class);
	}
	
	
	
	@Test
	void updateTaskStatus_異常系_存在しないタスクIDの場合はNoSuchElementExceptionが投げられること() {
		
		when(taskRepository.findById(999L)).thenReturn(Optional.empty());
		
		//【実行＆検証】
		assertThatThrownBy(() -> taskService.updateTaskStatus(999L, TaskStatus.TODO.name())).isInstanceOf(NoSuchElementException.class);
	}
	
	
	
	@Test
	void updateTaskStatus_異常系_存在しない文字列を渡した場合はIllegalArgumentExceptionが投げられること() {
		
		Task task = new Task();
		Long taskId = 100L;
		task.setId(taskId);
		
		when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
		
		//【実行＆検証】
		assertThatThrownBy(() -> taskService.updateTaskStatus(taskId, "ウソのステータス")).isInstanceOf(IllegalArgumentException.class);
	}
	
	
	
	@Test
	void updateTaskDueDate_異常系_存在しないタスクIDの場合はNoSuchElementExceptionが投げられること() {
		
		when(taskRepository.findById(999L)).thenReturn(Optional.empty());
		
		//【実行＆検証】
		assertThatThrownBy(() -> taskService.updateTaskDueDate(999L, LocalDate.now())).isInstanceOf(NoSuchElementException.class);
	}
	
	
	
//	@Test
//	void t() {
//		
//		
//		
//		//【実行＆検証】
//		
//	}
	
	
	
//	@Test
//	void t() {
//		
//		
//		
//		//【実行】
//		
//		
//		//【検証】
//		
//	}
}
