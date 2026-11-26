package com.example.WorkLogPlus.mapper;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.example.WorkLogPlus.dto.ReportDto;
import com.example.WorkLogPlus.entity.Comment;
import com.example.WorkLogPlus.entity.Report;
import com.example.WorkLogPlus.entity.User;

class ReportMapperTest {
	
	private final ReportMapper reportMapper = new ReportMapper();

	
	
	@Test
	void ReportエンティティからReportDtoへ正しくデータが加工変換できること() {

		User writer = new User();
		writer.setId(1L);
		writer.setUsername("リポートテスト太郎");
		
		Report report = new Report();
		report.setId(100L);
		report.setUser(writer);
		report.setReportDate(LocalDate.of(2027, 1, 11));
		report.setCreatedAt(LocalDateTime.of(2026, 1, 11, 13, 0));
		report.setContent("業務内容どす");
		report.setImpressions("所感どす");
		report.setIssues("課題どす");
		report.setComments(new ArrayList<>());
		
		ReportDto dto = reportMapper.toDto(report, writer);
		
		
		//	【検証】
		//	マッパーから戻ってきたDTOが、空っぽ（null）でないか
		assertThat(dto).isNotNull();
		
		// 曜日や日時のフォーマット（DATE_FORMAT / DATETIME_FORMAT）が合っているか
		assertThat(dto.reportDate()).isEqualTo("2027/01/11(月)");
		assertThat(dto.createdAt()).isEqualTo("2026/01/11 13:00");
		
		// 自分の日報かどうかの判定（isOwn）が true になっているか
		assertThat(dto.isOwn()).isTrue();
		
		// その他の値も正しく移し替えられているか
		assertThat(dto.content()).isEqualTo("業務内容どす");
		assertThat(dto.impressions()).isEqualTo("所感どす");
		assertThat(dto.issues()).isEqualTo("課題どす");
		assertThat(dto.commentCount()).isEqualTo(0);
		assertThat(dto.unRead()).isFalse();
	}
	
	
	
	@Test
	void 未読コメントがある場合_Dtoの件数と未読フラグが正しく加工されること() {
		
		User writer = new User();
		writer.setId(1L);
		writer.setUsername("リポートテスト太郎");
		
		Comment comment1 = new Comment();
		comment1.setRead(true); // 1件目は既読
		
		Comment comment2 = new Comment();
		comment2.setRead(false); // 2件目は未読
		
		Report report = new Report();
		report.setId(100L);
		report.setUser(writer);
		report.setReportDate(LocalDate.of(2026, 9, 26));
		report.setComments(List.of(comment1, comment2));
		
		ReportDto dto = reportMapper.toDto(report, writer);
		
		
		//	【検証】 
		assertThat(dto).isNotNull();
		
		//	コメント数が「2件」になっているか？
		assertThat(dto.commentCount()).isEqualTo(2);
		
		//	未読のコメント（comment2）があるので、unReadが「true」になっているか？
		assertThat(dto.unRead()).isTrue();
	}
	
	
	
	@Test
	void 既読コメントのみの場合_unReadフラグがfalseになること() {
		
		User writer = new User();
		writer.setId(1L);
		writer.setUsername("リポートテスト太郎");
		
		Comment comment1 = new Comment();
		comment1.setRead(true);
		
		Comment comment2 = new Comment();
		comment2.setRead(true);
		
		Report report = new Report();
		report.setId(100L);
		report.setUser(writer);
		report.setReportDate(LocalDate.of(2026, 9, 26));
		report.setComments(List.of(comment1, comment2));
		
		ReportDto dto = reportMapper.toDto(report, writer);
		
		
		//【検証】
		assertThat(dto.commentCount()).isEqualTo(2);
		assertThat(dto.unRead()).isFalse();
	}
	
	
	
	@Test
	void 他人の日報の場合_isOwnフラグが正しくfalseになること() {
		
		User otherWriter = new User();
		otherWriter.setId(1L);
		otherWriter.setUsername("日報書いた人");
		
		Report report = new Report();
		report.setId(100L);
		report.setUser(otherWriter);
		report.setReportDate(LocalDate.of(2026, 9, 26));
		
		//	ログインユーザーを「他人（IDが違うユーザー）」にする
		User loginUser = new User();
		loginUser.setId(5L);
		loginUser.setUsername("ログインしている人");
		
		ReportDto dto = reportMapper.toDto(report, loginUser);
		
		
		//	【検証】
		assertThat(dto).isNotNull();
		
		//	他人の日報なので、isOwnが「false」になっているか確認する
		assertThat(dto.isOwn()).isFalse();
	}
	
	
	
	@Test
	void エンティティがnullの場合はnullを返すこと() {
		assertThat(reportMapper.toDto(null, new User())).isNull();
	}
}
