# SAP Hybris - CaaS Spring Boot Starter for API Documentation with Swagger (OpenAPI)
---
Adds HTML Swagger API console to your Spring Boot application.

## Features
___
* Exposes a swagger ui at the context root of the service from a supplied api description YAML file.
* Replaces or add the server url entry in the swagger api specification

The following table captures the swagger-ui version on which the starter it is based on.

Release Version |  Swagger-ui Version
--------------- | -------------------
1.0.1           | 3.0.12
&gt; 1.0.1      | 3.0.7
2.0.0           | 3.4.3
3.25.1          | 3.18.3
3.27.0          | 3.22.1
3.32.2          | 3.24.3

### Note:
 As of CAAS-Starters version 3.25.1 we have updated to swagger-ui version 3.18.3 thus breaking relative path
 $ref's pointing to external files that were supported in older versions, if this feature is still desired downgrade by specifying an earlier
 version number, else avoid using this previously supported feature.
___

### Design First vs Code First API Development
There are 2 primary schools of thought when it comes to producing API documentation. Once the business requirements are understood by the team and ready to be implemented, the team may opt to design the API first or to immediately start producing code.

Swagger supports both models. When following a design-first approach, the team produces an API description file in JSON/YAML format that describes the APIs that will be implemented. This design can be shared with other teams and serves as a contract. When following a code-first approach, the team implements the features and then generates an API description file from the code. This helps deliver quickly and is usually aimed at teams developing internal APIs.

In this spring-boot-starter, there is currently only support for the _Design First API Development_ approach.

#### Design First
In order to load a swagger ui for your API descriptor, you will need to produce YAML files which document all the endpoints in the service. A service can have more than one YAML file(this helps to split documentation into more manageable sections). All YAML files that are candidates for swagger documentation must be suffixed with `api-description.yaml`. For example the following file names can be rendered as swagger documentation:

    api-description.yaml
    module1-api-description.yaml
    test-api-description.yaml
    
If your service has more than one API descriptor, you will be able to switch between the API docs using the API definition selector once the swagger endpoint has been loaded.

The swagger/openAPI specification file should be in the root of the classpath:

	src/main/resources/api-description.yaml
	src/main/resources/module1-api-description.yaml
	src/main/resources/test-api-description.yaml

The API descriptor should follow the Swagger specification as describes here: [Swagger 2.0](https://github.com/OAI/OpenAPI-Specification/blob/master/versions/2.0.md)
or [Open API 3.0](https://swagger.io/docs/specification)

### API Descriptor
You should set the following attributes in your api descriptor (samples values are provided):
```yaml
openapi: '3.0.0'
info:
  title: Product Content API
  description: Product content API 0.4.0
  version: "0.4.0"
  contact:
    name: "team Polar Bears"
    email: "team-priceless@sap.com"
paths: {}
```

### Swagger-UI Security
If your service has authentication enabled, you will have to add custom paths to permit the following swagger-ui files:
* /
* /api-description.yaml
* /module1-api-description.yaml
* /api/index.html
* /api.swagger-ui.css
* /api/swagger-ui-bundle.js
* /api/swagger-ui-standalone-preset.js
* /api/favicon-16x16.png


#### OAuth 2
To add support for OAuth 2 to your production deployment, you can add the security entry in your corresponding API description YAML file.
```yaml
security:
  - OAuth2Token: []
```
With this configuration, you will have to generate a valid JWT and paste it in the authorization section of the swagger-ui console along with the Bearer prefix.

#### Beta Mark Plugin
A swagger UI plugin which adds to the summary of the path with a BETA mark.  
In order to mark a path, add the path (such as "/v2/promotions") into a JSON array with the HTTP method as the variable name in the `resources/static/api/beta-paths.js` file of the respective service that requires it.
Make sure that the file is not secured. This can be done and verified via the `CloudWebSecurityConfig`.
The beta mark can only be manually added to the tag's description.

### API description Server URL rewrite

By default, the `SwaggerController` will override the OpenApi specification entry `servers.url` with the current host and context path.
If working behind a proxy, like AppRouter, it will use `X-Forwarded` headers to build the host and path.
This functionality will replace all entries for `servers*.url` with the replaced entry. (`servers` is a list of `url`s and other fields).
To disable this functionality, set the `caas.swagger.rewrite-servers-url` property to `false` value in your SpringBoot configuration

    caas.swagger.rewrite-servers-url: false 

### Configurable static paths in swagger

By default, the `SwaggerConfig` will always add the Spring Boot defaults as path patterns and resource locations. However, `404s` will
not provide error messages. This can be overridden if `spring.resources.add-mappings` is set to `false`, preventing the Spring defaults
and ensuring error messages are displayed. In any case, the `SwaggerConfig` will always add `/api/**` as a path pattern and
`classpath:/static/api/` as a resource location. If one or more configurable static paths need to be added,
they can be specified in pairs in the `application.yaml`. The following example shows the format to follow in the properties:

    swagger:
      swagger-paths:
        - root-directory: public
          sub-directory: paths
        - root-directory: public
          sub-directory: definitions
        - root-directory: example-root
          sub-directory: example-folder

The `sub-directory` parameter will act as the path pattern to access the page.
The resource location will be defined by the `root-directory` and the `sub-directory` combined together.
For example, using the first given pair in the example above:

    path pattern: paths/**
    resource location: classpath:/public/paths/

## Update swagger-ui

A list of changes done to the swagger-ui configuration can be found in [changelog](changelog.md) file.

To change the swagger-ui version being used follow these steps

* Get the swagger-ui version/tag to be used from
 
    `https://github.com/swagger-api/swagger-ui/releases`
    
* Clone the git repository or download the zip file for that specific version/tag to be updated
    ```bash
    # for example updating to version v3.22.1
    # from distribution tgz file
    wget https://github.com/swagger-api/swagger-ui/archive/v3.22.1.tar.gz
    tar zxvf v3.22.1.tar.gz
    # or via cloning the git repo
    git clone https://github.com/swagger-api/swagger-ui.git
    git checkout v3.22.1
    ```
    
* Delete the contents of the directory `caas-spring-boot-starter-swagger/src/main/resources/static/api`
    ```bash
    rm caas-spring-boot-starter-swagger/src/main/resources/static/api/*
    ```
    
* From swagger-ui copy the contents from `swagger-ui/dist` into `caas-spring-boot-starter-swagger/src/main/resources/static/api`
    ```bash
    cp swagger-ui/dist/* caas-spring-boot-starter-swagger/src/main/resources/static/api/
    ```

* Apply the patch file `index-html.patch` to the `index.html` to update specific properties
    ```bash
    git apply caas-spring-boot-starter-swagger/index-html.patch
    ```
    
* Update this `README.md` file (at the top) to match the caas starters version with the updated swagger version

* Please include the swagger-ui version in the commit message

### Update the patch file

To update the patch file, bring the `index.html` to its unchanged state, either by 
* getting the same`index.html` from the same swagger-ui dist version, 
* or by undoing/reverting the patch.

```bash
git apply -R caas-spring-boot-starter-swagger/index-html.patch
```

Modify the `index.html` as needed, and then update the `index-html.patch` file using this command:
Make sure only the index.html file is modified

```bash
git add *
git diff --cached > caas-spring-boot-starter-swagger/index-html.patch
```


### K8 swagger

For kubernetes the property 'caas.swagger.server.context' should be set to the context path as it will not be automatically carried over from the request. 
