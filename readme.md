# To compile the project with Apache Maven:

# (Re-)Generate model:
mvn clean process-sources -Dswagger.io.generate.phase=process-sources -pl=../mule-shell-model -f mule-shell-universe/modules.pom.xml

# Compile model:
mvn compile package -pl=../mule-shell-model -f mule-shell-universe/modules.pom.xml

# Compile client:
mvn clean compile package -pl=../mule-shell-client -f mule-shell-universe/modules.pom.xml

# Compile server:
mvn clean compile package -pl=../mule-shell-server -f mule-shell-universe/modules.pom.xml

# Compile universe:
mvn clean compile package -pl=../mule-shell-universe -f mule-shell-universe/universe.pom.xml
