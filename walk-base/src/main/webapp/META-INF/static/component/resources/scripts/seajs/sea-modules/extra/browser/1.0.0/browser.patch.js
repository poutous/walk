var Browser = {
	isFF : (window.navigator.appName.indexOf("Netscape") != -1 ? true : false),
	isIE : (!!window.ActiveXObject || "ActiveXObject" in window)
};
/** firefox transition */
if (!Browser.isIE) {
	/** search event */
	function SearchEvent() {
		var func = SearchEvent.caller;
		while (func != null) {
			var arg = func.arguments[0];
			if (arg) {
				if (String(arg.constructor).indexOf('Event') > -1) {
					return arg;
				}
			}
			func = func.caller;
		}
		return null;
	}
	/** transition event */
	window.constructor.prototype.__defineGetter__("event", function() {
		return SearchEvent();
		});
	/** transition modal dialog */
	window.constructor.prototype.showModalDialog = function(url, arg, parameters) {
		parameters = "width=" + arg.width + ",innerHeight=" + (parseInt(arg.height) + 30) + ",toolbar=no,menubar=no,scrollbars=no,location=no,resizable=no,alwaysRaised=yes,depended=yes";
		var hasUrlParam = url.indexOf("?") == -1;
		for (a in arg) {
			url += (hasUrlParam ? "?" : "&") + a + "=" + encodeURIComponent(arg[a]);
			if (hasUrlParam) hasUrlParam = false;
		}
		this.open(url, null, parameters);
		};
	/** transition event properties */
	if (window.Event) {
		Event.prototype.__defineSetter__("returnValue", function(b) {
			if (!b) this.preventDefault();
			return b;
			});
		Event.prototype.__defineSetter__("cancelBubble", function(b) {
	    	if (b) this.stopPropagation();
			return b;
			});
		Event.prototype.__defineGetter__("srcElement", function() {
	        var node = this.target;
	        while (node && node.nodeType != 1) node = node.parentNode;
			return node;
			});
		Event.prototype.__defineGetter__("fromElement", function() {
	        var node;
	        if (this.type == "mouseover")
	            node = this.relatedTarget;
	        else if (this.type == "mouseout")
	            node = this.target;
	        if (!node) return;
	        while (node.nodeType != 1) node = node.parentNode;
			return node;
			});
		Event.prototype.__defineGetter__("toElement", function() {
	        var node;
	        if (this.type == "mouseout")
	            node = this.relatedTarget;
	        else if (this.type == "mouseover")
	            node = this.target;
	        if (!node) return;
	        while (node.nodeType != 1) node = node.parentNode;
			return node;
			});
		Event.prototype.__defineGetter__("offsetX", function() {
			return this.layerX;
			});
		Event.prototype.__defineGetter__("offsetY", function() {
        	return this.layerY;
			});
		Event.prototype.__defineGetter__("keyCode", function() {
			return this.charCode;
			});
	}
	/** transition document properties */
	if (window.Document) {
		Document.prototype.getAttribute=function(attrName) {
	        return this.attrName;
		}
    }
    /** transition node properties */
	if (window.Node) {
		Node.prototype.replaceNode = function(Node) {
			this.parentNode.replaceChild(Node, this);
        }
		Node.prototype.removeNode = function(removeChildren) {
        	if (removeChildren)
            	return this.parentNode.removeChild(this);
        	else {
				var range = document.createRange();
				range.selectNodeContents(this);
				return this.parentNode.replaceChild(range.extractContents(), this);
			}
        }
		Node.prototype.swapNode = function(Node) {
			var nextSibling = this.nextSibling;
			var parentNode = this.parentNode;
			node.parentNode.replaceChild(this, Node);
			parentNode.insertBefore(node, nextSibling);
        }
    }
    /** transition html element properties */
	if (window.HTMLElement) {
		HTMLElement.prototype.__defineGetter__("all", function() {
	    	var a = this.getElementsByTagName("*");
	    	var node = this;
	    	a.tags = function(sTagName) {
	        	return node.getElementsByTagName(sTagName);
	        }
			return a;
			});
		HTMLElement.prototype.__defineGetter__("parentElement", function() {
        	if (this.parentNode == this.ownerDocument) return null;
        	return this.parentNode;
        	});
		HTMLElement.prototype.__defineGetter__("children", function() {
	        var tmp = [];
	        var j = 0;
	        var n;
	        for (var i=0; i<this.childNodes.length; i++) {
	            n = this.childNodes[i];
	            if (n.nodeType == 1) {
	                tmp[j++] = n;
	                if (n.name) {
	                    if(!tmp[n.name])
							tmp[n.name] = [];
	                    	tmp[n.name][tmp[n.name].length] = n;
						}
					if (n.id)
						tmp[n.id] = n;
	                }
			}
			return tmp;
			});
		HTMLElement.prototype.__defineGetter__("currentStyle", function() {
			return this.ownerDocument.defaultView.getComputedStyle(this, null);
			});
		HTMLElement.prototype.__defineSetter__("outerHTML", function(sHTML) {
	        var r = this.ownerDocument.createRange();
	        r.setStartBefore(this);
	        var df = r.createContextualFragment(sHTML);
	        this.parentNode.replaceChild(df, this);
	        return sHTML;
	        });
		HTMLElement.prototype.__defineGetter__("outerHTML", function() {
	        var attr;
	        var attrs = this.attributes;
	        var str = "<" + this.tagName;
	        for (var i=0; i<attrs.length; i++) {
	            attr = attrs[i];
	            if (attr.specified)
	        		str += " " + attr.name + '="' + attr.value + '"';
	        }
	        if (!this.canHaveChildren)
				return str + ">";
			return str + ">" + this.innerHTML + "</" + this.tagName + ">";
	        });
		HTMLElement.prototype.__defineGetter__("canHaveChildren", function() {
	        switch (this.tagName.toLowerCase()){
	            case "area":
	            case "base":
	            case "basefont":
	            case "col":
	            case "frame":
	            case "hr":
	            case "img":
	            case "br":
	            case "input":
	            case "isindex":
	            case "link":
	            case "meta":
	            case "param":
	                return false;
	            }
	        return true;
	        });
		HTMLElement.prototype.__defineSetter__("innerText", function(sText) {
	        var parsedText=document.createTextNode(sText);
	        this.innerHTML=parsedText;
	        return parsedText;
	        });
	    HTMLElement.prototype.__defineGetter__("innerText", function() {
	        var r = this.ownerDocument.createRange();
	        r.selectNodeContents(this);
	        return r.toString();
	        });
	    HTMLElement.prototype.__defineSetter__("outerText", function(sText) {
	        var parsedText = document.createTextNode(sText);
	        this.outerHTML = parsedText;
	        return parsedText;
	        });
	    HTMLElement.prototype.__defineGetter__("outerText", function() {
	        var r = this.ownerDocument.createRange();
	        r.selectNodeContents(this);
	        return r.toString();
	        });
	    /** define properties */
	    HTMLElement.prototype.__defineGetter__("parameters", function() {
	        return this.getAttribute("parameters");
	        });
	    /** define click event */
		HTMLElement.prototype.click = function() {
			var mevt = document.createEvent("MouseEvent"); 
  			mevt.initEvent("click", false, false);
  			this.dispatchEvent(mevt);
		}
	    HTMLElement.prototype.attachEvent = function(sType, fHandler) {
	    	var shortTypeName = sType.replace(/on/, "");
	        fHandler._ieEmuEventHandler=function(e){
	            window.event = e;
	            return fHandler();
	        }
			this.addEventListener(shortTypeName,fHandler._ieEmuEventHandler,false);
		}
	    HTMLElement.prototype.detachEvent = function(sType, fHandler) {
	    	var shortTypeName = sType.replace(/on/, "");
			if (typeof(fHandler._ieEmuEventHandler) == "function")
	            this.removeEventListener(shortTypeName, fHandler._ieEmuEventHandler, false);
	        else
	            this.removeEventListener(shortTypeName, fHandler, true);
		}
	    HTMLElement.prototype.contains = function(Node) {
	        do if (Node == this) return true;
	        while (Node = Node.parentNode);
			return false;
	    }
		HTMLElement.prototype.insertAdjacentElement = function(where, parsedNode) {
	        switch (where) {
	            case "beforeBegin":
	                this.parentNode.insertBefore(parsedNode, this);
	                break;
	            case "afterBegin":
	                this.insertBefore(parsedNode, this.firstChild);
	                break;
	            case "beforeEnd":
	                this.appendChild(parsedNode);
	                break;
	            case "afterEnd":
	                if(this.nextSibling)
	                    this.parentNode.insertBefore(parsedNode, this.nextSibling);
	                else
	                    this.parentNode.appendChild(parsedNode);
	                break;
			}
		}
	    HTMLElement.prototype.insertAdjacentHTML = function(where, htmlStr) {
	        var r = this.ownerDocument.createRange();
	        r.setStartBefore(this);
	        var parsedHTML = r.createContextualFragment(htmlStr);
	        this.insertAdjacentElement(where, parsedHTML);
	        }
	    HTMLElement.prototype.insertAdjacentText = function(where, txtStr) {
	        var parsedText = document.createTextNode(txtStr);
	        this.insertAdjacentElement(where, parsedText);
	        }
		HTMLElement.prototype.attachEvent = function(sType, fHandler) {
	        var shortTypeName = sType.replace(/on/, "");
	        fHandler._ieEmuEventHandler = function(e) {
	            window.event = e;
	            return fHandler();
	        }
			this.addEventListener(shortTypeName,fHandler._ieEmuEventHandler,false);
		}
	    HTMLElement.prototype.detachEvent=function(sType, fHandler) {
	        var shortTypeName=sType.replace(/on/, "");
	        if (typeof(fHandler._ieEmuEventHandler) == "function")
	            this.removeEventListener(shortTypeName, fHandler._ieEmuEventHandler, false);
	        else
	            this.removeEventListener(shortTypeName, fHandler, true);
		}
	}
};