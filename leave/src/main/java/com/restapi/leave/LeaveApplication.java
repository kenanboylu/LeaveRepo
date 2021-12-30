package com.restapi.leave;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

import com.restapi.leave.model.Employee;
import com.restapi.leave.model.Leave;
import com.restapi.leave.model.LeaveStatus;
import com.restapi.leave.repository.EmployeeRepository;

import springfox.documentation.swagger2.annotations.EnableSwagger2;

@EnableSwagger2
@SpringBootApplication
@ComponentScan
public class LeaveApplication {
	
	@Autowired
    EmployeeRepository repository;

	public static void main(String[] args) {
		SpringApplication.run(LeaveApplication.class, args);
	}
	
	@Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasenames("classpath:/messages");
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setUseCodeAsDefaultMessage(true);
        messageSource.setCacheSeconds((int) TimeUnit.HOURS.toSeconds(1));
        messageSource.setFallbackToSystemLocale(false);
        return messageSource;
    }
	
	@Bean
    InitializingBean sendDatabase() {
        return () -> {

            Calendar calendarStart = Calendar.getInstance();
            calendarStart.set(Calendar.YEAR, 2021);
            calendarStart.set(Calendar.MONTH, 12);
            calendarStart.set(Calendar.DATE, 31);

            Employee e=new Employee();
            e.setAddress("istanbul");
            e.setEmail("email@gmail.com");
            e.setGender("male");
            e.setName("name");
            e.setSurname("surname");
            e.setPhoneNumber("5555555");
            e.setWorkStartDate(calendarStart.getTime());


            Leave leave=new Leave();
            leave.setStatus(String.valueOf(LeaveStatus.approved));
            calendarStart.add(Calendar.DATE, -5);
            leave.setStartDate(calendarStart.getTime());
            calendarStart.add(Calendar.DATE, 3);
            leave.setEndDate(calendarStart.getTime());
            leave.setWorkDay(3);


            List<Leave> leaveList=new ArrayList<Leave>();
            leaveList.add(leave);

            e.setLeaveList(leaveList);

            repository.save(e);
        };
    }

}
