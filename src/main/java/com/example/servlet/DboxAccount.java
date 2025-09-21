package com.example.servlet;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONObject;

@WebServlet("/DboxAccount")
public class DboxAccount extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");

        HttpSession session = request.getSession();
        String accessToken = (String) session.getAttribute("accessToken");
        String accountId = (String) session.getAttribute("accountId"); // stored from DboxToken

        if (accessToken == null || accountId == null) {
            response.getWriter().print("{\"error\":\"No access token or account ID. Please connect first.\"}");
            return;
        }

        try {
            URL url = new URL("https://api.dropboxapi.com/2/users/get_account");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + accessToken);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            JSONObject payload = new JSONObject();
            payload.put("account_id", accountId);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(payload.toString().getBytes("UTF-8"));
            }

            int status = conn.getResponseCode();
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    status == 200 ? conn.getInputStream() : conn.getErrorStream()
            ));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) sb.append(line);
            in.close();

            response.getWriter().print(sb.toString());

        } catch (Exception e) {
            JSONObject err = new JSONObject();
            err.put("error", "Exception occurred");
            err.put("message", e.getMessage());
            response.getWriter().print(err.toString());
        }
    }
}
