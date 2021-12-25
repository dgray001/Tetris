class AllButtons {
  private quitButton qB = new quitButton();
  
  private newGameButton ngB = new newGameButton();
  private pauseGameButton pgB = new pauseGameButton();
  private leaveLobbyButton llB = new leaveLobbyButton();
  
  private endGameButton egB = new endGameButton();
  private hostGameButton hgB = new hostGameButton();
  private kickPlayerButton kpB = new kickPlayerButton();
  private playAgainButton paB = new playAgainButton();
  
  private findGameButton fgB = new findGameButton();
  private startGameButton sgB = new startGameButton();
  
  private listBar cSB = new listBar(10, 740, 260, 798);
  
  private joinLobbyButton jlB = new joinLobbyButton();
  private showInfoButton siB = new showInfoButton();
  
  private findIpButton fiB = new findIpButton();
  
  private chatBox lcB = new chatBox(400, 690, 930, 798);
  
  private customizePieceButton cpB = new customizePieceButton();
  private customizeBoardButton cbB = new customizeBoardButton();
  private customizeSoundButton csB = new customizeSoundButton();
  private customizeKeysButton ckB = new customizeKeysButton();
  
  AllButtons() {
  }
  
  void update(GameState state) {
    fill(constants.defaultBackgroundColor);
    stroke(constants.defaultBackgroundColor);
    rectMode(CORNERS);
    rect(0, 685, 600, 720);
    qB.update(mouseX, mouseY);
    cpB.update(mouseX, mouseY);
    cbB.update(mouseX, mouseY);
    csB.update(mouseX, mouseY);
    ckB.update(mouseX, mouseY);
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
      case OPTIONS:
        break;
      case CONNECTING_TO_LOBBY:
        break;
      case SINGLEPLAYER:
        this.pgB.update(mouseX, mouseY);
        this.egB.update(mouseX, mouseY);
        break;
      case MULTIPLAYER_LOBBY_HOSTING:
        this.llB.update(mouseX, mouseY);
        if (this.cSB.getHIGH() > 0) {
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
        this.lcB.update(mouseX, mouseY);
        break;
      case MULTIPLAYER_LOBBY_JOINED:
        this.llB.update(mouseX, mouseY);
        this.siB.update(mouseX, mouseY);
        this.cSB.update(mouseX, mouseY);
        this.lcB.update(mouseX, mouseY);
        break;
      case MULTIPLAYER_HOSTING:
        this.llB.update(mouseX, mouseY);
        this.paB.update(mouseX, mouseY);
        this.siB.update(mouseX, mouseY);
        this.cSB.update(mouseX, mouseY);
        this.lcB.update(mouseX, mouseY);
        break;
      case MULTIPLAYER_JOINED:
        this.llB.update(mouseX, mouseY);
        this.paB.update(mouseX, mouseY);
        this.siB.update(mouseX, mouseY);
        this.cSB.update(mouseX, mouseY);
        this.lcB.update(mouseX, mouseY);
        break;
    }
  }
  void mousePress(GameState state) {
    qB.mousePress();
    cpB.mousePress();
    cbB.mousePress();
    csB.mousePress();
    ckB.mousePress();
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
      case OPTIONS:
        break;
      case CONNECTING_TO_LOBBY:
        break;
      case SINGLEPLAYER:
        this.pgB.mousePress();
        this.egB.mousePress();
        break;
      case MULTIPLAYER_LOBBY_HOSTING:
        this.llB.mousePress();
        if (this.cSB.getSTRS().length == 2) {
          this.sgB.mousePress();
        }
        this.siB.mousePress();
        if (this.cSB.getHIGH() > 0) {
          if (this.kpB.getMON()) {
            this.kpB.mousePress();
          } else {
            this.cSB.mousePress();
          }
        }
        else {
          this.cSB.mousePress();
        }
        this.lcB.mousePress();
        break;
      case MULTIPLAYER_LOBBY_JOINED:
        this.llB.mousePress();
        this.siB.mousePress();
        this.cSB.mousePress();
        this.lcB.mousePress();
        break;
      case MULTIPLAYER_HOSTING:
        this.llB.mousePress();
        this.paB.mousePress();
        this.siB.mousePress();
        this.cSB.mousePress();
        this.lcB.mousePress();
        break;
      case MULTIPLAYER_JOINED:
        this.llB.mousePress();
        this.paB.mousePress();
        this.siB.mousePress();
        this.cSB.mousePress();
        this.lcB.mousePress();
        break;
    }
  }
  void mouseRelease(GameState state) {
    qB.mouseRelease();
    cpB.mouseRelease();
    cbB.mouseRelease();
    csB.mouseRelease();
    ckB.mouseRelease();
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
      case OPTIONS:
        break;
      case CONNECTING_TO_LOBBY:
        break;
      case SINGLEPLAYER:
        this.pgB.mouseRelease();
        this.egB.mouseRelease();
        break;
      case MULTIPLAYER_LOBBY_HOSTING:
        this.llB.mouseRelease();
        if (this.cSB.getHIGH() > 0) {
          this.kpB.mouseRelease();
        }
        if (this.cSB.getSTRS().length == 2) {
          this.sgB.mouseRelease();
        }
        this.siB.mouseRelease();
        this.cSB.mouseRelease();
        this.lcB.mouseRelease();
        break;
      case MULTIPLAYER_LOBBY_JOINED:
        this.llB.mouseRelease();
        this.siB.mouseRelease();
        this.cSB.mouseRelease();
        this.lcB.mouseRelease();
        break;
      case MULTIPLAYER_HOSTING:
        this.llB.mouseRelease();
        this.paB.mouseRelease();
        this.siB.mouseRelease();
        this.cSB.mouseRelease();
        this.lcB.mouseRelease();
        break;
      case MULTIPLAYER_JOINED:
        this.llB.mouseRelease();
        this.paB.mouseRelease();
        this.siB.mouseRelease();
        this.cSB.mouseRelease();
        this.lcB.mouseRelease();
        break;
    }
  }
  void pressedKey(String username) {
    this.lcB.pressedKey(username);
  }
  void scroll(int count) {
    this.cSB.scroll(count);
    this.lcB.scroll(count);
  }
  
  void clearButtons(int[] ids) {
    for (int id : ids) {
      this.clearButton(id);
    }
  }
  void clearButton(int id) {
    fill(constants.defaultBackgroundColor);
    stroke(constants.defaultBackgroundColor);
    rectMode(CORNERS);
    switch(id) {
      case 0: // scrollbar
        rect(10, 720, 265, 800);
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
    super("Exit", 14, 10, 690, 60, 715);
    this.setREB(true);
  }
  void click() {
    int response = showConfirmDialog(null, "Are you sure you want to exit?", "Tetris", YES_NO_OPTION, PLAIN_MESSAGE);
    if (response == YES_OPTION) {
      exit();
    }
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
class pauseGameButton extends recButton {
  pauseGameButton() {
    super("Pause Game", 14, 70, 690, 160, 715);
    this.setREB(true);
  }
  void click() {
    this.setMON(false);
    this.setCLK(false);
    currGame.pauseGame();
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
class endGameButton extends recButton {
  endGameButton() {
    super("End Game", 14, 170, 690, 260, 715);
    this.setREB(true);
  }
  void click() {
    this.setMON(false);
    this.setCLK(false);
    currGame.endGame();
  }
}
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
class playAgainButton extends recButton {
  playAgainButton() {
    super("Rematch", 14, 170, 690, 260, 715);
    super.setREB(true);
    super.setDIS(true);
    super.changeColor(color(235), color(80), color(170), color(120));
    super.setMOT(true);
    super.setMOMES("Rematch");
  }
  void click() {
    this.setMON(false);
    this.setCLK(false);
    currGame.toggleRematch();
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

// Option Buttons
class customizePieceButton extends recButton {
  customizePieceButton() {
    super("Pieces", 14, 1240, 709, 1320, 729);
    this.setREB(true);
    this.setRND(3);
  }
  void click() {
    this.setMON(false);
    this.setCLK(false);
    String[] styles = PieceStyle.getStyleList();
    String response = (String)showInputDialog(null, "Piece Style", "Tetris", PLAIN_MESSAGE, null, styles, options.pieceStyle.getStyle());
    if (response == null) {
      return;
    }
    try {
      options.pieceStyle = PieceStyle.VALUES.get(Arrays.asList(styles).indexOf(response));
    } catch (Exception e) {
      showMessageDialog(null, "Couldn't adjust piece style.", "Tetris", PLAIN_MESSAGE);
    }
    options.saveOptions();
  }
}
class customizeBoardButton extends recButton {
  customizeBoardButton() {
    super("Board", 14, 1240, 732, 1320, 752);
    this.setREB(true);
    this.setRND(3);
  }
  void click() {
    this.setMON(false);
    this.setCLK(false);
    int response = showConfirmDialog(null, "Gridlines?", "Tetris", YES_NO_OPTION, PLAIN_MESSAGE);
    if (response == YES_OPTION) {
      options.gridlines = true;
    }
    else if (response == NO_OPTION) {
      options.gridlines = false;
    }
    options.saveOptions();
  }
}
class customizeSoundButton extends recButton {
  customizeSoundButton() {
    super("Sounds", 14, 1240, 755, 1320, 775);
    this.setREB(true);
    this.setRND(3);
  }
  void click() {
    this.setMON(false);
    this.setCLK(false);
  }
}
class customizeKeysButton extends recButton {
  customizeKeysButton() {
    super("Hotkeys", 14, 1240, 778, 1320, 798);
    this.setREB(true);
    this.setRND(3);
  }
  void click() {
    this.setMON(false);
    this.setCLK(false);
  }
}
