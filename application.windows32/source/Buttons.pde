class AllButtons {
  private quitButton qB = new quitButton();
  
  private newGameButton ngB = new newGameButton();
  private leaveLobbyButton llB = new leaveLobbyButton();
  
  private hostGameButton hgB = new hostGameButton();
  private kickPlayerButton kpB = new kickPlayerButton();
  
  private findGameButton fgB = new findGameButton();
  private startGameButton sgB = new startGameButton();
  
  private listBar cSB = new listBar(10, 740, 260, 798);
  
  private joinLobbyButton jlB = new joinLobbyButton();
  private showInfoButton siB = new showInfoButton();
  
  private findIpButton fiB = new findIpButton();
  
  AllButtons() {
  }
  
  void update(GameState state) {
    fill(constants.defaultBackgroundColor);
    stroke(constants.defaultBackgroundColor);
    rectMode(CORNERS);
    rect(0, 685, 600, 720);
    qB.update(mouseX, mouseY);
    switch(state) {
      case MAIN_MENU:
        this.ngB.update(mouseX, mouseY);
        this.hgB.update(mouseX, mouseY);
        this.fgB.update(mouseX, mouseY);
        this.cSB.update(mouseX, mouseY);
        if (this.cSB.getHIGH() != -1) {
          this.jlB.changeColor(color(220), color(0), color(170), color(120));
        }
        else {
          this.jlB.changeColor(color(235), color(80), color(170), color(120));
        }
        this.jlB.update(mouseX, mouseY);
        this.fiB.update(mouseX, mouseY);
        break;
      case CONNECTING_TO_LOBBY:
        break;
      case SINGLEPLAYER:
        this.ngB.update(mouseX, mouseY);
        break;
      case MULTIPLAYER_LOBBY_HOSTING:
        this.llB.update(mouseX, mouseY);
        if (this.cSB.getHIGH() != -1) {
          this.kpB.changeColor(color(220), color(0), color(170), color(120));
        } else {
          this.kpB.changeColor(color(235), color(80), color(170), color(120));
        }
        this.kpB.update(mouseX, mouseY);
        if (this.cSB.getSTRS().length == 2) {
          this.sgB.changeColor(color(220), color(0), color(170), color(120));
        } else {
          this.sgB.changeColor(color(235), color(80), color(170), color(120));
        }
        this.sgB.update(mouseX, mouseY);
        this.cSB.update(mouseX, mouseY);
        this.siB.update(mouseX, mouseY);
        break;
      case MULTIPLAYER_LOBBY_JOINED:
        this.llB.update(mouseX, mouseY);
        this.siB.update(mouseX, mouseY);
        this.cSB.update(mouseX, mouseY);
        break;
      case MULTIPLAYER_HOSTING:
        break;
      case MULTIPLAYER_JOINED:
        break;
    }
  }
  void mousePress(GameState state) {
    qB.mousePress();
    switch(state) {
      case MAIN_MENU:
        this.ngB.mousePress();
        this.hgB.mousePress();
        this.fgB.mousePress();
        if (this.cSB.getHIGH() != -1) {
          if (this.jlB.getMON()) {
            this.jlB.mousePress();
          } else {
            this.cSB.mousePress();
          }
        }
        else {
          this.cSB.mousePress();
        }
        this.fiB.mousePress();
        break;
      case CONNECTING_TO_LOBBY:
        break;
      case SINGLEPLAYER:
        this.ngB.mousePress();
        break;
      case MULTIPLAYER_LOBBY_HOSTING:
        this.llB.mousePress();
        if (this.cSB.getSTRS().length == 2) {
          this.sgB.mousePress();
        }
        this.siB.mousePress();
        if (this.cSB.getHIGH() != -1) {
          if (this.kpB.getMON()) {
            this.kpB.mousePress();
          } else {
            this.cSB.mousePress();
          }
        }
        else {
          this.cSB.mousePress();
        }
        break;
      case MULTIPLAYER_LOBBY_JOINED:
        this.llB.mousePress();
        this.siB.mousePress();
        this.cSB.mousePress();
        break;
      case MULTIPLAYER_HOSTING:
        break;
      case MULTIPLAYER_JOINED:
        break;
    }
  }
  void mouseRelease(GameState state) {
    qB.mouseRelease();
    switch(state) {
      case MAIN_MENU:
        this.ngB.mouseRelease();
        this.hgB.mouseRelease();
        this.fgB.mouseRelease();
        this.cSB.mouseRelease();
        if (this.cSB.getHIGH() != -1) {
          this.jlB.mouseRelease();
        }
        this.fiB.mouseRelease();
        break;
      case CONNECTING_TO_LOBBY:
        break;
      case SINGLEPLAYER:
        this.ngB.mouseRelease();
        break;
      case MULTIPLAYER_LOBBY_HOSTING:
        this.llB.mouseRelease();
        if (this.cSB.getHIGH() != -1) {
          this.kpB.mouseRelease();
        }
        if (this.cSB.getSTRS().length == 2) {
          this.sgB.mouseRelease();
        }
        this.siB.mouseRelease();
        this.cSB.mouseRelease();
        break;
      case MULTIPLAYER_LOBBY_JOINED:
        this.llB.mouseRelease();
        this.siB.mouseRelease();
        this.cSB.mouseRelease();
        break;
      case MULTIPLAYER_HOSTING:
        break;
      case MULTIPLAYER_JOINED:
        break;
    }
  }
  void scroll(int count) {
    this.cSB.scroll(count);
  }
  
  void clearButton(int id) {
    fill(constants.defaultBackgroundColor);
    stroke(constants.defaultBackgroundColor);
    rectMode(CORNERS);
    switch(id) {
      case 0:
        rect(10, 740, 260, 800);
        break;
      case 1:
        rect(10, 690, 60, 715);
        break;
      case 2:
        rect(70, 690, 160, 715);
        break;
      case 3:
        rect(170, 690, 260, 715);
        break;
      case 4:
        rect(270, 690, 360, 715);
        break;
      case 5:
        rect(270, 740, 360, 765);
        break;
      case 6:
        rect(270, 773, 360, 798);
        break;
      default:
        println("ERROR: button id " + id + " not found to clear.");
        break;
    }
  }
}

// Button 1
class quitButton extends recButton {
  quitButton() {
    super("Quit", 14, 10, 690, 60, 715);
    this.setREB(true);
  }
  void click() {
    exit();
  }
}

// Button 2
class newGameButton extends recButton {
  newGameButton() {
    super("New Game", 14, 70, 690, 160, 715);
    this.setREB(true);
  }
  void click() {
    this.setMON(false);
    this.setCLK(false);
    currGame.startSinglePlayerGame();
  }
}
class leaveLobbyButton extends recButton {
  leaveLobbyButton() {
    super("Leave Lobby", 14, 70, 690, 160, 715);
    this.setREB(true);
  }
  void click() {
    this.setMON(false);
    this.setCLK(false);
    currGame.leaveLobby();
  }
}

// Button 3
class hostGameButton extends recButton {
  hostGameButton() {
    super("Host Game", 14, 170, 690, 260, 715);
    this.setREB(true);
  }
  void click() {
    this.setMON(false);
    this.setCLK(false);
    currGame.hostTwoPlayerGame();
  }
}
class kickPlayerButton extends recButton {
  kickPlayerButton() {
    super("Kick Player", 14, 170, 690, 260, 715);
    this.setREB(true);
  }
  void click() {
    this.setMON(false);
    this.setCLK(false);
    currGame.kickPlayer();
  }
}

// Button 4
class findGameButton extends recButton {
  findGameButton() {
    super("Find Game", 14, 270, 690, 360, 715);
    this.setREB(true);
  }
  void click() {
    this.setMON(false);
    this.setCLK(false);
    currGame.findMultiPlayerGame();
  }
}
class startGameButton extends recButton {
  startGameButton() {
    super("Start Game", 14, 270, 690, 360, 715);
    this.setREB(true);
  }
  void click() {
    this.setMON(false);
    this.setCLK(false);
    currGame.startGame();
  }
}

// Button 5
class joinLobbyButton extends recButton {
  joinLobbyButton() {
    super("Join Lobby", 14, 270, 740, 360, 765);
    this.setREB(true);
  }
  void click() {
    this.setMON(false);
    this.setCLK(false);
    currGame.joinSelectedLobby();
  }
}
class showInfoButton extends recButton {
  showInfoButton() {
    super("Lobby Info", 14, 270, 740, 360, 765);
    this.setREB(true);
  }
  void click() {
    this.setMON(false);
    this.setCLK(false);
    currGame.showLobbyInfo();
  }
}

// Button 6
class findIpButton extends recButton {
  findIpButton() {
    super("Find IP", 14, 270, 773, 360, 798);
    this.setREB(true);
  }
  void click() {
    this.setMON(false);
    this.setCLK(false);
    currGame.directConnect();
  }
}
