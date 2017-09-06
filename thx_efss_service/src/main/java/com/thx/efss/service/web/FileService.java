package com.thx.efss.service.web;

import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.springframework.web.multipart.MultipartFile;

import com.thx.efss.dao.bean.ThxFile;

public interface FileService {
	public void saveFile(MultipartFile uploadFile) throws Exception;

	public void downloadFile(long fileId, HttpServletResponse response) throws Exception;

	public List<ThxFile> getFileList() throws Exception;
}
