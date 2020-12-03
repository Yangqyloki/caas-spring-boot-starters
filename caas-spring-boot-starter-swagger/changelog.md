
# Swagger-UI Changelog

This file is meant to keep track of any customizations done to the swagger-ui distribution in order to facilitate upgrading the swagger-ui console.  
The HTML tags should remain the same as in swagger-ui `index.html`, we only change SwaggerUIBundle configuration.  
Copy the index.html, and in the SwaggerUIBundle configuration make the following changes.  

## Changes

file                  | change           
----------------------|---------------------------------------------------------------------------------
1. index.html	      |	default url to "../api-description.yaml"
2. index.html	      |	remove deepLinking: true  // by default (if not present) deepLinking is disabled
3. index.html	      |	add validatorUrl: null    // disable default https://online.swagger.io/validator swagger validator
4. index.html	      |	move swagger ui bundle config into upscale-csrf-token.js
5. upscale-csrf-token |	fetch the CSRF token and append it on every "Try it out" request 

## Configuration

See [upscale-csrf-token.js](src/main/resources/static/api/upscale-csrf-token.js)

```javascript
const ui = SwaggerUIBundle({
    url: '../api-description.yaml',
    // requestInterceptor: // see upscale-csrf-token.js
    dom_id: '#swagger-ui',
    validatorUrl: null,
    presets: [
        SwaggerUIBundle.presets.apis,
        SwaggerUIStandalonePreset
    ],
    plugins: [
        SwaggerUIBundle.plugins.DownloadUrl
    ],
    layout: "StandaloneLayout"
})
```

## References
[Swagger UI Bundle Configuration](https://github.com/swagger-api/swagger-ui/blob/master/docs/usage/configuration.md)
