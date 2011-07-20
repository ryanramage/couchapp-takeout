function(doc, req) { 
	if (!this._attachments) return''; 
	var codebase = 'http://' + req.headers.Host + '/' + req.path[0] + '/_design/'+req.path[2]+'/'; 
	var defaults = { codebase : codebase, href : '_show/takeout.jnlp'  }; 
	var result = '<?xml version=\"1.0\" encoding=\"utf-8\"?>'; 
	result += '<jnlp spec=\"1.0+\" codebase=\"'+codebase+'\" href=\"'+defaults.href+'\">'; 
	var cur = this.takeout;
    var advanced = cur.advanced;
	result += '<information><title>'+cur.appName+'</title><vendor>'+cur.vendor+'</vendor><homepage>'+cur.homepage+'</homepage><description kind=\"one-line\">'+cur.description+'</description>';
	if (this._attachments['splash.png']) { 
		result += '<icon kind=\"splash\" href=\"splash.png\"/>'; 
	} 
	if (this._attachments['icon.png']) { 
		result += '<icon href=\"icon.png\"/>';
	} 
	result += ' <offline-allowed/> ';
	result += ' <shortcut online=\"false\">';
	result += '   <desktop/>'; 
	result += '   <menu submenu=\"'+cur.appName+'\"/>';
	result += ' </shortcut>';
	result += '</information>';
	result += '  <security><all-permissions/></security>';
	result += '  <resources> <j2se version=\"1.4+\" initial-heap-size=\"32m\" max-heap-size=\"128m\" /> ';
	String.prototype.endsWith = function(suffix) { return this.indexOf(suffix, this.length - suffix.length) !== -1; };
	for (var a in this._attachments) { if (a.endsWith('.jar')) { var main = ''; if (a == advanced['main-jar']) main = 'main=\"true\"'; result += ' <jar href=\"'+a+'\" '+main+'/> '; } }
	result += '</resources>'; 
	result += '  <application-desc main-class=\"'+advanced['main-class']+'\">';
    result += '  <argument>' + cur.appName + '</argument>';
	result += '  <argument>' + req.headers.Host + '</argument>'; 
	result += '  <argument>' + req.userCtx.db + '</argument>';
	if (req.userCtx && req.userCtx.name && req.userCtx.name != null) { result += '  <argument>' + req.userCtx.name + '</argument>'; } 
	result += ' </application-desc>'; 
	result += '</jnlp>'; 
	return { 'headers' : {'Content-Type' : 'application/x-java-jnlp-file'}, 'body' :  result } 
}