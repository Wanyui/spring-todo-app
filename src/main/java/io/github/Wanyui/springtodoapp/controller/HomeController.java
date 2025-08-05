package io.github.Wanyui.springtodoapp.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {
	
	@GetMapping("/")
	public String home() {
		return "Hello, World!";
	}

	@GetMapping("/api/health")
	public String health() {
		return "OK";
	}
}
