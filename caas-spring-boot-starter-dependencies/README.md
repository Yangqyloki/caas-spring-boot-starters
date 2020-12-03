# SAP Hybris - CaaS Spring Boot Starter Dependencies
---
Provides a BOM (Bill of Materials) for the caas-spring-boot-starters. 
A BOM is a special kind of POM that is used to control the versions of a project's dependencies and provide a central place to define and update those versions. BOM provides the flexibility to add a dependency to our module without worrying about the version that we should depend on.

You can import the BOM using the [Spring dependency-management plugin](https://docs.spring.io/dependency-management-plugin/docs/current/reference/html/).

Import the plugin directly or via the spring-boot-plugin.
```groovy
buildscript {

    ext {
        springBootVersion = '2.1.7.RELEASE'
    }

    dependencies {
        classpath "org.springframework.boot:spring-boot-gradle-plugin:${springBootVersion}"
    }
}

apply plugin: "io.spring.dependency-management"
```

Import the BOM and caas starters dependencies
```groovy
ext {
	caasStartersBomVersion = '3.30.4'
}

dependencyManagement {
     imports {
          mavenBom "com.sap.caas:caas-spring-boot-starter-dependencies:${caasStartersBomVersion}"
     }
}

dependencies {
     compile 'com.sap.caas:caas-spring-boot-starter-error-handling'
     compile 'com.sap.caas:caas-spring-boot-starter-kafka'
     compile 'com.sap.caas:caas-spring-boot-starter-logging'
     compile 'com.sap.caas:caas-spring-boot-starter-multitenant'
     compile 'com.sap.caas:caas-spring-boot-starter-security'
     compile 'com.sap.caas:caas-spring-boot-starter-swagger'
     compile 'com.sap.caas:caas-spring-boot-starter-web'
     testCompile 'com.sap.caas:caas-spring-boot-starter-test'
}
```

