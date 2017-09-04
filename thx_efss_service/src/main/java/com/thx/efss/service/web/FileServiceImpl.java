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

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
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

		// aws s3에 파일 저장
		//BasicAWSCredentials creds = new BasicAWSCredentials(AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY);
		AmazonS3 s3Client = AmazonS3ClientBuilder.standard().withRegion(Regions.AP_NORTHEAST_2).withCredentials(new ProfileCredentialsProvider())
				.build();

		String contentKey = getUuid();
		ObjectMetadata metadata = new ObjectMetadata();
		metadata.setContentLength(uploadFile.getSize());
		metadata.setContentType(uploadFile.getContentType());
		s3Client.putObject(new PutObjectRequest("thxcloud.com", contentKey, uploadFile.getInputStream(), metadata));

		// s3에 저장 성공하면 DB에 파일 관련 정보 저장
		ThxFile thxFile = new ThxFile();
		thxFile.setOriginalFileName(originFileName);
		thxFile.setStoredFileName(contentKey);

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
