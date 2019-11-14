$(document).ready(function(){

    var tableData;

    loadData();
    function loadData() {
        $.ajax({
          type: "GET",
          url: "/config/roomPhone/get",
          dataType: "json",
          success: function(msg){
                tableData = JSON.parse(msg.data);
                var data = tableData;
                if (data.length > 0) {
                    $("#room_number_table").show();
                    updateTable(data);
                } else {
                    $("#room_number_table").hide();
                }
          },
          error: function(XMLHttpRequest, textStatus, errorThrown){
                     alert(textStatus);
                     alert(errorThrown);
          }
        });
    }

    function updateTable(data) {
        $("#room_number_table tr:gt(0)").remove();

        for (var i = 0; i < data.length; i++) {
            var numbers = "";
            for (var j = 0; j < data[i].numbers.length; j++) {
                if (data[i].numbers[j] != "") {
                    if (j == 0) {
                        numbers = data[i].numbers[j];
                    } else {
                        numbers = numbers + ";" + data[i].numbers[j];
                    }
                }
            }
        	var row = "<tr><td>"+(i+1)+"</td><td>"+data[i].room+"</td><td>"+numbers+"</td></tr>";
        	$("#room_number_table tr:last").after(row);
		}
    }

    $("#phoneApply").click(function(){
            var checked = $("#input_delete").prop("checked");

            var room = $("#inputRoom").val();
            var number = $("#inputPhoneNumber").val();

            if (checked) {
                $.ajax({
                      type: "POST",
                      url: "/config/roomPhone/delete",
                      data: {"room":room, "number":number},
                      dataType: "json",
                      success: function(msg){
                            alert(msg.data);
                            loadData();
                      },
                      error: function(XMLHttpRequest, textStatus, errorThrown){
                                 alert(textStatus);
                                 alert(errorThrown);
                      }
                });
            } else {
                var order = 0;
                for (var i = 0; i < tableData.length; i++) {
                    if (tableData[i].room == room) {
                        order = tableData[i].numbers.length + 1;
                    }
                }
                $.ajax({
                      type: "POST",
                      url: "/config/roomPhone/add",
                      data: {"room":room, "number":number},
                      dataType: "json",
                      success: function(msg){
                            alert(msg.data);
                            loadData();
                      },
                      error: function(XMLHttpRequest, textStatus, errorThrown){
                                 alert(textStatus);
                                 alert(errorThrown);
                      }
                });
            }
    });
});