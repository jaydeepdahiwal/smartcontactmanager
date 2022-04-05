package com.smart.controller;

import java.util.Random;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.entities.User;
import com.smart.repository.UserRepository;
import com.smart.service.emailService;

@Controller
public class FogotController {
	
	@Autowired
	private emailService Emailservice;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	Random random = new Random(1001);
	
	@RequestMapping("/forgot")
	public String forgot_password() {
		return "forgot_password";
	}
	
	@PostMapping("/send-otp")
	public String sendOtp(@RequestParam("email") String email, HttpSession session) {
	
		int otp=random.nextInt(999999);
		
		String subject="OTP from SCM";
		String message = ""
						+"<div style'border:1px solid #e2e2e2; padding:20px'>"
						+"<h1>"
						+"OTP is "
						+"<b>"+otp
						+"</b>"
						+"</h1"
						+"</div>";
		String to=email;
		
		boolean flag = this.Emailservice.sendEmail(subject, message, to);
		
		if(flag) {
			session.setAttribute("myotp", otp);
			session.setAttribute("email", email);
			return "verify";
		}
		else {
			session.setAttribute(message, "check your email id" );
			return "forgot_password";
		}
		
	}
	
	@PostMapping("/confirm-otp")
	public String verifyOtp(@RequestParam("otp") int otp, HttpSession session) {
		int myOtp=(int)session.getAttribute("myotp");
		String email= (String)session.getAttribute("email");
		if(otp==myOtp) {
			
			User user= this.userRepository.getUserByUserName(email);
			if(user==null) {
				session.setAttribute("message", "Invalid User");
				return "forgot_password";
			}
			else {
				return "change-password_form";
			}

		}
		else {
			session.setAttribute("message", "You've Entered wrong OTP");
			return "verify";
		}
	}
	
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("newPassword") String newPassword, HttpSession session) {
		String userEmail=(String)session.getAttribute("email");
		User user=this.userRepository.getUserByUserName(userEmail);
		String pass=bCryptPasswordEncoder.encode(newPassword);
		user.setPassword(pass);
		this.userRepository.save(user);
		session.setAttribute("message", "Password Changed Successfully");
		return "redirect:/login?change=password changed successfully...";
	}
}
