package ge.edu.sangu;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;

public class DiscordWebhookSender {

    public static void sendImage(String webhookUrl, File imageFile) throws IOException {
        String boundary = "----WebKitFormBoundary" + System.currentTimeMillis();
        HttpURLConnection conn = (HttpURLConnection) new URL(webhookUrl).openConnection();
        conn.setDoOutput(true);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

        try (OutputStream out = conn.getOutputStream()) {
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(out, "UTF-8"), true);
            writer.append("--").append(boundary).append("\r\n");
            writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"output.png\"\r\n");
            writer.append("Content-Type: image/png\r\n\r\n").flush();

            Files.copy(imageFile.toPath(), out);
            out.flush();

            writer.append("\r\n--").append(boundary).append("--\r\n").flush();
        }

        if (conn.getResponseCode() != 200 && conn.getResponseCode() != 204) {
            throw new IOException("Erreur Discord: " + conn.getResponseCode());
        }
    }
}
