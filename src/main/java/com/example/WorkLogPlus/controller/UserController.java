package com.example.WorkLogPlus.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.WorkLogPlus.form.UserForm;
import com.example.WorkLogPlus.service.UserService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/register")
@RequiredArgsConstructor
public class UserController {

	private final UserService userService;
	
	@GetMapping
	public String userRegisterForm(Model model) {
		model.addAttribute("userForm", new UserForm());
		return "register";
	}
	
	@PostMapping
	public String register(@Validated @ModelAttribute UserForm userForm, BindingResult result, RedirectAttributes redirectAttributes, Model model) {
		if(result.hasErrors()) {
			return "register";
		}
		
		try {
			userService.registerUser(userForm);
		} catch(RuntimeException e) {
			model.addAttribute("errorMessage", e.getMessage());
			return "register";
		}
		
		redirectAttributes.addFlashAttribute("okMessage", "ユーザー登録が完了しました。\n引き続きログインされてください。");
		return "redirect:/login";
	}
}
