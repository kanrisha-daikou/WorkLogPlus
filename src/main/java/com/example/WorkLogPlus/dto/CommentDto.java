package com.example.WorkLogPlus.dto;

public record CommentDto(
		Long id,
		Long reportId,
		Long adminId,
		String adminName,
		String body,
		String createdAt,
		boolean isOwn) {
}
