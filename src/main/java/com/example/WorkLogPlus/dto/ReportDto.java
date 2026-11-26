package com.example.WorkLogPlus.dto;

public record ReportDto(
		Long id,
		Long userId,
		String username,
		String reportDate,
		String content,
		String impressions,
		String issues,
		String createdAt,
		int commentCount,
		boolean unRead,
		boolean isOwn) {
}
