package com.example.WorkLogPlus.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import lombok.Data;

@Entity
@Table(name = "reports", uniqueConstraints = {
		@UniqueConstraint(columnNames = {"user_id", "report_date"})
})
@Data
public class Report {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;
	
	@Column(name = "report_date", nullable = false)
	private LocalDate reportDate;
	
	@Column(nullable = false, length = 1000)
	private String content;
	
	@Column(length = 1000)
	private String impressions;
	
	@Column(length = 1000)
	private String issues;
	
	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;
	
	@UpdateTimestamp
	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;
	
	
//	commentsテーブルと結びつけるための設定（※reportsテーブルに列は増えません）
	@OneToMany(mappedBy = "report", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("createdAt DESC")
	private List<Comment> comments;
	
//	★ cascade = CascadeType.ALL, orphanRemoval = true について ★
//	日報を削除したり、ここのcommentsからコメントが消えたりしても、
//	連動してCommentテーブル内の関連データも消して幽霊データが残らないようにしてくれる
}
