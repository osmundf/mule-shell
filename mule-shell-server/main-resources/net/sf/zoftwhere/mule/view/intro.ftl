<#-- @ftlvariable name="contextPath" type="java.lang.String" -->
<#--noinspection HtmlUnknownTarget-->
<!doctype html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport"
          content="width=device-width, user-scalable=no, initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0">
    <meta http-equiv="X-UA-Compatible" content="ie=edge">

    <title>Intro · Mule Shell</title>

    <link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.3.1/css/bootstrap.min.css"
            <#--noinspection SpellCheckingInspection-->
          integrity="sha384-ggOyR0iXCbMQv3Xipma34MD+dH/1fQ784/j6cY/iJTQUOhcWr7x9JvoRxT2MZw1T" crossorigin="anonymous">

    <link rel="stylesheet" href="${contextPath}/assets/mule.css">
</head>
<body class="d-flex align-items-start flex-column" style="height: 100vh; margin-bottom: 0; border: 0">

<div id="north" class="w-100">
    <nav class="navbar navbar-expand-md skinny" style="border-bottom: 1px solid black">
        <a class="navbar-brand" style="padding-left: 10px;" href="${contextPath}"><b>Mule Shell</b></a>

        <div class="p-2 flex-fill"></div>

        <nav class="nav nav-masthead justify-content-center">
            <a class="nav-link" href="${contextPath}">Home</a>
            <a class="nav-link active" href="${contextPath}/intro">Intro</a>
            <a class="nav-link" href="${contextPath}/console">Console</a>
            <a class="nav-link" href="${contextPath}">&nbsp;</a>
            <div class="skinny">
                <form class="form-inline my-2 my-lg-0 skinny">
                    <button class="btn btn-outline-success my-2 my-sm-0" type="submit">Login</button>
                </form>
            </div>
        </nav>
    </nav>
</div>

<div id="center" class="d-flex mb-auto flex-column w-100"
     style="height: 100%; overflow: auto; box-shadow: inset 0 0 5rem rgba(0, 0, 0, 1);">
    <div class="fixed-font smallw-100" style="overflow: none; padding: 2px;">
        <p>Lorem ipsum dolor sit amet, consectetur adipiscing elit. Aenean in bibendum ex. Sed sed libero id erat tempor
            efficitur. Fusce dignissim ante magna, at varius dolor convallis ut. Curabitur sed venenatis purus.
            Suspendisse orci metus, tincidunt sit amet pellentesque at, eleifend vitae leo. Vestibulum tincidunt
            imperdiet ex, quis elementum enim dictum ac. Donec auctor nisi turpis, ac volutpat diam viverra sed. Nunc
            nec diam tincidunt, lacinia mi ac, eleifend tellus. Nullam tincidunt, mi eu placerat rhoncus, tellus turpis
            accumsan diam, sed dignissim dolor eros quis enim. Nulla varius, neque in aliquam lobortis, justo purus
            laoreet sapien, sed lobortis ipsum odio mattis orci. Cras accumsan mattis nibh, lobortis fermentum libero
            bibendum a. Mauris sit amet faucibus arcu. Donec sit amet convallis urna, sit amet euismod nunc.</p>

        <p>Nulla sed sem ultricies, efficitur nisi ac, feugiat risus. Suspendisse efficitur magna nisl, nec finibus
            turpis mattis in. Quisque tempus vel sem quis lacinia. Etiam feugiat, nibh mattis ornare posuere, erat dolor
            rhoncus nunc, et vestibulum nisi mi sit amet nibh. Orci varius natoque penatibus et magnis dis parturient
            montes, nascetur ridiculus mus. Sed non arcu a nunc mollis pellentesque eget vel felis. Phasellus vitae eros
            a diam rhoncus gravida. Pellentesque at porta ipsum. Nullam faucibus metus a turpis finibus porta. Quisque
            tristique suscipit nisl in malesuada. Aliquam erat volutpat. Donec a metus euismod, volutpat diam quis,
            egestas mauris. Proin vitae cursus ante. Duis ac dolor at ipsum bibendum elementum eget et sapien.</p>

        <p>Aliquam pellentesque egestas augue, vel efficitur diam egestas sit amet. Nulla gravida, ex eu scelerisque
            consectetur, ante tellus molestie dolor, ac mattis lacus metus eget lectus. Fusce vitae ligula faucibus,
            vehicula enim in, fermentum sapien. Maecenas vulputate ex et gravida rhoncus. In imperdiet, mauris sed
            vulputate porta, mauris arcu volutpat lectus, eu consequat tortor lectus condimentum nunc. Suspendisse
            potenti. Etiam non nunc molestie, bibendum dolor et, mattis libero. Nullam non laoreet ante. Vestibulum
            tincidunt nunc varius consectetur cursus. Cras porttitor congue nulla, et pharetra tortor. In interdum
            rutrum massa, ut accumsan ligula aliquet sed. Nunc elementum tellus non dui porttitor, ut tempor ex
            finibus.</p>

        <p>Ut convallis tristique velit. Pellentesque eget lacus semper, eleifend est id, scelerisque lorem. Duis
            pellentesque, diam eleifend vulputate condimentum, lorem est eleifend augue, eget lacinia justo enim quis
            dui. Morbi massa tortor, interdum tincidunt metus tempor, feugiat consequat dui. Nunc quis cursus lectus.
            Pellentesque luctus, sapien eget suscipit tempus, turpis turpis pulvinar diam, a auctor mauris purus sed
            odio. Ut tincidunt ultricies ex, eu laoreet ligula luctus sit amet. Morbi non sodales dui, nec accumsan mi.
            Nulla facilisi. Phasellus id ex mi. Suspendisse interdum nisi et ligula fermentum commodo. Phasellus ante
            lacus, volutpat consectetur mauris vel, bibendum condimentum risus. Vestibulum pretium odio sit amet est
            posuere, sit amet dapibus elit mattis. Nunc efficitur aliquet dui, sit amet laoreet enim consequat vitae.
            Vivamus sollicitudin, arcu sed varius gravida, odio est blandit sapien, et convallis magna magna sed augue.
            Nulla luctus ex erat, nec fringilla metus pretium malesuada.</p>

        <p>Donec enim nunc, hendrerit id lacus ac, imperdiet pulvinar lectus. Suspendisse potenti. Nam sed velit felis.
            Donec eu mauris nibh. Morbi quis mauris nisi. Integer imperdiet urna et nunc sollicitudin pretium. Praesent
            non finibus nibh, vel feugiat est. Mauris eu mi nisl. Aliquam interdum hendrerit lorem, ut lacinia arcu
            varius ut. Pellentesque scelerisque volutpat neque vel faucibus. Pellentesque semper mollis odio id congue.
            Quisque malesuada congue urna nec scelerisque. Nulla ante dui, semper eget augue et, commodo molestie
            lectus. Donec consequat dapibus velit, et lobortis ligula iaculis ut. </p>
    </div>
    <div class="fixed-font smallw-100" style="overflow: none; padding: 2px;">
        <p>Lorem ipsum dolor sit amet, consectetur adipiscing elit. Aenean in bibendum ex. Sed sed libero id erat tempor
            efficitur. Fusce dignissim ante magna, at varius dolor convallis ut. Curabitur sed venenatis purus.
            Suspendisse orci metus, tincidunt sit amet pellentesque at, eleifend vitae leo. Vestibulum tincidunt
            imperdiet ex, quis elementum enim dictum ac. Donec auctor nisi turpis, ac volutpat diam viverra sed. Nunc
            nec diam tincidunt, lacinia mi ac, eleifend tellus. Nullam tincidunt, mi eu placerat rhoncus, tellus turpis
            accumsan diam, sed dignissim dolor eros quis enim. Nulla varius, neque in aliquam lobortis, justo purus
            laoreet sapien, sed lobortis ipsum odio mattis orci. Cras accumsan mattis nibh, lobortis fermentum libero
            bibendum a. Mauris sit amet faucibus arcu. Donec sit amet convallis urna, sit amet euismod nunc.</p>

        <p>Nulla sed sem ultricies, efficitur nisi ac, feugiat risus. Suspendisse efficitur magna nisl, nec finibus
            turpis mattis in. Quisque tempus vel sem quis lacinia. Etiam feugiat, nibh mattis ornare posuere, erat dolor
            rhoncus nunc, et vestibulum nisi mi sit amet nibh. Orci varius natoque penatibus et magnis dis parturient
            montes, nascetur ridiculus mus. Sed non arcu a nunc mollis pellentesque eget vel felis. Phasellus vitae eros
            a diam rhoncus gravida. Pellentesque at porta ipsum. Nullam faucibus metus a turpis finibus porta. Quisque
            tristique suscipit nisl in malesuada. Aliquam erat volutpat. Donec a metus euismod, volutpat diam quis,
            egestas mauris. Proin vitae cursus ante. Duis ac dolor at ipsum bibendum elementum eget et sapien.</p>

        <p>Aliquam pellentesque egestas augue, vel efficitur diam egestas sit amet. Nulla gravida, ex eu scelerisque
            consectetur, ante tellus molestie dolor, ac mattis lacus metus eget lectus. Fusce vitae ligula faucibus,
            vehicula enim in, fermentum sapien. Maecenas vulputate ex et gravida rhoncus. In imperdiet, mauris sed
            vulputate porta, mauris arcu volutpat lectus, eu consequat tortor lectus condimentum nunc. Suspendisse
            potenti. Etiam non nunc molestie, bibendum dolor et, mattis libero. Nullam non laoreet ante. Vestibulum
            tincidunt nunc varius consectetur cursus. Cras porttitor congue nulla, et pharetra tortor. In interdum
            rutrum massa, ut accumsan ligula aliquet sed. Nunc elementum tellus non dui porttitor, ut tempor ex
            finibus.</p>

        <p>Ut convallis tristique velit. Pellentesque eget lacus semper, eleifend est id, scelerisque lorem. Duis
            pellentesque, diam eleifend vulputate condimentum, lorem est eleifend augue, eget lacinia justo enim quis
            dui. Morbi massa tortor, interdum tincidunt metus tempor, feugiat consequat dui. Nunc quis cursus lectus.
            Pellentesque luctus, sapien eget suscipit tempus, turpis turpis pulvinar diam, a auctor mauris purus sed
            odio. Ut tincidunt ultricies ex, eu laoreet ligula luctus sit amet. Morbi non sodales dui, nec accumsan mi.
            Nulla facilisi. Phasellus id ex mi. Suspendisse interdum nisi et ligula fermentum commodo. Phasellus ante
            lacus, volutpat consectetur mauris vel, bibendum condimentum risus. Vestibulum pretium odio sit amet est
            posuere, sit amet dapibus elit mattis. Nunc efficitur aliquet dui, sit amet laoreet enim consequat vitae.
            Vivamus sollicitudin, arcu sed varius gravida, odio est blandit sapien, et convallis magna magna sed augue.
            Nulla luctus ex erat, nec fringilla metus pretium malesuada.</p>

        <p>Donec enim nunc, hendrerit id lacus ac, imperdiet pulvinar lectus. Suspendisse potenti. Nam sed velit felis.
            Donec eu mauris nibh. Morbi quis mauris nisi. Integer imperdiet urna et nunc sollicitudin pretium. Praesent
            non finibus nibh, vel feugiat est. Mauris eu mi nisl. Aliquam interdum hendrerit lorem, ut lacinia arcu
            varius ut. Pellentesque scelerisque volutpat neque vel faucibus. Pellentesque semper mollis odio id congue.
            Quisque malesuada congue urna nec scelerisque. Nulla ante dui, semper eget augue et, commodo molestie
            lectus. Donec consequat dapibus velit, et lobortis ligula iaculis ut. </p>
    </div>
    <div class="mt-auto w-100"></div>
</div>

<footer class="footer mt-auto">
    <div class="container w-100">
        <span class="text-muted">{ Mule Shell 2019 }</span>
    </div>
</footer>

<script src="https://code.jquery.com/jquery-3.3.1.slim.min.js"
        <#--noinspection SpellCheckingInspection-->
        integrity="sha384-q8i/X+965DzO0rT7abK41JStQIAqVgRVzpbzo5smXKp4YfRvH+8abtTE1Pi6jizo"
        crossorigin="anonymous"></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.14.7/umd/popper.min.js"
        <#--noinspection SpellCheckingInspection-->
        integrity="sha384-UO2eT0CpHqdSJQ6hJty5KVphtPhzWj9WO1clHTMGa3JDZwrnQq4sF86dIHNDz0W1"
        crossorigin="anonymous"></script>
<script src="https://stackpath.bootstrapcdn.com/bootstrap/4.3.1/js/bootstrap.min.js"
        <#--noinspection SpellCheckingInspection-->
        integrity="sha384-JjSmVgyd0p3pXB1rRibZUAYoIIy6OrQ6VrjIEaFf/nJGzIxFDsf4x0xIM+B07jRM"
        crossorigin="anonymous"></script>

</body>
</html>