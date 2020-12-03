import betaMarkPlugin from './beta-mark.js'

window.onload = function () {

    function fetchCsrfToken() {
        return fetch(location.href, {
            method: 'HEAD',
            headers: {'x-csrf-token': 'fetch'}
        })
        .then(function (resp) {
            return resp.headers.get('x-csrf-token')
        })
    }

    function fetchApiDocs() {
        const appPath =  location.pathname.split('/api/index.html')[0];
        const apiDocsEndpoint = location.origin + appPath + '/apidocs';
        return fetch(apiDocsEndpoint)
        .then(function(response) {
            return response.clone().json();
        })
    }

    fetchCsrfToken().then(function (csrfToken) {
        fetchApiDocs().then(function(apiDocs) {

            // Begin Swagger UI call region
            const ui = SwaggerUIBundle({
                urls: apiDocs.apiDescriptions,
                requestInterceptor: req => {
                    if (csrfToken) {
                        req.headers['x-csrf-token'] = csrfToken
                    }
                    return req
                },
                dom_id: '#swagger-ui',
                validatorUrl: null,
                presets: [
                    SwaggerUIBundle.presets.apis,
                    SwaggerUIStandalonePreset
                ],
                plugins: [
                    SwaggerUIBundle.plugins.DownloadUrl,
                    betaMarkPlugin
                ],
                layout: "StandaloneLayout"
            })
            // End Swagger UI call region

            window.ui = ui
        })
    })


}
