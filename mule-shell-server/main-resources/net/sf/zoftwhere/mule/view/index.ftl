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

    <title>Mule Shell · A cloud based JShell interface.</title>

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
            <a class="nav-link active" href="${contextPath}/">Home</a>
            <a class="nav-link" href="${contextPath}/intro">Intro</a>
            <a class="nav-link" href="${contextPath}/console">Console</a>
            <a class="nav-link" href="${contextPath}">&nbsp;</a>
            <div class="skinny">
                <button class="btn btn-outline-success" id="loadingButton">Login</button>
            </div>
        </nav>
    </nav>
</div>

<div id="center" class="d-flex mb-auto flex-column box-shadow w-100 scroll-view-container">
    <div class="mb-auto w-100"></div>

    <main role="main" class="text-center" style="min-height: 100px; height: 25vh">
        <h1>Run with Mule Shell</h1>
        <p>Execute Java code online with a JShell powered tool.</p>
        <p class="lead">
            <a href="intro" class="btn btn-lg btn-secondary">Learn more</a>
        </p>
    </main>

    <div class="mb-auto w-100"></div>
</div>

<div id="south" class="w-100" style="padding: 5px; background-color: #222;">
    <footer class="footer">
        <div class="container w-100 text-center">
            <span class="text-muted">{ Mule Shell v${version} – 2019 }</span>
        </div>
    </footer>
</div>

<#-- Preverse the order of these JavaScript libraries for Bootstrap to work. -->
${JQueryJS?no_esc}
${popperJS?no_esc}
${bootstrapJS?no_esc}

</body>
</html>