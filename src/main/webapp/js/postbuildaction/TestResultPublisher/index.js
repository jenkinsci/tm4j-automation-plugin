(function(){
    setTimeout(function(){
        var formatSelect = document.querySelector('#tm4jAutomationPostBuildActionFormatSelect');
        var filePath = document.querySelector('#tm4jAutomationPostBuildActionFilePath');
        var currentPath;
        if (filePath) {
            currentPath = formatSelect.value == 'Cucumber' ? filePath.value : null;
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
            }
            else {
                filePath.value = 'tm4j_result.json';
                filePath.disabled = true;
            }
            filePath.onchange();
        }
    },200)
})();