package com.example.servlet;

import io.github.cdimascio.dotenv.Dotenv;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.net.URLEncoder;

@WebServlet("/DboxAuth")
public class DboxAuth extends HttpServlet {
    private static final Dotenv dotenv = Dotenv.load();
    private static final String CLIENT_ID = dotenv.get("DROPBOX_CLIENT_ID");
    private static final String REDIRECT_URI = dotenv.get("DROPBOX_REDIRECT_URI");

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String authUrl = "https://www.dropbox.com/oauth2/authorize" +
                "?response_type=code" +
                "&client_id=" + URLEncoder.encode(CLIENT_ID, "UTF-8") +
                "&redirect_uri=" + URLEncoder.encode(REDIRECT_URI, "UTF-8");
        response.sendRedirect(authUrl);
    }
}
