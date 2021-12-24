// Tetris
// v0.3.4d
// 20211223

import java.util.*;
import javafx.util.*;
import static javax.swing.JFrame.*;
import static javax.swing.JOptionPane.*;
import static javax.swing.JPanel.*;
import java.net.*;
import java.io.*;
import processing.net.*;
import processing.sound.*;

Constants constants = new Constants();
Options options;
CurrGame currGame = new CurrGame(this);
int frameTimer = millis();
int frameCounter = frameCount;
float lastFPS = constants.maxFPS;

void setup() {
  size(1330, 800);
  frameRate(constants.maxFPS);
  background(constants.defaultBackgroundColor);
  constants.loadImages();
  options = new Options(constants);
  fill(0);
  textSize(14);
  textAlign(LEFT, BOTTOM);
  text("Customize", 1240, 707);
}

void draw() {
  currGame.update();
  // FPS counter
  if (millis() - frameTimer > constants.frameUpdateTime) {
    lastFPS = (constants.frameAverageCache * lastFPS + float(frameCount - frameCounter) * (1000.0 / constants.frameUpdateTime)) / (constants.frameAverageCache + 1);
    textSize(12);
    textAlign(LEFT, TOP);
    fill(constants.defaultBackgroundColor);
    stroke(constants.defaultBackgroundColor);
    rectMode(CORNERS);
    rect(5, 5, 50, 25);
    fill(0);
    text(int(lastFPS) + " FPS", 5, 5);
    frameCounter = frameCount + 1;
    frameTimer = millis();
  }
}

void keyPressed() {
  currGame.keyPress();
}

void mousePressed() {
  currGame.mousePress();
}

void mouseReleased() {
  currGame.mouseRelease();
}

void mouseWheel(MouseEvent event) {
  currGame.scroll(event.getCount());
}

// Occurs when client connects to server
void serverEvent(Server someServer, Client someClient) {
  currGame.clientConnects(someClient);
}

// Occurs when client disconnects from server
void disconnectEvent(Client someClient) {
  currGame.clientDisconnects(someClient);
}

// Occurs when server sends a value to an existing client
void clientEvent(Client someClient) {
  currGame.clientEvent(someClient);
}
