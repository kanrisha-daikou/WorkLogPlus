package com.example.WorkLogPlus.form;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import lombok.Data;

@Data
public class UserForm {

	@NotBlank(message = "ユーザー名は必須です")
	@Size(max = 20, message = "ユーザー名は２０文字以内で入力してください")
	private String username;
	
	@NotBlank(message = "メールアドレスは必須です")
	@Email(message = "正しいメールアドレスの形式で入力してください")
	private String email;
	
	@NotBlank(message = "パスワードは必須です")
	@Size(min = 8, message = "パスワードは８文字以上で入力してください")
	@Pattern(
			regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]+$",
			message = "半角英数字の混在で入力してください"
			)
	private String password;
}
