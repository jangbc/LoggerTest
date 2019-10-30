package net.smartworks.log.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LogTestController {

	@RequestMapping(value = "/log/test", method = RequestMethod.GET)
	@ResponseStatus(value = HttpStatus.OK)
	public String logTest() {
		return "Log Test";
	}
}