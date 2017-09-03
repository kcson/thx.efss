package com.thx.efss.dao.mapper;

import java.util.List;
import java.util.Map;

import com.thx.efss.dao.bean.ThxFile;

public interface ThxFileMapper {
	public int insertFile(ThxFile file);

	public List<ThxFile> selectFileList(Map<String, Object> paramMap);

}
