/**
 * 美化checkboxes, radios
 */
 
$.fn.customBeautify = function(){
	if(!$.isIE9Under()){
		return $(this).each(function() {
			var input = $(this);
			if(input.parent(".w-checkbox,.w-radio").size() == 0){
				input.wrapAll('<span class="w-'+ input.attr('type') +'"></span>');
				var custom = input.parent('.w-checkbox,.w-radio');
				
				custom.hover(
					function(){ $(this).addClass('hover');},
					function(){ $(this).removeClass('hover'); }
				);
				
				input.bind('updateState', function(){	
					input.is(':checked') ? custom.addClass('checked') : custom.removeClass('checked'); 
				})
				.trigger('updateState')
				.click(function(){
					var name = $(this).attr('name');
					if(name){
						$('input[name='+ name +']').trigger('updateState'); 
					}
				});
			}
		});
	}
	return null;
};