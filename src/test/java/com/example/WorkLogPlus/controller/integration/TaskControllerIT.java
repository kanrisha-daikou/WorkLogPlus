package com.example.WorkLogPlus.controller.integration;

import static org.assertj.core.api.Assertions.*;
import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.example.WorkLogPlus.config.LoginUser;
import com.example.WorkLogPlus.entity.Role;
import com.example.WorkLogPlus.entity.Task;
import com.example.WorkLogPlus.entity.TaskStatus;
import com.example.WorkLogPlus.entity.User;
import com.example.WorkLogPlus.factory.TaskFactory;
import com.example.WorkLogPlus.factory.UserFactory;
import com.example.WorkLogPlus.repository.TaskRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TaskControllerIT {
	
	@Autowired
	private MockMvc mockMvc;
	
	@Autowired
	private TaskRepository taskRepository;
	
	@Autowired
	private UserFactory userFactory;
	
	@Autowired
	private TaskFactory taskFactory;
	
	private User mockUser;
	
	private LoginUser mockLogin;
	
	private User otherUser;
	
	@BeforeEach
	void setUp() {
		
		this.mockUser = userFactory.create("testUser");
		this.mockLogin = new LoginUser(mockUser);
		
		taskFactory.create(mockUser, "2遠い期限", TaskStatus.TODO, LocalDate.of(2026, 1, 13));
		taskFactory.create(mockUser, "3期限未設定", TaskStatus.TODO, null);
		taskFactory.create(mockUser, "1近い期限", TaskStatus.TODO, LocalDate.of(2020, 12, 23));
		
		this.otherUser = userFactory.create("otherUser");
		taskFactory.create(otherUser, "他人のタスク", TaskStatus.TODO, LocalDate.of(2020, 5, 3));
	}
	
	
	
	@Test
	void showTaskList_正常系ソート条件なし_本人のタスクだけが昇順で取得され一覧画面が表示されること() throws Exception {
		
		mockMvc.perform(get("/tasks")
				.with(user(mockLogin)))
		.andExpect(status().isOk())
		.andExpect(view().name("task_list"))
		.andExpect(model().attribute("currentSort", "asc"))
		.andExpect(model().attributeExists("taskForm"))
		.andExpect(model().attribute("tasks", hasSize(3)))
		.andExpect(model().attribute("tasks", contains(
				hasToString(containsString("title=1近い期限")), 
				hasToString(containsString("title=2遠い期限")), 
				hasToString(containsString("title=3期限未設定"))
				)));
	}
	
	
	
	@Test
	void showTaskList_正常系ソートdesc指定_本人のタスクだけが降順で取得され一覧画面が表示されること() throws Exception {
		
		mockMvc.perform(get("/tasks")
				.param("sort", "desc")
				.with(user(mockLogin)))
		.andExpect(status().isOk())
		.andExpect(view().name("task_list"))
		.andExpect(model().attribute("currentSort", "desc"))
		.andExpect(model().attributeExists("taskForm"))
		.andExpect(model().attribute("tasks", hasSize(3)))
		.andExpect(model().attribute("tasks", contains(
				hasToString(containsString("title=3期限未設定")), 
				hasToString(containsString("title=2遠い期限")), 
				hasToString(containsString("title=1近い期限"))
				)));
	}
	
	
	
	@Test
	void showAdminTaskList_正常系ソート条件なし_全ユーザーのタスクが昇順で取得され一覧画面が表示されること() throws Exception {
		
		User admin = userFactory.create("testAdmin", Role.ADMIN);
		LoginUser loginAdmin = new LoginUser(admin);
		taskFactory.create(admin, "adminのタスク", TaskStatus.DOING, LocalDate.of(2026, 1, 1));
		
		mockMvc.perform(get("/tasks/admin")
				.with(user(loginAdmin)))
		.andExpect(status().isOk())
		.andExpect(view().name("admin/all_tasks"))
		.andExpect(model().attribute("currentSort", "asc"))
		.andExpect(model().attribute("tasks", containsInRelativeOrder(
				hasToString(containsString("title=他人のタスク")), 
				hasToString(containsString("title=1近い期限")), 
				hasToString(containsString("title=adminのタスク")), 
				hasToString(containsString("title=2遠い期限")), 
				hasToString(containsString("title=3期限未設定"))
				)));
	}
	
	
	
	@Test
	void showAdminTaskList_正常系ソートdesc指定_全ユーザーのタスクが降順で取得され一覧画面が表示されること() throws Exception {
		
		User admin = userFactory.create("testAdmin", Role.ADMIN);
		LoginUser loginAdmin = new LoginUser(admin);
		taskFactory.create(admin, "adminのタスク", TaskStatus.DOING, LocalDate.of(2026, 1, 1));
		
		mockMvc.perform(get("/tasks/admin")
				.param("sort", "desc")
				.with(user(loginAdmin)))
		.andExpect(status().isOk())
		.andExpect(view().name("admin/all_tasks"))
		.andExpect(model().attribute("currentSort", "desc"))
		.andExpect(model().attribute("tasks", containsInRelativeOrder(
				hasToString(containsString("title=3期限未設定")), 
				hasToString(containsString("title=2遠い期限")), 
				hasToString(containsString("title=adminのタスク")), 
				hasToString(containsString("title=1近い期限")), 
				hasToString(containsString("title=他人のタスク"))
				)));
	}
	
	
	
	@Test
	void showAdminTaskList_異常系_管理者権限が無い場合は403エラーになること() throws Exception {
		
		mockMvc.perform(get("/tasks/admin")
				.with(user(mockLogin)))
		.andExpect(status().isForbidden())
		.andExpect(view().name("error/403"))
		.andExpect(model().attributeDoesNotExist("tasks"))
		.andExpect(model().attributeDoesNotExist("currentSort"));
	}
	
	
	
	@Test
	void createTask_正常系_新規タスクが登録されて一覧画面にリダイレクトされること() throws Exception {
		
		long beforeCount = taskRepository.count();
		
		mockMvc.perform(post("/tasks/create")
				.param("title", "新規タスク登録のテストです")
				.param("status", TaskStatus.TODO.name())
				.with(user(mockLogin))
				.with(csrf()))
		.andExpect(status().isFound())
		.andExpect(redirectedUrl("/tasks"))
		.andExpect(flash().attribute("okMessage", "タスクを登録しました"));
		
		long afterCount = taskRepository.count();
		assertThat(afterCount).isEqualTo(beforeCount + 1);
	}
	
	
	
	@Test
	void createTask_異常系_入力エラー時は登録されずにエラー情報を伴って一覧画面へリダイレクトされること() throws Exception {
		
		long beforeCount = taskRepository.count();
		
		mockMvc.perform(post("/tasks/create")
				.param("title", "")
				.param("status", "")
				.with(user(mockLogin))
				.with(csrf()))
		.andExpect(status().isFound())
		.andExpect(redirectedUrl("/tasks"))
		.andExpect(flash().attributeExists("org.springframework.validation.BindingResult.taskForm"))
		.andExpect(flash().attributeExists("taskForm"));
		
		long afterCount = taskRepository.count();
		assertThat(afterCount).isEqualTo(beforeCount);
	}
	
	
	
	@Test
	void updateTitle_正常系_自分のタスクのタイトルがDB上で更新されること() throws Exception {
		
		Task task = taskFactory.create(mockUser, "事前準備のタスク", TaskStatus.TODO);
		Long taskId = task.getId();
		String newTitle = "タスクタイトル更新のテストです";
		
		mockMvc.perform(post("/tasks/{id}/update-title", taskId)
				.param("title", newTitle)
				.with(user(mockLogin))
				.with(csrf()))
		.andExpect(status().isOk());
		
		Task dbTask = taskRepository.findById(taskId).orElseThrow();
		assertThat(dbTask.getTitle()).isEqualTo(newTitle);
	}
	
	
	
	@Test
	void updateTitle_異常系_入力エラー時は400が返りDBが更新されないこと() throws Exception {
		
		String beforeTitle = "事前準備のタスク";
		Task task = taskFactory.create(mockUser, beforeTitle, TaskStatus.TODO);
		Long taskId = task.getId();
		
		mockMvc.perform(post("/tasks/{id}/update-title", taskId)
				.param("title", "")
				.with(user(mockLogin))
				.with(csrf()))
		.andExpect(status().isBadRequest())
		.andExpect(jsonPath("$.detail").value("タスク名は必須です"));
		
		Task dbTask = taskRepository.findById(taskId).orElseThrow();
		assertThat(dbTask.getTitle()).isEqualTo(beforeTitle);
	}
	
	
	
	@Test
	void updateTitle_異常系_本人以外のアクセスで403が返りDBが更新されないこと() throws Exception {
		
		String beforeTitle = "事前準備のタスク";
		Task task = taskFactory.create(otherUser, beforeTitle, TaskStatus.TODO);
		Long taskId = task.getId();
		
		mockMvc.perform(post("/tasks/{id}/update-title", taskId)
				.param("title", "タスクタイトル更新のテストです")
				.with(user(mockLogin))
				.with(csrf()))
		.andExpect(status().isForbidden())
		.andExpect(view().name("error/403"));
		
		Task dbTask = taskRepository.findById(taskId).orElseThrow();
		assertThat(dbTask.getTitle()).isEqualTo(beforeTitle);
	}
	
	
	
	@Test
	void updateTitle_異常系_存在しないタスクIDを指定した場合もPreAuthorizeによって403エラーになること() throws Exception {
		
		Long taskId = 9999L;
		
		mockMvc.perform(post("/tasks/{id}/update-title", taskId)
				.param("title", "タスクタイトル更新のテストです")
				.with(user(mockLogin))
				.with(csrf()))
		.andExpect(status().isForbidden())
		.andExpect(view().name("error/403"));
	}
	
	
	
	@Test
	void updateStatus_正常系_自分のタスクのステータスがDB上で更新されること() throws Exception {
		
		Task task = taskFactory.create(mockUser, "事前準備のタスク", TaskStatus.TODO);
		Long taskId = task.getId();
		
		mockMvc.perform(post("/tasks/{id}/update-status", taskId)
				.param("status", TaskStatus.DOING.name())
				.with(user(mockLogin))
				.with(csrf()))
		.andExpect(status().isOk());
		
		Task dbTask = taskRepository.findById(taskId).orElseThrow();
		assertThat(dbTask.getStatus()).isEqualTo(TaskStatus.DOING);
	}
	
	
	
	@Test
	void updateStatus_異常系_本人以外のアクセスで403が返りDBが更新されないこと() throws Exception {
		
		Task task = taskFactory.create(otherUser, "事前準備のタスク", TaskStatus.TODO);
		Long taskId = task.getId();
		
		mockMvc.perform(post("/tasks/{id}/update-status", taskId)
				.param("status", TaskStatus.DOING.name())
				.with(user(mockLogin))
				.with(csrf()))
		.andExpect(status().isForbidden())
		.andExpect(view().name("error/403"));
		
		Task dbTask = taskRepository.findById(taskId).orElseThrow();
		assertThat(dbTask.getStatus()).isEqualTo(TaskStatus.TODO);
	}
	
	
	
	@Test
	void updateDueDate_正常系_自分のタスクの期限日がDB上で更新されること() throws Exception {
		
		Task task = taskFactory.create(mockUser, "事前準備のタスク", TaskStatus.TODO, LocalDate.of(1999, 1, 1));
		Long taskId = task.getId();
		LocalDate targetDate = LocalDate.of(2026, 11, 16);
		
		mockMvc.perform(post("/tasks/{id}/update-duedate", taskId)
				.param("dueDate", targetDate.toString())
				.with(user(mockLogin))
				.with(csrf()))
		.andExpect(status().isOk());
		
		Task dbTask = taskRepository.findById(taskId).orElseThrow();
		assertThat(dbTask.getDueDate()).isEqualTo(targetDate);
	}
	
	
	
	@Test
	void updateDueDate_異常系_本人以外のアクセスで403が返りDBが更新されないこと() throws Exception {
		
		LocalDate targetDate = LocalDate.of(1999, 1, 1);
		Task task = taskFactory.create(otherUser, "事前準備のタスク", TaskStatus.TODO, targetDate);
		Long taskId = task.getId();
		
		mockMvc.perform(post("/tasks/{id}/update-duedate", taskId)
				.param("dueDate", "2026-11-16")
				.with(user(mockLogin))
				.with(csrf()))
		.andExpect(status().isForbidden())
		.andExpect(view().name("error/403"));
		
		Task dbTask = taskRepository.findById(taskId).orElseThrow();
		assertThat(dbTask.getDueDate()).isEqualTo(targetDate);
	}
	
	
	
	@Test
	void deleteTask_正常系_自分のタスクがDB上で削除されること() throws Exception {
		
		Task task = taskFactory.create(mockUser, "事前準備のタスク", TaskStatus.TODO);
		Long taskId = task.getId();
		
		mockMvc.perform(post("/tasks/{id}/delete", taskId)
				.with(user(mockLogin))
				.with(csrf()))
		.andExpect(status().isOk());
		
		boolean result = taskRepository.existsById(taskId);
		assertThat(result).isFalse();
	}
	
	
	
	@Test
	void deleteTask_異常系_本人以外のアクセスで403が返りDBから削除されないこと() throws Exception {
		
		Task task = taskFactory.create(otherUser, "事前準備のタスク", TaskStatus.TODO);
		Long taskId = task.getId();
		
		mockMvc.perform(post("/tasks/{id}/delete", taskId)
				.with(user(mockLogin))
				.with(csrf()))
		.andExpect(status().isForbidden())
		.andExpect(view().name("error/403"));
		
		boolean result = taskRepository.existsById(taskId);
		assertThat(result).isTrue();
	}
	
	
	
//	@Test
//	void test() {
//		
//		
//	}
}
