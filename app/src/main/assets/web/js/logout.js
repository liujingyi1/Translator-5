$(document).ready(function(){
  $("#logoutBtn").click(function(){
        $.ajax({
          type: "POST",
          url: "/user/logout",
          cache: false,
          dataType: "json",
          success: function(msg){
             if (msg.data == "success") {
                window.location.href="login.html";
             } else {
                alert("退出失败.");
             }
          }
        });
  });

});