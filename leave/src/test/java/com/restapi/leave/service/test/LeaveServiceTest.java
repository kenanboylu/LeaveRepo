package com.restapi.leave.service.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.restapi.leave.model.Employee;
import com.restapi.leave.model.Leave;
import com.restapi.leave.model.LeaveStatus;
import com.restapi.leave.repository.EmployeeRepository;
import com.restapi.leave.repository.LeaveRepository;
import com.restapi.leave.service.impl.LeaveServiceImpl;

@ExtendWith(MockitoExtension.class)
public class LeaveServiceTest {

	@InjectMocks
	LeaveServiceImpl leaveService;

	@Mock
	LeaveRepository leaveRepository;

	@Mock
	EmployeeRepository employeeRepository;

	@Test
	public void findLeavesByPendingStatus_whenLeaveRecordExistsOnPendingStatus_returnLeaveList() {
		Mockito.when(leaveRepository.findLeavesByStatus(String.valueOf(LeaveStatus.pending)))
				.thenReturn(createLeaveList(1, String.valueOf(LeaveStatus.pending)));
		List<Leave> resultList = leaveService.findLeavesByPendingStatus();
		assertEquals(1, resultList.size());
	}

	@Test
	public void approveLeave_whenLeaveIsNotFound_returnMessageCode() {
		Mockito.when(leaveRepository.existsById(1L)).thenReturn(false);
		String messageCode = leaveService.approveLeave(1L);
		assertEquals("leaveNotFound", messageCode);
	}

	@Test
	public void approveLeave_whenLeaveIsFound_returnMessageCode() {
		Mockito.when(leaveRepository.existsById(1L)).thenReturn(true);
		Mockito.when(leaveRepository.findById(1L)).thenReturn(leave(1L));
		Mockito.when(leaveRepository.save(Mockito.any(Leave.class))).thenReturn(null);
		String messageCode = leaveService.approveLeave(1L);
		assertEquals("leaveApproved", messageCode);
	}

	@Test
	public void rejectLeave_whenLeaveIsNotFound_returnMessageCode() {
		Mockito.when(leaveRepository.existsById(1L)).thenReturn(false);
		String messageCode = leaveService.approveLeave(1L);
		assertEquals("leaveNotFound", messageCode);
	}

	@Test
	public void rejectLeave_whenLeaveIsFound_returnMessageCode() {
		Mockito.when(leaveRepository.existsById(1L)).thenReturn(true);
		Mockito.when(leaveRepository.findById(1L)).thenReturn(leave(1L));
		Mockito.when(leaveRepository.save(Mockito.any(Leave.class))).thenReturn(null);
		String messageCode = leaveService.approveLeave(1L);
		assertEquals("leaveApproved", messageCode);
	}

	@Test
	public void findLeavesByEmployeeId_whenEmployeeHasLeaves_returnLeaveList() {
		Mockito.when(employeeRepository.findById(1L)).thenReturn(employee(1L,new Date()));
		List<Leave> resultList = leaveService.findLeavesByEmployeeId(1L);
		assertEquals(1, resultList.size());
	}

	@Test
	public void getUsedLeaveDays_whenPendingLeaveExist_returnDayCount() {
		List<Leave> list = createLeaveList(1, String.valueOf(LeaveStatus.pending));
		Integer result = leaveService.getUsedLeaveDays(list);
		assertEquals(1, result);
	}
	
	@Test
	public void getUsedLeaveDays_whenApprovedLeaveExist_returnDayCount() {
		List<Leave> list = createLeaveList(1, String.valueOf(LeaveStatus.approved));
		Integer result = leaveService.getUsedLeaveDays(list);
		assertEquals(1, result);
	}
	
	@Test
	public void getTotalLeaveRights_whenEmployeeWorkedLessThanOneYear_returnFiveDaysRight() {
		Calendar c = Calendar.getInstance(); 
		c.setTime(new Date()); 
		c.add(Calendar.DATE, -100);
		Integer result=leaveService.getTotalLeaveRights(c.getTime());
		assertEquals(5, result);
	}
	
	@Test
	public void getTotalLeaveRights_whenEmployeeWorkedFiveYear_returnFifteenDaysRight() {
		Calendar c = Calendar.getInstance(); 
		c.setTime(new Date()); 
		c.add(Calendar.DATE, 365);
		Integer result=leaveService.getTotalLeaveRights(c.getTime());
		assertEquals(15, result);
	}
	
	@Test
	public void getTotalLeaveRights_whenEmployeeWorkedTenYears_return165DaysRight() {
		Calendar c = Calendar.getInstance(); 
		c.setTime(new Date()); 
		c.add(Calendar.DATE, 365*10);
		Integer result=leaveService.getTotalLeaveRights(c.getTime());
		assertEquals(165, result);
	}
	
	@Test
	public void getTotalLeaveRights_whenEmployeeWorkedFifteenYears_return285DaysRight() {
		Calendar c = Calendar.getInstance(); 
		c.setTime(new Date()); 
		c.add(Calendar.DATE, 365*15);
		Integer result=leaveService.getTotalLeaveRights(c.getTime());
		assertEquals(285, result);
	}
	
	@Test
	public void calculateWorkDays_whenStartDateEqualsEndDate_returnZeroWorkDays() {
		Calendar c = Calendar.getInstance(); 
		c.setTime(new Date()); 
		try {
			Integer result=leaveService.calculateWorkDays(c.getTime(),c.getTime());
			assertEquals(0, result);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void calculateWorkDays_whenStartDateDoesNotEqualsEndDate_returnNonZeroWorkDays() {
		Calendar c1 = Calendar.getInstance(); 
		c1.setTime(new Date()); 
		
		Calendar c2 = Calendar.getInstance(); 
		c2.setTime(new Date()); 
		c2.add(Calendar.DATE, 100);
		
		try {	
			Integer result=leaveService.calculateWorkDays(c1.getTime(),c2.getTime());
			assertTrue(result !=0);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void requestLeave_whenEmployeeIsNotFound_returnNotFoundMessageCode() {
		Calendar c1 = Calendar.getInstance(); 
		c1.setTime(new Date()); 
		
		Calendar c2 = Calendar.getInstance(); 
		c2.setTime(new Date()); 
		c2.add(Calendar.DATE, 365);	
		
		Mockito.when(employeeRepository.existsById(1L)).thenReturn(false);
		
		String messageCode=leaveService.requestLeave(1L, c1.getTime(), c2.getTime());
		
		assertEquals("employeeNotFound", messageCode);
	}

	public static List<Leave> createLeaveList(int count, String status) {
		List<Leave> list = new ArrayList<Leave>();

		for (int i = 0; i < count; i++) {
			Leave leave = new Leave();
			leave.setStatus(status);
			leave.setWorkDay(1);
			list.add(leave);
		}

		return list;
	}

	public static Optional<Leave> leave(Long id) {
		Leave leave = new Leave();
		leave.setId(id);
		return Optional.of(leave);
	}

	public static Optional<Employee> employee(Long id,Date startWorkDate) {
		Employee emp = new Employee();
		emp.setEmployeeId(id);
		emp.setWorkStartDate(startWorkDate);
		emp.setLeaveList(createLeaveList(1, String.valueOf(LeaveStatus.approved)));
		return Optional.of(emp);
	}

}
