package com.example.WorkLogPlus.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.WorkLogPlus.dto.ReportDto;
import com.example.WorkLogPlus.entity.Report;
import com.example.WorkLogPlus.entity.User;
import com.example.WorkLogPlus.exception.DupeReportException;
import com.example.WorkLogPlus.form.ReportForm;
import com.example.WorkLogPlus.mapper.ReportMapper;
import com.example.WorkLogPlus.repository.ReportRepository;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {
	
	@Mock
	private ReportRepository reportRepository;
	
	@Mock
	private ReportMapper reportMapper;
	
	@InjectMocks
	private ReportService reportService;
	
	
	
	@Test
	void getReportsBySearchUserId_検索IDがある場合_指定したユーザーIDで絞り込まれること() {
		
		Long searchUserId = 99L;
		User user = new User();
		Report r1 = new Report();	
		ReportDto dto1 = mock(ReportDto.class);
		
		when(reportRepository.findByUserIdOrderByReportDateDesc(searchUserId)).thenReturn(List.of(r1));
		when(reportMapper.toDto(r1, user)).thenReturn(dto1);
		
		//【実行】
		List<ReportDto> result = reportService.getReportsBySearchUserId(searchUserId, user);
		
		
		//【検証】
		assertThat(result).hasSize(1);
		verify(reportRepository, times(1)).findByUserIdOrderByReportDateDesc(searchUserId);
	}
	
	
	
	@Test
	void getReportsBySearchUserId_検索IDがNullの場合_全員分の日報が取得されること() {
		
		User user = new User();
		
		Report r1 = new Report();
		Report r2 = new Report();		
		
		ReportDto dto1 = mock(ReportDto.class);
		ReportDto dto2 = mock(ReportDto.class);
		
		when(reportRepository.findAllByOrderByReportDateDesc()).thenReturn(List.of(r1, r2));
		when(reportMapper.toDto(r1, user)).thenReturn(dto1);
		when(reportMapper.toDto(r2, user)).thenReturn(dto2);
		
		//【実行】
		List<ReportDto> result = reportService.getReportsBySearchUserId(null, user);
		
		
		//【検証】
		assertThat(result).hasSize(2);
		verify(reportRepository, times(1)).findAllByOrderByReportDateDesc();
	}
	
	
	
	@Test
	void getReportById__と__getReportFormById__存在しない日報IDの場合はNoSuchElementExceptionが投げられること() {
		
		Long reportId = 9999L;
		User loginUser = new User();
		
		when(reportRepository.findById(reportId)).thenReturn(Optional.empty());
		
		assertThatThrownBy(() -> reportService.getReportById(reportId, loginUser)).isInstanceOf(NoSuchElementException.class);
		assertThatThrownBy(() -> reportService.getReportFormById(reportId)).isInstanceOf(NoSuchElementException.class);
	}
	
	
	
	@Test
	void saveReport_同じ日付の日報がある場合_DupeReportExceptionが発生し保存されないこと() {
		
		User loginUser = new User();
		ReportForm form = new ReportForm();
		form.setReportDate(LocalDate.of(2026, 10, 6));
		
		when(reportRepository.existsByUserAndReportDate(loginUser, form.getReportDate())).thenReturn(true);
		
		
		//【実行＆検証】assertThatThrownBy でエラーを捕まえる
		assertThatThrownBy(() -> reportService.saveReport(loginUser, form))
		//	発生したエラーの種類が DupeReportException であること
		.isInstanceOf(DupeReportException.class)
		//	エラーメッセージが本番コードと一致していること
		.hasMessage("この日付の日報は既に存在します");
		
		verify(reportRepository, never()).save(any());
	}
	
	
	
	@Test
	void updateReport_存在しない日報IDの場合はNoSuchElementExceptionが投げられること() {
		
		Long reportId = 999L;
		User loginUser = new User();
		ReportForm form = new ReportForm();
		
		when(reportRepository.findById(reportId)).thenReturn(Optional.empty());
		
		
		//【実行＆検証】
		assertThatThrownBy(() -> reportService.updateReport(reportId, loginUser, form))
		.isInstanceOf(NoSuchElementException.class);
		
		verify(reportMapper, never()).updateEntity(any(), any());
	}
	
	
	
	@Test
	void updateReport_編集中の日報と日付が重複する日報がある場合_DupeReportExceptionが発生すること() {
		
		Long reportId = 10L;
		User loginUser = new User();
		ReportForm form = new ReportForm();
		form.setReportDate(LocalDate.of(2026, 10, 3));
		
		Report report = new Report();
		
		when(reportRepository.findById(reportId)).thenReturn(Optional.of(report));
		
		when(reportRepository.existsByIdNotAndUserAndReportDate(reportId, loginUser, form.getReportDate())).thenReturn(true);
		
		
		//【実行＆検証】
		assertThatThrownBy(() -> reportService.updateReport(reportId, loginUser, form))
		.isInstanceOf(DupeReportException.class)
		.hasMessage("この日付の日報は既に存在します");
		
		verify(reportMapper, never()).updateEntity(any(), any());
	}
	
	
	
//	@Test
//	void test() {
//		
//	}
}
