<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<form class="form-inline" enctype="multipart/form-data" action="/file" name="fileUpload" method="post">
	<div class="form-group">
		<input type="file" name="file" class="form-control">
	</div>
	<!-- input type="hidden" name="${_csrf.parameterName}"	value="${_csrf.token}"/-->
	<button type="submit" class="btn btn-default">업로드</button>
	
	<div class="form-group">
		<label>본문 검색</label>
		<input type="text" name="fullTextSearchParam" class="form-control">
	</div>
	<button type="button" onclick="fullTextSearch()" class="btn btn-default">검색</button>
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
		<table class="table table-condensed">
    		<thead>
      		<tr>
        		<th>속성 키</th>
        		<th>속성 값</th>
        		<th></th>
      		</tr>
    		</thead>
    		<tbody id="fileProperty">
    		</tbody>
		</table>
	</div>
      <div class="modal-footer">
        <button type="button" class="btn btn-default" data-dismiss="modal">취소</button>
        <button id="propertyConfirm" type="button" class="btn btn-default" data-dismiss="modal">확인</button>
      </div>
    </div>

  </div>
</div>

<script type="text/javascript">

$(document).ready(function(){
	getFileList();
	
	$('#propertyConfirm').on('click', function(e){
		//var token = $("meta[name='_csrf']").attr("content");
		//var header = $("meta[name='_csrf_header']").attr("content");
		
		var propertyList = new Array();
		var fileId = $('#fileProperty').data('fileId');

		$('#fileProperty tr').each(function(){
			var $tr = $(this);
			var $td = $tr.children();
			var property = {};
			property.propertyKey = $td.eq(0).children('input').val();
			property.propertyValue = $td.eq(1).children('input').val();
			propertyList.push(property);
		})
		
		$.ajax({
			url: '/fileproperty/' + fileId,
			type : 'put',
			contentType : 'application/json',
			dataType : 'json',
			data : JSON.stringify(propertyList),
			success : function(data) {
				
			},
			//beforeSend : function(xhr){
		    //	xhr.setRequestHeader(header, token);
			//},
			error : function(request, status, error) {
				alert(status + ":" + error);
			}
		})
		
	});
	
});

function fullTextSearch() {
	var searchParam = $("[name='fullTextSearchParam']").val();
	$.ajax({
		url: '/file/list?searchParam=' + searchParam,
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

function getFileList() {
	$.ajax({
		url: '/file/list?searchParam=',
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
			$tbody.data("fileId", fileId);
			
			data.forEach(function(property){
				$tr = $('<tr></tr>');
				$tr.append($('<td><input type="text" value="'+property.propertyKey+'"></td>'));
				$tr.append($('<td><input type="text" value="'+property.propertyValue+'"></td>'));
				$tr.append($('<td><span class="glyphicon glyphicon-remove"></span></td>'));
				$tbody.append($tr);
			});
			$tr = $('<tr></tr>');
			$tr.append($('<td><input type="text" value=""></td>'));
			$tr.append($('<td><input type="text" value=""></td>'));
			$tr.append($('<td><span class="glyphicon glyphicon-plus"></span></td>'));
			$tbody.append($tr);
			
			$('#fileProperty .glyphicon-remove').on('click', removeProperty);			
			$('#fileProperty .glyphicon-plus').on('click',addProperty);
			
			$('#myModal').modal('show');
		},
		error: function(request, status, error) {
			alert(status + ":" + error);
		}
	});
}

function addProperty(e) {
	$tbody = $('#fileProperty');
	$tr = $('<tr></tr>');
	$tr.append($('<td><input type="text" value=""></td>'));
	$tr.append($('<td><input type="text" value=""></td>'));
	$tr.append($('<td><span class="glyphicon glyphicon-plus"></span></td>'));
	$tbody.append($tr);
	
	$(e.target).removeClass('glyphicon-plus');
	$(e.target).addClass('glyphicon-remove');
	
	$(e.target).off();
	$(e.target).on('click', removeProperty);
	$tr.find('.glyphicon-plus').on('click',addProperty);
	
	//$('#fileProperty .glyphicon-plus').on('click',addProperty);
}

function removeProperty(e) {
	$tr = $(e.target).parent().parent();
	$tr.remove();
}

function deleteFile(fileId) {
	//var token = $("meta[name='_csrf']").attr("content");
	//var header = $("meta[name='_csrf_header']").attr("content");
	
	$.ajax({
		url: '/file/' + fileId,
		type: 'delete',
		//dataType : 'json',
		//beforeSend : function(xhr){
	    //	xhr.setRequestHeader(header, token);
		//},
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
