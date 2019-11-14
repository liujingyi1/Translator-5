$(document).ready(function(){

        $(".form-signin").submit(function(e){
            e.preventDefault();
            var data = $(".form-signin").serializeObject();
//            var data = $(".form-signin").serialize();

            alert(data.password);

            var options={
//                beforeSubmit:showRequest,
//                success:showResponse,
                data:data,
            }

            $(".form-signin").ajaxSubmit(options);
        });

    $("#login-btn").click(function(){

        var account = $("#account").val();
        var password = $("#password").val();

        var data = $(".form-signin").serializeObject();
        alert(data.account);


//        var accEncode = Base64.encode(account);
//        var pswCrypt = hex_md5(password);

//        $.ajax({
//              type: "POST",
//              url: "/user/login",
//              data: {"account":account,"password":password},
//              dataType: "json",
//              success: function(msg){
//                alert(msg.data);
//                   if (msg.data == "Login successful") {
//                      window.location.href="index.html";
//                   }
//              },
//              error: function(XMLHttpRequest, textStatus, errorThrown){
//                         alert(textStatus);
//                         alert(errorThrown);
//              }
//        });

    });

});

$.fn.serializeObject = function() {
		var o = {};
		var a = this.serializeArray();
		$.each(a, function() {
			if (o[this.name]) {
				if (!o[this.name].push) {
					o[this.name] = [ o[this.name] ];
				}
				o[this.name].push(this.value || '');
			} else {
				o[this.name] = this.value || '';
			}
		});
		return o;
	};