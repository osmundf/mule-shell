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
                <button class="btn btn-outline-success hidden" id="guestButton">Guest</button>
            </div>
        </nav>
    </nav>
</div>

<div id="center" class="d-flex mb-auto flex-column box-shadow w-100" style="height: 100%; overflow: auto;">
    <div class="mb-auto w-100"></div>
    <div class="w-100" style="overflow: none; padding: 2px;" id="console">
    </div>
</div>

<div id="south" class="w-100" style="padding: 5px; background-color: #222;">
    <form id="prompt-form" class="input-group">
        <div class="input-group-prepend align-items-center fixed-font dark-prompt terminal" id="prompt">jshell&gt;</div>
        <input type="text" class="d-flex flex-grow-1 fixed-font dark-input terminal" autocomplete="off" autofocus id="console-input">
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

<input type="hidden" class="hidden" value="${contextPath}" id="context-path">
<input type="hidden" class="hidden" value="" id="shell-id">

<script type="application/javascript">

    var guestButton = $('button#guestButton');
    var loginForm = $("form#form-log-in");
    var consoleDiv = $("div#console");
    var promptForm = $("form#prompt-form");
    var promptSymbol = $("div#prompt");
    var promptInput = $("input#console-input");
    var contextPath = $("input#context-path").val();
    var shellId = $("input#shell-id");

    $(function () {
        $('button#loadingButton').addClass("hidden");
        $('button#guestButton').removeClass("hidden");
        loginGuest();
    });

    guestButton.bind("click", function () {
        if (guestButton.hasClass("disabled")) {
            return;
        }

        guestButton.addClass("disabled");
        guestButton.removeClass("disabled");
    });

    function loginGuest() {

        $.ajax({
            type: "POST",
            url: contextPath + "/account/login?role=GUEST",
            dataType: 'json',
            async: true,
            data: null
        }).done(function (data) {
            console.log("Token stored: " + data.token);
            setLoginStore(data.token);
            startShellSession();
        }).fail(function (status, error) {
            console.log("Fail." + JSON.stringify(error));
        });
    }

    function startShellSession() {

        var token = getLoginStore();
        disablePrompt();

        $.ajax({
            type: "GET",
            url: contextPath + "/session/list",
            contentType: "application/json; charset=utf-8",
            dataType: 'json',
            async: true,
            data: {},
            beforeSend: function (xhr) {
                xhr.setRequestHeader('Authorization', "Bearer " + token);
            }
        }).done(function (data) {
            console.log("Session stored: " + data[0].id);
            document.location.hash = data[0].id;
            shellId.val(data[0].id);

            consoleDiv.append("<pre class=\"line terminal\">$ jshell</pre>");
            consoleDiv.append("<pre class=\"line terminal\">|  Welcome to JShell -- Version 11.0.2</pre>");
            consoleDiv.append("<pre class=\"line terminal\">|  For an introduction type: /help intro</pre>");
            consoleDiv.append("<pre class=\"line terminal\">&nbsp;</pre>");
            enablePrompt();

        }).fail(function (status, error) {
            console.log("Fail." + JSON.stringify(error));
        });
    }

    loginForm.bind("submit", function () {
        $('div#myModal').modal('hide');

        var username = $("input#inputUsername").val();
        var password = $("input#inputPassword").val();

        $.ajax({
            type: "POST",
            url: contextPath + "/account/login",
            dataType: 'json',
            async: true,
            data: null,
            beforeSend: function (xhr) {
                console.log("Sending.");
                xhr.setRequestHeader('Authorization', "Basic " + btoa(username + ':' + password));
            }
        }).done(function (data) {
            console.log("Done: " + JSON.stringify(data));
            // enablePrompt();
        }).fail(function (status, error) {
            console.log("Fail: " + JSON.stringify(error));
            // disablePrompt();
        });

        return false;
    });

    promptForm.bind("submit", function () {

        var token = getLoginStore();
        var shellId = $("#shell-id").val();
        var contextPath = $("#context-path").val();
        var expression = promptInput.val().trim();

        if (expression === "") {
            $('<pre class="line terminal"/>').text("jshell>").appendTo(consoleDiv);
            $('<pre class="line terminal">&nbsp;</pre>').appendTo(consoleDiv);
            return false;
        }

        if (token == null) {
            alert("Login please.");
            return false;
        }

        disablePrompt();

        $.ajax({
            type: "POST",
            url: contextPath + "/expression?sessionId=" + shellId,
            contentType: "application/json; charset=utf-8",
            dataType: 'json',
            async: true,
            data: JSON.stringify({"input": expression}),
            beforeSend: function (xhr) {
                xhr.setRequestHeader('Authorization', "Bearer " + token);
            }
        }).done(function (data) {
            console.log("Response: " + JSON.stringify(data));
            promptInput.val("");

            var lines = data != null && data.output != null ? data.output : [];
            var size = lines.length;

            $('<pre class="line terminal"/>').text("jshell> " + expression.trim()).appendTo(consoleDiv);

            for (var i = 0; i < size; i++) {
                var line = lines[i];

                if (line === "") {
                    $('<pre class="line terminal">&nbsp;</pre>').appendTo(consoleDiv);
                } else {
                    $('<pre class="line terminal"/>').text(line).appendTo(consoleDiv);
                }
            }

            if (true === data.continuation) {
                promptSymbol.text("...>");
            } else {
                promptSymbol.text("jshell>");
            }

            enablePrompt();

        }).fail(function (status, error) {
            console.log("Fail status: " + JSON.stringify(status));
            console.log("Fail error: " + JSON.stringify(error));
            enablePrompt();
        });

        return false;
    });

    function postToSession() {
        var contextPath = document.getElementById("context-path");
        console.log("Here: " + contextPath);
    }

    function disablePrompt() {
        promptInput.addClass("disabled");
        promptInput.attr('disabled', 'disabled');
        promptInput.prop('disabled', true);
    }

    function enablePrompt() {
        promptInput.prop('disabled', false);
        promptInput.attr('disabled', null);
        promptInput.removeClass("disabled");
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