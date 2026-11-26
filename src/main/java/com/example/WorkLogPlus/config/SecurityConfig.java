package com.example.WorkLogPlus.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

	// ログインやログアウトや認証・認可などの設定
	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		
		http.authorizeHttpRequests(authorize -> authorize
					// 💡 足し算：CSSやJavaScript、画像ファイルだけは、ログイン前でも全員に読み込みを許可する
					.requestMatchers("/css/**", "/js/**", "/images/**", "/register").permitAll()
					// 🔑 ルール1：/admin/ から始まるURLは「ADMIN」の権限（ロール）を持った人だけ許可
					.requestMatchers("/admin/**").hasRole("ADMIN")
					// 🔑 ルール2：それ以外のすべてのURLは、ログイン（認証）している人に限定する
					.anyRequest().authenticated()
					)
			.formLogin(form -> form
					.loginPage("/login")// 自作のログイン画面(AuthController)のURL
					.defaultSuccessUrl("/", true)// ログイン成功時に最初に開く画面のURL
					.permitAll()// ログイン画面だけは、ログイン前でも全員アクセスを許可する
					// ログイン失敗でURL + "?error"（つまり /login?error）にリダイレクトされる
					)
			.logout(logout -> logout
					//	.logoutUrl("/logout")// ログアウトのURL（ログアウト画面は用意していないのでデフォルト）
					.logoutSuccessUrl("/login?logout")// ログアウト成功時に開くURL（画面にメッセージを出す用の目印付き）
					.permitAll()
					);
		
		return http.build();
	}
	
	
	
	// 🔒 パスワードを安全にハッシュ化（暗号化）して比較するための計算機
	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}
