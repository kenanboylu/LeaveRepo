package com.restapi.leave.controller;

import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.restapi.leave.model.Leave;
import com.restapi.leave.service.LeaveService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping(path = "manager")
@Api(value = "Manager Rest Api")
public class ManagerController {

	@Autowired
	LeaveService leaveService;
	
	@Autowired
	MessageSource messageSource;

	@GetMapping("/leave")
	@ApiOperation(value = "list leaves on pending status")
	public ResponseEntity findPendingLeaves() {
		List<Leave> leaveList = leaveService.findLeavesByPendingStatus();
		return new ResponseEntity<>(leaveList, HttpStatus.OK);

	}

	@PostMapping("/approve")
	@ApiOperation(value = "approve leave with leave id")
	public ResponseEntity approveLeave(@RequestParam(name = "id") Long id) {
		String response=leaveService.approveLeave(id);
		return new ResponseEntity<>(messageSource.getMessage(response,new Object[0], new Locale("tr")), HttpStatus.OK);
	}

	@PostMapping("/reject")
	@ApiOperation(value = "reject leave with leave id")
	public ResponseEntity rejectLeave(@RequestParam(name = "id") Long id) {
		String response = leaveService.rejectLeave(id);
		return new ResponseEntity<>(messageSource.getMessage(response,new Object[0], new Locale("tr")), HttpStatus.OK);

	}

}
