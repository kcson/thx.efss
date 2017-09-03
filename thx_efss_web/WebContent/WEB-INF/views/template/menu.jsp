<%@ taglib prefix="security" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<style type="text/css">
.menu-sideslide-submenu .menu-sideslide-hidden li{
	height: 0px;
}
li ul li {
	list-style: none;
	position: relative;
}

</style>
<script type="text/javascript">
$(document).ready(function(){
	$("ul.menu li ul").hide();
	/* $(this).children().next().css('background-color','#428bca'); */
	$("ul.menu li").hover(function () 
	{ 
		$(this).children().next().css('color','white');
		$(this).children().next().css('background-color','#428bca');
        $(this).children().next().slideDown('fast');
    }, function () 
    {
        $(this).children().next().slideUp('slow');
    });
});
function showSubMenu(menuObj){
	var $subMenu = $(menuObj).next();

	if($subMenu.hasClass("menu-sideslide-hiddenmenu")){
		$subMenu.css("height",
			$subMenu.children().first().height() * $subMenu.children().length
		);
		$subMenu.removeClass("menu-sideslide-hiddenmenu");			
	}else{
		$subMenu.removeAttr("style");
		$subMenu.addClass("menu-sideslide-hiddenmenu");
	}
}
</script>

<div class="menu-sideslide">
	<!-- Logo Info Setting: Start -->
	<div class="menu-sideslide-logo">
		<img id="logo-header" src="<%=request.getContextPath()%>/resources/img/fs_prd_usagetracer.png" alt="Logo" style="width:220px;">
	</div>
	<!-- Logo Info Setting: Start -->	
	<!-- Login Info Setting: Start -->
	<div class="menu-sideslide-auth">		
		<div style="float:right;color:#ffffff;margin-left:10px;">
			${user.userId}
			<a class="btn btn-default btn-xs" href="j_spring_security_logout" style="color:#47a3da"><i class="fa fa-sign-out"></i> LOGOUT</a>
		</div>
		<div style="clear:both"></div>
	</div>
	<!-- Login Info Setting: End -->
	
	<!-- ----------------------------------------------------- -->
	<!-- Menu Setting: Start                                   -->
	<div class="menu-sideslide-submenu">	
		<ul class="menu">			
			<li onclick="location.href='dashboard'"><i class="fa fa-tachometer"></i> <spring:message code="fut.menu.label.dashboard"/></li>
			<li onclick="location.href='monitoring'"><i class="fa fa-desktop"></i> <spring:message code="fut.menu.label.monitoring"/></li>
			<li onclick="location.href='report'"><i class="fa fa-pencil-square-o"></i> <spring:message code="fut.menu.label.report"/></li>
			<li>
				<div onClick="javascript:showSubMenu(this);"><i class="fa fa-bar-chart-o"></i> <spring:message code="fut.menu.label.logs"/></div>
				<ul class="menu-sideslide-hiddenmenu">
					<li onclick="location.href='logs/documents'"><i class="fa fa-angle-right"></i> <spring:message code="fut.submenu.label.logs.usagelogs"/></li>
					<li onclick="location.href='statistics/documents'"><i class="fa fa-angle-right"></i> <spring:message code="fut.submenu.label.logs.statistics"/></li>
					<li onclick="location.href='logs/policy'"><i class="fa fa-angle-right"></i> <spring:message code="fut.submenu.label.logs.policylogs"/></li>
					<li onclick="location.href='logs/license'"><i class="fa fa-angle-right"></i> <spring:message code="fut.submenu.label.logs.licenselogs"/></li>
				</ul>
			</li>
			<li><i class="fa fa-gears"></i> <spring:message code="fut.menu.label.settings"/>
				<ul class="menu-sideslide-hiddenmenu">
					<li onclick="location.href='settings/admins'"><i class="fa fa-angle-right"></i> <spring:message code="fut.submenu.label.settings.admin"/></li>
					<li onclick="location.href='settings/account'"><i class="fa fa-angle-right"></i> <spring:message code="fut.submenu.label.settings.account"/></li>
					<li onclick="location.href='settings/system'"><i class="fa fa-angle-right"></i> <spring:message code="fut.submenu.label.settings.system"/></li>
					<li onclick="location.href='settings/product'"><i class="fa fa-angle-right"></i> <spring:message code="fut.submenu.label.settings.product"/></li>
					<li onclick="location.href='settings/rights'"><i class="fa fa-angle-right"></i> <spring:message code="fut.submenu.label.settings.rights"/></li>
				</ul>
			</li>
			
			<security:authorize access="hasRole('ROLE_ADMIN')">
				<li><i class="fa fa-key"></i> ADMIN-MENU</li>	
			</security:authorize>
			<security:authorize access="hasRole('ROLE_DEVELOPER')">
				<li onclick="location.href='console'"><i class="fa fa-gears"></i> DEVELOPER-MENU</li>				
			</security:authorize>
			
			<li onclick="location.href='group'"><i class="fa fa-sitemap"></i> GROUP</li>
		</ul>
	</div>
	<!-- Menu Setting: End                                     -->
	<!-- ----------------------------------------------------- -->
	
	<div class="menu-sideslide-copyright">
		<div style="color:#fff">
			© 2014 Fasoo.com, Inc. <br>
			All rights reserved.
		</div>
		<div class="fut-corner-sm" style="text-align:right;padding-top:4px;background-color:#428bca;width:201px;height:30px;text-align:center;margin:10px auto;">
			<div style="float:left;margin:0px 10px;color:#ffffff;border-right:1px solid #fff;padding-right:10px;">
				Language
			</div>
			<div style="float:left;">
				<a href="?lang=ko"><img src="<%=request.getContextPath()%>/resources/locale-${pageContext.response.locale}/img/icon-lang-ko.png" style="width:30px;"></a>
				<a href="?lang=en"><img src="<%=request.getContextPath()%>/resources/locale-${pageContext.response.locale}/img/icon-lang-en.png" style="width:30px;"></a>
				<a href="?lang=ja"><img src="<%=request.getContextPath()%>/resources/locale-${pageContext.response.locale}/img/icon-lang-ja.png" style="width:30px;"></a>
			</div>
			<div style="clear:both"></div>
		</div>
	</div>
</div>

<div class="menu-sidebuttons menu-sidebuttons-open">
	<div>
		<ul>
			<li id="side_btn_menu"><span class="fa fa-list" style="vertical-align: middle;"></span></li>
			<li onclick="location.href='dashboard'"><i class="fa fa-dashboard"></i></li>
			<li><i class="fa fa-bell"></i></li>
			
			<security:authorize access="hasRole('ROLE_DEVELOPER')">
				<li onclick="location.href='console'"><i class="fa fa-gears"></i></li>
			</security:authorize>
		</ul>
	</div>
	<div class="menu-slidebuttons-overarea"></div>
	<div></div>
</div>

<div class="menu-hider"></div>

<script>
	$(document).ready(function(){
		$("#side_btn_menu").click(function(){
			var $menu = $(".menu-sideslide");
			
			if($menu.hasClass("menu-sideslide-open")){				
				hideSlideMenu();
			}else{
				showSlideMenu();
			}
		});
		
		$(".menu-hider").click(function(){			
			if($(this).hasClass("menu-hider-open")){
				hideSlideMenu();
			}
		});
		
		setTimeout(function() {
			$(".menu-sidebuttons-open").removeClass("menu-sidebuttons-open");
		}, 500);
	});
	
	function showSlideMenu(){
		$(".menu-sideslide").addClass("menu-sideslide-open");
		$(".menu-hider").addClass("menu-hider-open");
	}
	
	function hideSlideMenu(){
		$(".menu-sideslide").removeClass("menu-sideslide-open");
		$(".menu-hider").removeClass("menu-hider-open");
	}
</script>