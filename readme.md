# Mule Shell (Still in Development)
A cloud-based JDK JShell.

## To compile the project with Apache Maven:

### Rebuild base project:
mvn clean compile package -Dswagger.io.generate.phase=generate-sources -f pom.base.xml

### Generate model:
mvn clean generate-sources -Dswagger.io.generate.phase=generate-sources -pl=mule-shell-model -f pom.base.xml

### Compile model:
mvn compile -pl=mule-shell-model -f pom.base.xml

### Compile client: (IDE only)
mvn clean compile package -pl=mule-shell-client -f pom.all.xml

### Compile server: (IDE only)
mvn clean compile package -pl=mule-shell-server -f pom.all.xml

### Compile universe: (IDE only)
mvn clean compile package -pl=mule-shell-universe -f pom.all.xml
