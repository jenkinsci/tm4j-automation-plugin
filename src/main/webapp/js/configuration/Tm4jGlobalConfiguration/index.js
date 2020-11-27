(function() {
    function extractBaseUrl(jwt) {
        try {
            const jwtDecodedPayload = atob(jwt.split('.')[1]);
            const baseUrl = JSON.parse(jwtDecodedPayload).context.baseUrl;

            return decodeURIComponent(baseUrl)
        } catch(e) {
          console.warn('Failed to extract baseUrl from JWT: ', e);
        }

        return '';
    }

    function handleJwtFieldEvent(e) {
        const elem = e.target;
        if (!elem) {
            return;
        }

        const tm4jJwt = elem.getAttribute('data-tm4j-jwt');
        if (tm4jJwt) {
            const jiraInstanceContainer = elem.closest('[data-jira-instance-container]');
            const jiraUrlElem = jiraInstanceContainer.querySelector('[data-tm4j-jira-url]');
            setTimeout(function() {
                if (!e.target.value) {
                    jiraUrlElem.value = '';
                    return;
                }

                if (e.target.value !== e.target.defaultValue) {
                    const jwt = e.target.value;
                    jiraUrlElem.value = extractBaseUrl(jwt);
                }
            })
        }
    }

    document.addEventListener('paste', handleJwtFieldEvent);
    document.addEventListener('change', handleJwtFieldEvent);
})()