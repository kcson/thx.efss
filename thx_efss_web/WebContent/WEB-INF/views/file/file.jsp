<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<form class="form-inline" enctype="multipart/form-data" action="/fileUpload" name="fileUpload" method="post">
	<div class="form-group">
		<input type="file" name="file" class="form-control">
	</div>
	<input type="hidden" name="${_csrf.parameterName}"	value="${_csrf.token}"/>
	<button type="submit" class="btn btn-default">업로드</button>
</form>
<table class="table table-striped">
    <thead>
      <tr>
        <th>Firstname</th>
        <th>Lastname</th>
        <th>Email</th>
      </tr>
    </thead>
    <tbody>
      <tr>
        <td>John</td>
        <td>Doe</td>
        <td>john@example.com</td>
      </tr>
      <tr>
        <td>Mary</td>
        <td>Moe</td>
        <td>mary@example.com</td>
      </tr>
      <tr>
        <td>July</td>
        <td>Dooley</td>
        <td>july@example.com</td>
      </tr>
    </tbody>
</table>