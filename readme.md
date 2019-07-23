# Mule Shell (Still in Development)
A cloud-based JDK JShell.

## To compile the project with Apache Maven:

### Rebuild entire project:
mvn clean compile package -Dswagger.io.generate.phase=generate-sources -f mule-shell-universe/pom.universe.xml

### Generate model:
mvn clean process-sources -Dswagger.io.generate.phase=generate-sources -pl=../mule-shell-model -f mule-shell-universe/pom.modules.xml

### Compile model:
mvn compile package -pl=../mule-shell-model -f mule-shell-universe/pom.modules.xml

### Compile client:
mvn clean compile package -pl=../mule-shell-client -f mule-shell-universe/pom.modules.xml

### Compile server:
mvn clean compile package -pl=../mule-shell-server -f mule-shell-universe/pom.modules.xml

### Compile universe:
mvn clean compile package -pl=../mule-shell-universe -f mule-shell-universe/pom.universe.xml
