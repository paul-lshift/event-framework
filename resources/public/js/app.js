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
            console.log("AJAX read failed, status:" + textStatus)
            //ajaxread({messages: [], position: data.position})
        })
    }
    ajaxread({messages: [], position: "0"})
    $("#form").submit(function(event) {
        // event.preventDefault()
        $("#uuid").val(uuid.v4())
        $.post("/ajax/putmsg", $("#form").serialize())
        this.reset()
        //$("#message").focus()
        return false // Oddly, this doesn't suffice to prevent default
    })
    $("#message").focus()
});
