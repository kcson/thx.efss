package com.thx.efss.common;

import java.util.HashMap;

import org.apache.commons.codec.binary.StringUtils;

public enum ThxContentType {
	DOC_OOXML("application/vnd.openxmlformats-officedocument.wordprocessingml.document"), DOC_OLE("application/msword"), XLX_OOXML(
			"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"), XLX_OLE("application/vnd.ms-excel"), PPT_OOXML(
					"application/vnd.openxmlformats-officedocument.presentationml.presentation"), PPT_OLE("application/vnd.ms-powerpoint"), PDF("application/pdf");

	private String contentType;
	private static HashMap<String, ThxContentType> contentMap = new HashMap<>();

	private ThxContentType(String contentType) {
		this.contentType = contentType;
	}

	public static ThxContentType getValueOf(String contentType) {
		ThxContentType thxContentType = contentMap.get(contentType);
		if (thxContentType == null) {
			for (ThxContentType type : ThxContentType.values()) {
				if (StringUtils.equals(type.getContentType(), contentType)) {
					contentMap.put(contentType, type);
					return type;
				}
			}
		}
		return thxContentType;
	}

	public String getContentType() {
		return this.contentType;
	}

}
