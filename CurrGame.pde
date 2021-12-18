public enum GameState {
  MAIN_MENU, CONNECTING_TO_LOBBY, SINGLEPLAYER, MULTIPLAYER_LOBBY_HOSTING, MULTIPLAYER_LOBBY_JOINED, MULTIPLAYER_HOSTING, MULTIPLAYER_JOINED;
}

class CurrGame {
  private Game myGame;
  private Game otherGame;
  private Server server;
  private int portHosting;
  private String lobbyName;
  private Joinee otherPlayer = null;
  private ArrayList<Joinee> lobbyClients = new ArrayList<Joinee>();
  private Tetris thisInstance;
  private GameState state = GameState.MAIN_MENU;
  AllButtons buttons = new AllButtons();
  private Queue<String> messageQ = new LinkedList<String>();
  
  CurrGame(Tetris thisInstance) {
    this.thisInstance = thisInstance;
  }
  
  void startSinglePlayerGame() {
    this.myGame = new Game(constants.game1Borders);
    this.state = GameState.SINGLEPLAYER;
  }
  
  void hostTwoPlayerGame() {
    for (int port = constants.portRangeFirst; port <= constants.portRangeLast; port++) {
      try {
        this.server = new Server(this.thisInstance, port);
        if (this.server.active()) {
          this.lobbyName = showInputDialog("Choose a name for this lobby");
          if (this.lobbyName == null) {
            this.server.stop();
            this.server = null;
            return;
          }
          println("Server active on port " + port + " with ip: " + Server.ip() + ".");
          this.portHosting = port;
          this.state = GameState.MULTIPLAYER_LOBBY_HOSTING;
          break;
        }
      } catch(Exception e) {}
    }
    if ((this.server == null) || (!this.server.active())) {
      this.state = GameState.MAIN_MENU;
      println("ERROR: Server not created.");
    }
    this.buttons.clearButton(5);
    this.buttons.clearButton(6);
  }
  
  void showLobbyInfo() {
    if (this.server.active()) {
      showMessageDialog(null, "IP address: " + Server.ip() + "\nPort: " + this.portHosting, "", PLAIN_MESSAGE);
    }
  }
  
  void directConnect() {
    String possibleIP = showInputDialog("Choose IP address");
    if (possibleIP == null) {
      return;
    }
    String portString = showInputDialog("Enter the port number");
    if (portString == null) {
      return;
    }
    int port = 0;
    try {
      port = Integer.valueOf(portString);
    } catch(NumberFormatException e) {
      showMessageDialog(null, "Not a number", "", PLAIN_MESSAGE);
      return;
    }
    if ((port < constants.portRangeFirst) || (port > constants.portRangeLast)) {
      showMessageDialog(null, "Invalid port number", "", PLAIN_MESSAGE);
      return;
    }
    final ArrayList<Joinee> possibleLobby = new ArrayList<Joinee>();
    final Tetris inst = this.thisInstance;
    final String ipTesting = possibleIP;
    final int portTesting = port;
    Thread thread = new Thread(new Runnable() {
      public void run() {
        Client testClient = new Client(inst, ipTesting, portTesting);
        if (!testClient.active()) {
          testClient.stop();
          testClient = null;
          showMessageDialog(null, "Couldn't connect to that IP / port", "", PLAIN_MESSAGE);
          return;
        }
        possibleLobby.add(new Joinee(testClient, portTesting, "LOBBY: "));
      }
    });
    thread.start();
    delay(constants.defaultPingTimeout);
    try {
      thread.interrupt();
    } catch(Exception e) {
      //e.printStackTrace();
    }
    if (possibleLobby.size() > 0) {
      if (possibleLobby.get(0).client.active()) {
        this.otherPlayer = possibleLobby.get(0);
        this.state = GameState.CONNECTING_TO_LOBBY;
      }
    }
  }
  
  void findMultiPlayerGame() {
    rectMode(CORNERS);
    fill(constants.defaultBackgroundColor);
    stroke(constants.defaultBackgroundColor);
    rect(10, 720, 265, 738);
    ArrayList<String> IPs = this.findAddressesOnLAN();
    this.lobbyClients = this.findHosts(IPs);
    if (this.lobbyClients.size() == 0) {
      textSize(13);
      textAlign(LEFT, TOP);
      fill(0);
      text("No games found. Maybe try \"Find IP\"", 10, 723);
    }
  }
  ArrayList<String> findAddressesOnLAN() {
    final ArrayList<String> IPs = new ArrayList<String>();
    final ArrayList<Thread> threads = new ArrayList<Thread>();
    for (int i = 1; i < 255; i++) {
      final int j = i;
      Thread thread1 = new Thread(new Runnable() {
        public void run() {
          try {
            if (InetAddress.getByName("192.168.1." + j).isReachable(constants.defaultPingTimeout)) {
              IPs.add("192.168.1." + j);
            }
          } catch (Exception e) {
            //e.printStackTrace();
          }
        }
      });
      Thread thread2 = new Thread(new Runnable() {
        public void run() {
          try {
            if (InetAddress.getByName("192.168.56." + j).isReachable(constants.defaultPingTimeout)) {
              IPs.add("192.168.56." + j);
            }
          } catch (Exception e) {
            //e.printStackTrace();
          }
        }
      });
      threads.add(thread1);
      threads.add(thread2);
      thread1.start();
      thread2.start();
    }
    Thread localhostThread = new Thread(new Runnable() {
      public void run() {
        try {
          if (InetAddress.getByName("localhost").isReachable(constants.defaultPingTimeout)) {
            IPs.add("localhost");
          }
        } catch (Exception e) {
          //e.printStackTrace();
        }
      }
    });
    threads.add(localhostThread);
    localhostThread.start();
    for(Thread thread : threads) {
      try {
        thread.join();
      } catch(InterruptedException e) {
        //e.printStackTrace();
      }
    }
    return IPs;
  }
  ArrayList<Joinee> findHosts(ArrayList<String> IPs) {
    ArrayList<Thread> threads = new ArrayList<Thread>();
    final ArrayList<Joinee> possibleLobbies = new ArrayList<Joinee>();
    final Tetris inst = this.thisInstance;
    final int[] ports = new int[constants.portRangeLast - constants.portRangeFirst];
    for (int i = 0; i < ports.length; i++) {
      ports[i] = constants.portRangeFirst + i;
    }
    for(final String ip : IPs) {
      for (final int port : ports) {
        Thread thread = new Thread(new Runnable() {
          public void run() {
            try {
              Client testClient = new Client(inst, ip, port);
              if (testClient.active()) {
                possibleLobbies.add(new Joinee(testClient, port, "LOBBY: "));
              }
              else {
                testClient.stop();
                testClient = null;
              }
            } catch(Exception e) {
              //e.printStackTrace();
            } finally {
              try {
              } catch(Exception e) {}
            }
          }
        });
        threads.add(thread);
        thread.start();
      }
    }
    delay(constants.defaultPingTimeout);
    for(Thread thread : threads) {
      try {
        thread.interrupt();
      } catch(Exception e) {
        //e.printStackTrace();
      }
    }
    return possibleLobbies;
  }
  
  void joinSelectedLobby() {
    int index = this.buttons.cSB.getHIGH();
    if ((index < 0) || (index >= this.lobbyClients.size())) {
      println("ERROR: selected lobby " + index + " but only " + this.lobbyClients.size() + " lobbies exist.");
      return;
    }
    this.otherPlayer = this.lobbyClients.get(this.buttons.cSB.getHIGH());
    this.otherPlayer.write("Join Lobby");
  }
  
  void clientConnects(Client someClient) {
    println("Client connected with IP address: " + someClient.ip());
    if (this.state == GameState.MULTIPLAYER_LOBBY_HOSTING) {
      this.lobbyClients.add(new Joinee(someClient, portHosting, "LOBBY: "));
    }
  }
  void clientDisconnects(Client someClient) {
    println("Client with IP address " + someClient.ip() + " has disconnected.");
    if (this.state == GameState.MULTIPLAYER_LOBBY_HOSTING) {
      for(int i = 0; i < this.lobbyClients.size(); i++) {
        if (this.lobbyClients.get(i).id.equals(someClient.ip())) {
          this.lobbyClients.remove(i);
          i--;
        }
      }
    }
  }
  void leaveLobby() {
    if (this.state == GameState.MULTIPLAYER_LOBBY_JOINED) {
      this.otherPlayer.write("Quit Lobby");
      this.otherPlayer.client.stop();
    }
    if (this.state == GameState.MULTIPLAYER_LOBBY_HOSTING) {
      this.server.write("LOBBY: Quit Lobby|");
      this.server.stop();
    }
    this.state = GameState.MAIN_MENU;
  }
  
  void kickPlayer() {
    if (this.otherPlayer == null) {
      println("No client to kick.");
    }
    else {
      println("Kicked client with ID: " + this.otherPlayer.id);
      this.server.write("LOBBY: Kick Player|");
      this.server.disconnect(this.otherPlayer.client);
      this.otherPlayer = null;
    }
  }
  
  void startGame() {
    this.myGame = new Game(constants.game1Borders);
    this.otherGame = new Game(constants.game2Borders);
    this.state = GameState.MULTIPLAYER_HOSTING;
    this.server.write("LOBBY: Start Game|");
  }
  
  void clientEvent(Client someClient) {
    String[] messages = split(someClient.readString(), '|');
    for(String message : messages) {
      message = trim(message);
      if (message.equals("")) {
        continue;
      }
      this.messageQ.add(message);
    }
  }
  
  int getJoineeIndex(String[] splitMessage) {
    if (splitMessage.length < 3) {
      return -1;
    }
    for(int i = 0; i < this.lobbyClients.size(); i++) {
      if (this.lobbyClients.get(i).id.equals(trim(splitMessage[2]))) {
        return i;
      }
    }
    return -1;
  }
  
  void update() {
    this.buttons.update(this.state);
    switch(this.state) {
      case MAIN_MENU:
        if (this.lobbyClients.size() == 0) {
          break;
        }
        rectMode(CORNERS);
        fill(constants.defaultBackgroundColor);
        stroke(constants.defaultBackgroundColor);
        rect(10, 720, 265, 738);
        textSize(13);
        textAlign(LEFT, TOP);
        fill(0);
        float currY = 725;
        String s1 = "Game             ";
        String s2 = "IP Address         ";
        float width1 = textWidth(s1);
        float width2 = textWidth(s1 + s2);
        text(s1 + s2 + "Ping", 10, currY);
        textSize(12);
        String[] listBarStrings = new String[0];
        for(int i = 0; i < this.lobbyClients.size(); i++) {
          Joinee j = this.lobbyClients.get(i);
          if ((j == null) || (j.client == null)) {
            this.lobbyClients.remove(i);
            i--;
            continue;
          }
          boolean displayMessage = true;
          boolean removeClient = false;
          if (j.client.active()) {
            if (j.waitingForResponse) {
              if (j.ping < 0) {
                displayMessage = false;
              }
              if (millis() - j.lastPingRequest > constants.defaultPingTimeout) {
                if (!j.receivedInitialResponse) {
                  removeClient = true;
                }
                j.ping = millis() - j.lastPingRequest;
                if (j.ping > constants.defaultPingTimeout * 2) {
                  removeClient = true;
                }
              }
            }
            else if (millis() - j.lastPingRequest > constants.pingRequestFrequency) {
              j.pingRequest();
            }
          }
          else {
            removeClient = true;
          }
          if (removeClient) {
            j.client.stop();
            this.lobbyClients.remove(i);
            i--;
          }
          else if (displayMessage) {
            int firstGap = round((width1 - textWidth(j.name + " ")) / textWidth(" "));
            int secondGap = round((width2 - textWidth(j.name + " " + multiplyString(" ", firstGap) + j.id + ": ")) / textWidth(" "));
            listBarStrings = append(listBarStrings, j.name + " " + multiplyString(" ", firstGap) + j.id + " " + multiplyString(" ", secondGap) + j.ping + " ms");
          }
        }
        this.buttons.cSB.setSTR(listBarStrings);
        break;
      case CONNECTING_TO_LOBBY:
        if (!this.otherPlayer.client.active()) {
          this.otherPlayer.client.stop();
          this.otherPlayer = null;
          this.state = GameState.MAIN_MENU;
          break;
        }
        if (this.otherPlayer.receivedInitialResponse) {
          this.otherPlayer.write("Join Lobby");
          break;
        }
        break;
      case SINGLEPLAYER:
        if (this.myGame.isOver()) {
          //
        } else {
          this.myGame.update();
        }
        break;
      case MULTIPLAYER_HOSTING:
        String myGameChanges = this.myGame.update("| HOST_GAME: ");
        String otherGameChanges = this.otherGame.update("| JOINEE_GAME: ");
        if (!myGameChanges.equals("")) {
          this.server.write(myGameChanges);
        }
        if (!otherGameChanges.equals("")) {
          this.server.write(otherGameChanges);
        }
        break;
      case MULTIPLAYER_JOINED:
        break;
      default:
        break;
    }
    while(this.messageQ.peek() != null) {
      String message = this.messageQ.remove();
      String[] splitMessage = split(message, ':');
      if (splitMessage.length < 2) {
        println("ERROR: invalid message: " + splitMessage);
        continue;
      }
      int index = this.getJoineeIndex(splitMessage);
      switch(this.state) {
        case MAIN_MENU:
          switch(trim(splitMessage[0])) {
            case "LOBBY":
              if (index == -1) {
                println("ERROR: client ID not found.");
                break;
              }
              switch(trim(splitMessage[1])) {
                case "Ping Resolve":
                  this.lobbyClients.get(index).resolvePingRequest();
                  break;
                case "Initial Resolve":
                  if (splitMessage.length < 5) {
                    println("ERROR: initial resolve message invalid");
                    break;
                  }
                  this.lobbyClients.get(index).resolveInitialRequest(trim(splitMessage[3]), splitMessage[4]);
                  break;
                case "Join Lobby":
                  this.otherPlayer = this.lobbyClients.get(index);
                  this.lobbyClients.clear();
                  this.messageQ.clear();
                  this.state = GameState.MULTIPLAYER_LOBBY_JOINED;
                  break;
                case "Lobby Full":
                  showMessageDialog(null, "Lobby already has a player", "", PLAIN_MESSAGE);
                  break;
                default:
                  println("ERROR: LOBBY message not recognized -> " + trim(splitMessage[1]));
                  break;
              }
              break;
            default:
              println("ERROR: message header not recognized -> " + trim(splitMessage[0]));
              break;
          }
          break;
        case CONNECTING_TO_LOBBY:
          switch(trim(splitMessage[0])) {
            case "LOBBY":
              switch(trim(splitMessage[1])) {
                case "Ping Resolve":
                  this.otherPlayer.resolvePingRequest();
                  break;
                case "Initial Resolve":
                  if (splitMessage.length < 5) {
                    println("ERROR: initial resolve message invalid");
                    break;
                  }
                  this.otherPlayer.resolveInitialRequest(trim(splitMessage[3]), splitMessage[4]);
                  break;
                case "Join Lobby":
                  this.lobbyClients.clear();
                  this.messageQ.clear();
                  this.state = GameState.MULTIPLAYER_LOBBY_JOINED;
                  break;
                default:
                  println("ERROR: LOBBY message not recognized -> " + trim(splitMessage[1]));
                  break;
              }
              break;
            default:
              println("ERROR: message header not recognized -> " + trim(splitMessage[0]));
              break;
          }
          break;
        case MULTIPLAYER_LOBBY_HOSTING:
          switch(trim(splitMessage[0])) {
            case "LOBBY":
              switch(trim(splitMessage[1])) {
                case "Quit Lobby":
                  println("Your client quit the lobby");
                  if (this.otherPlayer != null) {
                    if (this.otherPlayer.client != null) {
                      this.otherPlayer.client.stop();
                    }
                    this.otherPlayer = null;
                  }
                  break;
                case "Ping Request":
                  if (splitMessage.length < 3) {
                    println("ERROR: No IP address for ping request");
                    break;
                  }
                  this.server.write("LOBBY: Ping Resolve: " + trim(splitMessage[2]) + "|");
                  break;
                case "Initial Request":
                  if (splitMessage.length < 3) {
                    println("ERROR: No IP address for initial request");
                    break;
                  }
                  this.server.write("LOBBY: Initial Resolve: " + trim(splitMessage[2]) + ": " + this.lobbyName + ":Game             :|");
                  break;
                case "Join Lobby":
                  if (splitMessage.length < 3) {
                    println("ERROR: No IP address for ping request");
                    break;
                  }
                  if (this.otherPlayer == null) {
                    if (index == -1) {
                      println("ERROR: client ID not found.");
                      break;
                    }
                    this.server.write("LOBBY: Join Lobby: " + trim(splitMessage[2]) + "|");
                    this.otherPlayer = this.lobbyClients.get(index);
                  }
                  else {
                    this.server.write("LOBBY: Lobby Full: " + trim(splitMessage[2]) + "|");
                  }
                  break;
                default:
                  println("ERROR: LOBBY message not recognized -> " + trim(splitMessage[1]));
                  break;
              }
              break;
            default:
              println("ERROR: message header not recognized -> " + trim(splitMessage[0]));
              break;
          }
          break;
        case MULTIPLAYER_LOBBY_JOINED:
          switch(trim(splitMessage[0])) {
            case "LOBBY":
              switch(trim(splitMessage[1])) {
                case "Start Game":
                  println("Game is starting");
                  this.myGame = new Game(constants.game1Borders);
                  this.otherGame = new Game(constants.game2Borders);
                  this.messageQ.clear();
                  this.state = GameState.MULTIPLAYER_JOINED;
                  break;
                case "Kick Player":
                  println("You were kicked from the lobby");
                  this.otherPlayer = null;
                  this.messageQ.clear();
                  this.state = GameState.MAIN_MENU;
                  break;
                case "Quit Lobby":
                  println("The host quit the lobby");
                  this.otherPlayer = null;
                  this.messageQ.clear();
                  this.state = GameState.MAIN_MENU;
                  break;
                default:
                  println("ERROR: LOBBY message not recognized -> " + trim(splitMessage[1]));
                  break;
              }
              break;
            default:
              println("ERROR: message header not recognized -> " + trim(splitMessage[0]));
              break;
          }
          break;
        case MULTIPLAYER_HOSTING:
          switch(trim(splitMessage[0])) {
            case "HOST_GAME":
              if (this.myGame.executeMessage(trim(splitMessage[1]))) {
                this.server.write(message);
              }
              else {
                println("ERROR: HOST_GAME message not recognized -> " + trim(splitMessage[1]));
              }
              break;
            case "JOINEE_GAME":
              if (this.otherGame.executeMessage(trim(splitMessage[1]))) {
                this.server.write(message);
              }
              else {
                println("ERROR: JOINEE_GAME message not recognized -> " + trim(splitMessage[1]));
              }
              break;
            default:
              println("ERROR: message header not recognized -> " + trim(splitMessage[0]));
              println(this.state + message);
              break;
          }
          break;
        case MULTIPLAYER_JOINED:
          switch(trim(splitMessage[0])) {
            case "HOST_GAME":
              if (!this.otherGame.executeMessage(trim(splitMessage[1]))) {
                println("ERROR: HOST_GAME message not recognized -> " + trim(splitMessage[1]));
              }
              break;
            case "JOINEE_GAME":
              if (!this.myGame.executeMessage(trim(splitMessage[1]))) {
                println("ERROR: JOINEE_GAME message not recognized -> " + trim(splitMessage[1]));
              }
              break;
            default:
              println("ERROR: message header not recognized -> " + trim(splitMessage[0]));
              break;
          }
          break;
        default:
          break;
      }
    }
  }
  
  void keyPress() {
    switch(this.state) {
      case SINGLEPLAYER:
        this.myGame.pressedKey();
        break;
      case MULTIPLAYER_HOSTING:
        String myGameChanges = this.myGame.pressedKey("| HOST_GAME: ");
        if (!myGameChanges.equals("")) {
          this.server.write(myGameChanges);
        }
        break;
      case MULTIPLAYER_JOINED:
        String gameChanges = this.myGame.pressedKey("| JOINEE_GAME: ", false);
        if (!gameChanges.equals("")) {
          this.otherPlayer.write(gameChanges);
        }
        break;
      default:
        break;
    }
  }
  
  void mousePress() {
    this.buttons.mousePress(this.state);
  }
  void mouseRelease() {
    this.buttons.mouseRelease(this.state);
  }
  void scroll(int count) {
    this.buttons.scroll(count);
  }
}
