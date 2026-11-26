package com.example.WorkLogPlus.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.WorkLogPlus.config.LoginUser;
import com.example.WorkLogPlus.entity.Report;
import com.example.WorkLogPlus.entity.User;
import com.example.WorkLogPlus.repository.ReportRepository;

@ExtendWith(MockitoExtension.class)
class ReportSecurityServiceTest {
	
	@Mock
	private ReportRepository reportRepository;
	
	@InjectMocks
	private ReportSecurityService reportSecurityService;

	
	
	@Test
	void isOwner_所有者本人の場合はtrueが返ってくること() {

		User user = new User();
		user.setId(1L);
		
		Report report = new Report();
		report.setId(200L);
		report.setUser(user);
		
		when(reportRepository.findById(200L)).thenReturn(Optional.of(report));
		
		LoginUser loginUser = new LoginUser(user);
		
		//【実行】
		boolean result = reportSecurityService.isOwner(200L, loginUser);
		
		
		//【検証】
		//	本人なので true になるか検証
		assertThat(result).isTrue();
	}
	
	
	
	@Test
	void isOwner_他人の日報の場合はfalseが返ってくること() {
		
		User owner = new User();
		owner.setId(10L);
		
		Report report = new Report();
		report.setId(200L);
		report.setUser(owner);
		
		when(reportRepository.findById(200L)).thenReturn(Optional.of(report));
		
		User otherUser = new User();
		otherUser.setId(99L);
		LoginUser loginUser = new LoginUser(otherUser);
		
		//【実行】
		boolean result = reportSecurityService.isOwner(200L, loginUser);
		
		
		//【検証】
		//	他人の日報なので false になるか検証
		assertThat(result).isFalse();
	}
	
	
	
	@Test
	void isOwner_loginUserやuserがnullの場合はDB検索を行わずfalseを返すこと() {
		
		assertThat(reportSecurityService.isOwner(100L, (LoginUser) null));
		assertThat(reportSecurityService.isOwner(100L, (User) null));
		
		verify(reportRepository, never()).findById(any());
	}
	
	
	
	@Test
	void isOwner_存在しない日報IDの場合はfalseが返ってくること() {
		
		when(reportRepository.findById(999L)).thenReturn(Optional.empty());
		
		User user = new User();
		LoginUser loginUser = new LoginUser(user);
		
		//【実行】
		boolean result = reportSecurityService.isOwner(999L, loginUser);
		
		
		//【検証】
		//	日報が無いので false になるか検証
		assertThat(result).isFalse();
	}
}
