package com.thx.efss.controller.web;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.thx.efss.dao.bean.ThxFile;
import com.thx.efss.dao.bean.ThxFileProperty;
import com.thx.efss.service.web.FileService;

@Controller
public class FileController {

	@Autowired
	FileService fileService;

	@RequestMapping(value = "/file", method = RequestMethod.POST) // method = RequestMethod.GET
	public String fileUpload(@RequestParam("file") MultipartFile uploadFile, @RequestParam(value="contentKey",required=false) String contentKey) {
		try {
			if (uploadFile != null) {
				fileService.saveFile(uploadFile, contentKey);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "fileList";
	}

	@RequestMapping(value = "/file/list", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody List<ThxFile> fileList(@RequestParam("searchParam") String searchParam) {
		List<ThxFile> fileList = new ArrayList<>();
		try {
			fileList = fileService.getFileList(searchParam);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return fileList;
	}

	@RequestMapping(value = "/file/{fileId}", method = RequestMethod.GET)
	public void fileDownload(@PathVariable long fileId, HttpServletRequest request, HttpServletResponse response) {
		try {
			fileService.downloadFile(fileId, request, response);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@RequestMapping(value = "/fileproperty/{fileId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody List<ThxFileProperty> getFileProperty(@PathVariable long fileId) {
		List<ThxFileProperty> fileList = new ArrayList<>();

		try {
			fileList = fileService.getFileProperty(fileId);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return fileList;
	}

	@RequestMapping(value = "/file/{fileId}", method = RequestMethod.DELETE)
	public @ResponseBody void deleteFile(@PathVariable long fileId) {
		try {
			fileService.deleteFile(fileId);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@RequestMapping(value = "/fileproperty/{fileId}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody HashMap<String, Object> putFileProperty(@PathVariable long fileId, @RequestBody List<HashMap<String, Object>> properties) {
		HashMap<String, Object> returnMap = new HashMap<>();

		try {
			returnMap = fileService.putFileProperty(fileId, properties);
		} catch (Exception e) {
			returnMap.put("result", "fail");
			e.printStackTrace();
		}

		return returnMap;
	}

}
