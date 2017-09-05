package com.thx.efss.service.web;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.thx.efss.dao.bean.ThxFile;

public interface FileService {
	public void saveFile(MultipartFile uploadFile) throws Exception;

	public List<ThxFile> getFileList() throws Exception;
}
