/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package app.components;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.json.JSONObject;
/**
 *
 * @author vuong
 */
public class ClientIPFetcher {
    public static String getServerIPFromAPI() {
        try {
            String api = "https://retoolapi.dev/DoZiRh/data/1";
            Document doc = Jsoup.connect(api)
                    .ignoreContentType(true).ignoreHttpErrors(true)
                    .header("Content-Type", "application/json")
                    .method(Connection.Method.GET)
                    .execute()
                    .parse();

            JSONObject jsonObject = new JSONObject(doc.text());
            String ip = jsonObject.getString("ip");
            System.out.println("Server IP lay tu API: " + ip);
            return ip;
        } catch (Exception e) {
            e.printStackTrace();
            return "127.0.0.1"; // fallback nếu lỗi
        }
    }
}
