$(document).ready(function(){

    function changeState(checked) {
        $("#IPAddress").prop("readonly",checked);
        $("#mask").prop("readonly",checked);
        $("#gateway").prop("readonly",checked);
        $("#dns").prop("readonly",checked);
    }

    $("#input_dynamicIP").change(function() {
        var checked = $("#input_dynamicIP").prop("checked");
        changeState(checked);
    });

    $.ajax({
      type: "GET",
      url: "/config/get/etherent",
      dataType: "json",
      success: function(msg){
            var data = JSON.parse(msg.data);
            var checked = data.isDHCP == 1;
            $("#input_dynamicIP").prop("checked", checked);
            $("#IPAddress").val(data.ip);
            $("#mask").val(data.mask);
            $("#gateway").val(data.gateway);
            $("#dns").val(data.DNS);
            changeState(checked);
      },
      error: function(XMLHttpRequest, textStatus, errorThrown){
                 alert(textStatus);
                 alert(errorThrown);
      }
    });

    $("#netApply").click(function(){
      var etherent = {
                 isDHCP: $("#input_dynamicIP").prop("checked") ? 1 : 0,
                 ip: $("#IPAddress").val(),
                 mask: $("#mask").val(),
                 gateway: $("#gateway").val(),
                 DNS: $("#dns").val(),
      };

            $.ajax({
                  type: "POST",
                  url: "/config/set/etherent",
                  contentType: 'application/json;charset=utf-8',
                  data: JSON.stringify(etherent),
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