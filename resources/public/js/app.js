$(document).ready(function() {
    function ajaxread(data) {
        messages = data.messages
        for (var i=0; i < messages.length; i++) {
            var li = document.createElement('li')
            $(li).text(messages[i])
            $("#rubbish-here").append(li)
        }
        $.ajax({
            url: "/ajax/getmsg",
            data: {position: data.position}
        })
        .done(ajaxread)
        .fail(function(jqXHR, textStatus) {
            $("#complainhere").text(
                "Net problem, you will need to reload this page. Status: "
                + textStatus)
            //ajaxread({messages: [], position: data.position})
        })
    }
    ajaxread({messages: [], position: "0"})
    $("#form").submit(function(event) {
        var newuid = uuid.v4()
        $.ajax({
            type: 'PUT',
            url: "/ajax/putmsg/" + newuid,
            data: $("#form").serialize()
        })
        this.reset()
        return false
    })
    $("#message").focus()
});
