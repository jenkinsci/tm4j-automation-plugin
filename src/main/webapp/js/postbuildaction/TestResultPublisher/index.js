(function(){
    setTimeout(function(){
        const formatSelect = document.querySelector('#tm4jAutomationPostBuildActionFormatSelect');
        const filePath = document.querySelector('#tm4jAutomationPostBuildActionFilePath');
        let currentPath;
        if (filePath) {
            currentPath = formatSelect.value === 'Cucumber' ? filePath.value : null;
        }
        formatSelect.onchange=changeEventHandler;
        checkFormat(formatSelect);

        function changeEventHandler(event) {
            checkFormat(event.target)
        }

        function checkFormat(target){
            if  (target.value === 'Cucumber') {
                filePath.value = currentPath ? currentPath : 'target/cucumber/*.json';
                filePath.disabled = false;
            } else if (target.value == 'JUnit XML Result File') {
                filePath.value = 'target/surefire-reports/*.xml';
                filePath.disabled = false;
            } else {
                filePath.value = 'zephyrscale_result.json';
                filePath.disabled = true;
            }
            filePath.onchange();
        }
    },200)
})();