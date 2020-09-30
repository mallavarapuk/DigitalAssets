package com.mss.sample;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DigitalAssetsController {

	@Autowired
	private DigitalAssetsDao digitalAssetsDao;

	@GetMapping("/digital-assets")
	public Map getFoldersAndFiles(@RequestParam(value = "folderkey") String folderKey) {
		Map response = digitalAssetsDao.getFoldersAndFiles(folderKey);
		return response;
	}

	public static void main(String[] args) {
		List<String> list = new ArrayList();
		list.add("user");
		list.add("User");

		List<String> duplicates = new ArrayList();
		for (String str : list) {
			if (!duplicates.contains(str)) {
				System.out.println(str);
				duplicates.add(str);
			}
		}
	}

}
