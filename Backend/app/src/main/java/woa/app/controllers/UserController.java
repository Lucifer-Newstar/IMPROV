package woa.app.controllers;

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
}
