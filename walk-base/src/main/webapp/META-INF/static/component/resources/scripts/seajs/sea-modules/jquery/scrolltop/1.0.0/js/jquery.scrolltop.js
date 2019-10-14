var JqueryScrollToTop = JqueryScrollToTop||{
	setup:function(){
//		if($("body").find(".bodyWrapInner").size() == 0){
//			$("body").wrapInner("<div class='bodyWrapInner'></div>");
//		}
		var totalHeight = 0;
		$("body").children().each(function(){
			totalHeight += $(this).height();
		});
		$(window).scroll(function(){
			//if(($(window).height() + $(window).scrollTop()) >= $(".bodyWrapInner").height()) {
			if(($(window).height() + $(window).scrollTop()) >= totalHeight) {
				$("#JqueryScrollToTop").removeClass("Offscreen");
			} else {
				$("#JqueryScrollToTop").addClass("Offscreen");
				
			}
		});
		if($("#JqueryScrollToTop").size() == 0){
			$("body").append('<button id="JqueryScrollToTop" class="btnimg Button2 WhiteButton Offscreen" type="button">返回<br />顶部</button>');
		}
		$("#JqueryScrollToTop").click(function(){
			$("html, body").animate({scrollTop:"0px"}, 400);
			return false;
		})
	}
};
$(document).ready(function(){
	JqueryScrollToTop.setup();
});