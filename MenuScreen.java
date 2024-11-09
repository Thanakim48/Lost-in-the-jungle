package game2;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class MenuScreen extends JFrame {
    private BufferedImage backgroundImage;
    private String[] bestPlayersInfo;

    public MenuScreen() {
        setTitle("Lost in the Jungle - Menu");
        setSize(800, 600);
        setLocationRelativeTo(null); // Center the menu screen
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Load background image
        try {
            backgroundImage = ImageIO.read(new File("/resources/background.png/"));
        } catch (IOException e) {
            System.out.println("Cannot load background image. Please check the file path!");
            e.printStackTrace();
        }

        // Fetch best players info
        bestPlayersInfo = getBestPlayersInfo();

        // Set up the main menu
        MenuPanel panel = new MenuPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel("Lost in the Jungle");
        titleLabel.setFont(new Font("Serif", Font.BOLD, 42)); // Change font and size for title
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setForeground(new Color(34, 139, 34)); // Dark green to match jungle theme

        JButton startButton = new JButton("Start Game");
        startButton.setFont(new Font("Serif", Font.BOLD, 26)); // Adjust font size and style
        startButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        startButton.addActionListener(new ActionListener() {
    @Override
    public void actionPerformed(ActionEvent e) {
        dispose(); // Close menu
        JFrame gameFrame = new JFrame("Lost in the Jungle - Level 1");
        PlatformGame game = new PlatformGame();
        gameFrame.add(game);
        gameFrame.setSize(1366, 768); // เปลี่ยนขนาดเป็น 1366 x 768
        gameFrame.setLocationRelativeTo(null); // Center game window
        gameFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        gameFrame.setVisible(true);
    }
        });

        JButton exitButton = new JButton("Exit");
        exitButton.setFont(new Font("Serif", Font.BOLD, 26)); // Adjust font size and style
        exitButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        exitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0); // Exit program
            }
        });

        // Add components to the panel
        panel.add(Box.createVerticalGlue());
        panel.add(titleLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 50)));
        panel.add(startButton);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        panel.add(exitButton);
        panel.add(Box.createVerticalGlue());

        add(panel);
    }

    // Fetch top players from the database
    private String[] getBestPlayersInfo() {
        String dbUrl = "jdbc:mysql://localhost:3306/gameDB";
        String dbUser = "root";
        String dbPassword = "1234"; // Change to match your password

        String[] bestPlayersInfo = new String[5];
        String query = "SELECT PlayerName, Score, PlayTime FROM PlayerHistory ORDER BY Score DESC, PlayTime ASC LIMIT 5";

        try (Connection connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            int index = 0;
            while (resultSet.next() && index < 5) {
                String playerName = resultSet.getString("PlayerName");
                int score = resultSet.getInt("Score");
                long playTimeMillis = resultSet.getLong("PlayTime");

                long playTimeSeconds = playTimeMillis / 1000;
                long milliseconds = playTimeMillis % 1000;
                String formattedTime = String.format("%02d:%02d.%03d", playTimeSeconds / 60, playTimeSeconds % 60, milliseconds);

                bestPlayersInfo[index] = String.format("Rank %d: %s | Score: %d | Time: %s", index + 1, playerName, score, formattedTime);
                index++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bestPlayersInfo;
    }

    // Inner class to handle background drawing and display top players info
    private class MenuPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (backgroundImage != null) {
                g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
            }

            g.setFont(new Font("SansSerif", Font.PLAIN, 16));
            g.setColor(Color.WHITE);

            // Draw top 5 players info in the top right corner
            String[] bestPlayersInfo = getBestPlayersInfo();
            int y = 20; // Starting Y position for the first line
            for (String info : bestPlayersInfo) {
                if (info != null) {
                    g.drawString(info, getWidth() - g.getFontMetrics().stringWidth(info) - 10, y);
                    y += 20; // Move Y position down for each line
                }
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                MenuScreen menu = new MenuScreen();
                menu.setVisible(true);
            }
        });
    }
}
