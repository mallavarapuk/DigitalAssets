//package com.mss.sample;
//
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.PropertySource;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//@RestController
//@PropertySource("classpath:map.properties")
//public class DigitalAssets {
//
//	@Value("${GOTOWEBINAR.USERNAME}")
//	private String webUserName;
//
//	@GetMapping("/formgetEnvData")
//	public String getEnvData() {
//		return webUserName;
//	}
//
//}
