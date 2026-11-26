package com.example.WorkLogPlus.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.WorkLogPlus.config.LoginUser;
import com.example.WorkLogPlus.entity.Task;
import com.example.WorkLogPlus.entity.User;
import com.example.WorkLogPlus.repository.TaskRepository;

@ExtendWith(MockitoExtension.class)
class TaskSecurityServiceTest {
	
	@Mock
	private TaskRepository taskRepository;
	
	@InjectMocks
	private TaskSecurityService taskSecurityService;

	
	
	@Test
	void isOwner_所有者本人の場合はtrueが返ってくること() {

		User user = new User();
		user.setId(1L);
		
		Task task = new Task();
		task.setId(100L);
		task.setUser(user);
		
		when(taskRepository.findById(100L)).thenReturn(Optional.of(task));
		
		LoginUser loginUser = new LoginUser(user);
		
		//【実行】
		boolean result = taskSecurityService.isOwner(100L, loginUser);
		
		
		//【検証】
		//	本人なので、ちゃんと true になっているか検証
		assertThat(result).isTrue();
	}
	
	
	
	@Test
	void isOwner_他人のタスクの場合はfalseが返ってくること() {
		
		User owner = new User();
		owner.setId(10L);
		
		Task task = new Task();
		task.setId(100L);
		task.setUser(owner);
		
		when(taskRepository.findById(100L)).thenReturn(Optional.of(task));
		
		User otherUser = new User();
		otherUser.setId(99L);
		LoginUser loginUser = new LoginUser(otherUser);
		
		//【実行】
		boolean result = taskSecurityService.isOwner(100L, loginUser);
		
		
		//【検証】
		//	他人なので false になるか検証
		assertThat(result).isFalse();
	}
	
	
	
	@Test
	void isOwner_存在しないタスクIDの場合はfalseが返ってくること() {
		
		when(taskRepository.findById(999L)).thenReturn(Optional.empty());
		
		User user = new User();
		LoginUser loginUser = new LoginUser(user);
		
		//【実行】
		boolean result = taskSecurityService.isOwner(999L, loginUser);
		
		
		//【検証】
		//	タスクが無いので false になるか検証
		assertThat(result).isFalse();
	}
}
