package com.example.WorkLogPlus.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.Data;

/**
 * タイトルの非同期更新専用フォーム。
 * 新規登録用の TaskForm を使うと、タイトル更新時に送信されない項目（status の @NotNull など）まで
 * バリデーションチェックが走ってエラーになるため、タイトルだけを検証する目的で分離している。
 **/
@Data
public class TaskTitleForm {

	@NotBlank(message = "タスク名は必須です")
	@Size(max = 100, message = "タスク名は１００文字以内で入力してください")
	private String title;
}
