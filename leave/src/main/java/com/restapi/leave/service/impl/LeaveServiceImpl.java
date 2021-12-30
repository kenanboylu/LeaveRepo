package com.restapi.leave.service.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.restapi.leave.model.Employee;
import com.restapi.leave.model.Leave;
import com.restapi.leave.model.LeaveStatus;
import com.restapi.leave.model.PublicHoliday;
import com.restapi.leave.repository.EmployeeRepository;
import com.restapi.leave.repository.LeaveRepository;
import com.restapi.leave.service.LeaveService;

import kong.unirest.GenericType;
import kong.unirest.Unirest;

@Service
public class LeaveServiceImpl implements LeaveService {

	@Autowired
	LeaveRepository leaveRepository;

	@Autowired
	EmployeeRepository employeeRepository;

	@Override
	public List<Leave> findLeavesByPendingStatus() {
		List<Leave> list = leaveRepository.findLeavesByStatus(String.valueOf(LeaveStatus.pending));
		return list;
	}

	@Override
	public String approveLeave(Long id) {		
		
		if(!leaveRepository.existsById(id))
			return "leaveNotFound";
		
		Leave leave = leaveRepository.findById(id).get();

		leave.setStatus(String.valueOf(LeaveStatus.approved));

		leaveRepository.save(leave);
		
		return "leaveApproved";

	}

	@Override
	public String rejectLeave(Long id) {
		
		if(!leaveRepository.existsById(id))
			return "leaveNotFound";
		
		Leave leave = leaveRepository.findById(id).get();

		leave.setStatus(String.valueOf(LeaveStatus.rejected));

		leaveRepository.save(leave);
		
		return "leaveRejected";
	}

	@Override
	public List<Leave> findLeavesByEmployeeId(Long id) {		
		Employee employee = employeeRepository.findById(id).get();

		return employee.getLeaveList();
	}

	@Override
	public String requestLeave(Long employeeId, Date startDate, Date endDate) {
		
		Integer requestedDays = null;
		try {
			requestedDays = calculateWorkDays(startDate, endDate);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		if(!employeeRepository.existsById(employeeId))
			return "employeeNotFound";

		Employee emp = employeeRepository.findById(employeeId).get();

		Integer usedRights = getUsedLeaveDays(emp.getLeaveList());

		Integer maxRights = getTotalLeaveRights(emp.getWorkStartDate());

		Integer currentRight = maxRights - usedRights;

		if (requestedDays > currentRight)
			return "exceedLeaveRight";

		Leave leave = new Leave();
		leave.setStartDate(startDate);
		leave.setEndDate(endDate);
		leave.setStatus(String.valueOf(LeaveStatus.pending));
		leave.setWorkDay(requestedDays);

		emp.getLeaveList().add(leave);
		employeeRepository.save(emp);

		return "leaveRequestCreated";
	}
	

	public Integer calculateWorkDays(Date startDate, Date endDate) throws ParseException {

		Calendar startDateCal = Calendar.getInstance();
		startDateCal.setTime(startDate);

		Calendar endDateCal = Calendar.getInstance();
		endDateCal.setTime(endDate);

		List<Calendar> holidayList = getOfficalHolidays();

		List<Integer> holidayDayOfYearList = new ArrayList<Integer>();

		for (int i = 0; i < holidayList.size(); i++) {
			holidayDayOfYearList.add(holidayList.get(i).get(Calendar.DAY_OF_YEAR));
		}

		int workDays = 0;

		if (startDateCal.getTimeInMillis() == endDateCal.getTimeInMillis()) {
			return 0;
		}

		do {
			startDateCal.add(Calendar.DAY_OF_MONTH, 1);
			if (startDateCal.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY
					&& startDateCal.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY
					&& !holidayDayOfYearList.contains((Integer) startDateCal.get(Calendar.DAY_OF_YEAR))) {
				++workDays;
			}
		} while (startDateCal.getTimeInMillis() < endDateCal.getTimeInMillis());

		return workDays;

	}
	
	public List<Calendar> getOfficalHolidays() throws ParseException {

        List<PublicHoliday> publicHoliday = Unirest.get("https://date.nager.at/api/v2/publicholidays/"
                        + String.valueOf(Calendar.getInstance().get(Calendar.YEAR)) + "/tr")
                .asObject(new GenericType<List<PublicHoliday>>() {
                }).getBody();

        List<Calendar> officialHolidayList = new ArrayList<Calendar>();

        for (int i = 0; i < publicHoliday.size(); i++) {

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd");
            Date date = sdf.parse(publicHoliday.get(i).getDate());
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            officialHolidayList.add(calendar);

        }

        return officialHolidayList;
    }


	public Integer getTotalLeaveRights(Date startDate) {

		long diffInMillies = Math.abs(new Date().getTime() - startDate.getTime());
		long difference_In_Days = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);

		Integer years = (int) (difference_In_Days / 365);

		if (years < 1) {
			return 5;
		} else if (1 <= years && years <= 5 ) {
			return (years) * 15;
		} else if (5 < years && years <= 10) {
			return (5 * 15) + ((years - 5) * 18);
		} else {
			return (5 * 15) + (5 * 18) + ((years - 10) * 24);
		}

	}

	public Integer getUsedLeaveDays(List<Leave> leaves) {

		Integer usedDays = 0;

		for (int i = 0; i < leaves.size(); i++) {

			if (leaves.get(i).getStatus().equalsIgnoreCase(String.valueOf(LeaveStatus.approved))
					|| leaves.get(i).getStatus().equalsIgnoreCase(String.valueOf(LeaveStatus.pending))) {
				usedDays = usedDays + leaves.get(i).getWorkDay();
			}

		}

		return usedDays;

	}
	

}
