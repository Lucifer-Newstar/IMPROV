package woa.app.controllers;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import woa.app.entity.UserEntity;
import woa.app.repository.UserRepository;


@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {
	@Autowired
	private UserRepository userRepository;
	
	
	@PostMapping("/register")
	public UserEntity register(@RequestBody UserEntity user) {
		
		return userRepository.save(user);  
	}
	
	
	@PostMapping("/login")
	public Map<String, String> login(@RequestBody Map<String, String> body){
		String user_name=body.get("user_name");
		String user_password=body.get("user_password");
		
	    return userRepository.findByLogin(user_name, user_password).map(u -> Map.of("status","success","message","Login successful!"))
	    		.orElse(Map.of("status","failure","message","Invalid username or password."));
	
		
	}
}
