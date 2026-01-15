JAVAC=javac
JAVA=java
SRC=src
CLASSES=$(SRC)/*.class

all: compile

compile:
	$(JAVAC) $(SRC)/*.java

server:
	$(JAVA) -cp $(SRC) DirectorySyncServer

client:
	$(JAVA) -cp $(SRC) DirectorySyncClient

clean:
	rm -f $(CLASSES)

rebuild: clean compile

