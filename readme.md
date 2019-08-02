# Mule Shell (Still in Development)
A cloud-based JDK JShell.

## Requirements

The project has the following requirements to compile/run:

1. Java Development Kit 11 

    The project used Java Development Kit version 11 (Oracle) to compile the Java sources.
    
    The project uses the JShell included in the JDK, so running the program under just the Java Runtime Environment will not work.

2. Apache Maven 3.6

    The project used Maven 3.6.1 to manage/build with dependencies. 

## To run the demonstration project:

1. Make sure you have at least Java Development Kit 11 (project created with JDK 11.0.2 Oracle).

2. Make sure you have at least Apache Maven 3.6 (project tested with Maven 3.6.1).

3. Create the demo.jar file by packaging the project:

        mvn package -f pom.xml

4. Run the demo.jar file with JDK Java (The JDK is needed as the JRE does not have the JDK JShell modules):

        java -jar mule-shell-universe/demo.ja
        
5. Navigate to the local host in a browser:

        http://localhost:8080/mule-shell-demo

6. Try out the interactive console (still in development).

## To compile/package the project with Apache Maven:

### Rebuild base project (skipping tests):

    mvn clean package -pl '!mule-shell-universe' -DskipTests=true

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

IntelliJ IDE does not (or shouldn't?) include the BOM POM as a resolved dependency.  To be able to repackage only a given module, ensure that you add the BOM module to the Maven module playlist.

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

## Running the Drop Wizard application in Java 9, 10, 11

The Java Platform Module System will print a warning message about illegal reflective access. At the time of writing, no added effort has been made by the contributors of Mule Shell to bring the application to JPMS (as most dependencies are/were unnamed modules).  The only reflective access dependency noted was that of Faster XML.  The message can "suppressed" by flagging all unnamed modules as open (JVM parameter):

```--add-opens java.base/java.lang=ALL-UNNAMED```

## Deploying to Local Repository

Artifacts can be installed to the local repository with the deploy plugin, ensuring that test jar dependencies are also installed.  The ```shadePhase``` property is to suppress the creation of the shaded ```demo.jar``` file.

```mvn clean deploy -Durl=file://<path> -DskipTests=true -DshadePhase=none -f pom.xml```
