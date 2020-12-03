# SAP Hybris - CaaS Spring Boot Starter for data
---
Provides persistence layer features

## Features
* Provides default JPA configuration for eclipse link.
* Provides entity, dto and converter for auditing.
    * To enable auditing for an entity, following steps are necessary:
        * Make your entity class to extend **Audit** class
        * Configure required scope to populate audit fields in response through below properties 
          ```properties
              cloud.security:
                  xsappname: dummy
                  tenant-regex: dummy
                  manage-scope: dummy
          ```
        * Add **MetadataDto** class as a property to your response object
        * Inject **AuditToMetadataDtoConverter** to your response converter, to convert **Audit** object to **MetadataDto** object. This converter relies on above properties to decide whether to populate audit fields in response or not.
    * If the above properties or the **AuditToMetadataDtoConverter** do not meet your needs, you can override either(or both) the properties file and the converter. But in order to avoid any potential conflicts, please make sure to configure your Properties bean with name: **_cloudSecurityProperties_** and converter bean with name: **_auditToMetadataDtoConverter_** 

* Consolidates shared queryDsl objects, if `com.querydsl.core.Query` is on the classpath
* Copies into filesystem and provides the path of the AWS RDS `rds-combined-ca-bundle.pem` certificate bundle 
  via an application property: `caas.datasource.sslrootcert`
* Common JPA attribute converters
  * UUID converter
  * String Array converter
  * HStore converter
  
  
### Aes Encryption Converter 

* This converter is used to encrypt/decrypt a string value, as credentials to be stored in the database securely using the Advanced Encryption Standard (AES).

* Environment variables are required in the application.yml file, the Aes key and salt.
```yaml
caas:
  encryption.aes:
    key: ${AES_KEY}
    salt: ${AES_SALT}
```

* Usage of the converter in your service entity for a given attribute
```java
    @Convert(converter = AesEncryptionConverter.class)
    @Column(name = "password")
    private String password;
```