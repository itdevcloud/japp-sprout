		function submitpage() {
			//let action = document.getElementById("login_redirect").action;
			//let cookieName = document.getElementById("cookieName").value;
			//let cookieValue = document.getElementById("cookieValue").value;
			//let cookieStr = cookieName + "=" + cookieValue;
			//if(cookieName !== '' && cookieValue !== ''){
			//	setCookie(cookieName, cookieValue, 60);
			//}
			//window.location.href = action;
			document.getElementById("login_redirect").submit();
		}
		function setCookie(name,value,seconds) {
		    var expires = "";
		    if (seconds) {
		        var date = new Date();
		        date.setTime(date.getTime() + (seconds*1000));
		        expires = "; expires=" + date.toUTCString();
		    }
		    document.cookie = name + "=" + (value || "")  + expires + "; path=/";
		}
		submitpage();