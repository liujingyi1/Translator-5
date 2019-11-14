$(document).ready(function(){

        $.ajax({
          type: "GET",
          url: "/config/get/sysinfo/mqttState",
          dataType: "json",
          success: function(msg){
                $("#mqtt_state").text(msg.data ? "已连接" : "未连接");
          },
          error: function(XMLHttpRequest, textStatus, errorThrown){
                     alert(textStatus);
                     alert(errorThrown);
          }
        });

        $.ajax({
          type: "GET",
          url: "/config/get/sysinfo/sipState",
          dataType: "json",
          success: function(msg){
                $("#sip_state").text(msg.data ? "已连接" : "未连接");
          },
          error: function(XMLHttpRequest, textStatus, errorThrown){
                     alert(textStatus);
                     alert(errorThrown);
          }
        });

        $.ajax({
          type: "GET",
          url: "/config/get/sysinfo/version",
          dataType: "json",
          success: function(msg){
                var data = JSON.parse(msg.data);
                $("#sys_version").text(data.swVersion);
                $("#readhead_version").text(data.readHeadVersion);
                $("#cloud_version").text(data.cloudCallVersion);
          },
          error: function(XMLHttpRequest, textStatus, errorThrown){
                     alert(textStatus);
                     alert(errorThrown);
          }
        });

        $.ajax({
          type: "GET",
          url: "/config/get/sysinfo/deviceMac",
          dataType: "json",
          success: function(msg){
                $("#device_mac").text(msg.data);
          },
          error: function(XMLHttpRequest, textStatus, errorThrown){
                     alert(textStatus);
                     alert(errorThrown);
          }
        });


});