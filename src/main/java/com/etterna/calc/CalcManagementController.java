package com.etterna.calc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/calc")
@RestController
public class CalcManagementController {

	
	@Autowired
	private CalcManager calc;
	
	@GetMapping("/test")
	public String a() {
		return "calc version "+calc.dothing();
	}
}
