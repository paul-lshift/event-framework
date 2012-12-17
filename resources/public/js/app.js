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
        var request = {
            type: 'PUT',
            url: "/ajax/putmsg/" + uuid.v4(),
            data: $("#form").serialize()
        }
        // Test idempotency - send everything twice
        // Test idempotency - send everything twice
        $.ajax(request).done(function() {
            $.ajax(request)
        })
        this.reset()
        return false
    })
    $("#message").focus()
});
