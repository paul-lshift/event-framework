$(document).ready(function() {
    $("#rubbish-here").append("<li>hi</li>");
    function ajaxread(data) {
        var li = document.createElement('li');
        $(li).text(data);
        $("#rubbish-here").append(li);
        $.ajax({
            url: "/ajax/getmsg"
        }).done(ajaxread);
    }
    ajaxread("test");
    $("#button").click(function() {
        $.post("/ajax/putmsg", {
            message: "zow"
        })
    })
});
