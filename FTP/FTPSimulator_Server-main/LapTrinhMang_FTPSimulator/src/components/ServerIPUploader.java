/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package components;

import java.net.Socket;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
/**
 *
 * @author vuong
 */
public class ServerIPUploader {
    public static void uploadLocalIPToAPI() {
        try {
            // Tạo socket kết nối tạm để lấy local IP bằng cách tạo socket đến 1 website tạm
            Socket socket = new Socket("thongtindaotao.sgu.edu.vn", 80);
            String localIP = socket.getLocalAddress().toString().substring(1); // bỏ dấu "/"
            socket.close();

            // Gửi IP lên API
            String api = "https://retoolapi.dev/DoZiRh/data/1";
            String jsonData = "{\"ip\":\"" + localIP + "\"}";

            Jsoup.connect(api)
                .ignoreContentType(true)
                .ignoreHttpErrors(true)
                .header("Content-Type", "application/json")
                .requestBody(jsonData)
                .method(Connection.Method.PUT)
                .execute();

            System.out.println("Da gui IP local len API: " + localIP);
        } catch (Exception e) {
            System.err.println(" Loi khi gui IP local: " + e.getMessage());
        }
    }
}
