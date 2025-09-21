package com.example.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/DboxDownload")
public class DboxDownload extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String fileName = request.getParameter("file");
        if (fileName == null || fileName.isEmpty()) {
            response.getWriter().println("No file selected!");
            return;
        }

        HttpSession session = request.getSession();
        String accessToken = (String) session.getAttribute("accessToken");
        if (accessToken == null) {
            response.getWriter().println("No access token found. Please <a href='DboxAuth'>connect</a> first.");
            return;
        }

        try {
            URL url = new URL("https://content.dropboxapi.com/2/files/download");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + accessToken);
            conn.setRequestProperty("Dropbox-API-Arg", "{\"path\": \"/" + fileName + "\"}");
            conn.setDoOutput(true);

            int status = conn.getResponseCode();
            if (status == 200) {
                response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
                try (InputStream is = conn.getInputStream(); OutputStream os = response.getOutputStream()) {
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = is.read(buffer)) != -1) {
                        os.write(buffer, 0, bytesRead);
                    }
                }
            } else {
                InputStream errorStream = conn.getErrorStream();
                StringBuilder sb = new StringBuilder();
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = errorStream.read(buffer)) != -1) {
                    sb.append(new String(buffer, 0, bytesRead));
                }
                response.getWriter().println("<h3>❌ Download Error</h3>");
                response.getWriter().println("<pre>" + sb.toString() + "</pre>");
            }
        } catch (Exception e) {
            response.getWriter().println("<h3>❌ Download Exception</h3>");
            e.printStackTrace(response.getWriter());
        }
    }
}
