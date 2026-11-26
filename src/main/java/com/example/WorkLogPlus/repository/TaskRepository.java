package com.example.WorkLogPlus.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.WorkLogPlus.entity.Task;
import com.example.WorkLogPlus.entity.TaskStatus;
import com.example.WorkLogPlus.entity.User;

public interface TaskRepository extends JpaRepository<Task, Long> {

//	任意のユーザーのタスクを取得（基本的にid順になるので結果として登録順になる）
//	List<Task> findByUser(User user);
	
//	ユーザーに紐づくタスクを、ページング条件に従って取得
	//	(JPAの「名前からクエリの自動生成」がヌルを扱えないため、@Queryで無効化してクエリを組み立てていく)
	@Query("SELECT t FROM Task t WHERE t.user = :user")
	Page<Task> findByUserIdCustom(User user, Pageable pageable);
	
//	全ユーザーのタスクをページング条件に従って取得
	@Query("SELECT t FROM Task t")
	Page<Task> findAllCustom(Pageable pageable);
	
//	「countBy」件数を数える、「UserAndStatus」UserカラムかつStatusカラムに該当する条件で
	long countByUserAndStatus(User user, TaskStatus status);
}
