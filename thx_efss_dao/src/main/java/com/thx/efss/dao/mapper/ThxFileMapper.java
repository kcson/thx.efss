package com.thx.efss.dao.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.thx.efss.dao.bean.ThxFile;
import com.thx.efss.dao.bean.ThxFileProperty;

public interface ThxFileMapper {
	public int insertFile(ThxFile file);

	public int insertFileProperty(ThxFileProperty thxFileProperty);

	public List<ThxFile> selectFileList(Map<String, Object> paramMap);

	public List<ThxFileProperty> selectFileProperty(@Param("fileId") long fileId);

	public int deleteFile(@Param("fileId") long fileId);

	public int deleteFileProperty(@Param("fileId") long fileId);

}
