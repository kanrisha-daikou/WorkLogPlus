package com.example.WorkLogPlus.repository;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.example.WorkLogPlus.entity.Task;
import com.example.WorkLogPlus.entity.TaskStatus;
import com.example.WorkLogPlus.entity.User;
import com.example.WorkLogPlus.factory.TaskFactory;
import com.example.WorkLogPlus.factory.UserFactory;

@DataJpaTest
@Import({UserFactory.class, TaskFactory.class})
class TaskRepositoryTest {
	
	@Autowired
	private TaskRepository taskRepository;
	
	@Autowired
	private UserFactory userFactory;
	
	@Autowired
	private TaskFactory taskFactory;
	
	
	
	@Test
	void findByUserIdCustom_特定のユーザーに紐づくタスクだけがページングされて取得できること() {
		
		User user1 = userFactory.create("user1");
		User user2 = userFactory.create("user2");
		
		taskFactory.create(user1, "ユーザー１のタスクA", TaskStatus.TODO);
		taskFactory.create(user2, "ユーザー２のタスク", TaskStatus.TODO);
		taskFactory.create(user1, "ユーザー１のタスクB", TaskStatus.TODO);
		
		Pageable pageable = PageRequest.of(0, 10);
		Page<Task> resultPage = taskRepository.findByUserIdCustom(user1, pageable);
		
		
		//【検証】
		assertThat(resultPage).isNotNull();
		
		//	ユーザー1の全タスクを取得したので、件数は「2件」であるべき
		assertThat(resultPage.getTotalElements()).isEqualTo(2);
		
		//	中身を取り出して、本当にユーザー1のタスクだけが入っているか確認
		assertThat(resultPage.getContent())
		.extracting(Task::getTitle)
		.containsExactlyInAnyOrder("ユーザー１のタスクA", "ユーザー１のタスクB");
	}
	
	
	
	@Test
	void findAllCustom_全ユーザーの全タスクが取得できること() {
		
		User user1 = userFactory.create("user1");
		User user2 = userFactory.create("user2");
		
		taskFactory.create(user1, "ユーザー１のタスクA", TaskStatus.TODO);
		taskFactory.create(user2, "ユーザー２のタスク", TaskStatus.DONE);
		taskFactory.create(user1, "ユーザー１のタスクB", TaskStatus.DOING);
		
		Pageable pageable = PageRequest.of(0, 10);
		Page<Task> resultPage = taskRepository.findAllCustom(pageable);
		
		
		//【検証】
		assertThat(resultPage).isNotNull();
		
		//	全員の全タスクを取得したので、トータル件数は「3件」であるべき
		assertThat(resultPage.getTotalElements()).isEqualTo(3);
		
		//	中身を確認して、AさんもBさんも両方のタスクとも含まれているか検証
		assertThat(resultPage.getContent())
		.extracting(Task::getTitle)
		.containsExactlyInAnyOrder("ユーザー１のタスクA", "ユーザー２のタスク", "ユーザー１のタスクB");
	}
}
