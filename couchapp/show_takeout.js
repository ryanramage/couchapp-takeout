function(doc, req) { 
	if (!this._attachments) return''; 
	if (!doc) return '';
	var ddoc = req.path[2];
	var codebase = 'http://' + req.headers.Host + '/' + req.path[0] + '/_design/'+ddoc+'/'; 
	var defaults = { codebase : codebase, href : '_show/takeout.jnlp/_design/takeout-settings.jnlp'  }; 
	var result = '<?xml version=\"1.0\" encoding=\"utf-8\"?>'; 
	result += '<jnlp spec=\"1.5+\" codebase=\"'+codebase+'\" href=\"'+defaults.href+'\">'; 
	var cur = doc;
    var advanced = cur.advanced;
	result += '<information><title>'+cur.appName+'</title><vendor>'+cur.vendor+'</vendor><homepage>'+cur.homepage+'</homepage><description kind=\"one-line\">'+cur.description+'</description>';
	var icon = 'icon.png';
	var splash = 'splash.png';
	if (doc._attachments) {
		if (doc._attachments['splash.png']) { 
			splash = '../takeout-settings.jnlp/splash.png';
		} 
		if (doc._attachments['icon.png']) { 
			icon = '../takeout-settings.jnlp/icon.png'
		} 
	}
	result += '<icon kind=\"splash\" href=\"'+ splash +'\"/>'; 
	result += '<icon href=\"' + icon + '\"/>'; 
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
    if (advanced.embedded) {
       var extended = 'http://' + req.headers.Host + '/' + req.path[0] + '/_design/takeout-extended/_show/JxBrowser.jnlp';
       result += '   <extension name=\"jxbrowser\" href=\"' + extended + '\"/>';
    }
    if (ddoc != 'takeout') {
    	result += '   <property name=\"jnlp.ddoc\" value=\"'+ ddoc +'\"/>';
    }
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