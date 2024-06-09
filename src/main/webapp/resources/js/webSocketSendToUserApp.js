function connect() {
    // creamos un socket con la url del endpoint
    var socket = new WebSocket('ws://localhost:8080/greeting');
    ws = Stomp.over(socket); // creamos un objeto Stomp sobre el socket
    // conectamos el socket
    ws.connect({}, function(frame) {
        // suscribimos a la cola /user/queue/errors
        ws.subscribe("/user/queue/errors", function(message) {
            alert("Error " + message.body);
        });
        // suscribimos a la cola /user/queue/reply
        ws.subscribe("/user/queue/reply", function(message) {
            alert("Message " + message.body);
        });
    }, function(error) { // en caso de error
        alert("STOMP error " + error);
    });
}
// función para cerrar conexión
function disconnect() {
    if (ws != null) {
        ws.close();
    }
    setConnected(false);
    console.log("Disconnected");
}