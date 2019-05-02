(function() {
    function extractBaseUrl(jwt) {
        try {
          var jwtDecodedPayload = atob(jwt.split('.')[1]);
          var baseUrl = JSON.parse(jwtDecodedPayload).context.baseUrl;

          return decodeURIComponent(baseUrl)
        } catch(e) {
          console.warn('Failed to extract baseUrl from JWT: ', e);
        }

        return '';
    }

    function setJiraBaseUrl(jwt) {
        var baseUrl = extractBaseUrl(jwt);
        var cloudBaseUrlField = document.getElementById('tm4j-cloud-base-url');
        cloudBaseUrlField.value = baseUrl;
    }

    function handleJwtFieldEvent(e) {
        if (e.target && e.target.id === 'tm4j-cloud-jwt-field') {
            setTimeout(function() {
                if (e.target.value !== e.target.defaultValue) {
                    setJiraBaseUrl(e.target.value);
                }
            })
        }
    }

    document.addEventListener('paste', handleJwtFieldEvent);
    document.addEventListener('change', handleJwtFieldEvent);
})()