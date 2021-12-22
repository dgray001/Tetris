class Joinee {
  private Client client;
  private String id;
  private String writeHeader = "";
  private String name = "";
  private int port;
  private int ping = -1;
  private int lastPingRequest = 0;
  private boolean waitingForResponse = false;
  private boolean receivedInitialResponse = false;
  private int pingRequestsMissed = 0;
  
  Joinee(Client newClient, int port, String header) {
    this.client = newClient;
    this.port = port;
    this.writeHeader = header;
    if (newClient != null) {
      this.id = newClient.ip() + ", " + port + ", " + millis();
      this.initialRequest();
    }
    println("New joinee made with id: " + this.id);
  }
  Joinee(Client newClient, int port, String header, String id) {
    this.client = newClient;
    this.port = port;
    this.writeHeader = header;
    this.id = id;
    println("New joinee made with set id to: " + this.id);
  }
  
  boolean messageForMe(String message) {
    return this.messageForMe(split(message, ":"));
  }
  boolean messageForMe(String[] splitMessage) {
    if (splitMessage.length >= 3) {
      if (this.id.equals(trim(splitMessage[2]))) {
        return true;
      }
    }
    return false;
  }
  
  void setNewName(String newName, String s1) {
    this.name = "";
    for (char i : newName.toCharArray()) {
      if (textWidth(this.name + i) < textWidth(s1 + " ")) {
        this.name += i;
      }
      else {
        break;
      }
    }
  }
  
  void initialRequest() {
    if ((this.client != null) && (this.client.active())) {
      this.lastPingRequest = millis();
      this.write("Initial Request");
      this.waitingForResponse = true;
    }
  }
  void resolveInitialRequest() {
    this.resolveInitialRequest("", "");
  }
  void resolveInitialRequest(String newName, String s1) {
    if ((this.client != null) && (this.client.active())) {
      if (waitingForResponse && !receivedInitialResponse) {
        this.ping = millis() - this.lastPingRequest;
        this.lastPingRequest = millis();
        this.waitingForResponse = false;
        this.receivedInitialResponse = true;
        this.setNewName(newName, s1);
      }
    }
  }
  
  void pingRequest() {
    if ((this.client != null) && (this.client.active())) {
      this.lastPingRequest = millis();
      this.write("Ping Request");
      this.waitingForResponse = true;
    }
  }
  void resolvePingRequest() {
    if ((this.client != null) && (this.client.active())) {
      if (waitingForResponse) {
        this.ping = millis() - this.lastPingRequest;
        this.lastPingRequest = millis();
        this.waitingForResponse = false;
        this.pingRequestsMissed = 0;
      }
    }
  }
  void missedPingRequest() {
    this.waitingForResponse = false;
    this.ping = millis() - this.lastPingRequest + this.pingRequestsMissed * constants.defaultPingTimeout;
    this.pingRequestsMissed++;
  }
  
  void write(String message) {
    if ((this.client != null) && (this.client.active())) {
      this.client.write("|" + this.writeHeader + message + ": " + this.id);
    }
  }
}
