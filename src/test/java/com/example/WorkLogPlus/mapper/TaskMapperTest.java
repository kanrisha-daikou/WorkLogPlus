package com.example.WorkLogPlus.mapper;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import com.example.WorkLogPlus.dto.TaskDto;
import com.example.WorkLogPlus.entity.Task;
import com.example.WorkLogPlus.entity.TaskStatus;
import com.example.WorkLogPlus.entity.User;

class TaskMapperTest {
	
	private final TaskMapper taskMapper = new TaskMapper();

	
	
	@Test
	void TaskエンティティからTaskDtoへ期限ありのデータが正しく加工変換できること() {
		
		User user = new User();
		user.setId(1L);
		user.setUsername("テストユーザー");
		
		Task task = new Task();
		task.setId(10L);
		task.setUser(user);
		task.setTitle("テストタスク");
		task.setStatus(TaskStatus.TODO);
		task.setDueDate(LocalDate.of(2026, 9, 28));
		task.setCreatedAt(LocalDateTime.of(2026, 9, 28, 10, 1));
		task.setUpdatedAt(LocalDateTime.of(2026, 9, 28, 11, 0));
		
		TaskDto dto = taskMapper.toDto(task);
		
		
		//【検証】
		assertThat(dto).isNotNull();
		
		//	期限がそれぞれのパターンで正しく加工されているか
		assertThat(dto.dueDate()).isEqualTo("2026/09/28");
		assertThat(dto.inputDueDate()).isEqualTo("2026-09-28");
		
		//	その他の値やEnumのラベルも正しく移し替えられているか
		assertThat(dto.title()).isEqualTo("テストタスク");
		assertThat(dto.status()).isEqualTo("TODO");
		assertThat(dto.statusLabel()).isEqualTo("未着手");
		assertThat(dto.createdAt()).isEqualTo("2026/09/28 10:01");
		assertThat(dto.updatedAt()).isEqualTo("2026/09/28 11:00");
	}
	
	
	
	@Test
	void Taskの期限が未設定の場合_一覧画面用は未設定になり編集画面用は空文字になること() {
		
		User user = new User();
		user.setId(1L);
		user.setUsername("テストユーザー");
		
		Task task = new Task();
		task.setId(10L);
		task.setUser(user);
		task.setTitle("期限なしタスク");
		task.setStatus(TaskStatus.TODO);
		task.setDueDate(null);	// 期限が未設定
		
		TaskDto dto = taskMapper.toDto(task);
		
		
		//	【検証】
		assertThat(dto.dueDate()).isEqualTo("未設定");
		assertThat(dto.inputDueDate()).isEmpty();
	}
	
	
	
	@Test
	void toDto_エンティティがnullの場合はnullを返すこと() {
		assertThat(taskMapper.toDto(null)).isNull();
	}
}
