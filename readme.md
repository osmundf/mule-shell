# Mule Shell (Still in Development)
A cloud-based JDK JShell.

## To compile/package the project with Apache Maven:

### Rebuild base project (skipping tests):

    mvn clean package -pl '!mule-shell-universe' -Dskip.tests=true

### Generate model:

    mvn clean generate-sources -pl=mule-shell-model -am

### Compile common:

    mvn test-compile -pl=mule-shell-common -am

### Compile model:

    mvn test-compile -pl=mule-shell-model -am

### Compile client:

    mvn test-compile -pl=mule-shell-client -am

### Compile server:

    mvn test-compile -pl=mule-shell-server -am

### Compile universe:

    mvn test-compile -pl=mule-shell-universe -am -f pom.all.xml


## Compiling the project in an IDE:

### Jetbrains IntelliJ IDEA:

To rebuild the project in IntelliJ, simply call the clean and/or package Maven goals.

IntelliJ IDE doesn't (or shouldn't?) include the BOM POM as a resolved dependency.  To be able to repackage only a given module, ensure that you add the BOM module to the Maven module playlist.


1. Repackage mule-shell-common with resolve workspace dependencies selected:

       clean package -pl=mule-shell-common,mule-shell-bom -f pom.xml

2. Repackage mule-shell-model with resolve workspace dependencies selected:
   
       clean package -pl=mule-shell-model,mule-shell-bom -f pom.xml

3. Repackage mule-shell-client with resolve workspace dependencies selected:

       clean package -pl=mule-shell-client,mule-shell-bom -f pom.xml

4. Repackage mule-shell-server with resolve workspace dependencies selected:

       clean package -pl=mule-shell-server,mule-shell-bom -f pom.xml

5. Repackage mule-shell-universe with resolve workspace dependencies selected:

       clean package -pl=mule-shell-universe,mule-shell-bom -f pom.xml

When running a Maven goal against a single module, ensure that all other project modules, that are dependencies, are built.