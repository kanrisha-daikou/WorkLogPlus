package com.example.WorkLogPlus.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.WorkLogPlus.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {

	// 💡 要件（Spring Securityログイン）のために追加：メールアドレスでユーザーを検索するメソッド
	Optional<User> findByEmail(String email);
}
