<#-- @ftlvariable name="contextPath" type="java.lang.String" -->
<#--noinspection HtmlUnknownTarget-->
<!doctype html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport"
          content="width=device-width, user-scalable=no, initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0">
    <meta http-equiv="X-UA-Compatible" content="ie=edge">

    <title>Console · Mule Shell</title>

    <link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.3.1/css/bootstrap.min.css"
            <#--noinspection SpellCheckingInspection-->
          integrity="sha384-ggOyR0iXCbMQv3Xipma34MD+dH/1fQ784/j6cY/iJTQUOhcWr7x9JvoRxT2MZw1T" crossorigin="anonymous">

    <link rel="stylesheet" href="${contextPath}/assets/mule-theme.css">
    <link rel="stylesheet" href="${contextPath}/assets/mule.css">
</head>
<body class="d-flex align-items-start flex-column" style="height: 100vh; margin-bottom: 0; border: 0;">

<div id="north" class="w-100">
    <nav class="navbar navbar-expand-md skinny" style="border-bottom: 1px solid black">
        <a class="navbar-brand" style="padding-left: 10px;" href="${contextPath}"><b>Mule Shell</b></a>

        <div class="p-2 flex-fill"></div>

        <nav class="nav nav-masthead justify-content-center">
            <a class="nav-link" href="${contextPath}">Home</a>
            <a class="nav-link" href="${contextPath}/intro">Intro</a>
            <a class="nav-link active" href="${contextPath}/console">Console</a>
            <a class="nav-link" href="${contextPath}">&nbsp;</a>
            <div class="skinny">
                <button class="btn btn-outline-success disabled" id="loadingButton">Login</button>
                <button class="btn btn-outline-success hidden" data-toggle="modal" data-target="#myModal"
                        id="loginButton">Login
                </button>
                <button class="btn btn-outline-success hidden" data-toggle="modal" data-target="#myModal"
                        id="guestButton">Guest
                </button>
            </div>
        </nav>
    </nav>
</div>

<div id="center" class="d-flex mb-auto flex-column box-shadow w-100" style="height: 100%; overflow: auto;">
    <div class="mb-auto w-100"></div>
    <div class="w-100" style="overflow: none; padding: 2px;">
        <pre class="line">$ jshell</pre>
        <pre class="line">|  Welcome to JShell -- Version 11.0.2</pre>
        <pre class="line">|  For an introduction type: /help intro</pre>
        <pre class="line">&nbsp;</pre>
    </div>
</div>

<div id="south" class="w-100" style="padding: 5px; background-color: #222;">
    <form class="input-group">
        <div class="input-group-prepend">
            <span class="input-group-text prompt-bevel fixed-font">shell></span>
        </div>
        <input type="text" class="d-flex flex-grow-1 fixed-font dark-input" id="prompt" autocomplete="off">
    </form>
</div>

<div class="container">
    <!-- The Modal -->
    <div class="modal" id="myModal">
        <div class="modal-dialog">
            <div class="modal-content">

                <!-- Modal Header -->
                <div class="modal-header">
                    <h4 class="modal-title text-center">Login to Mule Shell</h4>
                </div>

                <!-- Modal body -->
                <div class="modal-body">
                    <form class="form-signin" id="form-log-in">
                        <label for="inputUsername" class="sr-only">Email address</label>
                        <input type="text" class="form-control" placeholder="Username" id="inputUsername"
                               autofocus="">
                        <label for="inputPassword" class="sr-only">Password</label>
                        <input type="password" class="form-control" placeholder="Password" id="inputPassword">
                        <button class="btn btn-lg btn-primary btn-block" type="submit">Sign in</button>
                    </form>
                </div>

                <!-- Modal footer -->
                <div class="modal-footer">
                    <button type="button" class="btn btn-danger" data-dismiss="modal">Close</button>
                </div>
            </div>
        </div>
    </div>
</div>

<script src="https://code.jquery.com/jquery-3.4.1.min.js"></script>
<#--<script src="https://code.jquery.com/jquery-3.3.1.slim.min.js"
        &lt;#&ndash;noinspection SpellCheckingInspection&ndash;&gt;
        integrity="sha384-q8i/X+965DzO0rT7abK41JStQIAqVgRVzpbzo5smXKp4YfRvH+8abtTE1Pi6jizo"
        crossorigin="anonymous"></script>-->
<script src="https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.14.7/umd/popper.min.js"
        <#--noinspection SpellCheckingInspection-->
        integrity="sha384-UO2eT0CpHqdSJQ6hJty5KVphtPhzWj9WO1clHTMGa3JDZwrnQq4sF86dIHNDz0W1"
        crossorigin="anonymous"></script>
<script src="https://stackpath.bootstrapcdn.com/bootstrap/4.3.1/js/bootstrap.min.js"
        <#--noinspection SpellCheckingInspection-->
        integrity="sha384-JjSmVgyd0p3pXB1rRibZUAYoIIy6OrQ6VrjIEaFf/nJGzIxFDsf4x0xIM+B07jRM"
        crossorigin="anonymous"></script>

<input type="hidden" id="context-path" value="${contextPath}">

<script type="application/javascript">

    $(function () {
        console.log("ready!");
        $('button#loadingButton').addClass("hidden");
        $('button#guestButton').removeClass("hidden");
        var active = loginGuest();
        console.log("Guest? " + active);
    });

    var loginForm = $("#form-log-in");

    loginForm.bind("submit", function () {
        $('div#myModal').modal('hide');

        var username = $("input#inputUsername").val();

        var password = $("input#inputPassword").val();

        $.ajax({
            type: "POST",
            url: "/mule-shell/account/login",
            dataType: 'json',
            async: true,
            data: null,
            beforeSend: function (xhr) {
                console.log("Sending.");
                xhr.setRequestHeader('Authorization', "Basic " + btoa(username + ':' + password));
            }
        }).done(function (data) {
            console.log("Done." + JSON.stringify(data));
        }).fail(function (status, error) {
            console.log("Fail." + JSON.stringify(error));
        });

        return false;
    });

    var promptObject = $("#prompt-form");

    promptObject.bind("submit", function () {
        var jwt = getLoginStore();

        if (jwt == null) {
            alert("Login please.");
            return false;
        }

        var contextPath = $("context-path").val();
        var url = contextPath + "/login";
        $.post(url,
            {
                name: "Donald Duck",
                city: "Duckburg"
            },
            function (data, status) {
                alert("Data: " + data + "\nStatus: " + status);
            });

        return false;
    });

    function loginGuest() {
        $.ajax({
            type: "POST",
            url: "/mule-shell/account/login?role=GUEST",
            dataType: 'json',
            async: true,
            data: null,
            beforeSend: function (xhr) {
                console.log("Sending.");
                // xhr.setRequestHeader('Authorization', "Basic " + btoa(username + ':' + password));
            }
        }).done(function (data) {
            console.log("Done." + JSON.stringify(data));
        }).fail(function (status, error) {
            console.log("Fail." + JSON.stringify(error));
        });
    }

    function postToSession() {
        var contextPath = document.getElementById("context-path");
        console.log("Here: " + contextPath);
    }

    function disablePrompt() {
        promptObject.addClass("disabled");
    }

    function enablePrompt() {
        promptObject.removeClass("disabled");
    }

    function getLoginStore() {
        return localStorage.getItem("mule-shell-token");
    }

    function setLoginStore(jwt) {
        localStorage.setItem("mule-shell-token", jwt);
    }

    function makeBasicBase64(user, password) {
        var tok = user + ':' + password;
        return btoa(tok);
    }
</script>

</body>
</html>