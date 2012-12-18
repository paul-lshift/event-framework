function sendCommand(type, payload) {
    var request = {
        type: 'PUT',
        url: "/ajax/command/" + type + "/" + uuid.v4(),
        data: payload
    }
    console.log("Sending command")
    // Test idempotency - send everything twice
    // Test idempotency - send everything twice
    $.ajax(request).done(function() {
        console.log("Re-sending command")
        $.ajax(request)
    })
}
var handleEvent = {
    newthread: function(event) {
        var h = document.createElement('h2')
        $(h).text(event.payload.title)
        $("#threads-here").append(h)
        $("#threads-here").append(
            "<ul id=\"ul-" + event.uuid + "\"></ul>" +
            "<form id=\"thread-" + event.uuid + "\">" +
            "<input type=\"hidden\" name=\"thread\" value=\"" +
            event.uuid + "\">" +
            "<input type=\"text\" name=\"message\" size=\"100\">" +
            "<input type=\"submit\" value=\"Submit\">" +
            "</form>")
        $("#thread-" + event.uuid).submit(function(event) {
            sendCommand("message", $(this).serialize())
            this.reset()
            return false
        })
    },
    message: function(event) {
        var li = document.createElement('li')
        $(li).text(event.payload.message)
        $("#ul-" + event.payload.thread).append(li);
    }
}
function readEvents(position) {
    console.log("Requesting new data from position: " + position)
    var requeststart = new Date()
    $.ajax({
        url: "/ajax/events",
        data: {position: position}
    })
    .done(function(data) {
        if (data.goaway) {
            console.log("Server told us to go away");
            $("#complainhere").text("Server has told us to go away; please reload.")
        } else {
            var events = data.events
            console.log("Got " + events.length + " events")
            for (var i=0; i < events.length; i++) {
                handleEvent[events[i].type](events[i])
            }
            readEvents(data.position)
        }
    })
    .fail(function(jqXHR, textStatus) {
        var failtime = new Date()
        var interval = failtime.getTime() - requeststart.getTime()
        console.log("Poll failed after " + interval + " ms: " + textStatus)
        // Wait at least 10s before trying again after failure
        var nextwait = 10000 - interval
        if (nextwait <= 0) {
            console.log("Making new request immediately")
            readEvents(position)
        } else {
            console.log("Making new request after " + nextwait + " ms")
            setTimeout(function() {
                console.log("Timeout expired, making request")
                readEvents(position)
            }, nextwait)
        }
    })
}
$(document).ready(function() {
    console.log("Document ready")
    $("#newthread").submit(function(event) {
        sendCommand("newthread", $(this).serialize())
        this.reset()
        return false
    })
    $("#initfocus").focus()
    readEvents("")
})
