package com.example.servlet;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONArray;
import org.json.JSONObject;

@WebServlet("/DboxList")
public class DboxList extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");

        HttpSession session = request.getSession();
        String accessToken = (String) session.getAttribute("accessToken");
        if (accessToken == null) {
            response.getWriter().print("{\"error\":\"No access token in session\"}");
            return;
        }

        try {
            URL url = new URL("https://api.dropboxapi.com/2/files/list_folder");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + accessToken);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                os.write("{\"path\": \"\"}".getBytes());
            }

            int status = conn.getResponseCode();
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    status == 200 ? conn.getInputStream() : conn.getErrorStream()
            ));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) sb.append(line);
            in.close();

            String jsonResponse = sb.toString();

            if (status == 200) {
                JSONObject obj = new JSONObject(jsonResponse);
                JSONArray entries = obj.getJSONArray("entries");

                JSONArray result = new JSONArray();
                for (int i = 0; i < entries.length(); i++) {
                    JSONObject entry = entries.getJSONObject(i);
                    JSONObject fileObj = new JSONObject();
                    fileObj.put("name", entry.getString("name"));
                    fileObj.put("type", entry.getString(".tag"));
                    result.put(fileObj);
                }

                response.getWriter().print(result.toString());
            } else {
                response.getWriter().print("{\"error\":\"Request failed\",\"status\":" + status +
                        ",\"response\":" + JSONObject.quote(jsonResponse) + "}");
            }

        } catch (Exception e) {
            JSONObject err = new JSONObject();
            err.put("error", "Exception occurred");
            err.put("message", e.getMessage());
            response.getWriter().print(err.toString());
        }
    }
}
