package com.thx.efss.service.web;

import org.springframework.web.multipart.MultipartFile;

public interface FileService {
	public void saveFile(MultipartFile uploadFile) throws Exception;
}
