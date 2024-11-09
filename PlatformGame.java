package game2;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;

class Obstacle {
    int x, y, width, height;
    BufferedImage image;

    public Obstacle(int x, int y, int width, int height, BufferedImage image) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.image = image;
    }

    public void draw(Graphics g) {
        g.drawImage(image, x, y, width, height, null);
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }
}

class Trap {
    int x, y, width, height;
    BufferedImage image;

    public Trap(int x, int y, int width, int height, BufferedImage image) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.image = image;
    }

    public void draw(Graphics g) {
        g.drawImage(image, x, y, width, height, null);
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }
}

class Coin {
    int x, y, width, height;
    BufferedImage image;

    public Coin(int x, int y, int width, int height, BufferedImage image) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.image = image;
    }

    public void draw(Graphics g) {
        g.drawImage(image, x, y, width, height, null);
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }
}

class ScoreReducingTrap extends Trap {
    private int scorePenalty;

    public ScoreReducingTrap(int x, int y, int width, int height, BufferedImage image, int scorePenalty) {
        super(x, y, width, height, image);
        this.scorePenalty = scorePenalty;
    }

    public int getScorePenalty() {
        return scorePenalty;
    }
}

class HealPotion {
    int x, y, width, height;
    BufferedImage image;
    private int healAmount;

    public HealPotion(int x, int y, int width, int height, BufferedImage image, int healAmount) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.image = image;
        this.healAmount = healAmount;
    }

    public void draw(Graphics g) {
        g.drawImage(image, x, y, width, height, null);
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }

    public int getHealAmount() {
        return healAmount;
    }
}

class HealthReducingTrap extends Trap { //Inheritance
    private int healthPenalty; // Number of hearts lost upon collision

    public HealthReducingTrap(int x, int y, int width, int height, BufferedImage image, int healthPenalty) {
        super(x, y, width, height, image);
        this.healthPenalty = healthPenalty;
    }

    public int getHealthPenalty() {
        return healthPenalty;
    }
}

class Enemy {
    int x, y, width, height;
    BufferedImage image;
    ArrayList<Laser> lasers;
    private int fireRate; // Specific firing rate for each enemy
    private int fireCounter = 0;

    public Enemy(int x, int y, int width, int height, BufferedImage image, int fireRate) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.image = image;
        this.lasers = new ArrayList<>();
        this.fireRate = fireRate; // Set the specific fireRate for this enemy
    }

    public void draw(Graphics g) {
        g.drawImage(image, x, y, width, height, null);
        for (Laser laser : lasers) {
            laser.draw(g);
        }
    }

    public void update() {
        fireCounter++;
        if (fireCounter >= fireRate) {
            fireCounter = 0;
            shoot();
        }
        lasers.removeIf(laser -> !laser.update());
    }

    private void shoot() {
        lasers.add(new Laser(x, y + height / 2, 20, 5)); // Shoot laser from the enemy's position
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }

    public ArrayList<Laser> getLasers() {
        return lasers;
    }
}

class Laser {
    int x, y, width, height;
    int speed = -5; // Move left

    public Laser(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public boolean update() {
        x += speed; // Laser moves to the left
        return x + width > 0; // Remove laser when it goes off the left side of the screen
    }

    public void draw(Graphics g) {
        g.setColor(Color.RED);
        g.fillRect(x, y, width, height); // Use the laser width and height settings
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }
}

public class PlatformGame extends JPanel implements ActionListener {
    private Timer timer;
    private int playerX = 50, playerY = 500;
    private int playerSpeed = 7;
    private boolean jump = false, falling = true;
    private int jumpStrength = 19, jumpVelocity;
    private BufferedImage background, caveImage;
    private ArrayList<BufferedImage> walkingFramesLeft;
    private ArrayList<BufferedImage> walkingFramesRight;
    private ArrayList<Obstacle> obstacles;
    private ArrayList<Trap> traps;
    private ArrayList<Coin> coins;
    private int frameIndex = 0;
    private int frameDelay = 5;
    private int frameCounter = 0;
    private boolean isFacingLeft = false;
    private boolean isFacingRight = true;
    private boolean movingLeft = false;
    private boolean movingRight = false;
    private Rectangle exitZone;
    private boolean levelComplete = false;
    private boolean playerDead = false;
    private int currentLevel = 1;
    private int score = 0;
    private int lives = 3; // Starting number of hearts
    private ArrayList<HealPotion> healPotions;
    private int maxLives = 3; // Set the maximum number of hearts to 3
    private ArrayList<Enemy> enemies;

    private Instant gameStartTime; // Record the game start time

    public PlatformGame() {
        walkingFramesLeft = new ArrayList<>();
        walkingFramesRight = new ArrayList<>();
        obstacles = new ArrayList<>();
        traps = new ArrayList<>();
        coins = new ArrayList<>();
        healPotions = new ArrayList<>();
        enemies = new ArrayList<>(); // Create ArrayList to store enemies
        loadLevel(currentLevel);

        timer = new Timer(20, this);
        timer.start();
        setFocusable(true);

        gameStartTime = Instant.now(); // Set the game start time

        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventDispatcher() {
            @Override
            public boolean dispatchKeyEvent(KeyEvent e) {
                int key = e.getKeyCode();
                if (e.getID() == KeyEvent.KEY_PRESSED) {
                    if (key == KeyEvent.VK_LEFT) {
                        movingLeft = true;
                        isFacingLeft = true;
                        isFacingRight = false;
                    }
                    if (key == KeyEvent.VK_RIGHT) {
                        movingRight = true;
                        isFacingRight = true;
                        isFacingLeft = false;
                    }
                    if (key == KeyEvent.VK_SPACE) {
                        if (!jump && !falling && !playerDead) {
                            jump = true;
                            jumpVelocity = jumpStrength;
                        }
                    }
                    if (key == KeyEvent.VK_UP) {
                        if (getBounds().intersects(exitZone)) {
                            levelComplete = true;
                        }
                    }
                } else if (e.getID() == KeyEvent.KEY_RELEASED) {
                    if (key == KeyEvent.VK_LEFT) {
                        movingLeft = false;
                    }
                    if (key == KeyEvent.VK_RIGHT) {
                        movingRight = false;
                    }
                }
                return false;
            }
        });
    }

    private void loadLevel(int level) {
        obstacles.clear();
        traps.clear();
        coins.clear();
        healPotions.clear();
        enemies.clear(); // Clear all enemies before loading a new level
        playerX = 50;
        playerY = 500;

        JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
        if (frame != null) {
            frame.setTitle(" Lost in the jungle - Level " + level);
        }

        try {
            walkingFramesLeft.clear();
            walkingFramesRight.clear();
            walkingFramesLeft.add(ImageIO.read(new File("D:\\Tnakim\\resources\\walk_left_1.png")));
            walkingFramesLeft.add(ImageIO.read(new File("D:\\Tnakim\\resources\\walk_left_2.png")));
            walkingFramesLeft.add(ImageIO.read(new File("D:\\Tnakim\\resources\\walk_left_3.png")));
            walkingFramesLeft.add(ImageIO.read(new File("D:\\Tnakim\\resources\\walk_left_4.png")));
            walkingFramesRight.add(ImageIO.read(new File("D:\\Tnakim\\resources\\walk_right_1.png")));
            walkingFramesRight.add(ImageIO.read(new File("D:\\Tnakim\\resources\\walk_right_2.png")));
            walkingFramesRight.add(ImageIO.read(new File("D:\\Tnakim\\resources\\walk_right_3.png")));
            walkingFramesRight.add(ImageIO.read(new File("D:\\Tnakim\\resources\\walk_right_4.png")));

            BufferedImage coinImage = ImageIO.read(new File("D:\\Tnakim\\resources\\coin.png"));
            BufferedImage reducingTrapImage = ImageIO.read(new File("D:\\Tnakim\\resources\\score_trap.png")); // Image for score-reducing trap
            BufferedImage healPotionImage = ImageIO.read(new File("D:\\Tnakim\\resources\\heal_potion.png"));
            BufferedImage healthReducingTrapImage = ImageIO.read(new File("D:\\Tnakim\\resources\\health_trap.png"));
            switch (level) {
                case 1: // Easiest level
                    background = ImageIO.read(new File("D:\\Tnakim\\resources\\background_level1.png"));
                    caveImage = ImageIO.read(new File("D:\\Tnakim\\resources\\cave.png"));
                    exitZone = new Rectangle(1100, 370, caveImage.getWidth() / 2, caveImage.getHeight() / 2);

                    obstacles.add(new Obstacle(-50, 500, 200, 30, ImageIO.read(new File("D:\\Tnakim\\resources\\obstacle.png"))));
                    obstacles.add(new Obstacle(300, 400, 200, 30, ImageIO.read(new File("D:\\Tnakim\\resources\\obstacle.png"))));
                    obstacles.add(new Obstacle(700, 400, 200, 30, ImageIO.read(new File("D:\\Tnakim\\resources\\obstacle.png"))));
                    
                    traps.add(new Trap(400, 575, 100, 20, ImageIO.read(new File("D:\\Tnakim\\resources\\trap.png"))));
                    traps.add(new Trap(500, 575, 100, 20, ImageIO.read(new File("D:\\Tnakim\\resources\\trap.png"))));
                    traps.add(new Trap(600, 575, 100, 20, ImageIO.read(new File("D:\\Tnakim\\resources\\trap.png"))));
                    traps.add(new Trap(700, 575, 100, 20, ImageIO.read(new File("D:\\Tnakim\\resources\\trap.png"))));
                    traps.add(new Trap(800, 575, 100, 20, ImageIO.read(new File("D:\\Tnakim\\resources\\trap.png"))));
                    traps.add(new ScoreReducingTrap(300, 340, 60, 60, reducingTrapImage, 5)); // Score reduction by 5
                    healPotions.add(new HealPotion(850, 340, 30, 60, healPotionImage, 1)); // HealPotion that restores 1 health
                    traps.add(new HealthReducingTrap(450, 340, 50, 50, healthReducingTrapImage, 1)); // Reduces health by 1

                    coins.add(new Coin(200, 340, 50, 50, coinImage));
                    enemies.add(new Enemy(900, 520, 80, 80, ImageIO.read(new File("D:\\Tnakim\\resources\\enemy.png")), 20)); // Fires every 20 frames
                    break;

                case 2: // Increased complexity
                    background = ImageIO.read(new File("D:\\Tnakim\\resources\\background_level2.png"));
                    caveImage = ImageIO.read(new File("D:\\Tnakim\\resources\\cave.png"));
                    exitZone = new Rectangle(1100, 300, caveImage.getWidth() / 2, caveImage.getHeight() / 2);

                    obstacles.add(new Obstacle(300, 450, 150, 30, ImageIO.read(new File("D:\\Tnakim\\resources\\obstacle.png"))));
                    obstacles.add(new Obstacle(300, 220, 150, 30, ImageIO.read(new File("D:\\Tnakim\\resources\\obstacle.png"))));
                    obstacles.add(new Obstacle(700, 220, 150, 30, ImageIO.read(new File("D:\\Tnakim\\resources\\obstacle.png"))));
                    obstacles.add(new Obstacle(800, 220, 150, 30, ImageIO.read(new File("D:\\Tnakim\\resources\\obstacle.png"))));
                    obstacles.add(new Obstacle(500, 400, 150, 30, ImageIO.read(new File("D:\\Tnakim\\resources\\obstacle.png"))));
                    obstacles.add(new Obstacle(900, 400, 150, 30, ImageIO.read(new File("D:\\Tnakim\\resources\\obstacle.png"))));
                    obstacles.add(new Obstacle(1100, 550, 180, 30, ImageIO.read(new File("D:\\Tnakim\\resources\\obstacle.png"))));

                    traps.add(new Trap(580, 390, 60, 20, ImageIO.read(new File("D:\\Tnakim\\resources\\trap.png"))));   
                    healPotions.add(new HealPotion(980, 350 , 30, 60, healPotionImage, 1)); // HealPotion that restores 1 health
                    healPotions.add(new HealPotion(300, 170, 30, 60, healPotionImage, 1));
                    enemies.add(new Enemy(900, 150, 80, 80, ImageIO.read(new File("D:\\Tnakim\\resources\\enemy.png")), 70)); 

                    traps.add(new Trap(400, 575, 100, 20, ImageIO.read(new File("D:\\Tnakim\\resources\\trap.png")))); 
                    traps.add(new Trap(500, 575, 100, 20, ImageIO.read(new File("D:\\Tnakim\\resources\\trap.png")))); 
                    traps.add(new Trap(600, 575, 100, 20, ImageIO.read(new File("D:\\Tnakim\\resources\\trap.png")))); 
                    traps.add(new Trap(700, 575, 100, 20, ImageIO.read(new File("D:\\Tnakim\\resources\\trap.png")))); 
                    traps.add(new Trap(800, 575, 100, 20, ImageIO.read(new File("D:\\Tnakim\\resources\\trap.png")))); 
                    
                    enemies.add(new Enemy(900, 520, 80, 80, ImageIO.read(new File("D:\\Tnakim\\resources\\enemy.png")), 30)); // Fires every 30 frames
                    coins.add(new Coin(200, 420, 30, 30, coinImage));
                    coins.add(new Coin(450, 390, 30, 30, coinImage));
                    break;

                case 3: // Increased difficulty
                    background = ImageIO.read(new File("D:\\Tnakim\\resources\\background_level3.png"));
                    caveImage = ImageIO.read(new File("D:\\Tnakim\\resources\\cave.png"));
                    exitZone = new Rectangle(700, 370, caveImage.getWidth() / 2, caveImage.getHeight() / 2);

                    obstacles.add(new Obstacle(300, 450, 150, 30, ImageIO.read(new File("D:\\Tnakim\\resources\\obstacle.png"))));
                    traps.add(new ScoreReducingTrap(350, 390, 70, 70, reducingTrapImage, 10));
                    obstacles.add(new Obstacle(0, 350, 150, 30, ImageIO.read(new File("D:\\Tnakim\\resources\\obstacle.png"))));
                    coins.add(new Coin(100, 300, 30, 30, coinImage));
                    obstacles.add(new Obstacle(300, 250, 150, 30, ImageIO.read(new File("D:\\Tnakim\\resources\\obstacle.png"))));
                    traps.add(new HealthReducingTrap(350, 180, 70, 70, healthReducingTrapImage, 1));
                    
                    obstacles.add(new Obstacle(1000, 400, 150, 30, ImageIO.read(new File("D:\\Tnakim\\resources\\obstacle.png"))));
                    
                    obstacles.add(new Obstacle(600, 300, 150, 30, ImageIO.read(new File("D:\\Tnakim\\resources\\obstacle.png"))));
                    obstacles.add(new Obstacle(700, 300, 150, 30, ImageIO.read(new File("D:\\Tnakim\\resources\\obstacle.png"))));
                    obstacles.add(new Obstacle(800, 300, 150, 30, ImageIO.read(new File("D:\\Tnakim\\resources\\obstacle.png"))));
                    healPotions.add(new HealPotion(800, 250, 30, 60, healPotionImage, 1));
                    coins.add(new Coin(700, 260, 30, 30, coinImage));

                    traps.add(new Trap(400, 575, 100, 20, ImageIO.read(new File("D:\\Tnakim\\resources\\trap.png")))); 
                    traps.add(new Trap(500, 575, 100, 20, ImageIO.read(new File("D:\\Tnakim\\resources\\trap.png")))); 
                    traps.add(new Trap(600, 575, 100, 20, ImageIO.read(new File("D:\\Tnakim\\resources\\trap.png")))); 

                    traps.add(new ScoreReducingTrap(600, 240, 70, 70, reducingTrapImage, 10));
                    enemies.add(new Enemy(880, 220, 80, 80, ImageIO.read(new File("D:\\Tnakim\\resources\\enemy.png")), 90)); 
                    enemies.add(new Enemy(1100, 330, 80, 80, ImageIO.read(new File("D:\\Tnakim\\resources\\enemy.png")), 70)); 
                    enemies.add(new Enemy(1250, 520, 80, 80, ImageIO.read(new File("D:\\Tnakim\\resources\\enemy.png")), 50)); 
                    coins.add(new Coin(1100, 560, 30, 30, coinImage));
                    healPotions.add(new HealPotion(1000, 540, 30, 60, healPotionImage, 1)); 

                    coins.add(new Coin(200, 540, 30, 30, coinImage));
                    coins.add(new Coin(300, 540, 30, 30, coinImage));
                    
                    break;

                default:
                    JOptionPane.showMessageDialog(this, "You have completed all levels!");
                    returnToMenu();
                    break;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(background, 0, 0, getWidth(), getHeight(), this);
        g.drawImage(caveImage, exitZone.x, exitZone.y, exitZone.width, exitZone.height, this);
        for (Obstacle obstacle : obstacles) obstacle.draw(g);
        for (Trap trap : traps) trap.draw(g);
        for (Coin coin : coins) coin.draw(g);
        for (HealPotion healPotion : healPotions) healPotion.draw(g);
        for (Enemy enemy : enemies) {
        enemy.draw(g);
           }
        // Display the score
        g.setFont(new Font("Arial", Font.BOLD, 16));
        g.setColor(Color.WHITE);
        g.drawString("Score: " + score, 10, 20);

        // Display the number of lives
        g.drawString("Lives: " + lives, 10, 40);

        // Display play time at the top right corner
        long playTimeMillis = Duration.between(gameStartTime, Instant.now()).toMillis();
        long playTimeSeconds = playTimeMillis / 1000;
        long milliseconds = playTimeMillis % 1000;
        String formattedTime = String.format("%02d:%02d.%03d", playTimeSeconds / 60, playTimeSeconds % 60, milliseconds);
        int stringWidth = g.getFontMetrics().stringWidth(formattedTime);
        g.drawString(formattedTime, getWidth() - stringWidth - 10, 20);

        // Display the character
        if (!playerDead) {
            if (isFacingLeft && !walkingFramesLeft.isEmpty()) {
                g.drawImage(walkingFramesLeft.get(frameIndex), playerX, playerY, 100, 100, this);
            } else if (isFacingRight && !walkingFramesRight.isEmpty()) {
                g.drawImage(walkingFramesRight.get(frameIndex), playerX, playerY, 100, 100, this);
            }
        }

        // Display message when the level is completed
        if (levelComplete) {
            g.setFont(new Font("Arial", Font.BOLD, 30));
            g.setColor(Color.BLACK);
            g.drawString("Level Complete! Go to next level!", getWidth() / 2 - 150, 200);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (playerDead) {
            timer.stop();
            handleGameOver();
            return;
        }

        if (movingLeft) playerX -= playerSpeed;
        if (movingRight) playerX += playerSpeed;
        if (falling) playerY += 8;

        if (jump) {
            playerY -= jumpVelocity;
            jumpVelocity -= 1;
            if (jumpVelocity <= 0) {
                jump = false;
                falling = true;
            }
        }

        if (playerY >= 500) {
            playerY = 500;
            jump = false;
            falling = false;
            jumpVelocity = jumpStrength;
        }

        boolean onObstacle = false;

        for (Obstacle obstacle : obstacles) {
            if (getBounds().intersects(obstacle.getBounds())) {
                Rectangle playerBounds = getBounds();
                Rectangle obstacleBounds = obstacle.getBounds();

                if (playerBounds.y + playerBounds.height > obstacleBounds.y &&
                    playerBounds.y + playerBounds.height - 8 <= obstacleBounds.y) {
                    playerY = obstacleBounds.y - playerBounds.height;
                    falling = false;
                    jump = false;
                    onObstacle = true;
                } else if (playerBounds.y < obstacleBounds.y + obstacleBounds.height &&
                           playerBounds.y >= obstacleBounds.y + obstacleBounds.height - 8) {
                    playerY = obstacleBounds.y + obstacleBounds.height;
                    jump = false;
                    falling = true;
                }
            }
        }
        for (Enemy enemy : enemies) {
            enemy.update();

            // Check for collisions between laser and player
            for (int i = 0; i < enemy.getLasers().size(); i++) {
                Laser laser = enemy.getLasers().get(i);
                if (laser.getBounds().intersects(getBounds())) {
                    lives--;  // Reduce player HP
                    enemy.getLasers().remove(i);  // Remove laser from screen when it hits the player
                    i--;  // Decrement counter to avoid skipping laser in the loop
                    if (lives <= 0) {
                        playerDead = true;  // Set playerDead flag when HP runs out
                    }
                    break;  // Exit laser collision check loop immediately upon collision
                }
            }
        }

        if (!onObstacle && !jump && playerY < 500) falling = true;

        // Use Iterator to remove only ScoreReducingTrap when colliding
        for (Trap trap : traps) {
            if (getBounds().intersects(trap.getBounds())) {
                if (trap instanceof ScoreReducingTrap) {
                    // Reduce score and remove ScoreReducingTrap
                    score -= ((ScoreReducingTrap) trap).getScorePenalty();
                    score = Math.max(score, 0); // Ensure score doesn't go negative
                    traps.remove(trap); // Remove ScoreReducingTrap from list
                    break; // Stop loop after removal
                } else if (trap instanceof HealthReducingTrap) {
                    // If HealthReducingTrap, reduce health and remove trap
                    lives -= ((HealthReducingTrap) trap).getHealthPenalty();
                    traps.remove(trap); // Remove HealthReducingTrap from list
                    if (lives <= 0) { // If lives are zero, the player is dead
                        playerDead = true;
                    }
                    break; // Stop loop after removal
                } else {
                    // For normal trap (not ScoreReducingTrap or HealthReducingTrap)
                    lives--;
                    if (lives <= 0) {
                        playerDead = true;
                    }
                    break; // Stop loop
                }
            }
        }

                coins.removeIf(coin -> {
            if (getBounds().intersects(coin.getBounds())) {
                score += 10;
                return true;
            }
            return false;
        });

        healPotions.removeIf(healPotion -> {
            if (getBounds().intersects(healPotion.getBounds())) {
                // Increase lives, ensuring they do not exceed maxLives
                lives = Math.min(lives + healPotion.getHealAmount(), maxLives);
                return true;
            }
            return false;
        });

        if (levelComplete) {
            currentLevel++;
            levelComplete = false;
            loadLevel(currentLevel);
        }

        frameCounter++;
        if (frameCounter >= frameDelay) {
            frameIndex = (frameIndex + 1) % (isFacingLeft ? walkingFramesLeft.size() : walkingFramesRight.size());
            frameCounter = 0;
        }

        repaint();
    }

    private void handleGameOver() {
        timer.stop();
        int choice = JOptionPane.showOptionDialog(
            this,
            "You Died! What would you like to do?",
            "Game Over",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            new String[]{"Restart", "Menu"},
            "Restart"
        );

        if (choice == JOptionPane.YES_OPTION) {
            // Restart game, continue with the current timing
            restartGame();
            timer.start();
        } else if (choice == JOptionPane.NO_OPTION) {
            // Ask for player name when choosing to return to the menu
            String playerName = JOptionPane.showInputDialog("Enter your name:");
            if (playerName != null && !playerName.trim().isEmpty()) {
                Instant gameEndTime = Instant.now();
                long playTimeMillis = Duration.between(gameStartTime, gameEndTime).toMillis();
                DatabaseHelper.savePlayerData(playerName, score, playTimeMillis);
            }
            returnToMenu();
        }
    }

    private void restartGame() {
        playerX = 50;
        playerY = 500;
        playerDead = false;
        levelComplete = false;
        score = 0;
        lives = 3; // Reset hearts to 3

        for (Enemy enemy : enemies) {
            enemy.getLasers().clear(); // Clear all lasers from each enemy
        }
        loadLevel(currentLevel); 
        repaint();
    }

    private void returnToMenu() {
        JFrame menuFrame = new MenuScreen();
        menuFrame.setVisible(true);
        SwingUtilities.getWindowAncestor(this).dispose();
    }

    public Rectangle getBounds() {
        return new Rectangle(playerX + 5, playerY, 80, 100); // Extend height for accurate bounding area
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Lost in the jungle - Level 1");
        PlatformGame game = new PlatformGame();
        frame.add(game);
        frame.setSize(1366, 768);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}

