public enum GameState {
  MAIN_MENU, OPTIONS, CONNECTING_TO_LOBBY, SINGLEPLAYER, MULTIPLAYER_LOBBY_HOSTING, MULTIPLAYER_LOBBY_JOINED, MULTIPLAYER_HOSTING, MULTIPLAYER_JOINED;
}

class CurrGame {
  private User user;
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
  private boolean[] wantRematch = new boolean[2];
  private boolean paused = false;
  
  CurrGame(Tetris thisInstance) {
    this.thisInstance = thisInstance;
  }
  void initiateUser() {
    if (options.defaultUsername == null) {
      if (options.usernames.size() == 0) {
        this.user = createNewUser(options.usernames);
      } else {
        this.user = options.chooseDefaultUsername();
      }
      this.user.saveUser();
      options.defaultUsername = this.user.name;
      options.saveOptions();
      return;
    }
    String[] userFile = loadStrings("data/users/" + options.defaultUsername + ".user.tetris");
    if (userFile == null) {
      if (options.usernames.size() == 0) {
        this.user = createNewUser(options.usernames);
      } else {
        this.user = options.chooseDefaultUsername();
      }
      this.user.saveUser();
      options.defaultUsername = this.user.name;
      options.saveOptions();
      return;
    }
    this.user = new User(options.defaultUsername);
    for (String line : userFile) {
      String[] splitLine = split(line, ":");
      String value = trim(splitLine[1]);
      switch(trim(splitLine[0])) {
        default:
          break;
      }
    }
  }
  
  void goToMainMenu() {
    this.buttons.cSB.clearSTRS();
    this.buttons.clearButton(0);
    this.state = GameState.MAIN_MENU;
  }
  
  void startSinglePlayerGame() {
    this.myGame = new Game(constants.game1Borders);
    this.state = GameState.SINGLEPLAYER;
    this.buttons.clearButtons(new int[]{0, 5, 6});
  }
  
  void pauseGame() {
    if (this.paused) {
      this.paused = false;
      this.myGame.clearGameOverMessage();
      this.buttons.pgB.setMES("Pause Game");
      this.buttons.pgB.defaultColors();
    }
    else {
      this.paused = true;
      this.myGame.addGameOverMessage("Game", "Paused");
      this.buttons.pgB.setMES("Resume");
      this.buttons.pgB.changeColor(color(120), color(0), color(170), color(220));
    }
  }
  
  void endGame() {
    if (showConfirmDialog(null, "Are you sure you want to quit this game?", "Tetris", YES_NO_OPTION, PLAIN_MESSAGE) == YES_OPTION) {
      this.myGame = null;
      this.goToMainMenu();
    }
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
          this.lobbyName += ": " + millis();
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
    if (this.server != null && this.server.active()) {
      showMessageDialog(null, "IP address: " + Server.ip() + "\nPort: " + this.portHosting, "", PLAIN_MESSAGE);
    }
    else {
      showMessageDialog(null, "IP address: " + this.otherPlayer.id + "\nPort: " + this.otherPlayer.port, "", PLAIN_MESSAGE);
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
    // First search for IPs
    ArrayList<String> IPs = this.findAddressesOnLAN();
    // Then search for open ports
    this.lobbyClients = this.findHosts(IPs);
    // Reset timers so clients not removed before initial ping request comes in
    for (int i = 0; i < this.lobbyClients.size(); i++) {
      this.lobbyClients.get(i).lastPingRequest = millis();
    }
    // Message if no connections found
    rectMode(CORNERS);
    fill(constants.defaultBackgroundColor);
    stroke(constants.defaultBackgroundColor);
    rect(10, 720, 265, 738);
    if (this.lobbyClients.size() == 0) {
      textSize(13);
      textAlign(LEFT, TOP);
      fill(0);
      text("No connections found. Try \"Find IP\"", 10, 723);
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
    for(Thread thread : threads) {
      try {
        thread.join();
      } catch(InterruptedException e) {
        thread.interrupt();
        //e.printStackTrace();
      }
    }
    println("IPs found: " + IPs);
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
                println("Connected at " + ip + " with port " + port + ".");
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
    for(Thread thread : threads) {
      try {
        thread.join();
      } catch(Exception e) {
        thread.interrupt();
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
    this.lobbyClients.get(this.buttons.cSB.getHIGH()).write("Join Lobby");
  }
  
  void toggleRematch() {
    this.wantRematch[0] = !this.wantRematch[0];
    if (this.wantRematch[0]) {
      this.buttons.paB.setMES("Offer Sent!");
      this.buttons.paB.changeColor(color(0, 255, 0), color(0), color(200, 100, 100), color(255, 40, 20));
      this.buttons.paB.setMOMES("Cancel");
      if (this.state == GameState.MULTIPLAYER_HOSTING) {
        this.server.write("| LOBBY: Host Rematch Sent");
        this.checkRematches();
      }
      if (this.state == GameState.MULTIPLAYER_JOINED) {
        this.otherPlayer.write("Joinee Rematch Sent");
      }
    }
    else {
      this.buttons.paB.setMES("Canceled");
      this.buttons.paB.changeColor(color(255, 10, 10), color(0), color(100, 200, 100), color(40, 255, 20));
      this.buttons.paB.setMOMES("Resend Offer");
      if (this.state == GameState.MULTIPLAYER_HOSTING) {
        this.server.write("| LOBBY: Host Rematch Revoked");
      }
      if (this.state == GameState.MULTIPLAYER_JOINED) {
        this.otherPlayer.write("Joinee Rematch Revoked");
      }
    }
  }
  void checkRematches() {
    if ((this.wantRematch[0]) && (this.wantRematch[1])) {
      if (this.state == GameState.MULTIPLAYER_HOSTING) {
        this.buttons.paB = new playAgainButton();
        this.wantRematch[0] = false;
        this.wantRematch[1] = false;
        this.myGame = new Game(constants.game1Borders);
        this.otherGame = new Game(constants.game2Borders);
        for (Joinee j : this.lobbyClients) {
          if (j != null) {
            if (j.client != null) {
              j.client.stop();
            }
          }
        }
        this.server.write("| LOBBY: Start Game");
      }
    }
  }
  
  void clientConnects(Client someClient) {
    println("Client connected with IP address: " + someClient.ip());
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
    switch(this.state) {
      case MULTIPLAYER_LOBBY_HOSTING:
      case MULTIPLAYER_HOSTING:
        if (this.server != null) {
          this.server.write("| LOBBY: Quit Lobby");
          this.server.stop();
          this.server = null;
        }
      case MULTIPLAYER_LOBBY_JOINED:
      case MULTIPLAYER_JOINED:
        if (this.otherPlayer != null) {
          this.otherPlayer.write("Quit Lobby");
          this.otherPlayer.client.stop();
          this.otherPlayer = null;
        }
        for (int i = 0; i < this.lobbyClients.size(); i++) {
          if (this.lobbyClients.get(i) != null) {
            this.lobbyClients.get(i).client.stop();
            this.lobbyClients.get(i).client = null;
          }
        }
        this.buttons.cSB.clearSTRS();
        this.buttons.clearButton(0);
        this.lobbyClients.clear();
        break;
      default:
        println("ERROR: Leave lobby button pressed but state not recognized");
        break;
    }
    this.goToMainMenu();
  }
  
  void kickPlayer() {
    if (this.otherPlayer == null) {
      println("No client to kick.");
    }
    else {
      println("Kicked client with ID: " + this.otherPlayer.id);
      this.server.write("| LOBBY: Kick Player");
      this.server.disconnect(this.otherPlayer.client);
      this.otherPlayer = null;
    }
  }
  
  void startGame() {
    this.myGame = new Game(constants.game1Borders);
    this.otherGame = new Game(constants.game2Borders);
    this.state = GameState.MULTIPLAYER_HOSTING;
    for (Joinee j : this.lobbyClients) {
      if (j != null) {
        if (j.client != null) {
          j.client.stop();
        }
      }
    }
    this.server.write("| LOBBY: Start Game");
  }
  
  void clientEvent(Client someClient) {
    String[] messages = split(someClient.readString(), '|');
    for(String message : messages) {
      message = trim(message);
      if (message.equals("")) {
        continue;
      }
      if (this.state == GameState.MULTIPLAYER_LOBBY_HOSTING) {
        if (message.contains("LOBBY: Initial Request: ")) {
          if (split(message, ":").length > 2) {
            this.lobbyClients.add(new Joinee(someClient, this.portHosting, "LOBBY: ", trim(split(message, ":")[2])));
          }
        }
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
    String[] lbStrings;
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
        lbStrings = new String[0];
        for(int i = 0; i < this.lobbyClients.size(); i++) {
          Joinee j = this.lobbyClients.get(i);
          if ((j == null) || (j.client == null)) {
            println("A client was null and was removed.");
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
                  println("Client with id " + j.id + " has not received initial ping resolve so was removed");
                  removeClient = true;
                }
                j.missedPingRequest();
                if (j.pingRequestsMissed > constants.maxPingRequestsMissed) {
                  println("Client with id " + j.id + " has missed " + j.pingRequestsMissed + " ping requests so was removed.");
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
            int secondGap = round((width2 - textWidth(j.name + " " + multiplyString(" ", firstGap) + j.client.ip() + ": ")) / textWidth(" "));
            lbStrings = append(lbStrings, j.name + " " + multiplyString(" ", firstGap) + j.client.ip() + " " + multiplyString(" ", secondGap) + j.ping + " ms");
          }
        }
        this.buttons.cSB.setSTR(lbStrings);
        break;
      case CONNECTING_TO_LOBBY:
        if (!this.otherPlayer.client.active()) {
          this.otherPlayer.client.stop();
          this.otherPlayer = null;
          this.goToMainMenu();
          break;
        }
        if (this.otherPlayer.receivedInitialResponse && !this.otherPlayer.waitingForResponse) {
          this.otherPlayer.write("Join Lobby");
          this.otherPlayer.waitingForResponse = true;
          break;
        }
        break;
      case SINGLEPLAYER:
        if (this.paused) {
          return;
        }
        if (this.myGame.isOver()) {
          this.myGame = null;
          this.goToMainMenu();
          break;
        }
        this.myGame.update();
        break;
      case MULTIPLAYER_LOBBY_HOSTING:
        rectMode(CORNERS);
        fill(constants.defaultBackgroundColor);
        stroke(constants.defaultBackgroundColor);
        rect(10, 720, 265, 738);
        textSize(13);
        textAlign(LEFT, TOP);
        fill(0);
        text("Players in lobby", 10, 725);
        lbStrings = new String[0];
        lbStrings = append(lbStrings, "You (0 ms)");
        boolean removeClient = false;
        if (this.otherPlayer != null) {
          if (this.otherPlayer.client.active()) {
            lbStrings = append(lbStrings, "Other player (" + this.otherPlayer.ping + " ms)");
            if (this.otherPlayer.waitingForResponse) {
              if (millis() - this.otherPlayer.lastPingRequest > constants.defaultPingTimeout) {
                this.otherPlayer.missedPingRequest();
                if (this.otherPlayer.pingRequestsMissed > constants.maxPingRequestsMissed) {
                  removeClient = true;
                }
              }
            }
            else if (millis() - this.otherPlayer.lastPingRequest > constants.pingRequestFrequency) {
              this.otherPlayer.pingRequest();
            }
          }
          else {
            removeClient = true;
          }
        }
        if (removeClient) {
          this.otherPlayer.client.stop();
          this.otherPlayer = null;
        }
        this.buttons.cSB.setSTR(lbStrings);
        break;
      case MULTIPLAYER_LOBBY_JOINED:
        rectMode(CORNERS);
        fill(constants.defaultBackgroundColor);
        stroke(constants.defaultBackgroundColor);
        rect(10, 720, 265, 738);
        textSize(13);
        textAlign(LEFT, TOP);
        fill(0);
        text("Connection to lobby", 10, 725);
        lbStrings = new String[0];
        boolean leaveLobby = false;
        if (this.otherPlayer != null) {
          if (this.otherPlayer.client.active()) {
            lbStrings = append(lbStrings, trim(split(this.otherPlayer.name, ":")[0]) + "  (" + this.otherPlayer.ping + " ms)");
            if (this.otherPlayer.waitingForResponse) {
              if (millis() - this.otherPlayer.lastPingRequest > constants.defaultPingTimeout) {
                if (!this.otherPlayer.receivedInitialResponse) {
                  leaveLobby = true;
                }
                this.otherPlayer.missedPingRequest();
                if (this.otherPlayer.pingRequestsMissed > constants.maxPingRequestsMissed) {
                  leaveLobby = true;
                }
              }
            }
            else if (millis() - this.otherPlayer.lastPingRequest > constants.pingRequestFrequency) {
              this.otherPlayer.pingRequest();
            }
          }
          else {
            leaveLobby = true;
          }
          if (leaveLobby) {
            this.otherPlayer.client.stop();
            this.otherPlayer = null;
            showMessageDialog(null, "You lost connection to the lobby", "", PLAIN_MESSAGE);
            this.goToMainMenu();
          }
        }
        else {
          showMessageDialog(null, "There was an error connecting to the lobby", "", PLAIN_MESSAGE);
          this.goToMainMenu();
        }
        this.buttons.cSB.setSTR(lbStrings);
        break;
      case MULTIPLAYER_HOSTING:
        boolean hostGameOver = this.myGame.gameOver;
        boolean joineeGameOver = this.otherGame.gameOver;
        String myGameChanges = this.myGame.update("| HOST_GAME: ", false);
        String otherGameChanges = this.otherGame.update("| JOINEE_GAME: ", false);
        // check for game overs
        if ((!hostGameOver) && (this.myGame.gameOver)) {
          this.buttons.paB.setDIS(false);
          this.buttons.paB.changeColor(color(220), color(0), color(170), color(120));
          if (!joineeGameOver) {
            this.myGame.addGameOverMessage("You", "Lost");
            this.myGame.drawBoard();
            myGameChanges += "| HOST_GAME: addGameOverMessage=You, Lost";
            this.otherGame.addGameOverMessage("You", "Won");
            otherGameChanges += "| JOINEE_GAME: addGameOverMessage=You, Won";
          }
        }
        if ((!joineeGameOver) && (this.otherGame.gameOver)) {
          if (!hostGameOver) {
            this.myGame.addGameOverMessage("You", "Won");
            myGameChanges += "| HOST_GAME: addGameOverMessage=You, Won";
            this.otherGame.addGameOverMessage("You", "Lost");
            this.otherGame.drawBoard();
            otherGameChanges += "| JOINEE_GAME: addGameOverMessage=You, Lost";
          }
        }
        if (!myGameChanges.equals("")) {
          this.server.write(myGameChanges);
        }
        if (!otherGameChanges.equals("")) {
          this.server.write(otherGameChanges);
        }
        rectMode(CORNERS);
        fill(constants.defaultBackgroundColor);
        stroke(constants.defaultBackgroundColor);
        rect(10, 720, 265, 738);
        textSize(13);
        textAlign(LEFT, TOP);
        fill(0);
        text("Connection to Players", 10, 725);
        lbStrings = new String[0];
        boolean kickOpponent = false;
        if (this.otherPlayer != null) {
          if (this.otherPlayer.client.active()) {
            lbStrings = append(lbStrings, "Other Player (" + this.otherPlayer.ping + " ms)");
            if (this.otherPlayer.waitingForResponse) {
              if (millis() - this.otherPlayer.lastPingRequest > constants.defaultPingTimeout) {
                this.otherPlayer.missedPingRequest();
                if (this.otherPlayer.pingRequestsMissed > constants.maxPingRequestsMissed) {
                  kickOpponent = true;
                }
              }
            }
            else if (millis() - this.otherPlayer.lastPingRequest > constants.pingRequestFrequency) {
              this.otherPlayer.pingRequest();
            }
          }
          else {
            kickOpponent = true;
          }
          if (kickOpponent) {
            this.otherPlayer.client.stop();
            this.otherPlayer = null;
            showMessageDialog(null, "You lost connection to your opponent", "", PLAIN_MESSAGE);
            this.goToMainMenu();
          }
        }
        this.buttons.cSB.setSTR(lbStrings);
        break;
      case MULTIPLAYER_JOINED:
        rectMode(CORNERS);
        fill(constants.defaultBackgroundColor);
        stroke(constants.defaultBackgroundColor);
        rect(10, 720, 265, 738);
        textSize(13);
        textAlign(LEFT, TOP);
        fill(0);
        text("Connection to Host", 10, 725);
        lbStrings = new String[0];
        boolean leaveGame = false;
        if (this.otherPlayer != null) {
          if (this.otherPlayer.client.active()) {
            lbStrings = append(lbStrings, trim(split(this.otherPlayer.name, ":")[0]) + " (" + this.otherPlayer.ping + " ms)");
            if (this.otherPlayer.waitingForResponse) {
              if (millis() - this.otherPlayer.lastPingRequest > constants.defaultPingTimeout) {
                this.otherPlayer.missedPingRequest();
                if (this.otherPlayer.pingRequestsMissed > constants.maxPingRequestsMissed) {
                  leaveGame = true;
                }
              }
            }
            else if (millis() - this.otherPlayer.lastPingRequest > constants.pingRequestFrequency) {
              this.otherPlayer.pingRequest();
            }
          }
          else {
            leaveGame = true;
          }
          if (leaveGame) {
            this.otherPlayer.client.stop();
            this.otherPlayer = null;
            showMessageDialog(null, "You lost connection to the game", "", PLAIN_MESSAGE);
            this.goToMainMenu();
          }
        }
        else {
          showMessageDialog(null, "There was an error connecting to the game", "", PLAIN_MESSAGE);
          this.goToMainMenu();
        }
        this.buttons.cSB.setSTR(lbStrings);
        break;
      default:
        break;
    }
    while(this.messageQ.peek() != null) {
      String message = this.messageQ.remove();
      println("time: " + millis() + "\n  " + message);
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
                  if (this.lobbyClients.get(index).messageForMe(splitMessage)) {
                    this.lobbyClients.get(index).resolvePingRequest();
                  }
                  break;
                case "Initial Resolve":
                  if (splitMessage.length < 5) {
                    println("ERROR: initial resolve message invalid");
                    break;
                  }
                  if (this.lobbyClients.get(index).messageForMe(splitMessage)) {
                    boolean duplicateConnection = false;
                    for (int i = 0; i < this.lobbyClients.size(); i++) {
                      if (i == index) {
                        continue;
                      }
                      Joinee j = lobbyClients.get(i);
                      if ((j.receivedInitialResponse) && (j.name.equals(trim(splitMessage[3])))) {
                        duplicateConnection = true;
                        break;
                      }
                    }
                    if (duplicateConnection) {
                      println("Duplicate connection so removing " + this.lobbyClients.get(index).name);
                      this.lobbyClients.get(index).client.stop();
                      this.lobbyClients.remove(index);
                    }
                    else {
                      this.lobbyClients.get(index).resolveInitialRequest(trim(splitMessage[3]), splitMessage[4]);
                    }
                  }
                  break;
                case "Join Lobby":
                  if (this.lobbyClients.get(index).messageForMe(splitMessage)) {
                    this.otherPlayer = this.lobbyClients.get(index);
                    this.lobbyClients.remove(index);
                    for (int i = 0; i < this.lobbyClients.size(); i++) {
                      this.lobbyClients.get(i).client.stop();
                    }
                    this.lobbyClients.clear();
                    this.messageQ.clear();
                    this.buttons.clearButton(0);
                    this.buttons.clearButton(6);
                    this.buttons.cSB.setHIGH(-1);
                    this.state = GameState.MULTIPLAYER_LOBBY_JOINED;
                  }
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
                  if (this.otherPlayer.messageForMe(splitMessage)) {
                    this.otherPlayer.resolvePingRequest();
                  }
                  break;
                case "Initial Resolve":
                  if (splitMessage.length < 5) {
                    println("ERROR: initial resolve message invalid");
                    break;
                  }
                  if (this.otherPlayer.messageForMe(splitMessage)) {
                    this.otherPlayer.resolveInitialRequest(trim(splitMessage[3]), splitMessage[4]);
                  }
                  break;
                case "Initial Request":
                  if (this.otherPlayer.messageForMe(splitMessage)) {
                    if (splitMessage.length < 3) {
                      println("ERROR: No IP address for initial request");
                      break;
                    }
                    this.otherPlayer.write("Initial Resolve");
                  }
                  break;
                case "Join Lobby":
                  if (this.otherPlayer.messageForMe(splitMessage)) {
                    this.lobbyClients.clear();
                    this.messageQ.clear();
                    this.otherPlayer.waitingForResponse = false;
                    this.state = GameState.MULTIPLAYER_LOBBY_JOINED;
                    this.buttons.clearButton(5);
                    this.buttons.clearButton(6);
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
                  this.server.write("| LOBBY: Ping Resolve: " + trim(splitMessage[2]));
                  break;
                case "Ping Resolve":
                  if (this.otherPlayer != null) {
                    if (this.otherPlayer.messageForMe(splitMessage)) {
                      this.otherPlayer.resolvePingRequest();
                    }
                  }
                  break;
                case "Initial Request":
                  if (splitMessage.length < 3) {
                    println("ERROR: No IP address for initial request");
                    break;
                  }
                  this.server.write("| LOBBY: Initial Resolve: " + trim(splitMessage[2]) + ": " + this.lobbyName + ":Game             :");
                  break;
                case "Initial Resolve":
                  if (splitMessage.length < 3) {
                    println("ERROR: initial resolve message invalid");
                    break;
                  }
                  if (this.otherPlayer != null) {
                    if (this.otherPlayer.messageForMe(splitMessage)) {
                      this.otherPlayer.resolveInitialRequest();
                    }
                  }
                  break;
                case "Join Lobby":
                  if (splitMessage.length < 3) {
                    println("ERROR: No joinee id specified");
                    break;
                  }
                  if (this.otherPlayer == null) {
                    if (index == -1) {
                      println("ERROR: client ID not found.");
                      break;
                    }
                    this.server.write("| LOBBY: Join Lobby: " + trim(splitMessage[2]));
                    this.otherPlayer = this.lobbyClients.get(index);
                    this.lobbyClients.remove(index);
                  }
                  else {
                    this.server.write("| LOBBY: Lobby Full: " + trim(splitMessage[2]));
                  }
                  break;
                case "Chat":
                  if (splitMessage.length < 4) {
                    println("ERROR: no chat string to add");
                    break;
                  }
                  this.buttons.lcB.addChat(trim(splitMessage[2]), trim(splitMessage[3]));
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
                  this.goToMainMenu();
                  break;
                case "Quit Lobby":
                  println("The host quit the lobby");
                  this.otherPlayer = null;
                  this.messageQ.clear();
                  this.goToMainMenu();
                  break;
                case "Initial Request":
                  if (this.otherPlayer.messageForMe(splitMessage)) {
                    if (splitMessage.length < 3) {
                      println("ERROR: No IP address for initial request");
                      break;
                    }
                    this.otherPlayer.write("Initial Resolve");
                  }
                  break;
                case "Ping Request":
                  if (this.otherPlayer.messageForMe(splitMessage)) {
                    if (splitMessage.length < 3) {
                      println("ERROR: No IP address for ping request");
                      break;
                    }
                    this.otherPlayer.write("Ping Resolve");
                  }
                  break;
                case "Ping Resolve":
                  if (this.otherPlayer.messageForMe(splitMessage)) {
                    this.otherPlayer.resolvePingRequest();
                  }
                  break;
                case "Chat":
                  if (splitMessage.length < 4) {
                    println("ERROR: no chat string to add");
                    break;
                  }
                  this.buttons.lcB.addChat(trim(splitMessage[2]), trim(splitMessage[3]));
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
            case "LOBBY":
              switch(trim(splitMessage[1])) {
                case "Quit Lobby":
                  println("Your opponent quit the game");
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
                  this.server.write("| LOBBY: Ping Resolve: " + trim(splitMessage[2]));
                  break;
                case "Ping Resolve":
                  if (this.otherPlayer.messageForMe(splitMessage)) {
                    this.otherPlayer.resolvePingRequest();
                  }
                  break;
                case "Joinee Rematch Sent":
                  this.wantRematch[1] = true;
                  this.checkRematches();
                  break;
                case "Joinee Rematch Revoked":
                  this.wantRematch[1] = false;
                  break;
                case "Chat":
                  if (splitMessage.length < 4) {
                    println("ERROR: no chat string to add");
                    break;
                  }
                  this.buttons.lcB.addChat(trim(splitMessage[2]), trim(splitMessage[3]));
                  break;
                default:
                  println("ERROR: LOBBY message not recognized -> " + trim(splitMessage[1]));
                  break;
              }
              break;
            case "HOST_GAME":
              if (this.myGame.executeMessage(trim(splitMessage[1]))) {
                this.server.write("|" + message);
              }
              else {
                println("ERROR: HOST_GAME message not recognized -> " + trim(splitMessage[1]));
              }
              break;
            case "JOINEE_GAME":
              if (this.otherGame.executeMessage(trim(splitMessage[1]))) {
                this.server.write("|" + message);
              }
              else {
                println("ERROR: JOINEE_GAME message not recognized -> " + trim(splitMessage[1]));
              }
              break;
            default:
              println("ERROR: message header not recognized -> " + trim(splitMessage[0]));
              break;
          }
          break;
        case MULTIPLAYER_JOINED:
          switch(trim(splitMessage[0])) {
            case "LOBBY":
              switch(trim(splitMessage[1])) {
                case "Quit Lobby":
                  println("Your host quit the game");
                  if (this.otherPlayer != null) {
                    if (this.otherPlayer.client != null) {
                      this.otherPlayer.client.stop();
                    }
                    this.otherPlayer = null;
                  }
                  this.goToMainMenu();
                  break;
                case "Start Game":
                  println("Game is starting");
                  this.buttons.paB = new playAgainButton();
                  this.wantRematch[0] = false;
                  this.wantRematch[1] = false;
                  this.myGame = new Game(constants.game1Borders);
                  this.otherGame = new Game(constants.game2Borders);
                  this.messageQ.clear();
                  break;
                case "Ping Request":
                  if (splitMessage.length < 3) {
                    println("ERROR: No IP address for ping request");
                    break;
                  }
                  this.otherPlayer.write("Ping Resolve");
                  break;
                case "Ping Resolve":
                  if (this.otherPlayer.messageForMe(splitMessage)) {
                    this.otherPlayer.resolvePingRequest();
                  }
                  break;
                case "Host Rematch Sent":
                  this.wantRematch[1] = true;
                  break;
                case "Host Rematch Revoked":
                  this.wantRematch[1] = false;
                  break;
                case "Chat":
                  if (splitMessage.length < 4) {
                    println("ERROR: no chat string to add");
                    break;
                  }
                  this.buttons.lcB.addChat(trim(splitMessage[2]), trim(splitMessage[3]));
                  break;
                default:
                  println("ERROR: LOBBY message not recognized -> " + trim(splitMessage[1]));
                  break;
              }
              break;
            case "HOST_GAME":
              if (!this.otherGame.executeMessage(trim(splitMessage[1]))) {
                println("ERROR: HOST_GAMEfdas message not recognized -> " + trim(splitMessage[1]));
              }
              break;
            case "JOINEE_GAME":
              if (!this.myGame.executeMessage(trim(splitMessage[1]))) {
                println("ERROR: JOINEE_GAME message not recognized -> " + trim(splitMessage[1]));
              }
              if (splitMessage[1].contains("gameOver")) {
                this.buttons.paB.setDIS(false);
                this.buttons.paB.changeColor(color(220), color(0), color(170), color(120));
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
    String chatString = "";
    switch(this.state) {
      case SINGLEPLAYER:
        this.myGame.pressedKey();
        break;
      case MULTIPLAYER_LOBBY_HOSTING:
        chatString = this.buttons.lcB.pressedKey(this.user.name);
        if (!chatString.equals("")) {
          this.server.write("| LOBBY: Chat:  " + chatString);
        }
        break;
      case MULTIPLAYER_LOBBY_JOINED:
        chatString = this.buttons.lcB.pressedKey(this.user.name);
        if (!chatString.equals("")) {
          this.otherPlayer.write("| LOBBY: Chat:" + chatString);
        }
        break;
      case MULTIPLAYER_HOSTING:
        chatString = this.buttons.lcB.pressedKey(this.user.name);
        if (!chatString.equals("")) {
          this.server.write("| LOBBY: Chat:" + chatString);
        }
        String myGameChanges = this.myGame.pressedKey("| HOST_GAME: ");
        if (!myGameChanges.equals("")) {
          this.server.write(myGameChanges);
        }
        break;
      case MULTIPLAYER_JOINED:
        chatString = this.buttons.lcB.pressedKey(this.user.name);
        if (!chatString.equals("")) {
          this.otherPlayer.write("| LOBBY: Chat:" + chatString);
        }
        String gameChanges = this.myGame.pressedKey("| JOINEE_GAME: ", false);
        if (!gameChanges.equals("")) {
          this.otherPlayer.client.write(gameChanges);
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
