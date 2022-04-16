	    var myVar;
		function startSpin() {
			myVar = setTimeout(showPage, 10000);
		}
	
		function showPage() {
			document.getElementById("spinDiv").style.display = "none";
			document.getElementById("stopSpinDiv").style.display = "block";
		}
		function submitpage() {
			let action = document.getElementById("post_token").action;
			let token = document.getElementById("token").value;
			let tokenTransfer = document.getElementById("token_transfer").value;
			let callbackType = document.getElementById("callback_type").value;
			
			if(!action || action.toLowerCase().endsWith("/none") || action.toLowerCase().endsWith("/na")){
				action = null;
			}
			if(token === ("@" +"token" + "@")){
				token = "";
				document.getElementById("token").value = "";
			}
			if(tokenTransfer === ("@" + "token_transfer" + "@")){
				tokenTransfer = "SESSION_STORAGE"
				document.getElementById("token_transfer").value = "SESSION_STORAGE";
			}
			if(callbackType === ("@" + "callback_type" + "@")){
				callbackType = "POST";
				document.getElementById("callback_type").value = "POST";
			}
			
			if(tokenTransfer === "COOKIE"){
				setCookie("token", token, 120);
			}else if (tokenTransfer === "SESSION_STORAGE"){
				sessionStorage.setItem("token", token);
			}
			if (isEmpty(action)) {
				startSpin();
			}else{
				if(callbackType === "REDIRECT"){
					window.location.href = action;					
				}else {
					document.forms['post_token'].submit();
				}
			}
		}
		function isEmpty(str) {
		    return (!str || str.trim().length === 0 );
		}
		
		function setCookie(name, value, seconds) {
		    var expires = "";
		    if (seconds) {
		        var date = new Date();
		        date.setTime(date.getTime() + (seconds*1000));
		        expires = "; expires=" + date.toUTCString();
		    }
		    document.cookie = name + "=" + (value || "")  + expires + "; path=/";
		}
		submitpage() ;
