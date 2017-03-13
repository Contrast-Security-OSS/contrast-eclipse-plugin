# IDE plugin connecting to REST API

### Getting Started

- Install Java JDK (minimal version 7) and maven 3.3.x
- clone repository
- call

mvn clean verify

- start Eclipse (current target version is Mars, but I have tested with Neon)
- select Help>Install New Software>Add
- click Archive...
- select updatesite/target/contrastide.updatesite-1.0.0-SNAPSHOT.zip
- select Contrast IDE
- restart Eclipse

### Development

Requirements: Eclipse SDK Mars 4.5 or higher, recommended m2e and EGit

- import all "Existing Projects"
- cd mars; mvn install
- cd parent; mvn install 

### License
GPL version 3 
