package game2;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DatabaseHelper {
    private static final String URL = "jdbc:mysql://localhost:3306/gameDB"; // เปลี่ยนชื่อฐานข้อมูลตามที่คุณตั้งไว้
    private static final String USER = "root"; // ชื่อผู้ใช้ของคุณ
    private static final String PASSWORD = "1234"; // รหัสผ่านของคุณ

    public static Connection connect() {
        try {
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void savePlayerData(String playerName, int score, long playTimeMillis) {
    String dbUrl = "jdbc:mysql://localhost:3306/gameDB";
    String dbUser = "root";
    String dbPassword = "1234"; // เปลี่ยนให้ตรงกับรหัสผ่านของคุณ

    String insertQuery = "INSERT INTO PlayerHistory (PlayerName, Score, PlayTime) VALUES (?, ?, ?)";

    try (Connection connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
         PreparedStatement preparedStatement = connection.prepareStatement(insertQuery)) {

        preparedStatement.setString(1, playerName);
        preparedStatement.setInt(2, score);
        preparedStatement.setLong(3, playTimeMillis);

        preparedStatement.executeUpdate();
    } catch (Exception e) {
        e.printStackTrace();
    }
}
}
