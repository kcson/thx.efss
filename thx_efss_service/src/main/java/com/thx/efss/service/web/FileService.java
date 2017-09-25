package com.thx.efss.service.web;

import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.multipart.MultipartFile;

import com.thx.efss.dao.bean.ThxFile;
import com.thx.efss.dao.bean.ThxFileProperty;

public interface FileService {
	public void saveFile(MultipartFile uploadFile) throws Exception;

	public void downloadFile(long fileId, HttpServletRequest request, HttpServletResponse response) throws Exception;

	public void deleteFile(long fileId) throws Exception;

	public List<ThxFile> getFileList(String fullTextSearchParam) throws Exception;

	public List<ThxFileProperty> getFileProperty(long fileId) throws Exception;

	public HashMap<String, Object> putFileProperty(long fileId, List<HashMap<String, Object>> properties) throws Exception;
}
