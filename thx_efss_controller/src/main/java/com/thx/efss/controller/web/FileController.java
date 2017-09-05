package com.thx.efss.controller.web;

import java.io.IOException;
import java.util.List;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.thx.efss.dao.bean.ThxFile;
import com.thx.efss.service.web.FileService;

@Controller
public class FileController {

	@Autowired
	FileService fileService;

	@RequestMapping(value = "/file", method = RequestMethod.POST) // method = RequestMethod.GET
	public String fileUpload(@RequestParam("file") MultipartFile uploadFile) {
		try {
			if (uploadFile != null) {
				fileService.saveFile(uploadFile);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Docx4JException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "fileList";
	}

	@RequestMapping(value = "/file/list", method = RequestMethod.GET, produces=MediaType.APPLICATION_JSON_UTF8_VALUE) // method = RequestMethod.GET
	public List<ThxFile> fileList() {
		try {
			return fileService.getFileList();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
