package com.etterna.calc;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.etterna.calc.dto.CalcSSRRequest;

@RequestMapping("/calc")
@RestController
public class CalcManagementController {
	
	@Autowired
	private CalcManager calc;
	
	@GetMapping("/version")
	public Integer getVersion() {
		return calc.getCalcVersion();
	}
	
	@PostMapping("/ssr/{chartKey}")
	public List<Float> calculate(@PathVariable("chartKey") String chartKey, CalcSSRRequest req) {
		return calc.getSSR(chartKey, req.getRate(), req.getGoal());
	}
}
