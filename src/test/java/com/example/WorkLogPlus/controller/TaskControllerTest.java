package com.example.WorkLogPlus.controller;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.validation.BindingResult;

import com.example.WorkLogPlus.config.LoginUser;
import com.example.WorkLogPlus.dto.TaskDto;
import com.example.WorkLogPlus.entity.TaskStatus;
import com.example.WorkLogPlus.entity.User;
import com.example.WorkLogPlus.factory.UserFactory;
import com.example.WorkLogPlus.form.TaskForm;
import com.example.WorkLogPlus.service.TaskService;

@WebMvcTest(TaskController.class)
class TaskControllerTest {

	@Autowired
	private MockMvc mockMvc;
	
	@MockitoBean
	private TaskService taskService;
	
	private User mockUser;
	
	private LoginUser mockLogin;
	
	@BeforeEach
	void setUp() {
		
		UserFactory factory = new UserFactory();
		this.mockUser = factory.createUnsaved("testUser");
		this.mockLogin = new LoginUser(mockUser);
	}
	
	
	
	@Test
	void showTaskList_正常系_ソート指定なしのときは__asc__指定でタスク一覧画面が表示されること() throws Exception {
		
		List<TaskDto> tasks = new ArrayList<TaskDto>();
		
		when(taskService.getUserTasks(mockUser, "asc")).thenReturn(tasks);
		
		mockMvc.perform(get("/tasks")
				.with(user(mockLogin)))
		.andExpect(status().isOk())
		.andExpect(view().name("task_list"))
		.andExpect(model().attribute("tasks", tasks))
		.andExpect(model().attribute("currentSort", "asc"))
		.andExpect(model().attributeExists("taskForm"));
		
		verify(taskService, times(1)).getUserTasks(mockUser, "asc");
	}
	
	
	
	@Test
	void showTaskList_正常系_ソート指定__desc__のときはそれでタスク一覧画面が表示されること() throws Exception {
		
		List<TaskDto> tasks = new ArrayList<TaskDto>();
		
		when(taskService.getUserTasks(mockUser, "desc")).thenReturn(tasks);
		
		mockMvc.perform(get("/tasks")
				.param("sort", "desc")
				.with(user(mockLogin)))
		.andExpect(status().isOk())
		.andExpect(view().name("task_list"))
		.andExpect(model().attribute("tasks", tasks))
		.andExpect(model().attribute("currentSort", "desc"))
		.andExpect(model().attributeExists("taskForm"));
		
		verify(taskService, times(1)).getUserTasks(mockUser, "desc");
	}
	
	
	
	@Test
	void showAdminTaskList_正常系_ソート指定なしのときは__asc__指定で全ユーザーのタスク一覧画面が表示されること() throws Exception {
		
		List<TaskDto> tasks = new ArrayList<TaskDto>();
		
		when(taskService.getAllTasks("asc")).thenReturn(tasks);
		
		mockMvc.perform(get("/tasks/admin")
				.with(user(mockLogin)))
		.andExpect(status().isOk())
		.andExpect(view().name("admin/all_tasks"))
		.andExpect(model().attribute("tasks", tasks))
		.andExpect(model().attribute("currentSort", "asc"));
		
		verify(taskService, times(1)).getAllTasks("asc");
	}
	
	
	
	@Test
	void showAdminTaskList_正常系_ソート指定__desc__のときはそれで全ユーザーのタスク一覧画面が表示されること() throws Exception {
		
		List<TaskDto> tasks = new ArrayList<TaskDto>();
		
		when(taskService.getAllTasks("desc")).thenReturn(tasks);
		
		mockMvc.perform(get("/tasks/admin")
				.param("sort", "desc")
				.with(user(mockLogin)))
		.andExpect(status().isOk())
		.andExpect(view().name("admin/all_tasks"))
		.andExpect(model().attribute("tasks", tasks))
		.andExpect(model().attribute("currentSort", "desc"));
		
		verify(taskService, times(1)).getAllTasks("desc");
	}
	
	
	
	@Test
	void createTask_正常系_入力が正常な場合は登録処理が呼ばれ一覧画面にリダイレクトされること() throws Exception {
		
		mockMvc.perform(post("/tasks/create")
				.param("title", "タスク登録テストです")
				.param("status", TaskStatus.TODO.name())
				.with(user(mockLogin))
				.with(csrf()))
		.andExpect(status().isFound())
		.andExpect(view().name("redirect:/tasks"))
		.andExpect(flash().attribute("okMessage", "タスクを登録しました"));
		
		verify(taskService, times(1)).saveTask(any(TaskForm.class), eq(mockUser));
	}
	
	
	
	@Test
	void createTask_異常系_入力エラー時は登録処理が行われずにエラー情報を伴って一覧画面へリダイレクトされること() throws Exception {
		
		mockMvc.perform(post("/tasks/create")
				.param("title", "")
				.param("status", "")
				.with(user(mockLogin))
				.with(csrf()))
		.andExpect(status().isFound())
		.andExpect(view().name("redirect:/tasks"))
		.andExpect(flash().attributeExists("taskForm"))
		.andExpect(result -> {
			BindingResult br = (BindingResult) result.getFlashMap()
					.get("org.springframework.validation.BindingResult.taskForm");
			assertThat(br.hasFieldErrors("title")).isTrue();
			assertThat(br.hasFieldErrors("status")).isTrue();
		});
		
		verify(taskService, never()).saveTask(any(), any());
	}
	
	
	
	@Test
	void updateTitle_正常系_タイトル更新処理が呼ばれステータス200が返ること() throws Exception {
		
		Long taskId = 99L;
		String newTitle = "タスクタイトル更新のテストです";
		
		mockMvc.perform(post("/tasks/{id}/update-title", taskId)
				.param("title", newTitle)
				.with(user(mockLogin))
				.with(csrf()))
		.andExpect(status().isOk());
		
		verify(taskService, times(1)).updateTaskTitle(taskId, newTitle);
	}
	
	
	
	@Test
	void updateTitle_異常系_入力エラー空文字時は更新処理が呼ばれずエラー情報とステータス400が返ること() throws Exception {
		
		Long taskId = 88L;
		
		mockMvc.perform(post("/tasks/{id}/update-title", taskId)
				.param("title", "")
				.with(user(mockLogin))
				.with(csrf()))
		.andExpect(status().isBadRequest())
		.andExpect(jsonPath("$.detail").value("タスク名は必須です")); // フォームクラスの@NotBlankに設定しているメッセージ
		
		verify(taskService, never()).updateTaskTitle(anyLong(), any());
	}
	
	
	
	@Test
	void updateTitle_異常系_入力エラー101文字以上時は更新処理が呼ばれずエラー情報とステータス400が返ること() throws Exception {
		
		Long taskId = 44L;
		String overTitle = "あ".repeat(101);
		
		mockMvc.perform(post("/tasks/{id}/update-title", taskId)
				.param("title", overTitle)
				.with(user(mockLogin))
				.with(csrf()))
		.andExpect(status().isBadRequest())
		.andExpect(jsonPath("$.detail").value("タスク名は１００文字以内で入力してください")); // フォームクラスの@Sizeに設定しているメッセージ
		
		verify(taskService, never()).updateTaskTitle(anyLong(), any());
	}
	
	
	
	@Test
	void updateStatus_正常系_ステータス更新処理が呼ばれ200が返ること() throws Exception {
		
		Long taskId = 77L;
		
		mockMvc.perform(post("/tasks/{id}/update-status", taskId)
				.param("status", TaskStatus.DOING.name()) // 用意してる列挙型を使っとく
				.with(user(mockLogin))
				.with(csrf()))
		.andExpect(status().isOk());
		
		verify(taskService, times(1)).updateTaskStatus(eq(taskId), anyString());
	}
	
	
	
	@Test
	void updateDueDate_正常系_期限日更新処理が呼ばれステータス200が返ること() throws Exception {
		
		Long taskId = 66L;
		LocalDate targetDate = LocalDate.of(2026, 11, 10);
		
		mockMvc.perform(post("/tasks/{id}/update-duedate", taskId)
				.param("dueDate", targetDate.toString())
				.with(user(mockLogin))
				.with(csrf()))
		.andExpect(status().isOk());
		
		verify(taskService, times(1)).updateTaskDueDate(taskId, targetDate);
	}
	
	
	
	@Test
	void deleteTask_正常系_タスク削除処理が呼ばれステップ200が返ること() throws Exception {
		
		Long taskId = 55L;
		
		mockMvc.perform(post("/tasks/{id}/delete", taskId)
				.with(user(mockLogin))
				.with(csrf()))
		.andExpect(status().isOk());
		
		verify(taskService, times(1)).deleteTask(taskId);
	}
	
	
	
//	@Test
//	void test() {
//		
//		
//	}
}
