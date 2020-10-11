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

    <title>Intro · Mule Shell</title>

    ${bootstrapCSS?no_esc}
    <link rel="stylesheet" href="${contextPath}/assets/mule-theme.css">
    <link rel="stylesheet" href="${contextPath}/assets/mule.css">
</head>
<body class="d-flex align-items-start flex-column" style="height: 100vh; margin-bottom: 0; border: 0">

<div id="north" class="w-100">
    <nav class="navbar navbar-expand-md skinny" style="border-bottom: 1px solid black">
        <a class="navbar-brand" style="padding-left: 10px;" href="${contextPath}"><b>Mule Shell</b></a>

        <div class="p-2 flex-fill"></div>

        <nav class="nav nav-masthead justify-content-center">
            <a class="nav-link" href="${contextPath}/">Home</a>
            <a class="nav-link active" href="${contextPath}/intro">Intro</a>
            <a class="nav-link" href="${contextPath}/console">Console</a>
            <a class="nav-link" href="${contextPath}">&nbsp;</a>
            <div class="skinny">
                <button class="btn btn-outline-success" id="loadingButton">Login</button>
            </div>
        </nav>
    </nav>
</div>

<div id="center" class="d-flex mb-auto flex-column w-100 scroll-view-container"
     style="box-shadow: inset 0 0 5rem rgba(0, 0, 0, 1);">
    <div class="smallw-100 scroll-view-item">
        <h2 class="text-orange">Hello World!</h2>

        <p>Mule Shell was created as a way to run Java code online. The Java&reg; Development Kit revision 9 brought the
            JShell API, along with the JShell Tool. Mule Shell uses the JDK JShell API to be able to run code on a
            server, allowing the user to interact with the JShell API much in the same was as the JShell Tool does.</p>
        <p>Note, however, that Mule Shell is not a JShell Tool implementation, nor is its aim such. A noticeable
            difference is that Mule Shell allows running code as it were typed in a text editor; a feat it manages
            because it is stricter on semi-colons.</p>
    </div>

    <div class="smallw-100 scroll-view-item">
        <h2 class="text-lime">Features.</h2>

        <div class="card w-100">
            <div class="card-body">
                <h5 class="card-title text-teal">Stricter semi-colons;</h5>
                <h6 class="card-subtitle mb-2 text-muted">Like Java code has.</h6>
                <p class="card-text fixed-font">$ String message = "Hello World!";<br>
                    | created: String message = "Hello World!"</p>
            </div>
        </div>
        <br>

        <div class="card w-100">
            <div class="card-body">
                <h5 class="card-title text-teal">Multi-lined;</h5>
                <h6 class="card-subtitle mb-2 text-muted">Stricter semi-colons allows better concatenation.</h6>
                <p class="card-text fixed-font">$ String message = "Hello"<br>
                    &gt; + " "<br>
                    &gt; + "World!";<br>
                    | created: String message = "Hello World!"</p>
            </div>
        </div>
        <br>

        <div class="card w-100">
            <div class="card-body">
                <h5 class="card-title text-teal">Variable Inspection</h5>
                <h6 class="card-subtitle mb-2 text-muted">Variables can be check without semi-colons</h6>
                <p class="card-text fixed-font">$ int x = 10;<br>
                    | created: int x = 10<br>
                    <br>
                    $ x<br>
                    | int x = 10</p>
            </div>
        </div>
        <br>

        <div class="card w-100">
            <div class="card-body">
                <h5 class="card-title text-teal">Java Language Features</h5>
                <h6 class="card-subtitle mb-2 text-muted">Language features are included.</h6>
                <p class="card-text fixed-font">$ var variable = "Text";</p>
            </div>
        </div>
        <br>

        <div class="card w-100">
            <div class="card-body">
                <h5 class="card-title text-teal">Java JShell REPL Features</h5>
                <h6 class="card-subtitle mb-2 text-muted">Some JShell REPL features are included.</h6>
                <h6 class="card-subtitle mb-2 text-muted">Imports and base var-typing.</h6>
                <p class="card-text fixed-font">$ import java.util.function.*;<br>
                    <br>
                    $ var supplier = (Supplier&lt;String&gt;) () -> "Text";<br>
                    created: Supplier&lt;String&gt; supplier = $Lambda$...<br>
                    <br>
                    $ supplier<br>
                    | Supplier&lt;String&gt; supplier = $Lambda$...</p>
            </div>
        </div>
        <br>

        <div class="card w-100">
            <div class="card-body">
                <h5 class="card-title text-teal">Printer Stream Output</h5>
                <h6 class="card-subtitle mb-2 text-muted">How else do you say hello world?</h6>
                <p class="card-text fixed-font">$ System.out.println("Hello World!");<br>
                    Hello World!
                </p>
            </div>
        </div>
        <br>

    </div>

    <div class="mb-auto w-100"></div>
</div>

<div id="south" class="w-100" style="padding: 5px; background-color: #222;">
    <footer class="footer">
        <div class="container w-100 text-center">
            <span class="text-muted">{ Mule Shell v${version} – 2020 }</span>
        </div>
    </footer>
</div>


<#-- Preverse the order of these JavaScript libraries for Bootstrap to work. -->
${JQueryJS?no_esc}
${popperJS?no_esc}
${bootstrapJS?no_esc}

</body>
</html>