<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<form class="form-inline" enctype="multipart/form-data" action="/file" name="fileUpload" method="post">
	<div class="form-group">
		<input type="file" name="file" class="form-control">
	</div>
	<input type="hidden" name="${_csrf.parameterName}"	value="${_csrf.token}"/>
	<button type="submit" class="btn btn-default">업로드</button>
</form>

<form id="downLoadForm" method="get">
</form>

<table class="table table-striped">
    <thead>
      <tr>
        <th>파일 이름</th>
        <th>저장 이름</th>
        <th>생성일</th>
        <th></th>
        <th></th>
      </tr>
    </thead>
    <tbody id="fileList">
    </tbody>
</table>

<!-- Modal -->
<div id="myModal" class="modal fade" role="dialog">
  <div class="modal-dialog">
    <!-- Modal content-->
    <div class="modal-content">
      <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal">&times;</button>
        <h4 class="modal-title">Modal Header</h4>
      </div>
      <div class="modal-body">
		<table class="table table-striped">
    		<thead>
      		<tr>
        		<th>속성 키</th>
        		<th>속성 값</th>
      		</tr>
    		</thead>
    		<tbody id="fileProperty">
    		</tbody>
		</table>
	</div>
      <div class="modal-footer">
        <button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
      </div>
    </div>

  </div>
</div>

<script type="text/javascript">

$(document).ready(function(){
	getFileList();
});

function getFileList() {
	$.ajax({
		url: '/file/list',
		type: 'get',
		dataType : 'json',
		success: function(data){
			$tbody = $('#fileList');
			$tbody.empty();
			data.forEach(function(file){
				$tr = $('<tr></tr>');
				$tr.append($('<td style="cursor:pointer;" onclick="downLoadFile(\''+file.id+'\')">'+file.originalFileName+'</td>'));
				$tr.append($('<td>'+file.storedFileName+'</td>'));
				$tr.append($('<td>'+file.entryDate+'</td>'));
				$tr.append($('<td><button type="button" onclick="showInfo(\''+file.id+'\')" class="btn btn-info btn-sm" data-toggle="modal">속성보기</button></td>'));
				$tr.append($('<td><button type="button" onclick="deleteFile(\''+file.id+'\')" class="btn btn-warning btn-sm" data-toggle="modal">파일삭제</button></td>'));
				
				$tbody.append($tr);
				//$tr = $('<tr><td style="cursor:pointer;" onclick="downLoadFile(\''+file.id+'\')">'+file.originalFileName+'</td><td>'+file.storedFileName+'</td><td>'+file.entryDate+'</td></tr>');
				//$tr.appendTo($tbody);
			});
			
		},
		error: function(request, status, error) {
			alert(status + ":" + error);
		}
	});
}

function downLoadFile(fileId) {
	$('#downLoadForm').attr("action","/file/"+fileId);
	$('#downLoadForm').submit();
}

function showInfo(fileId) {
	$.ajax({
		url: '/fileproperty/' + fileId,
		type: 'get',
		dataType : 'json',
		success: function(data){
			$tbody = $('#fileProperty');
			$tbody.empty();
			data.forEach(function(property){
				$tr = $('<tr></tr>');
				$tr.append($('<td>'+property.propertyKey+'</td>'));
				$tr.append($('<td>'+property.propertyValue+'</td>'));
				
				$tbody.append($tr);
			});
			
			$('#myModal').modal('show');
		},
		error: function(request, status, error) {
			alert(status + ":" + error);
		}
	});
}

function deleteFile(fileId) {
	var token = $("meta[name='_csrf']").attr("content");
	var header = $("meta[name='_csrf_header']").attr("content");
	
	$.ajax({
		url: '/file/' + fileId,
		type: 'delete',
		//dataType : 'json',
		beforeSend : function(xhr){
	    	xhr.setRequestHeader(header, token);
		},
		success: function(data){
			getFileList();
		},
		error: function(request, status, error) {
			alert(status + ":" + error);
		}
	});
}

//$('#myModal').on('shown.bs.modal', function () {
//	  alert('modal');
//})
</script>
