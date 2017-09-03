package com.thx.efss.common.config;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;

public class PropertyPlaceholderConfigUtil extends PropertyPlaceholderConfigurer {
	final private static String ENC_PREFIX = "[enc]";

	protected String convertPropertyValue(String originalValue) {
		if (StringUtils.startsWithIgnoreCase(originalValue, ENC_PREFIX)) {
			return decryptProperty(originalValue);
		}

		return originalValue;
	}

	private String decryptProperty(String originalValue) {
		// TODO generate decypt code
		originalValue = StringUtils.substringAfter(originalValue, ENC_PREFIX);
		return originalValue;
	}
}
