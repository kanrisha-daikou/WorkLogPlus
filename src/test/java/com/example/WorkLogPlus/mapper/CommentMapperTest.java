package com.example.WorkLogPlus.mapper;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import com.example.WorkLogPlus.dto.CommentDto;
import com.example.WorkLogPlus.entity.Comment;
import com.example.WorkLogPlus.entity.Report;
import com.example.WorkLogPlus.entity.User;

class CommentMapperTest {
	
	private final CommentMapper commentMapper = new CommentMapper();
	
	
	
	@Test
	void CommentエンティティからCommentDtoへ正しく変換できること() {
		
		User adminUser = new User();
		adminUser.setId(1L);
		adminUser.setUsername("管理者太郎");
		
		Report report = new Report();
		report.setId(50L);
		
		Comment comment = new Comment();
		comment.setId(500L);
		comment.setReport(report);
		comment.setAdmin(adminUser);
		comment.setBody("テストコメントです");
		comment.setCreatedAt(LocalDateTime.of(2026, 9, 28, 15, 30));
		
		User loginUser = adminUser;
		
		CommentDto dto = commentMapper.toDto(comment, loginUser);
		
		//	【検証】
		assertThat(dto).isNotNull();
		
		//	投稿日時が「分」まで正しくフォーマットされているか
		assertThat(dto.createdAt()).isEqualTo("2026/09/28 15:30");
		
		//	自分が書いたコメント判定（isOwn）が true になっているか
		assertThat(dto.isOwn()).isTrue();
		
		//	その他の値も正しく移し替えられているか
		assertThat(dto.id()).isEqualTo(500L);
		assertThat(dto.reportId()).isEqualTo(50L);
		assertThat(dto.adminId()).isEqualTo(1L);
		assertThat(dto.adminName()).isEqualTo("管理者太郎");
		assertThat(dto.body()).isEqualTo("テストコメントです");
	}
	
	
	
	@Test
	void 他人のコメントの場合_isOwnフラグが正しくfalseになること() {
		
		User otherWriter = new User();
		otherWriter.setId(10L);
		otherWriter.setUsername("コメント書いた人");
		
		Report report = new Report();
		report.setId(50L);
		
		Comment comment = new Comment();
		comment.setId(500L);
		comment.setReport(report);
		comment.setAdmin(otherWriter);
		comment.setBody("コメント書いた人の本文です");
		
		//	ログインユーザーを「他人（IDが違うユーザー）」にする
		User loginUser = new User();
		loginUser.setId(99L);
		loginUser.setUsername("ログインしている人");
		
		CommentDto dto = commentMapper.toDto(comment, loginUser);
		
		
		//	【検証】
		assertThat(dto).isNotNull();
		
		//	他人のコメントなので、isOwnが「false」になっているか確認する
		assertThat(dto.isOwn()).isFalse();
	}
	
	
	
	@Test
	void 作成日時やログインユーザーがnullでもエラーにならず空文字やfalseになること() {
		
		User admin = new User();
		admin.setId(1L);
		
		Report report = new Report();
		report.setId(10L);
		
		Comment comment = new Comment();
		comment.setId(100L);
		comment.setReport(report);
		comment.setAdmin(admin);
		comment.setCreatedAt(null);
		
		CommentDto dto = commentMapper.toDto(comment, null);
		
		
		//	【検証】
		assertThat(dto).isNotNull();
		assertThat(dto.createdAt()).isEmpty();
		assertThat(dto.isOwn()).isFalse();
	}
	
	
	
	@Test
	void エンティティがnullの場合はnullを返すこと() {
		assertThat(commentMapper.toDto(null, new User())).isNull();
	}
}
