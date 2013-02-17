ServletCommunicator = function () {
    this.AJAX_FREQUENCY = 100;//ms
    this.x = 0;
    this.y = 0;
    this.jsonData = {};
    this.milliseconds = 0;

    this.sendVector = function (vector) {
        this.x = vector.x;
        this.y = vector.y;
        this.sendLastVector();
    };

    this.sendLastVector = function () {
        this.createJSON("Vector");
        this.jQueryAjaxPost();
    };

    this.sendTractor = function (vector) {
        this.x = vector.x;
        this.y = vector.y;
        var d= new Date();
        var milliseconds = d.getTime();
        if((milliseconds - this.milliseconds)>this.AJAX_FREQUENCY){
            this.milliseconds = milliseconds;
            this.sendLastTractor();
        }
    };

    this.sendLastTractor = function () {
        this.createJSON("Tractor");
        this.jQueryAjaxPost();
    };

    this.jQueryAjaxPost = function () {
        var jsonData = this.jsonData;
        $(document).ready(function () {
            $.post('servlet', jsonData
            );
        });
    };

    this.createJSON = function (type) {
        this.jsonData = {type: type, x: this.x, y: this.y, milliseconds: this.milliseconds};
    };
};