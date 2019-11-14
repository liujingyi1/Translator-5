$(document).ready(function(){

  $("#submit").click(function(){

  }

  $("#submit").click(function(){

	  var host = $("#input_host").val();
      var name = $("#input_account").val();
      var password = $("#input_password").val();

      $.get("/config/mqtthost",
      {
		host:host,
        username:name,
        password:password
      },
      function(data,status){
        alert("Data: " + data + "\nStatus: " + status);
      });
  });

  $("#sip_submit").click(function(){

	  var domain = $("#input_sip_domain").val();
      var usr = $("#input_sip_usr").val();
      var password = $("#input_sip_password").val();
      var outbund= $("#input_sip_outbund").val();

      $.get("/config/sip",
      {
		domain:domain,
        usr:usr,
        password:password,
        outbund:outbund
      },
      function(data,status){
        alert("Data: " + data + "\nStatus: " + status);
      });
  });

});