<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<form class="form-inline" enctype="multipart/form-data" action="/file" name="fileUpload" method="post">
	<div class="form-group">
		<input type="file" name="file" class="form-control">
	</div>
	<input type="hidden" name="${_csrf.parameterName}"	value="${_csrf.token}"/>
	<button type="submit" class="btn btn-default">업로드</button>
</form>
<table class="table table-striped">
    <thead>
      <tr>
        <th>파일 이름</th>
        <th>저장 이름</th>
        <th>생성일</th>
      </tr>
    </thead>
    <tbody>
    </tbody>
</table>

<script type="text/javascript">

$(document).ready(function(){
	$.ajax({
		url: '/file/list',
		type: 'get',
		dataType : 'json',
		success: function(data){
			$tbody = $('tbody');
			data.forEach(function(file){
				$tr = $('<tr><td style="cursor:pointer;" onclick="downLoadFile(\''+file.id+'\')">'+file.originalFileName+'</td><td>'+file.storedFileName+'</td><td>'+file.entryDate+'</td></tr>');
				$tr.appendTo($tbody);
				
			});
			
		},
		error: function(request, status, error) {
			alert(status + ":" + error);
		}
	});
});

function downLoadFile(fileId) {
	$.ajax({
		url: '/file/'+fileId,
		type: 'get',
		success: function(data){
		},
		error: function(request, status, error) {
			alert(status + ":" + error);
		}
	});
}

</script>
