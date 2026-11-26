package com.example.WorkLogPlus.service.integration;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.example.WorkLogPlus.dto.TaskDto;
import com.example.WorkLogPlus.entity.Task;
import com.example.WorkLogPlus.entity.TaskStatus;
import com.example.WorkLogPlus.entity.User;
import com.example.WorkLogPlus.factory.TaskFactory;
import com.example.WorkLogPlus.factory.UserFactory;
import com.example.WorkLogPlus.repository.TaskRepository;
import com.example.WorkLogPlus.service.TaskService;

@SpringBootTest
@Transactional
class TaskServiceIT {
	
	@Autowired
	private TaskService taskService;
	
	@Autowired
	private TaskRepository taskRepository;
	
	@Autowired
	private TaskFactory taskFactory;
	
	@Autowired
	private UserFactory userFactory;
	
	
	
	@Test
	void getUserTasks_ascの場合_期限がデフォルトの昇順で未設定は最後に取得できること() {
		
		User user = userFactory.create("ユーザ１");
		
		taskFactory.create(user, "遅いタスク", TaskStatus.TODO, LocalDate.of(2026, 10, 10));
		taskFactory.create(user, "早いタスク", TaskStatus.TODO, LocalDate.of(2026, 10, 1));
		taskFactory.create(user, "未設定タスク", TaskStatus.TODO, null);
		
		//【実行】
		List<TaskDto> result = taskService.getUserTasks(user, "asc");
		
		
		//【検証】
		//	早い順（10/1 ➔ 10/10 ➔ 未設定）に並び替えてくれたかチェック
		assertThat(result).hasSize(3);
		assertThat(result.get(0).title()).isEqualTo("早いタスク");
		assertThat(result.get(1).title()).isEqualTo("遅いタスク");
		assertThat(result.get(2).title()).isEqualTo("未設定タスク");
	}
	
	
	
	@Test
	void getUserTasks_descの場合_期限の降順で未設定は最初に取得できること() {
		
		User user = userFactory.create("ユーザ２");

		taskFactory.create(user, "遅いタスク", TaskStatus.TODO, LocalDate.of(2026, 10, 10));
		taskFactory.create(user, "早いタスク", TaskStatus.TODO, LocalDate.of(2026, 10, 10));
		taskFactory.create(user, "未設定タスク", TaskStatus.TODO, null);
		
		//【実行】
		List<TaskDto> result = taskService.getUserTasks(user, "desc");
		
		
		//【検証】
		//	降順（未設定 ➔ 10/10 ➔ 10/1）に並び替えてくれたかチェック
		assertThat(result).hasSize(3);
		assertThat(result.get(0).title()).isEqualTo("未設定タスク");
		assertThat(result.get(1).title()).isEqualTo("遅いタスク");
		assertThat(result.get(2).title()).isEqualTo("早いタスク");
	}
	
	
	
	@Test
	void getAllTasks_ascの場合_全員のタスクが期限の昇順で未設定は最後に取得できること() {
		
		User user = userFactory.create("管理者ユーザー1");
		
		taskFactory.create(user, "全員の遅いタスク", TaskStatus.TODO, LocalDate.of(2026, 10, 10));
		taskFactory.create(user, "全員の早いタスク", TaskStatus.TODO, LocalDate.of(2026, 10, 1));
		taskFactory.create(user, "全員の未設定タスク", TaskStatus.TODO, null);
		
		//【実行】
		List<TaskDto> result = taskService.getAllTasks("asc");
		
		
		//【検証】
		//	早い順（10/1 ➔ 10/10 ➔ 未設定）に並び替えてくれたかチェック
		assertThat(result).isNotEmpty();
		assertThat(result).extracting(TaskDto::title).containsSubsequence("全員の早いタスク", "全員の遅いタスク", "全員の未設定タスク");
	}
	
	
	
	@Test
	void getAllTasks_descの場合_全員のタスクが降順で未設定は最初に取得できること() {
		
		User user = userFactory.create("管理者ユーザー2");
		
		taskFactory.create(user, "全員の遅いタスク", TaskStatus.TODO, LocalDate.of(2026, 10, 10));
		taskFactory.create(user, "全員の早いタスク", TaskStatus.TODO, LocalDate.of(2026, 10, 1));
		taskFactory.create(user, "全員の未設定タスク", TaskStatus.TODO, null);
		
		//【実行】
		List<TaskDto> result = taskService.getAllTasks("desc");
		
		
		//【検証】
		//	降順（未設定 ➔ 10/10 ➔ 10/1）に並び替えてくれたかチェック
		assertThat(result).isNotEmpty();
		assertThat(result).extracting(TaskDto::title).containsSubsequence("全員の未設定タスク", "全員の遅いタスク", "全員の早いタスク");
	}
	
	
	
	@Test
	void update系三つ_指定したタスクのタイトルとステータスと期限がそれぞれ個別に更新されること() {
		
		User user = userFactory.create("testuser");
		
		Task savedTask = taskFactory.create(user, "古いタイトル", TaskStatus.TODO, LocalDate.of(1999, 9, 19));
		Long taskId = savedTask.getId();
		LocalDate newDate = LocalDate.of(2026, 11, 21);
		
		//【実行】
		taskService.updateTaskTitle(savedTask.getId(), "新しいタイトル");
		taskService.updateTaskStatus(taskId, TaskStatus.DOING.name());
		taskService.updateTaskDueDate(taskId, newDate);
		
		
		//【検証】
		//	save()を呼んでいないのに、本当にDBの値が書き換わっているか確認
		//	@Transactional による自動保存の仕組み自体の証明
		Task updatedTask = taskRepository.findById(savedTask.getId()).orElseThrow();
		assertThat(updatedTask.getTitle()).isEqualTo("新しいタイトル");
		assertThat(updatedTask.getStatus()).isEqualTo(TaskStatus.DOING);
		assertThat(updatedTask.getDueDate()).isEqualTo(newDate);
	}
	
	
	
	@Test
	void getStatusCounts_あるユーザのタスク件数が正しく集計されること() {
		
		User userA = userFactory.create("自分");
		User userB = userFactory.create("本人");
		
		taskFactory.create(userA, "自分のタスク1", TaskStatus.TODO);
		taskFactory.create(userA, "自分のタスク2", TaskStatus.TODO);
		taskFactory.create(userA, "自分のタスク3", TaskStatus.DOING);
		
		taskFactory.create(userB, "他人のタスク1", TaskStatus.TODO);
		taskFactory.create(userB, "他人のタスク2", TaskStatus.DOING);
		taskFactory.create(userB, "他人のタスク3", TaskStatus.DONE);
		
		//【実行】
		Map<TaskStatus, Long> counts = taskService.getStatusCounts(userA);
		
		
		//【検証】
		assertThat(counts)
		.isNotNull()
		.hasSize(3)
		.containsEntry(TaskStatus.TODO, 2L)
		.containsEntry(TaskStatus.DOING, 1L)
		.containsEntry(TaskStatus.DONE, 0L);
	}
	
	
	
//	@Test
//	void test() {
//	
//	}
}
