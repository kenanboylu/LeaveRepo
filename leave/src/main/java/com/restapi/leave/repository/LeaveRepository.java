package com.restapi.leave.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.restapi.leave.model.Leave;

@Repository
public interface LeaveRepository extends JpaRepository<Leave, Long>{

	
	public List<Leave> findLeavesByStatus(String status);
}
