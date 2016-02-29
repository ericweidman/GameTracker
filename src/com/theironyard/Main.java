package com.theironyard;

import spark.ModelAndView;
import spark.Session;
import spark.Spark;
import spark.template.mustache.MustacheTemplateEngine;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

public class Main {
    static HashMap<String, User> users = new HashMap<>();

    public static void main(String[] args) throws SQLException {
        Connection conn = DriverManager.getConnection("jdbc:h2:./main");

        Statement stmt = conn.createStatement();

        Spark.externalStaticFileLocation("public");
        stmt.execute("CREATE TABLE IF NOT EXISTS games (id IDENTITY, name VARCHAR, genre VARCHAR, platform VARCHAR, year VARCHAR)");



        Spark.init();
        Spark.get(
                "/",
                ((request, response) -> {
                    User user = getUserFromSession(request.session());
                    HashMap m = new HashMap();
                    if (user == null) {
                        return new ModelAndView(m, "login.html");
                    } else {
                        m.put("games",user.games);
                        return new ModelAndView(user, "home.html");
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
                    if(user == null){
                        //throw new Exception("User is not logged in");
                        Spark.halt(403);
                    }

                    String gameName = request.queryParams("gameName");
                    String gameGenre = request.queryParams("gameGenre");
                    String gamePlatform = request.queryParams("gamePlatform");
                    int gameYear = Integer.valueOf(request.queryParams("gameYear"));
                    if (gameGenre == null || gameName == null || gamePlatform == null){
                        throw new Exception("Didn't receive all query parameters.");
                    }
                    Game game = new Game(gameName, gameGenre, gamePlatform, gameYear);

                    user.games.add(game);
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

    }

    static User getUserFromSession(Session session){
        String name = session.attribute("userName");
        return users.get(name);
    }

   // static Game insertGame(){
     //   String gameName = request.queryParams("gameName");

   // }
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