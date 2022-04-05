package com.smart.controller;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.entities.User;
import com.smart.helper.Message;
import com.smart.repository.UserRepository;

@Controller
public class HomeController {

	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private BCryptPasswordEncoder PasswordEncoder;
	
	@RequestMapping("/")
	public String home(Model model) {
		model.addAttribute("title","Home - smart contact manager");
		return "home";
	}
	
	@RequestMapping("/about")
	public String about(Model model) {
		model.addAttribute("title","About - smart contact manager");
		return "about";
	}
	
	@RequestMapping("/signup")
	public String signup(Model model) {
		model.addAttribute("title","Register - smart contact manager");
		model.addAttribute("user", new User());
		return "signup";
	}
	
	@RequestMapping("/login")
	public String login(Model model) {
		model.addAttribute("title","Login - smart contact manager");
		return "login";
	}
	
	@PostMapping("/do_register")
	public String registerUser(@Valid @ModelAttribute("user") User user,BindingResult result1, @RequestParam(value = "agreement", defaultValue = "false") boolean agreement, Model model, HttpSession session) {
		try {
		//checking agreement checkbox	
		if(!agreement) {
			System.out.println("You've not agreed terms and condition");
			throw new Exception(" You've not agreed terms and condition");
		}
		if(result1.hasErrors()) {
			model.addAttribute("user",user);
			System.out.println("ERROR"+result1.toString());
			return "signup";
		}
		//setting role, enabled and image
		user.setRole("ROLE_USER");
		user.setEnabled(true);
		user.setImageUrl("default.png");
		user.setPassword(PasswordEncoder.encode(user.getPassword()));
		//saving user in database
		User result=this.userRepository.save(user);
		//after successfully registered it return blank fields for new registration
		model.addAttribute("user",result);
		//success message
		session.setAttribute("message",new Message("Successfully Registered!!" , "alert-success"));
		return "signup";
		}
		catch (Exception e) {
			e.printStackTrace();
			model.addAttribute("user",user);
			session.setAttribute("message",new Message("Something went wrong!!"+e.getMessage(), "alert-danger"));
			return "signup";
		}

	}
	
		@GetMapping("/login")
		public String signin(Model model) {
			model.addAttribute("title","Login - smart contact manager");
			return "signin";
		}
		@RequestMapping("/notfound")
		public String notfound(Model model) {
			model.addAttribute("title","Error - smart contact manager");
			return "notfound";
		}
	
}
