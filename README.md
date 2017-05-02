# Build Instructions

### Deploy to Bintray

Add settings to .m2/settings.xml

```xml

...

<servers>
  <server>
    <id>bintray-repo-moe-maven</id>
    <username>your user name</username>
    <password>******</password>
  </server>
</servers>

...


```

```sh
$ mvn clean deploy
```

### Maven dependency from Bintray

```xml
<dependency>
  <groupId>org.multi-os-engine</groupId>
  <artifactId>moe-maven</artifactId>
  <version>1.3.0</version>
  <type>pom</type>
</dependency>
```
