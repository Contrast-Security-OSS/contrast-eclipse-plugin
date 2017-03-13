# IDE plugin connecting to REST API

### Getting Started

- Install Java JDK (minimal version 7) and maven 3.3.x
- clone repository
- cd ide-plugin-eclipse2
- call

- cd targets; mvn clean install
- cd ..; mvn clean install 
- mvn clean verify

- start Eclipse (current target version is Mars, but I have tested with Neon)
- select Help>Install New Software>Add
- click Archive...
- select updatesite/target/contrastide.updatesite-1.0.0-SNAPSHOT.zip
- select Contrast IDE
- restart Eclipse

### Development

Requirements: Eclipse SDK Mars 4.5 or higher, recommended m2e and EGit

- import all "Existing Projects"
- create an Eclipse PDE Run/Debug configuration with included com.contrastsecurity.ide* plugins

### License
GPL version 3 
