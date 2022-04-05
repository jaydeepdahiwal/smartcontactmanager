package com.smart.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.Optional;

import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
	import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.Message;
import com.smart.repository.ContactRepository;
import com.smart.repository.UserRepository;

@Controller
@RequestMapping("/user")
public class UserController {

	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private ContactRepository contactRepository;
	
	@ModelAttribute
	public void addCommonData(Model model, Principal principal) {
		String username= principal.getName();
		User user = userRepository.getUserByUserName(username);
		model.addAttribute("user", user);
	}
	
	@RequestMapping("/index")
	public String dashboard(Model model) {
		model.addAttribute("title","Dashboard - smart contact manager");
		return "/normal/dashboard";
	}
	
	@GetMapping("/addcontact")
	public String addcontact(Model model) {
		model.addAttribute("title","Add Contact - smart contact manager");
		model.addAttribute("contact",new Contact());
		return "normal/addcontact";
	}
	
	@PostMapping("/process-contact")
	public String processContact(@ModelAttribute Contact contact, @RequestParam("profileImage") MultipartFile file, Principal principal, HttpSession session) {
		try {
			
		String name = principal.getName();
		User user= userRepository.getUserByUserName(name);
		if(file.isEmpty()) {
			System.out.println("File is Empty");
			contact.setImage("profile.png");
		}
		else {
			contact.setImage(file.getOriginalFilename());
			File file1= new ClassPathResource("/static/img").getFile();
			Path path= Paths.get(file1.getAbsolutePath()+File.separator+file.getOriginalFilename());
			Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
			System.out.println("Image is uploaded");
		}
		user.getContacts().add(contact);
		contact.setUser(user);
		this.userRepository.save(user);
		System.out.println("DATA:"+contact);
		//success message
		session.setAttribute("message",new Message("Contact Successfully added! add more..", "alert-success"));
		}
		catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error:"+e.getMessage());
			
			//error message
			session.setAttribute("message",new Message("Something went wrong!", "alert-danger"));
			return "normal/addcontact";

		}
		return "normal/addcontact";
	}
	
	@GetMapping("/viewcontacts/{page}")
	public String viewContacts(@PathVariable("page") Integer page ,Model model, Principal principal) {
		model.addAttribute("title","View Contacts - smart contact manager");
		String username=principal.getName();
		User user=this.userRepository.getUserByUserName(username);
		Pageable pageable= PageRequest.of(page, 3);
		Page<Contact> contacts=this.contactRepository.findContactsByUser(user.getId(), pageable);
		model.addAttribute("contacts",contacts);
		model.addAttribute("currentpage",page);
		model.addAttribute("totalpages",contacts.getTotalPages());
		return "/normal/viewcontacts";
		
	}
	
	@GetMapping("/{cId}/contact")
	public String showContact(@PathVariable("cId") Integer cId, Model model, Principal principal)
	{
		Optional<Contact> contactOptional = this.contactRepository.findById(cId);
		Contact contact = contactOptional.get();
		
		String userName= principal.getName();
		User user=this.userRepository.getUserByUserName(userName);
		//comparing user id with contact user id
		if(user.getId()==contact.getUser().getId())
		    model.addAttribute("contact",contact);
		
		return "normal/show-contact";
	}
	
	@GetMapping("/delete/{cId}")
	@Transactional
	public String deleteContact(@PathVariable("cId") Integer cId, HttpSession session,Model model, Principal principal) {
		Contact contact=this.contactRepository.findById(cId).get();
		
		User user= this.userRepository.getUserByUserName(principal.getName());
		user.getContacts().remove(contact);
		this.userRepository.save(user);
		
		this.contactRepository.delete(contact);
		System.out.println("contact deleted");
		session.setAttribute("message", new Message("Contact delete successfully...","alert-success"));
		return "redirect:/user/viewcontacts/0";
	}
	
   //open update contact form
	@PostMapping("/update-contact/{cid}")
	public String updateContact(@PathVariable("cid") Integer cid, Model model) {
		Contact contact=this.contactRepository.findById(cid).get();
		model.addAttribute("contact",contact);
		model.addAttribute("title","Update - smart contact manager");
		return "normal/update_contact";
	}
	
	//update contact handler
	@PostMapping("/process-update")
	public String processContact(@ModelAttribute Contact contact,@RequestParam("profileImage") MultipartFile file, Model model, HttpSession session,Principal principal) {
		try {
			Contact oldContactDetails=this.contactRepository.findById(contact.getcId()).get();
			if(!file.isEmpty()) {
				//delete file
				File deleteFile = new ClassPathResource("static/img").getFile();
				File file1 = new File(deleteFile, oldContactDetails.getImage());
				file1.delete();
				
                //update file				
				File file2= new ClassPathResource("/static/img").getFile();
				Path path= Paths.get(file2.getAbsolutePath()+File.separator+file.getOriginalFilename());
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);	
				contact.setImage(file.getOriginalFilename());
			}
			else {
				contact.setImage(oldContactDetails.getImage());
			}
			User user= this.userRepository.getUserByUserName(principal.getName());
			contact.setUser(user);
			this.contactRepository.save(contact);
			session.setAttribute("message", new Message("Contact updated successfully", "alert-success"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "redirect:/user/"+contact.getcId()+"/contact";
	}
	
	@GetMapping("/profile")
	public String showProfile(Model model) {
		model.addAttribute("title","My Profile - smart contact manager");
		return "normal/profile";
	}
	
	@GetMapping("/settings")
	public String settings(Model model) {
		model.addAttribute("title","Settings - smart contact manager");
		return "normal/settings";
	}
	
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("oldPassword") String oldPassword, @RequestParam("newPassword") String newPassword, Principal principal, HttpSession session) {
		String username= principal.getName();
		User user=this.userRepository.getUserByUserName(username);
		if(bCryptPasswordEncoder.matches(oldPassword, user.getPassword())) {
			//change
			bCryptPasswordEncoder.encode(newPassword);
			this.userRepository.save(user);
			session.setAttribute("message", new Message("Your Password is successfully changed..","alert-success" ));
		}
		else {
			//error
			session.setAttribute("message", new Message("Please enter correct old Password","alert-danger" ));
			return "redirect:/user/settings";
		}
		return "redirect:/user/index";
	}
	
}
