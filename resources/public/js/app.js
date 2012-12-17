$(document).ready(function() {
    console.log("Document ready")
    function ajaxread(position) {
        console.log("Requesting new data from position: " + position)
        var requeststart = new Date();
        $.ajax({
            url: "/ajax/getmsg",
            data: {position: position}
        })
        .done(function(data) {
            var messages = data.messages
            console.log("Got " + messages.length + " messages")
            for (var i=0; i < messages.length; i++) {
                var li = document.createElement('li')
                $(li).text(messages[i])
                $("#rubbish-here").append(li)
            }
            ajaxread(data.position)
        })
        .fail(function(jqXHR, textStatus) {
            var failtime = new Date()
            var interval = failtime.getTime() - requeststart.getTime()
            console.log("Poll failed after " + interval + " ms: " + textStatus)
            // Wait at least 10s before trying again after failure
            var nextwait = 10000 - interval
            if (nextwait <= 0) {
                console.log("Making new request immediately")
                ajaxread(position)
            } else {
                console.log("Making new request after " + nextwait + " ms")
                setTimeout(function() {
                    console.log("Timeout expired, making request")
                    ajaxread(position)
                }, nextwait)
            }
        })
    }
    ajaxread("0")
    $("#form").submit(function(event) {
        var request = {
            type: 'PUT',
            url: "/ajax/putmsg/" + uuid.v4(),
            data: $("#form").serialize()
        }
        console.log("Sending message")
        // Test idempotency - send everything twice
        // Test idempotency - send everything twice
        $.ajax(request).done(function() {
            console.log("Re-sending message")
            $.ajax(request)
        })
        this.reset()
        return false
    })
    $("#message").focus()
})
