package com.example.WorkLogPlus.service;

import org.springframework.stereotype.Component;

import com.example.WorkLogPlus.config.LoginUser;
import com.example.WorkLogPlus.entity.Task;
import com.example.WorkLogPlus.repository.TaskRepository;

import lombok.RequiredArgsConstructor;

@Component("taskSecurity") // @PreAuthorizeからこの名前で呼び出せるようにします
@RequiredArgsConstructor
public class TaskSecurityService {
	
	private final TaskRepository taskRepository;

	
	
//	指定されたタスクが、ログイン中のユーザーのものかどうかを判定する
	public boolean isOwner(Long taskId, LoginUser loginUser) {
		// 💡 存在しない場合は例外を投げず、一端 null として受け取る
		Task task = taskRepository.findById(taskId).orElse(null);
		if(task == null) {
			return false;
		}
		
		return task.getUser().getId().equals(loginUser.getUser().getId());
	}
}
