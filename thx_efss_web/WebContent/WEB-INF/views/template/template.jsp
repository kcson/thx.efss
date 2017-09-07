<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=EUC-KR">
<meta name="_csrf" content="${_csrf.token}"/>
<meta name="_csrf_header" content="${_csrf.headerName}"/>

<title><tiles:insertAttribute name="title" ignore="true" /></title>
<tiles:insertAttribute name="import" />
</head>
<body>
	<tiles:insertAttribute name="menu" ignore="true" />
	
	<!-- Body -->
	<div class="container">
		<tiles:insertAttribute name="body" ignore="true" />
	</div>
	
	<tiles:insertAttribute name="footer" ignore="true" />	
</body>
</html>