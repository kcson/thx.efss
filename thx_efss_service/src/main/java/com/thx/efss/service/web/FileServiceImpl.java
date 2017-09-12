package com.thx.efss.service.web;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.xml.sax.ContentHandler;

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
	@Autowired
	CommonsMultipartResolver commonsMultipartResolver;

	@Override
	@Transactional
	public void saveFile(MultipartFile uploadFile) throws Exception {
		String originFileName = new String(uploadFile.getOriginalFilename().getBytes("8859_1"), "UTF-8");

		// int thresHold =
		// commonsMultipartResolver.getFileItemFactory().getSizeThreshold();
		// 파일에서 사용자 속성 가져오기
		ContentHandler contenthandler = new BodyContentHandler(-1);
		Metadata mdata = new Metadata();

		AutoDetectParser parser = new AutoDetectParser();
		ParseContext context = new ParseContext();
		parser.parse(uploadFile.getInputStream(), contenthandler, mdata, context);
		String contentType = mdata.get(Metadata.CONTENT_TYPE);

		// aws s3에 파일 저장
		// BasicAWSCredentials creds = new BasicAWSCredentials(AWS_ACCESS_KEY_ID,
		// AWS_SECRET_ACCESS_KEY);
		AmazonS3 s3Client = AmazonS3ClientBuilder.standard().withRegion(Regions.AP_NORTHEAST_2)
				.withCredentials(new ProfileCredentialsProvider()).build();

		String contentKey = getUuid();
		ObjectMetadata metadata = new ObjectMetadata();
		metadata.setContentLength(uploadFile.getSize());
		metadata.setContentType(contentType);
		s3Client.putObject(new PutObjectRequest("thxcloud.com", contentKey, uploadFile.getInputStream(), metadata));

		// s3에 저장 성공하면 DB에 파일 관련 정보 저장
		ThxFile thxFile = new ThxFile();
		thxFile.setOriginalFileName(originFileName);
		thxFile.setStoredFileName(contentKey);

		thxFileMapper.insertFile(thxFile);

		String[] metadataNames = mdata.names();
		ThxFileProperty fileProperty = new ThxFileProperty();
		fileProperty.setFileId(thxFile.getId());
		for (String propertyName : metadataNames) {
			if (StringUtils.startsWith(propertyName, Metadata.USER_DEFINED_METADATA_NAME_PREFIX)) {
				fileProperty.setPropertyKey(
						StringUtils.substringAfter(propertyName, Metadata.USER_DEFINED_METADATA_NAME_PREFIX));
				fileProperty.setPropertyValue(mdata.get(propertyName));
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
		} else if (header.contains("Safari")) {
			fileName = new String(fileName.getBytes("UTF-8"), "8859_1");
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
