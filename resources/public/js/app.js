$(document).ready(function() {
    $("#rubbish-here").append("<li>hi</li>");
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
        }).done(ajaxread)
    }
    ajaxread({messages: [], position: "0"});
    $("#form").submit(function() {
        $.post("/ajax/putmsg", $("#form").serialize())
        return false
    })
});
