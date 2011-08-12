/**
 * Created by .
 * User: ryan
 * Date: 11-07-16
 * Time: 9:30 AM
 * To change this template use File | Settings | File Templates.
 */
(function( $ ){
  $.fn.takeout = function(options) {

    var match = /\/([a-zA-Z0-9_-]+)\//.exec(window.location);
    var db = match[1];
    var linkText = "Install";
    var localText = "Running Local."


    if (options && options.db) {
        db = options.db;
    }
    if (options && options.linkText) {
        linkText = options.linkText;
    }
    if (options && options.localText) {
        localText = options.localText;
    }

    var takeoutLocalDoc = "/" + db + "/_local/takeout" ;
    var div = this;
    $.ajax({
      url: takeoutLocalDoc,
      success: function(data) {
          div.each(function() {
              var $this = $(this);
              $this.text(localText);

          })
      },
      error: function() {
           div.each(function() {
              var $this = $(this);
              var jnlpLink = "/" + db + "/_design/takeout/_show/takeout.jnlp/_design/takeout-settings.jnlp" ;
              $this.append('<a class="takeoutLink" href="'+ jnlpLink +'">'+ linkText + '</a>');

          })         
      },
      dataType: 'json'
    });



    return this;  
  };
})( jQuery );