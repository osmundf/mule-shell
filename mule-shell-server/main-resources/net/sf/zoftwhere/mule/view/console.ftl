<#--noinspection HtmlUnknownTarget-->
<#-- @ftlvariable name="bootstrapCSS" type="java.lang.String" -->
<#-- @ftlvariable name="bootstrapJS" type="java.lang.String" -->
<#-- @ftlvariable name="contextPath" type="java.lang.String" -->
<#-- @ftlvariable name="JQueryJS" type="java.lang.String" -->
<#-- @ftlvariable name="popperJS" type="java.lang.String" -->
<#-- @ftlvariable name="version" type="java.lang.String" -->
<!doctype html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport"
          content="width=device-width, user-scalable=no, initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0">
    <meta http-equiv="X-UA-Compatible" content="ie=edge">

    <title>Console · Mule Shell</title>

    ${bootstrapCSS?no_esc}
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
    <!-- Empty div to fill the top (when needed). -->
    <div class="mb-auto w-100"></div>
    <div class="w-100" style="overflow: none; padding: 2px;" id="console">
    </div>
</div>

<div id="south" class="w-100" style="padding: 5px; background-color: #222;">
    <form id="prompt-form" class="input-group">
        <label for="console-input"></label>
        <div class="input-group-prepend align-items-center fixed-font dark-prompt terminal" id="prompt">
            $&nbsp;
        </div>
        <input type="text" class="d-flex flex-grow-1 fixed-font dark-input terminal" autocomplete="off" autofocus
               id="console-input">
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

<#-- Preverse the order of these JavaScript libraries for Bootstrap to work. -->
${JQueryJS?no_esc}
${popperJS?no_esc}
${bootstrapJS?no_esc}

<input type="hidden" class="hidden" value="${contextPath}" id="context-path">
<input type="hidden" class="hidden" value="" id="shell-id">
<input type="hidden" class="hidden" value="${version}" id="mule-version">

<script type="application/javascript" src="${contextPath}/assets/mule.js"></script>
<script type="application/javascript">

    const guestButton = $('button#guest-button');
    const loginForm = $("form#form-log-in");
    const consoleDiv = $("div#console");
    const promptForm = $("form#prompt-form");
    const promptSymbol = $("div#prompt");
    const promptInput = $("input#console-input");
    const contextPath = $("input#context-path").val();
    const shellId = $("input#shell-id");
    const muleVersion = $("input#mule-version").val();
    const mule = new MuleShellConsole(consoleDiv);

    $(function () {
        $('button#loading-button').addClass("hidden");
        $('button#guest-button').removeClass("hidden");
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

        const token = getLoginStore();
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
            console.debug("Session stored: " + data[0].id);
            document.location.hash = data[0].id;
            shellId.val(data[0].id);

            mule.addOutput(":  Welcome to MuleShell.");
            mule.addOutput(":  Version: " + muleVersion);
            mule.addOutput(":");
            mule.addOutput(":  For an help type: /help");
            mule.addOutput("\n");
            mule.scrollIntoView();

            enablePrompt();

        }).fail(function (status, error) {
            console.error("Fail." + JSON.stringify(error));
        });
    }

    loginForm.bind("submit", function () {
        $('div#myModal').modal('hide');

        const username = $("input#inputUsername").val();
        const password = $("input#inputPassword").val();

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

        const token = getLoginStore();
        const shellId = $("#shell-id").val();
        const contextPath = $("#context-path").val();
        const lastInput = promptInput.val();
        const expression = (mule.continuation ? mule.remainingCode + "\n" : "") + lastInput;

        if (mule.continuation === false && lastInput.trim().startsWith("/help")) {
            let m = mule.command(lastInput);
            console.debug(JSON.stringify(m));
            if (m === true) {
                promptInput.val("")
            }
            return false;
        }

        if (token == null) {
            alert("Login please.");
            return false;
        }

        if (mule.continuation === false && lastInput.trim().startsWith("/")) {
            let m = mule.command(lastInput);
            console.debug(JSON.stringify(m));
            if (m === true) {
                promptInput.val("")
            }
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
            promptInput.val("");
            try {
                mule.processExpressionResult(lastInput, expression, data);
            } catch (e) {
                console.error(e);
            } finally {
                // Prompt with spaces as non-breaking spaces.
                promptSymbol.text((MuleShellConsole.getPrompt(mule.continuation) + ' ').replace(/ /g, '\u00a0'));

                enablePrompt();
            }

        }).fail(function (status, error) {
            console.log("Fail status: " + JSON.stringify(status));
            console.log("Fail error: " + JSON.stringify(error));
            enablePrompt();
        });

        return false;
    });

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

    function clearLoginStore() {
        localStorage.removeItem("mule-shell-token");
    }

    function makeBasicBase64(user, password) {
        const tok = user + ':' + password;
        return btoa(tok);
    }
</script>

</body>
</html>