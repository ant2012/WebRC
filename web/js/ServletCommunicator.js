ServletCommunicator = function () {
    this.x = 0;
    this.y = 0;
    this.jsonData = {};

    this.sendVector = function (vector) {
        this.x = vector.x;
        this.y = vector.y;
        this.sendLastVector();
    };

    this.sendLastVector = function () {
        this.createJSON();
        this.jQueryAjaxPost();
    };

    this.jQueryAjaxPost = function () {
        var jsonData = this.jsonData;
        $(document).ready(function () {
            $.post('servlet', jsonData
            );
        });
    };

    this.createJSON = function () {
        var d= new Date();
        var milliseconds = d.getMilliseconds();
        this.jsonData = {x: this.x, y: this.y, milliseconds: milliseconds};
    };
};