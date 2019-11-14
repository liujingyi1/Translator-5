$(document).ready(function(){
    $.ajax({
      type: "GET",
      url: "/config/get/serverAddress",
      dataType: "json",
      success: function(msg){
            var data = JSON.parse(msg.data);
            $("#http_ip").val(data.httpIp);
            $("#mqtt_ip").val(data.mqttIp);
            $("#device_id").val(data.deviceId);
            $("#control_center").val(data.center);
            $("#sip_ip").val(data.sipIp);
            $("#sip_account").val(data.sipAccount);
            $("#sip_password").val(data.sipPassword);
            $("#stun_address").val(data.stunAddress);
            $("#stun_port").val(data.stunPort);
      },
      error: function(XMLHttpRequest, textStatus, errorThrown){
                 alert(textStatus);
                 alert(errorThrown);
      }
    });

    $("#tslAccessApply").click(function(){
      var data = {
                 httpIp: $("#http_ip").val(),
                 mqttIp: $("#mqtt_ip").val(),
                 deviceId: $("#device_id").val(),
                 center: $("#control_center").val(),
                 sipIp: $("#sip_ip").val(),
                 sipAccount: $("#sip_account").val(),
                 sipPassword: $("#sip_password").val(),
                 stunAddress: $("#stun_address").val(),
                 stunPort: $("#stun_port").val(),
      };

            $.ajax({
                  type: "POST",
                  url: "/config/set/serverAddress",
                  contentType: 'application/json;charset=utf-8',
                  data: JSON.stringify(data),
                  dataType: "json",
                  success: function(msg){
                        alert(msg.data);
                  },
                  error: function(XMLHttpRequest, textStatus, errorThrown){
                             alert(textStatus);
                             alert(errorThrown);
                  }
            });
    });
});