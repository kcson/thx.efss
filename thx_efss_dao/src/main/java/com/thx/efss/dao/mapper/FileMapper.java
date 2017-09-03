package com.thx.efss.dao.mapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.thx.efss.dao.bean.ThxFile;

public interface FileMapper {
	public int insertFile(ThxFile file);

	public List<HashMap<String, Object>> selectFileList(Map<String, Object> paramMap);

}
