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

    function handleJwtFieldEvent(e) {
        var elem = e.target;
        if (!elem) return;

        var tm4jJwt = elem.getAttribute('data-tm4j-jwt');
        if (tm4jJwt) {
            var jiraInstanceContainer = elem.closest('[data-jira-instance-container]');
            var jiraUrlElem = jiraInstanceContainer.querySelector('[data-tm4j-jira-url]');
            setTimeout(function() {
                if (!e.target.value) {
                    jiraUrlElem.value = '';
                    return;
                }

                if (e.target.value !== e.target.defaultValue) {
                    var jwt = e.target.value;
                    var baseUrl = extractBaseUrl(jwt);
                    jiraUrlElem.value = baseUrl;
                }
            })
        }
    }

    document.addEventListener('paste', handleJwtFieldEvent);
    document.addEventListener('change', handleJwtFieldEvent);
})()