var userid = uuid.v4();
function sendCommand(type, body) {
  var request = {
    type: 'PUT'
   ,url: "/ajax/command/" + type + "/" + uuid.v4()
   ,contentType: "application/json"
   ,data: JSON.stringify(body)
  };
  console.log("Sending command");
  // Test idempotency - send everything twice
  // Test idempotency - send everything twice
  $.ajax(request).done(function() {
    console.log("Re-sending command");
    $.ajax(request);
  });
}
function submitSendsCommand(form, type) {
	form.submit(function(event) {
		sendCommand(type, $(this).serializeObject());
		this.reset();
		return false;
	})
}
var handleEvent = {
  newthread: function(event) {
    $("#threads-here").append(
      "<div id=\"thread-" + event.id + "\">" +
      "<h2></h2>" +
      "<form>" +
      "<input type=\"hidden\" name=\"user\" value=\"" +
      userid + "\">" +
      "<input type=\"hidden\" name=\"thread\" value=\"" +
      event.id + "\">" +
      "<input type=\"submit\" value=\"Subscribe\">" +
      "</form>" +
      "</div>");
    $("#thread-" + event.id + " h2").text(event.body.title);
    submitSendsCommand($("#thread-" + event.id + " form"), "subscribe");
  }
 ,subscribe: function(event) {
    $("#thread-" + event.body.thread + " form").remove();
    $("#thread-" + event.body.thread).append(
      "<ul></ul>" +
      "<form>" +
      "<input type=\"hidden\" name=\"thread\" value=\"" +
      event.body.thread + "\">" +
      "<input type=\"text\" name=\"message\" size=\"100\">" +
      "<input type=\"submit\" value=\"Submit\">" +
      "</form>");
    submitSendsCommand($("#thread-" + event.body.thread + " form"), "message");
  }
 ,message: function(event) {
    var li = document.createElement('li');
    $(li).text(event.body.message);
    $("#thread-" + event.body.thread + " ul").append(li);
  }
};
function handleEventList(events) {
  console.log(">> Got " + events.length + " events");
  for (var i=0; i < events.length; i++) {
    var event = events[i];
    console.log("Handling: " + event.type);
    handleEvent[event.type](event);
    if (event.extraevents) {
      console.log("Event has extra events");
      handleEventList(event.extraevents);
    }
  }
  console.log("<< Events handled");
}
function readEvents(position) {
  console.log("Requesting new data from position: " + position);
  var requeststart = new Date();
  $.ajax({
    url: "/ajax/events/" + userid + "/" + position
  })
    .done(function(data) {
      if (data.goaway) {
        console.log("Server told us to go away");
        $("#complainhere").text("Server has told us to go away; please reload.");
      } else {
        handleEventList(data.events);
        readEvents(data.position);
      }
    })
    .fail(function(jqXHR, textStatus) {
      var failtime = new Date();
      var interval = failtime.getTime() - requeststart.getTime();
      console.log("Poll failed after " + interval + " ms: " + textStatus);
      // Wait at least 10s before trying again after failure
      var nextwait = 10000 - interval;
      if (nextwait <= 0) {
        console.log("Making new request immediately");
        readEvents(position);
      } else {
        console.log("Making new request after " + nextwait + " ms");
        setTimeout(function() {
          console.log("Timeout expired, making request");
          readEvents(position);
        }, nextwait);
      }
    });
}
$(document).ready(function() {
  console.log("Document ready");
  submitSendsCommand($("#newthread"), "newthread");
  $("#initfocus").focus();
  readEvents("0");
});
