package com.restapi.leave.service;

import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Component;

import com.restapi.leave.model.Leave;

@Component
public interface LeaveService {
	
	public List<Leave> findLeavesByPendingStatus();
	
	public String approveLeave(Long id);
	
	public String rejectLeave(Long id);

	public List<Leave> findLeavesByEmployeeId(Long id);
	
	public String requestLeave(Long employeeId,Date startDate,Date endDate);
	
}
