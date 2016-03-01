package com.theironyard;

/**
 * Created by ericweidman on 2/23/16.
 */
public class Game {
    int gameId;
    String name;
    String genre;
    String platform;
    int releaseYear;

    public Game(int gameId, String name, String genre, String platform, int releaseYear) {
        this.gameId = gameId;
        this.name = name;
        this.genre = genre;
        this.platform = platform;
        this.releaseYear = releaseYear;


    }

    public Game() {
    }
}
