<#-- @ftlvariable name="roleModel" type="net.sf.zoftwhere.mule.model.RoleModel" -->
<#-- @ftlvariable name="time" type="java.lang.String" -->
<!doctype html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport"
          content="width=device-width, user-scalable=no, initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0">
    <meta http-equiv="X-UA-Compatible" content="ie=edge">
    <title>Mule Shell Console</title>
</head>
<body onload="loginGuest()">
<input type="hidden" value="${time}">
<div>
    Console for ${roleModel} err ftl ${time}<br>
</div>

<script type="application/javascript" lang="en">
    function loginGuest() {
        console.log("${time}");
    }
</script>
</body>
</html>