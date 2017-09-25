package com.thx.efss.service.web;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.poi.POIDocument;
import org.apache.poi.POIXMLDocument;
import org.apache.poi.POIXMLProperties;
import org.apache.poi.hpsf.CustomProperties;
import org.apache.poi.hpsf.DocumentSummaryInformation;
import org.apache.poi.hslf.usermodel.HSLFSlideShowImpl;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.ContentHandlerDecorator;
import org.elasticsearch.action.DocWriteResponse.Result;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.openxmlformats.schemas.officeDocument.x2006.customProperties.CTProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.thx.efss.common.ThxContentType;
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
		String contentKey = getUuid();

		// int thresHold =
		// commonsMultipartResolver.getFileItemFactory().getSizeThreshold();
		// 파일에서 사용자 속성 가져오기
		// ContentHandler contenthandler = new BodyContentHandler(-1);
		ThxContentHandler contenthandler = new ThxContentHandler(contentKey);
		Metadata mdata = new Metadata();

		AutoDetectParser parser = new AutoDetectParser();
		ParseContext context = new ParseContext();
		parser.parse(TikaInputStream.get(uploadFile.getInputStream()), contenthandler, mdata, context);
		contenthandler.finalize();

		String contentType = mdata.get(Metadata.CONTENT_TYPE);

		// aws s3에 파일 저장
		// BasicAWSCredentials creds = new BasicAWSCredentials(AWS_ACCESS_KEY_ID,
		// AWS_SECRET_ACCESS_KEY);
		AmazonS3 s3Client = AmazonS3ClientBuilder.standard().withRegion(Regions.AP_NORTHEAST_2).withCredentials(new ProfileCredentialsProvider()).build();

		ObjectMetadata metadata = new ObjectMetadata();
		metadata.setContentLength(uploadFile.getSize());
		metadata.setContentType(contentType);
		s3Client.putObject(new PutObjectRequest("thxcloud.com", contentKey, uploadFile.getInputStream(), metadata));

		// elasticsearch 로 인덱싱
		// Async로(?)
		// tossToES(contentKey, contenthandler.toString());

		// s3에 저장 성공하면 DB에 파일 관련 정보 저장
		ThxFile thxFile = new ThxFile();
		thxFile.setOriginalFileName(originFileName);
		thxFile.setStoredFileName(contentKey);

		thxFileMapper.insertFile(thxFile);

		String[] metadataNames = mdata.names();
		ThxFileProperty fileProperty = new ThxFileProperty();
		fileProperty.setFileId(thxFile.getId());
		for (String propertyName : metadataNames) {
			if (StringUtils.contains(propertyName, Metadata.USER_DEFINED_METADATA_NAME_PREFIX)) {
				fileProperty.setPropertyKey(StringUtils.substringAfter(propertyName, Metadata.USER_DEFINED_METADATA_NAME_PREFIX));
				fileProperty.setPropertyValue(mdata.get(propertyName));
				thxFileMapper.insertFileProperty(fileProperty);
			}
		}
	}

	private class ThxContentHandler extends ContentHandlerDecorator {
		final private static int MAX_PROCESS_DATA_SIZE = 1024 * 1024 * 20;
		//final private static int MAX_PROCESS_DATA_SIZE = 1024 * 5;
		private StringBuilder stringBuilder = new StringBuilder();
		private String contentKey = null;
		private int index = 0;

		public ThxContentHandler(String contentKey) {
			this.contentKey = contentKey;
		}

		@Override
		public void characters(char[] ch, int start, int length) {
			try {
				stringBuilder.append(new String(ch, start, length));
				if (stringBuilder.length() >= MAX_PROCESS_DATA_SIZE) {
					tossToES(this.contentKey, stringBuilder.toString());
					index++;
					stringBuilder = new StringBuilder();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		public String toString() {
			return stringBuilder.toString();
		}

		public void finalize() throws Exception {
			if (stringBuilder.length() > 0) {
				tossToES(this.contentKey, stringBuilder.toString());
			}
		}

		private void tossToES(String contentKey, String content) throws Exception {

			RestClient restClient = RestClient.builder(new HttpHost(ES_END_POINT, 443, "https")).build();

			RestHighLevelClient client = new RestHighLevelClient(restClient);

			HashMap<String, Object> insertMap = new HashMap<>();
			insertMap.put("storedFileName", contentKey);
			insertMap.put("content" + index, content);

			HashMap<String, Object> updateMap = new HashMap<>();
			updateMap.put("content" + index, content);

			UpdateRequest updateRequest = new UpdateRequest("thxcloud", "doc", contentKey).doc(updateMap).upsert(insertMap);

			UpdateResponse updateResponse = client.update(updateRequest);

			Result result = updateResponse.getResult();
			switch (result) {
			case CREATED:
			case UPDATED:
			case DELETED:
			case NOOP:
			default:
			}

		}
	}

	static private String ES_END_POINT = "search-thxcloud-y5obigmkoyu4ig5j6inq22kqii.ap-northeast-2.es.amazonaws.com";

	// uuid 생성
	public String getUuid() {
		return UUID.randomUUID().toString().replaceAll("-", "");
	}

	@Override
	public List<ThxFile> getFileList(String fullTextSearchParam) throws Exception {
		HashMap<String, Object> paramMap = new HashMap<>();
		List<ThxFile> fileList = new ArrayList<>();
		if (!StringUtils.isBlank(fullTextSearchParam)) {
			RestClient restClient = RestClient.builder(new HttpHost(ES_END_POINT, 443, "https")).build();
			RestHighLevelClient client = new RestHighLevelClient(restClient);

			// search
			SearchRequest searchRequest = new SearchRequest();
			SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
			sourceBuilder.query(QueryBuilders.multiMatchQuery(fullTextSearchParam, "_all"));//matchQuery("content*", fullTextSearchParam));

			searchRequest.source(sourceBuilder);
			SearchResponse searchResponse = client.search(searchRequest);

			SearchHits hits = searchResponse.getHits();
			SearchHit[] searchHits = hits.getHits();
			for (SearchHit hit : searchHits) {
				Map<String, Object> source = hit.getSourceAsMap();
				String storedFileName = (String) source.get("storedFileName");
				paramMap.put("storedFileName", storedFileName);
				List<ThxFile> thxFileList = thxFileMapper.selectFileList(paramMap);
				if (thxFileList.size() > 0) {
					fileList.addAll(thxFileList);
				}
			}
		} else {
			fileList = thxFileMapper.selectFileList(paramMap);
		}
		return fileList;
	}

	@Override
	public void downloadFile(long fileId, HttpServletRequest request, HttpServletResponse response) throws Exception {
		HashMap<String, Object> paramMap = new HashMap<>();
		paramMap.put("id", fileId);

		OutputStream out = response.getOutputStream();

		List<ThxFile> fileList = thxFileMapper.selectFileList(paramMap);
		if (fileList != null && fileList.size() > 0) {
			ThxFile thxFile = fileList.get(0);

			AmazonS3 s3Client = AmazonS3ClientBuilder.standard().withRegion(Regions.AP_NORTHEAST_2).withCredentials(new ProfileCredentialsProvider()).build();

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

	private void setFileName(HttpServletRequest request, HttpServletResponse response, String fileName) throws Exception {
		String header = request.getHeader("User-Agent");
		if (header.contains("MSIE") || header.contains("Trident")) {
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

			// DB 삭제
			thxFileMapper.deleteFileProperty(fileId);
			thxFileMapper.deleteFile(fileId);

			// s3 삭제
			AmazonS3 s3Client = AmazonS3ClientBuilder.standard().withRegion(Regions.AP_NORTHEAST_2).withCredentials(new ProfileCredentialsProvider()).build();
			s3Client.deleteObject(new DeleteObjectRequest("thxcloud.com", thxFile.getStoredFileName()));

			// ES삭제
			RestClient restClient = RestClient.builder(new HttpHost(ES_END_POINT, 443, "https")).build();
			RestHighLevelClient client = new RestHighLevelClient(restClient);

			DeleteRequest deleteRequest = new DeleteRequest("thxcloud", "doc", thxFile.getStoredFileName());
			client.delete(deleteRequest);
		}

	}

	@Override
	public HashMap<String, Object> putFileProperty(long fileId, List<HashMap<String, Object>> properties) throws Exception {
		HashMap<String, Object> returnMap = new HashMap<>();
		returnMap.put("result", "success");

		HashMap<String, Object> parameterMap = new HashMap<>();
		parameterMap.put("id", fileId);

		List<ThxFile> fileList = thxFileMapper.selectFileList(parameterMap);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		if (fileList != null && fileList.size() > 0) {
			ThxFile thxFile = fileList.get(0);
			S3ObjectInputStream s3ObjectInputStream = null;

			POIXMLDocument xmlDocument = null;
			POIDocument poiDocument = null;
			PDDocument pddocument = null;
			try {
				// AWS S3 에서 문서 가져옴
				AmazonS3 s3Client = AmazonS3ClientBuilder.standard().withRegion(Regions.AP_NORTHEAST_2).withCredentials(new ProfileCredentialsProvider()).build();
				S3Object object = s3Client.getObject(new GetObjectRequest("thxcloud.com", thxFile.getStoredFileName()));
				ObjectMetadata objectMetaData = object.getObjectMetadata();
				s3ObjectInputStream = object.getObjectContent();

				// 문서 포맷별 속성 정보 업데이트
				// 동일한 인터페이스를 사용할 수 있는지 검토 필요
				String contentType = objectMetaData.getContentType();
				ThxContentType thxContentType = ThxContentType.getValueOf(contentType);
				switch (thxContentType) {
				case DOC_OOXML:
					xmlDocument = new XWPFDocument(s3ObjectInputStream);
					break;
				case XLX_OOXML:
					xmlDocument = new XSSFWorkbook(s3ObjectInputStream);
					break;
				case PPT_OOXML:
					xmlDocument = new XMLSlideShow(s3ObjectInputStream);
					break;
				case DOC_OLE:
					poiDocument = new HWPFDocument(s3ObjectInputStream);
					break;
				case XLX_OLE:
					poiDocument = new HSSFWorkbook(s3ObjectInputStream);
					break;
				case PPT_OLE:
					poiDocument = new HSLFSlideShowImpl(s3ObjectInputStream);
					break;
				case PDF:
					pddocument = PDDocument.load(s3ObjectInputStream);
					break;
				default:
				}

				if (xmlDocument != null) {
					setDocOOXMLProperty(properties, xmlDocument);
					xmlDocument.write(out);
				}

				if (poiDocument != null) {
					setDocOLEProperty(properties, poiDocument);
					poiDocument.write(out);
				}

				if (pddocument != null) {
					// DB에 있는 속성 가져옴
					// PDF는 사용자 속성만 삭제하는 인터페이스를 찾지 못해 DB에 있는 값을 이용하여
					// 해당 key값을 삭제함
					List<ThxFileProperty> storedProperties = thxFileMapper.selectFileProperty(fileId);

					setPdfProperty(properties, storedProperties, pddocument);
					pddocument.save(out);
				}

				// 속성이 변경된 문서를 다시 AWS S3에 씀
				ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
				objectMetaData.setContentLength(out.toByteArray().length);
				s3Client.putObject(new PutObjectRequest("thxcloud.com", thxFile.getStoredFileName(), in, objectMetaData));

				// DB에 있는 속성 정보 변경
				thxFileMapper.deleteFileProperty(fileId);
				ThxFileProperty fileProperty = new ThxFileProperty();
				fileProperty.setFileId(thxFile.getId());
				for (HashMap<String, Object> propertyMap : properties) {
					String propertyKey = (String) propertyMap.get("propertyKey");
					String propertyValue = (String) propertyMap.get("propertyValue");
					if (StringUtils.isBlank(propertyKey)) {
						continue;
					}
					fileProperty.setPropertyKey(propertyKey);
					fileProperty.setPropertyValue(propertyValue);
					thxFileMapper.insertFileProperty(fileProperty);
				}
			} catch (Exception e) {
				throw e;
			} finally {
				if (s3ObjectInputStream != null) {
					s3ObjectInputStream.close();
				}
				if (xmlDocument != null) {
					xmlDocument.close();
				}
				if (poiDocument != null) {
					poiDocument.close();
				}
				if (pddocument != null) {
					pddocument.close();
				}
			}
		}
		return returnMap;
	}

	private void setPdfProperty(List<HashMap<String, Object>> properties, List<ThxFileProperty> storedProperties, PDDocument pddocument) {
		PDDocumentInformation documentInformation = pddocument.getDocumentInformation();
		COSDictionary dic = documentInformation.getCOSObject();
		for (ThxFileProperty fileProperty : storedProperties) {
			dic.removeItem(COSName.getPDFName(fileProperty.getPropertyKey()));
		}

		for (HashMap<String, Object> propertyMap : properties) {
			String propertyKey = (String) propertyMap.get("propertyKey");
			String propertyValue = (String) propertyMap.get("propertyValue");
			if (StringUtils.isBlank(propertyKey)) {
				continue;
			}
			documentInformation.setCustomMetadataValue(propertyKey, propertyValue);
		}
		pddocument.setDocumentInformation(documentInformation);
	}

	private void setDocOLEProperty(List<HashMap<String, Object>> properties, POIDocument poiDocument) throws Exception {
		DocumentSummaryInformation summaryInfo = poiDocument.getDocumentSummaryInformation();
		summaryInfo.removeCustomProperties();

		CustomProperties customProperties = new CustomProperties();

		for (int i = 0; i < properties.size(); i++) {
			HashMap<String, Object> propertyMap = properties.get(i);
			String propertyKey = (String) propertyMap.get("propertyKey");
			String propertyValue = (String) propertyMap.get("propertyValue");
			if (StringUtils.isBlank(propertyKey)) {
				continue;
			}

			customProperties.put(propertyKey, propertyValue);
		}
		summaryInfo.setCustomProperties(customProperties);
	}

	private void setDocOOXMLProperty(List<HashMap<String, Object>> properties, POIXMLDocument xmlDocument) throws Exception {
		POIXMLProperties pOIXMLPropertie = xmlDocument.getProperties();
		POIXMLProperties.CustomProperties customProperties = pOIXMLPropertie.getCustomProperties();

		CTProperties ctProperties = customProperties.getUnderlyingProperties();
		int propertySize = ctProperties.sizeOfPropertyArray();

		for (int i = 0; i < propertySize; i++) {
			ctProperties.removeProperty(0);
		}

		for (int i = 0; i < properties.size(); i++) {
			HashMap<String, Object> propertyMap = properties.get(i);
			String propertyKey = (String) propertyMap.get("propertyKey");
			String propertyValue = (String) propertyMap.get("propertyValue");
			if (StringUtils.isBlank(propertyKey)) {
				continue;
			}

			customProperties.addProperty(propertyKey, propertyValue);
		}
	}
}
