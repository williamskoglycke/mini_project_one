package se.william;

import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class Main {
    public static void main(String[] args) {
        try {
            startSimulation();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            System.exit(1);
        } finally {
            System.out.println("Simulation over!");
        }

    }

    private static void startSimulation() throws IOException, InterruptedException {
        Terminal terminal = createTerminal();

        simulationLoop(terminal);
    }

    private static Terminal createTerminal() throws IOException {
        DefaultTerminalFactory terminalFactory = new DefaultTerminalFactory();
        Terminal terminal = terminalFactory.createTerminal();
        terminal.setCursorVisible(false);
        return terminal;
    }

    private static void simulationLoop(Terminal terminal) throws InterruptedException, IOException {

        Player player = new Player(10, 10, '\u263a');
        WriteAndRead writeAndRead = new WriteAndRead();
        List<OuterWall> outerWalls = createOuterFrame();
        List<MovingWall> movingWalls = createMovingWall();
        List<List<MovingWall>> allaVäggar = new ArrayList<>();
        allaVäggar.add(movingWalls);

        int timeCounterThreshold = 80;
        int timeCounter = 0;
        int wallCounter = 1;
        int highScore = writeAndRead.readFromFile();

        while (true) {
            KeyStroke keyStroke;
            do {
                // everything inside this loop will be called approximately every ~5 millisec.
                Thread.sleep(5);
                keyStroke = terminal.pollInput();

                timeCounter++;

                if (timeCounter >= timeCounterThreshold) {
                    timeCounter = 0;

                    // Skapar en ny vägg när den första kommit till x65
                    for (int i = 0; i < allaVäggar.size(); i++) {
                        if (allaVäggar.get(i).get(0).getX() == 65) {
                            allaVäggar.add(createMovingWall());
                            wallCounter++; // Lägger till en poäng per vägg
                        }
                    }

                    // Ökar svårighetsgraden var tredje skapad vägg
                    switch (wallCounter) {
                        case 3:
                            timeCounterThreshold = 50;
                            break;
                        case 6:
                            timeCounterThreshold = 40;
                            break;
                        case 9:
                            timeCounterThreshold = 30;
                            break;
                        case 12:
                            timeCounterThreshold = 20;
                            break;
                    }

                    if (!isPlayerAlive(player, allaVäggar)) {
                        break;
                    }

                    for (List<MovingWall> enVägg : allaVäggar) {
                        printMovingWall(terminal, enVägg);
                    }

                    for (List<MovingWall> enVägg : allaVäggar) {
                        for (MovingWall flytta : enVägg) {
                            flytta.moveLeft();
                        }
                    }

                    printPlayer(terminal, player);
                    printWall(terminal, outerWalls);
                    printScore(terminal, wallCounter, highScore, writeAndRead);
                    removeDeadWall(allaVäggar);
                    terminal.flush(); // don't forget to flush to see any updates!
                }


            } while (keyStroke == null);

            if (!isPlayerAlive(player, allaVäggar)) {
                printGameOverScreen(terminal, highScore, wallCounter);
                terminal.flush();
                break;
            }

            movePlayer(player, keyStroke);
            printPlayer(terminal, player);

            terminal.flush(); // don't forget to flush to see any updates!
        }
    }

    private static void printGameOverScreen(Terminal terminal, int highScore, int score) throws IOException {
        String gameOver =
                "oooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo&" +
                "oooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo&" +
                "ooooooooooooo+++++++++oooooo++++++oooooo+++ooooooooo+++ooo++++++++++++oooooooooo&" +
                "oooooooooooo/        `ooooo+      ooooo+   +oooooooo`  +oo`           /ooooooooo&" +
                "ooooooooo+..-::::::::/oo+..-::::::-.-oo+   ...ooo--.   +oo`  :::::::::+ooooooooo&" +
                "ooooooooo/  `oo+/////+oo+   //////   +o+   ```///.``   +oo`  //////+oooooooooooo&" +
                "ooooooooo/  `oo/     `oo+            +o+   +oo   +oo`  +oo`        /oooooooooooo&" +
                "ooooooooo/  `oo+//:  `oo+   /////:   +o+   +oo///ooo`  +oo`  ://///+oooooooooooo&" +
                "ooooooooo+.../////:  `oo+   ooooo+   +o+   +oooooooo`  +oo`  :////////+ooooooooo&" +
                "oooooooooooo/        `oo+  `ooooo+   +o+   +oooooooo`  +oo`           /ooooooooo&" +
                "oooooooooooo+/////////ooo///oooooo///ooo///ooooooooo///ooo////////////+ooooooooo&" +
                "oooooooooooo+//////oooooo///ooooooooo///ooo////////////ooo/////////+oooooooooooo&" +
                "oooooooooooo/     `ooooo+  `oooooooo+   +o+            +oo`        /oooooooooooo&" +
                "ooooooooo+...//////...oo+   oooooooo+   +o+   /////////ooo`  ://///...+ooooooooo&" +
                "ooooooooo/  `ooooo+  `oo+   oooooooo+   +o+   ://///oooooo`  ://///`  /ooooooooo&" +
                "ooooooooo/  `ooooo+  `oo+   oooooooo+   +o+         +ooooo`           /ooooooooo&" +
                "ooooooooo/  `ooooo+  `ooo++/```oo+```/++oo+   /+++++oooooo`  /++`  :+++ooooooooo&" +
                "ooooooooo+..-::::::..-ooooo+...:::-.-ooooo+   :::::::::+oo`  /oo--.:::+ooooooooo&" +
                "oooooooooooo/     `ooooooooooo+   oooooooo+            +oo`  /ooooo`  /ooooooooo&" +
                "ooooooooooooo++++++oooooooooooo+++ooooooooo++++++++++++ooo+++oooooo++++ooooooooo&" +
                "oooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo&" +
                "                                                                                ";

        String[] gameOverArray = gameOver.split("&");

        terminal.setForegroundColor(new TextColor.RGB(0, 255, 0));

        for (int i = 0; i < gameOverArray.length; i++) {

            for (int j = 0; j < gameOverArray[i].length(); j++) {
                //System.out.println(gameOverArray[i].length());
                //System.out.print(gameOverArray[i].charAt(j));
                terminal.setCursorPosition(j, i+1);
                terminal.putCharacter(gameOverArray[i].charAt(j));
            }
            System.out.println();
        }
    }

    private static void printPlayer(Terminal terminal, Player player) throws IOException {
        terminal.setCursorPosition(player.getPreviousX(), player.getPreviousY());
        terminal.putCharacter(' ');

        terminal.setCursorPosition(player.getX(), player.getY());
        terminal.putCharacter(player.getSymbol());
    }

    private static void printWall(Terminal terminal, List<OuterWall> outerWalls) throws IOException {

        for (OuterWall outerWall : outerWalls) {
            terminal.setCursorPosition(outerWall.getPreviousX(), outerWall.getPreviousY());
            terminal.putCharacter(' ');

            terminal.setCursorPosition(outerWall.getX(), outerWall.getY());
            terminal.setForegroundColor(new TextColor.RGB(0, 255, 0));
            terminal.putCharacter(outerWall.getSymbol());
        }
        terminal.resetColorAndSGR();
    }

    private static void printScore(Terminal terminal, int currentScore, int highscoreBeforeGame, WriteAndRead writeAndReadHighscoreFromFile) throws IOException {

        String playerText = "Player 1: Score " + (currentScore * 100);
        String highScore = "";
        if (currentScore * 100 > highscoreBeforeGame) {
            highScore = "Highscore: " + currentScore * 100;
            writeAndReadHighscoreFromFile.writeToFile(String.valueOf(currentScore * 100), false);
        } else {
            highScore = "Highscore: " + highscoreBeforeGame;
        }

        for (int i = 0; i < playerText.length(); i++) {
            terminal.setForegroundColor(new TextColor.RGB(0, 255, 0));
            terminal.setCursorPosition(i, 0);
            terminal.putCharacter(playerText.charAt(i));
        }

        int startArrayHighScore = 80 - highScore.length();
        for (int i = 0; i < highScore.length(); i++) {
            terminal.setForegroundColor(new TextColor.RGB(0, 255, 0));
            terminal.setCursorPosition(i + startArrayHighScore, 0);
            terminal.putCharacter(highScore.charAt(i));
        }

        terminal.resetColorAndSGR();
    }

    private static void printMovingWall(Terminal terminal, List<MovingWall> movingWalls) throws IOException {
        for (MovingWall movingWall : movingWalls) {
            int red = ThreadLocalRandom.current().nextInt(0, 256);
            int blue = ThreadLocalRandom.current().nextInt(0, 256);
            int green = ThreadLocalRandom.current().nextInt(0, 256);
            terminal.setForegroundColor(new TextColor.RGB(red, blue, green));
            terminal.setCursorPosition(movingWall.getPreviousX(), movingWall.getPreviousY());
            terminal.putCharacter(' ');

            terminal.setCursorPosition(movingWall.getX(), movingWall.getY());
            terminal.putCharacter(movingWall.getSymbol());
        }
        terminal.resetColorAndSGR();
    }


    private static void movePlayer(Player player, KeyStroke keyStroke) {
        switch (keyStroke.getKeyType()) {
            case ArrowUp:
                player.moveUp();
                break;
            case ArrowDown:
                player.moveDown();
                break;
            case ArrowLeft:
                player.moveLeft();
                break;
            case ArrowRight:
                player.moveRight();
                break;
        }
    }

    private static List<OuterWall> createOuterFrame() {
        List<OuterWall> outerWalls = new ArrayList<>();
        for (int i = 1; i < 24; i++) {
            outerWalls.add(new OuterWall(80, i, 'X'));
            outerWalls.add(new OuterWall(0, i, 'X'));
        }

        for (int i = 0; i < 80; i++) {
            outerWalls.add(new OuterWall(i, 1, 'X'));
            outerWalls.add(new OuterWall(i, 24, 'X'));
        }
        return outerWalls;
    }

    private static List<MovingWall> createMovingWall() {
        List<MovingWall> walls = new ArrayList<>();
        int gap = ThreadLocalRandom.current().nextInt(1, 21);
        for (int i = 1; i < 23; i++) {
            if (i == gap || i == gap + 1) {
                walls.add(new MovingWall(77, i, ' '));
            } else {
                walls.add(new MovingWall(77, i, '<'));
            }

        }
        return walls;
    }

    private static void removeDeadWall(List<List<MovingWall>> allWalls) {

        for (int i = 0; i < allWalls.size(); i++) {
            List<MovingWall> wall = allWalls.get(i);
            for (int j = 0; j < wall.size(); j++) {
                if (wall.get(j).getX() == -2) {
                    wall.remove(j);
                }
            }
        }

    }

    public static boolean isPlayerAlive(Player player, List<List<MovingWall>> allaVäggar) {
        for (List<MovingWall> movingWalls : allaVäggar) {
            for (MovingWall väggPlupp : movingWalls) {
                if ((player.getX() == väggPlupp.getX()) && (player.getY() == väggPlupp.getY()) && (väggPlupp.getSymbol() == '<')) {
                    return false;
                }
            }
        }
        return true;
    }
}


