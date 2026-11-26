package com.example.WorkLogPlus.config;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.example.WorkLogPlus.entity.Role;
import com.example.WorkLogPlus.entity.User;
import com.example.WorkLogPlus.factory.UserFactory;
import com.example.WorkLogPlus.repository.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SecurityConfigIT {
	
	@Autowired
	private MockMvc mockMvc;
	
	@Autowired
	private UserFactory userFactory;
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	@Autowired
	private UserRepository userRepository;
	
	
	
	// ==========================================
	// 1. 未ログイン時の公開URLへのアクセス許可
	// ==========================================
	
	@Test
	void 未ログイン状態でログイン画面にアクセスできること() throws Exception {
		
		mockMvc.perform(get("/login"))
		.andExpect(status().isOk())
		.andExpect(view().name("login")); // AuthControllerが返す画面名
	}
	
	
	
	@Test
	void 未ログイン状態で新規登録画面にアクセスできること() throws Exception {
		
		mockMvc.perform(get("/register"))
		.andExpect(status().isOk())
		.andExpect(view().name("register")); // UserControllerが返す画面名
	}
	
	
	
	@Test
	void 未ログイン状態で新規登録のPOST送信がセキュリティにブロックされないこと() throws Exception {
		
		// セキュリティで弾かれれば「302(リダイレクト)」や「403(拒否)」になるため、
		// あえて空送信でControllerのバリデーション画面(200 OK)まで到達させることで、検問を通過できたことを証明する
		mockMvc.perform(post("/register")
				.with(csrf()))
		.andExpect(status().isOk())
		.andExpect(view().name("register"));
	}
	
	
	
	// ==========================================
	// 2. 未ログイン時の保護されたURLへのリダイレクト
	// ==========================================
	
	@Test
	void 未ログイン状態でトップ画面にアクセスするとログイン画面にリダイレクトされること() throws Exception {
		
		mockMvc.perform(get("/"))
		.andExpect(status().isFound())
		.andExpect(redirectedUrl("http://localhost/login"));
	}
	
	
	
	@Test
	void 未ログイン状態で日報一覧にアクセスするとログイン画面にリダイレクトされること() throws Exception {
		
		mockMvc.perform(get("/reports"))
		.andExpect(status().isFound())
		.andExpect(redirectedUrl("http://localhost/login"));
	}
	
	
	
	@Test
	void 未ログイン状態で管理者用である全ユーザー日報一覧にアクセスするとログイン画面にリダイレクトされること() throws Exception {
		
		mockMvc.perform(get("/admin/reports"))
		.andExpect(status().isFound())
		.andExpect(redirectedUrl("http://localhost/login"));
	}
	
	
	
	// ==========================================
	// 3. URLベースの管理者制限（/admin/**）の検証
	// ==========================================
	
	@Test
	void 一般ユーザーで管理者画面にアクセスすると403エラーになること() throws Exception {
		
		User testUser = userFactory.create("testUser");
		LoginUser loginUser = new LoginUser(testUser);
		
		mockMvc.perform(get("/admin/reports")
				.with(user(loginUser)))
		.andExpect(status().isForbidden());
		// 管理者権限は一部を除き他のようなメソッドごとではなく、SecurityConfig の
		// .requestMatchers("/admin/**").hasRole("ADMIN")による外側の全体の設定であり、
		// テスト環境ではトムキャットなどが停止していることで
		// エラー時の自動フォワード処理も働かず画面が返ってこない
		// そのためここではステータスのみ検証し、画面はE2Eテストなど他で補う
	}
	
	
	
	@Test
	void ADMIN権限で管理者画面にアクセスできること() throws Exception {
		
		User testAdmin = userFactory.create("testAdmin", Role.ADMIN);
		LoginUser loginAdmin = new LoginUser(testAdmin);
		
		mockMvc.perform(get("/admin/reports")
				.with(user(loginAdmin)))
		.andExpect(status().isOk())
		.andExpect(view().name("admin/report/list"));
	}
	
	
	
	// ==========================================
	// 4. メソッドベースの管理者制限（@PreAuthorize）の検証
	// ==========================================
	
	@Test
	void 一般ユーザーで全タスク管理画面にアクセスすると403エラー画面になること() throws Exception {
		
		User testUser = userFactory.create("testUser");
		LoginUser loginUser = new LoginUser(testUser);
		
		mockMvc.perform(get("/tasks/admin")
				.with(user(loginUser)))
		.andExpect(status().isForbidden())
		.andExpect(view().name("error/403"));
		// ここは.requestMatchers("/admin/**").hasRole("ADMIN")を通過して
		// @PreAuthorize("hasRole('ADMIN')")に捕まるので画面の検証が出来る
	}
	
	
	
	@Test
	void 管理者ユーザーで全タスク管理画面にアクセスできること() throws Exception {
		
		User testAdmin = userFactory.create("testAdmin", Role.ADMIN);
		LoginUser loginAdmin = new LoginUser(testAdmin);
		
		mockMvc.perform(get("/tasks/admin")
				.with(user(loginAdmin)))
		.andExpect(status().isOk())
		.andExpect(view().name("admin/all_tasks"));
	}
	
	
	
	// ==========================================
	// 5. フォームログインの成功・失敗の挙動
	// ==========================================
	
	@Test
	void 正しいメールアドレスとパスワードでログインするとトップ画面にリダイレクトされること() throws Exception {
		
		User testUser = userFactory.createUnsaved("testUser");
		testUser.setPassword(passwordEncoder.encode("password"));
		userRepository.save(testUser); // userFactoryでは行ってないencodeもして保存
		
		// HTMLのフォームタグからのpostを再現、formLoginでより短く明示的に書ける
		// post送信なのでAuthControllerではなく、SecurityConfigが受け取る
		mockMvc.perform(formLogin("/login")
				.user("testUser@example.com")
				.password("password"))
		.andExpect(status().isFound())
		.andExpect(redirectedUrl("/"))
		.andExpect(authenticated().withUsername("testUser@example.com"));
	}
	
	
	
	@Test
	void 誤ったパスワードでログインするとログイン画面にエラーパラメータ付きでリダイレクトされること() throws Exception {
		
		User testUser = userFactory.createUnsaved("testUser");
		testUser.setPassword(passwordEncoder.encode("password"));
		userRepository.save(testUser);
		
		mockMvc.perform(formLogin("/login")
				.user("testUser@example.com")
				.password("usousopass")) // 間違ったパスワード
		.andExpect(status().isFound())
		.andExpect(redirectedUrl("/login?error"))
		.andExpect(unauthenticated());
	}
	
	
	
	// ==========================================
	// 6. ログアウトの挙動
	// ==========================================
	
	@Test
	void ログイン状態でログアウトを実行すると未認証状態になりログイン画面にパラメータ付きでリダイレクトされること() throws Exception {
		
		User testUser = userFactory.create("testUser");
		LoginUser loginUser = new LoginUser(testUser);
		
		mockMvc.perform(post("/logout")
				.with(user(loginUser))
				.with(csrf()))
		.andExpect(status().isFound())
		.andExpect(redirectedUrl("/login?logout"))
		.andExpect(unauthenticated());
	}
	
	
	
//	@Test
//	void t() {
//		
//		
//	}
}
