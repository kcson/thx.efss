package com.thx.efss.service.web;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
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
		// BasicAWSCredentials creds = new BasicAWSCredentials(AWS_ACCESS_KEY_ID,
		// AWS_SECRET_ACCESS_KEY);
		AmazonS3 s3Client = AmazonS3ClientBuilder.standard().withRegion(Regions.AP_NORTHEAST_2)
				.withCredentials(new ProfileCredentialsProvider()).build();

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

	@Override
	public List<ThxFile> getFileList() throws Exception {
		HashMap<String, Object> paramMap = new HashMap<>();
		return thxFileMapper.selectFileList(paramMap);
	}

	@Override
	public void downloadFile(long fileId, HttpServletRequest request, HttpServletResponse response) throws Exception {
		HashMap<String, Object> paramMap = new HashMap<>();
		paramMap.put("id", fileId);

		OutputStream out = response.getOutputStream();

		List<ThxFile> fileList = thxFileMapper.selectFileList(paramMap);
		if (fileList != null && fileList.size() > 0) {
			ThxFile thxFile = fileList.get(0);

			AmazonS3 s3Client = AmazonS3ClientBuilder.standard().withRegion(Regions.AP_NORTHEAST_2)
					.withCredentials(new ProfileCredentialsProvider()).build();

			S3Object object = s3Client.getObject(new GetObjectRequest("thxcloud.com", thxFile.getStoredFileName()));
			ObjectMetadata metaData = object.getObjectMetadata();
			InputStream objectData = object.getObjectContent();

			response.setContentType(metaData.getContentType());
			response.setContentLengthLong(metaData.getContentLength());
			setFileName(request, response, thxFile.getOriginalFileName());
			response.setHeader("Content-Transfer-Encoding", "binary");

			byte[] buffer = new byte[1024];
			int readBytes = 0;
			while ((readBytes = objectData.read(buffer, 0, 1024)) != -1) {
				out.write(buffer, 0, readBytes);
				out.flush();
			}
		}

		if (out != null) {
			out.close();
		}
	}

	private void setFileName(HttpServletRequest request, HttpServletResponse response, String fileName)
			throws Exception {
		String header = request.getHeader("User-Agent");
		if (header.contains("MSIE") || header.contains("Trident")) { // IE 11버전부터 Trident로 변경되었기때문에 추가해준다.
			fileName = URLEncoder.encode(fileName, "UTF-8").replaceAll("\\+", "%20");
			response.setHeader("Content-Disposition", "attachment;filename=" + fileName + ";");
		} else if (header.contains("Chrome")) {
			fileName = new String(fileName.getBytes("UTF-8"), "ISO-8859-1");
			response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
		} else if (header.contains("Opera")) {
			fileName = new String(fileName.getBytes("UTF-8"), "ISO-8859-1");
			response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
		} else if (header.contains("Firefox")) {
			fileName = new String(fileName.getBytes("UTF-8"), "ISO-8859-1");
			response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
		}else if(header.contains("Safari")) {
			fileName =  new String(fileName.getBytes("UTF-8"), "8859_1");
			response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
		}
	}

	@Override
	public List<ThxFileProperty> getFileProperty(long fileId) throws Exception {
		return thxFileMapper.selectFileProperty(fileId);
	}

	@Override
	@Transactional
	public void deleteFile(long fileId) throws Exception {
		HashMap<String, Object> paramMap = new HashMap<>();
		paramMap.put("id", fileId);

		List<ThxFile> fileList = thxFileMapper.selectFileList(paramMap);

		if (fileList != null && fileList.size() > 0) {
			ThxFile thxFile = fileList.get(0);

			thxFileMapper.deleteFileProperty(fileId);
			thxFileMapper.deleteFile(fileId);

			AmazonS3 s3Client = AmazonS3ClientBuilder.standard().withRegion(Regions.AP_NORTHEAST_2)
					.withCredentials(new ProfileCredentialsProvider()).build();
			s3Client.deleteObject(new DeleteObjectRequest("thxcloud.com", thxFile.getStoredFileName()));
		}

	}
}
