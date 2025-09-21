package com.example.servlet;

import io.github.cdimascio.dotenv.Dotenv;
import org.json.JSONObject;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

@WebServlet("/DboxToken")
public class DboxToken extends HttpServlet {
    private static final Dotenv dotenv = Dotenv.load();
    private static final String CLIENT_ID = dotenv.get("DROPBOX_CLIENT_ID");
    private static final String CLIENT_SECRET = dotenv.get("DROPBOX_CLIENT_SECRET");
    private static final String REDIRECT_URI = dotenv.get("DROPBOX_REDIRECT_URI");

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String code = request.getParameter("code");

        if (code == null) {
            String authUrl = "https://www.dropbox.com/oauth2/authorize" +
                    "?response_type=code" +
                    "&client_id=" + URLEncoder.encode(CLIENT_ID, "UTF-8") +
                    "&redirect_uri=" + URLEncoder.encode(REDIRECT_URI, "UTF-8");
            response.sendRedirect(authUrl);
            return;
        }

        URL url = new URL("https://api.dropboxapi.com/oauth2/token");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

        String params = "code=" + URLEncoder.encode(code, "UTF-8") +
                "&grant_type=authorization_code" +
                "&client_id=" + URLEncoder.encode(CLIENT_ID, "UTF-8") +
                "&client_secret=" + URLEncoder.encode(CLIENT_SECRET, "UTF-8") +
                "&redirect_uri=" + URLEncoder.encode(REDIRECT_URI, "UTF-8");

        try (OutputStream os = conn.getOutputStream()) {
            os.write(params.getBytes());
        }

        int status = conn.getResponseCode();
        BufferedReader in = new BufferedReader(new InputStreamReader(
                status == 200 ? conn.getInputStream() : conn.getErrorStream()
        ));

        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null) sb.append(line);
        in.close();

        response.setContentType("text/html;charset=UTF-8");

        if (status == 200) {
            try {
                JSONObject obj = new JSONObject(sb.toString());
                String accessToken = obj.getString("access_token");
                String accountId = obj.getString("account_id");

                HttpSession session = request.getSession();
                session.setAttribute("accessToken", accessToken);
                session.setAttribute("accountId", accountId);

                response.sendRedirect(request.getContextPath() + "/");

            } catch (Exception e) {
                response.getWriter().println("<h3>Error parsing access token ❌</h3>");
                response.getWriter().println("<pre>" + sb + "</pre>");
            }
        } else {
            response.getWriter().println("<h3>Error getting access token ❌</h3>");
            response.getWriter().println("<pre>" + sb + "</pre>");
        }
    }
}
