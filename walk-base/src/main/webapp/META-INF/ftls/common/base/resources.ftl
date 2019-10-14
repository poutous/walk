<#if SpringPropertyHolder.getContextProperty('common_css_path')??>
<link type="text/css" rel="stylesheet" href="${request.contextPath}${SpringPropertyHolder.getContextProperty('common_css_path')}"/>
<#else>
<link type="text/css" rel="stylesheet" href="${request.contextPath}/static/component/resources/css/common.css"/>
</#if>
<#if SpringPropertyHolder.getContextProperty('comboAble') == 'true'>
<script type="text/javascript" src="${request.contextPath}/static/component/resources/scripts/seajs/sea-modules/??seajs/seajs/2.2.1/sea.js,seajs/seajs-combo/1.0.1/seajs-combo.js,config.js,jquery/jquery/1.12.4/jquery.js,jquery/jquery/1.12.4/jquery-extend.js,jquery/easyui/1.8.1/easyloader.sea.js,walk.js,init.js"></script>
<#else>
<script type="text/javascript" src="${request.contextPath}/static/component/resources/scripts/seajs/sea-modules/seajs/seajs/2.2.1/sea.js"></script>
<script type="text/javascript" src="${request.contextPath}/static/component/resources/scripts/seajs/sea-modules/config.js"></script>
<script type="text/javascript" src="${request.contextPath}/static/component/resources/scripts/seajs/sea-modules/jquery/jquery/1.12.4/jquery.js"></script>
<script type="text/javascript" src="${request.contextPath}/static/component/resources/scripts/seajs/sea-modules/jquery/jquery/1.12.4/jquery-extend.js"></script>
<script type="text/javascript" src="${request.contextPath}/static/component/resources/scripts/seajs/sea-modules/jquery/easyui/1.8.1/easyloader.sea.js"></script>
<script type="text/javascript" src="${request.contextPath}/static/component/resources/scripts/seajs/sea-modules/walk.js"></script>
<script type="text/javascript" src="${request.contextPath}/static/component/resources/scripts/seajs/sea-modules/init.js"></script>
</#if>
<#if SpringPropertyHolder.getContextProperty('load_custom.config.js') == 'true'>
<script type="text/javascript" src="${request.contextPath}/static/resources/scripts/custom.config.js"></script>
</#if>