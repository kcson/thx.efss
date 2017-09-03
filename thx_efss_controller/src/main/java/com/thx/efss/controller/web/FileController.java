package com.thx.efss.controller.web;

import java.io.IOException;
import java.util.UUID;

import org.docx4j.XmlUtils;
import org.docx4j.docProps.custom.Properties;
import org.docx4j.docProps.custom.Properties.Property;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.DocPropsCustomPart;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class FileController {

	@RequestMapping(value = "/fileUpload", method = RequestMethod.POST) // method = RequestMethod.GET
	public String fileUpload(@RequestParam("file") MultipartFile uploadFile) {
		try {
			byte[] fileBytes = uploadFile.getBytes();
			String originFileName = new String(uploadFile.getOriginalFilename().getBytes("8859_1"), "UTF-8");
			System.out.println(originFileName);

			WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(uploadFile.getInputStream());

			DocPropsCustomPart customPart = wordMLPackage.getDocPropsCustomPart();
			if (customPart != null) {
				Properties properties = customPart.getContents();
				for (Property property : properties.getProperty()) {
					if (property.getLpwstr() != null) {
						System.out.println(property.getName() + " = " + property.getLpwstr());
					} else {
						System.out.println(property.getName() + ": \n "
								+ XmlUtils.marshaltoString(property, Context.jcDocPropsCustom));
					}
				}
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Docx4JException e) {
			e.printStackTrace();
		}
		return "fileList";
	}

	// uuid생성
	public static String getUuid() {
		return UUID.randomUUID().toString().replaceAll("-", "");
	}
}
