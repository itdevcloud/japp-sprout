		function submitpage() {
			let action = document.getElementById("login_redirect").action;
			if(!action || action.toLowerCase().endsWith("/none") || action.toLowerCase().endsWith("/na")){
				action = null;
			}
			sessionStorage.setItem("japp-jwt",
					document.getElementById("token").value);
			let url = sessionStorage.getItem("japp-login-redirect-url");
			sessionStorage.removeItem("japp-login-redirect-url");
			if (isEmpty(url) && isEmpty(action)) {
				startSpin();
			}else{
				if(!isEmpty(url)){
					document.getElementById("login_redirect").action = url;
				}
				document.forms['login_redirect'].submit();
			}
		}
		function isEmpty(str) {
		    return (!str || str.trim().length === 0 );
		}
		submitpage();