		function submitpage() {
			sessionStorage.setItem("token",
					document.getElementById("token").value);
			let action = document.getElementById("login_redirect").action;
			window.location.href = action;
		}
		submitpage();