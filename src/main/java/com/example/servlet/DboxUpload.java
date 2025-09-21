package com.example.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/DboxUpload")
@MultipartConfig
public class DboxUpload extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");

        // Get access token from session
        HttpSession session = request.getSession();
        String accessToken = (String) session.getAttribute("accessToken");
        if (accessToken == null) {
            response.getWriter().println("No access token found. Please <a href='DboxAuth'>connect</a> first.");
            return;
        }

        try {
            Part filePart = request.getPart("file");
            String fileName = filePart.getSubmittedFileName();

            java.net.URL url = new java.net.URL("https://content.dropboxapi.com/2/files/upload");
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + accessToken);
            conn.setRequestProperty("Dropbox-API-Arg", "{\"path\": \"/" + fileName + "\",\"mode\": \"add\",\"autorename\": true,\"mute\": false}");
            conn.setRequestProperty("Content-Type", "application/octet-stream");
            conn.setDoOutput(true);

            // Upload file data
            try (InputStream is = filePart.getInputStream(); OutputStream os = conn.getOutputStream()) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
            }

            int status = conn.getResponseCode();
            if (status == 200) {
                response.getWriter().println("<h2>⬆️ File uploaded to Dropbox: " + fileName + "</h2>");
            } else {
                // Read error stream manually (Java 8 compatible)
                InputStream errorStream = conn.getErrorStream();
                StringBuilder sb = new StringBuilder();
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = errorStream.read(buffer)) != -1) {
                    sb.append(new String(buffer, 0, bytesRead));
                }
                response.getWriter().println("<h3>❌ Upload Error</h3>");
                response.getWriter().println("<pre>" + sb.toString() + "</pre>");
            }

        } catch (Exception e) {
            response.getWriter().println("<h3>❌ Upload Exception</h3>");
            e.printStackTrace(response.getWriter());
        }
    }
}
