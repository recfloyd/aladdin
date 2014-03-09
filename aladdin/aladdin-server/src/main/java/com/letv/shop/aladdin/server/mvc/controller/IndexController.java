package com.letv.shop.aladdin.server.mvc.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class IndexController {
	@RequestMapping(value = "/index.do")
	public String index() {
		return "index";
	}
	
	@RequestMapping(value = "/index2.do")
	public String index2() {
		return "index2";
	}
}
