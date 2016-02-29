package com.theironyard;

import spark.ModelAndView;
import spark.Session;
import spark.Spark;
import spark.template.mustache.MustacheTemplateEngine;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

public class Main {
    static HashMap<String, User> users = new HashMap<>();

    public static void main(String[] args) throws SQLException {
        Connection conn = DriverManager.getConnection("jdbc:h2:./main");

        Statement stmt = conn.createStatement();

        Spark.externalStaticFileLocation("public");
        stmt.execute("CREATE TABLE IF NOT EXISTS games (id IDENTITY, gameName VARCHAR, gameGenre VARCHAR, " +
                "gamePlatform VARCHAR, gameYear INT)");


        Spark.init();
        Spark.get(
                "/",
                ((request, response) -> {
                    User user = getUserFromSession(request.session());
                    HashMap m = new HashMap();
                    if (user == null) {
                        return new ModelAndView(m, "login.html");
                    } else {
                        //selectGames(conn);
                       // ArrayList<Game> games = selectGames(conn);
                        m.put("games", selectGames(conn));

                        return new ModelAndView(m, "home.html");
                    }
                }),
                new MustacheTemplateEngine()
        );
        Spark.post(
                "/create-user",
                ((request, response) -> {
                    String name = request.queryParams("loginName");
                    User user = users.get(name);
                    if (user == null) {
                        user = new User(name);
                        users.put(name, user);
                    }
                    Session session = request.session();
                    session.attribute("userName", name);

                    response.redirect("/");
                    return "";
                })
        );
        Spark.post(
                "/create-game",
                ((request, response) -> {
                    User user = getUserFromSession(request.session());
                    if (user == null) {
                        //throw new Exception("User is not logged in");
                        Spark.halt(403);
                    }
                    Game game = new Game();
                    game.name = request.queryParams("gameName");
                    game.genre = request.queryParams("gameGenre");
                    game.platform = request.queryParams("gamePlatform");
                    game.releaseYear = Integer.valueOf(request.queryParams("gameYear"));
                    if (game.name == null || game.genre == null || game.platform == null) {
                        throw new Exception("Didn't receive all query parameters.");
                    }
                    insertGame(conn, game.name, game.genre, game.platform, game.releaseYear);


                    response.redirect("/");
                    return "";
                })
        );
        Spark.post(
                "/logout",
                ((request, response) -> {
                    Session session = request.session();
                    session.invalidate();
                    response.redirect("/");
                    return "";
                })
        );
        Spark.post(
                "/delete-game",
                ((request, response) -> {
                    String string = request.queryParams("gameDelete");
                    int id = Integer.valueOf(string);
                    deleteGame(conn, id);
                    response.redirect("/");
                    return "";

                })
        );
        //conn.close();
    }

    static User getUserFromSession(Session session) {
        String name = session.attribute("userName");
        return users.get(name);
    }

    static void insertGame(Connection conn, String gameName, String gameGenre, String gamePlatform, int gameYear) throws SQLException {
        PreparedStatement stmt2 = conn.prepareStatement("INSERT INTO games VALUES (NULL, ?, ?, ?, ?)");
        stmt2.setString(1, gameName);
        stmt2.setString(2, gameGenre);
        stmt2.setString(3, gamePlatform);
        stmt2.setInt(4, gameYear);
        stmt2.execute();

    }

    static void deleteGame(Connection conn, int id) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("DELETE FROM games WHERE id = ?");
        stmt.setInt(1, id);
        stmt.execute();
    }

    static ArrayList<Game> selectGames(Connection conn) throws SQLException {
        ArrayList<Game> games = new ArrayList<>();
        Statement stmt = conn.createStatement();
        ResultSet results = stmt.executeQuery("SELECT * FROM games");
        while (results.next()) {
            String gameName = results.getString("gameName");
            String gameGenre = results.getString("gameGenre");
            String gamePlatform = results.getString("gamePlatform");
            int gameYear = results.getInt("gameYear");
            Game game = new Game(gameName, gameGenre, gamePlatform, gameYear);
            games.add(game);
        }
        return games;
    }
}

//        Create the Connection and execute a query to create a games
//        table that stores the game name and other attributes.

//        Write a static method insertGame and run it in the /create-game route.
//        It should insert a new row with the user-supplied information.

//        Write a static method deleteGame and run it in the
//        /delete-game route. It should remove the correct row using id.

//        Write a static method selectGames that returns
//        an ArrayList<Game> containing all the games in the database.

//        Remove the global ArrayList<Game> and instead just call selectGames inside the "/" route.

//        Add a form to edit the game name and other attributes,
//        and create an /edit-game route. Write a static method updateGame
//        and use it in that route. Then redirect to "/".


//        Optional: Add a search form which filters the game list to only
//        those games whose name contains the (case-insensitive) search string.