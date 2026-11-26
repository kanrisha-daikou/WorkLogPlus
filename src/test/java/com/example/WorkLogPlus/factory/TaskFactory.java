package com.example.WorkLogPlus.factory;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.WorkLogPlus.entity.Task;
import com.example.WorkLogPlus.entity.TaskStatus;
import com.example.WorkLogPlus.entity.User;
import com.example.WorkLogPlus.repository.TaskRepository;

@Component
public class TaskFactory {
	
	@Autowired
	private TaskRepository taskRepository;
	
	
	
	public Task create(User user, String title, TaskStatus status) {
		
		return create(user, title, status, null);
	}
	
	
	
	public Task create(User user, String title, TaskStatus status, LocalDate duedate) {
		
		Task task = new Task();
		task.setUser(user);
		task.setTitle(title);
		task.setStatus(status);
		task.setDueDate(duedate);
		
		return taskRepository.save(task);
	}

}
