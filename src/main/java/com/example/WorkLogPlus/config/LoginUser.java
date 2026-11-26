package com.example.WorkLogPlus.config;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.example.WorkLogPlus.entity.User;

public class LoginUser implements UserDetails {

	private final User user;
	
	// コンストラクタ
	public LoginUser(User user) {
		this.user = user;
	}
	
	// ログインユーザーのUserエンティティを返す
	public User getUser() {
		return this.user;
	}
	
	// 本人の名前をゲット
	// （getUsername()がIDを指すものとして強制されているので、それとの混同防止メソッド）
	public String getRealName() {
		return this.user.getUsername();
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		// 権限（ロール）のリストを返す
		return List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
	}

	@Override
	public String getPassword() {
		// ハッシュ化して保存されたDB内のパスワードをゲット
		return user.getPassword();
	}

	@Override
	public String getUsername() {
		// ログイン画面ではメールアドレスをIDとしているので
		return user.getEmail();
	}
	
}
