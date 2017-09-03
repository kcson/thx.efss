package com.thx.efss.service.web;

import java.util.UUID;

import org.docx4j.XmlUtils;
import org.docx4j.docProps.custom.Properties;
import org.docx4j.docProps.custom.Properties.Property;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.DocPropsCustomPart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.thx.efss.dao.bean.ThxFile;
import com.thx.efss.dao.bean.ThxFileProperty;
import com.thx.efss.dao.mapper.ThxFileMapper;

@Service
public class FileServiceImpl implements FileService {

	@Autowired
	ThxFileMapper thxFileMapper;

	@Override
	@Transactional
	public void saveFile(MultipartFile uploadFile) throws Exception {
		// byte[] fileBytes = uploadFile.getBytes();
		String originFileName = new String(uploadFile.getOriginalFilename().getBytes("8859_1"), "UTF-8");
		System.out.println(originFileName);

		ThxFile thxFile = new ThxFile();
		thxFile.setOriginalFileName(originFileName);
		thxFile.setStoredFileName(getUuid());

		thxFileMapper.insertFile(thxFile);

		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(uploadFile.getInputStream());

		DocPropsCustomPart customPart = wordMLPackage.getDocPropsCustomPart();
		if (customPart != null) {
			ThxFileProperty fileProperty = new ThxFileProperty();
			fileProperty.setFileId(thxFile.getId());

			Properties properties = customPart.getContents();
			for (Property property : properties.getProperty()) {
				if (property.getLpwstr() != null) {
					System.out.println(property.getName() + " = " + property.getLpwstr());

					fileProperty.setPropertyValue(property.getLpwstr());
				} else {
					System.out.println(property.getName() + ": \n "
							+ XmlUtils.marshaltoString(property, Context.jcDocPropsCustom));

					fileProperty.setPropertyValue(XmlUtils.marshaltoString(property, Context.jcDocPropsCustom));
				}
				fileProperty.setPropertyKey(property.getName());
				thxFileMapper.insertFileProperty(fileProperty);
			}
		}

	}

	// uuid생성
	public String getUuid() {
		return UUID.randomUUID().toString().replaceAll("-", "");
	}
}
