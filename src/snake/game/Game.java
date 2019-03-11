package snake.game;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Random;

import javax.swing.JFrame;

import snake.elements.Apple;
import snake.elements.Player;

@SuppressWarnings("serial")
public class Game extends JFrame implements KeyListener{
	private static final Cursor INVISIBLE_CURSOR = Toolkit.getDefaultToolkit().createCustomCursor(
			Toolkit.getDefaultToolkit().getImage(""),
			new Point(0,0),
			"invisible");
	
	public static final int SNAKE_SIZE = 10;
	private static final int PANEL_HEIGHT = 100;
	
	private static final int MODE_MAIN_MENU = 0;
	private static final int MODE_GAME_START = 1;
	private static final int MODE_GAME_PLAY = 2;
	private static final int MODE_GAME_OVER = 3;
	private static final int MODE_GAME_PAUSE = 4;
	
	private static final int MENU_START = 0;
	private static final int MENU_EXIT = 1;
	
	private static final int MENU_PAUSE_CONTINUE= 0;
	private static final int MENU_PAUSE_RESTART = 1;
	private static final int MENU_PAUSE_EXIT = 2;
	
	private static final int APPLE_START_COUNT_GOOD = 10;
	private static final int APPLE_START_COUNT_BAD = 5;

	private static final int APPLE_TARGET_COUNT_GOOD = 3;
	private static final int APPLE_TARGET_COUNT_BAD = 25;

	private static final int APPLE_COUNT_TELE = 2;

	private static final int START_LIVES = 3;
	
	private int mode;
	private int menuId;
	private int pauseId;
	
	private BufferedImage backgroundImage;
	private int gameWait = 0;
	
	private Dimension screenSize;
	
	private int fps;
	private long targetTime;

	private boolean running;
	
	private BufferedImage image;
	private Graphics2D g2Image;

	private Rectangle bounds;
	private Color colorLime;
	
	private Player player;
	private ArrayList<Apple> apples;

	private int[] appleCount = new int[3];

	private int lives;
	public int score;

	private boolean doResetScoreOnDeath = false;
	
	public Game() {
		lives = START_LIVES;

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		setUndecorated(true);
		setExtendedState(this.getExtendedState() | JFrame.MAXIMIZED_BOTH);
		setResizable(true);
		setIgnoreRepaint(true);
		
		addKeyListener(this);
		
		setCursor(INVISIBLE_CURSOR);
		
		setVisible(true);
	}
	
	public void init() {
		fps = 50;
		targetTime = 1000 / fps;
		
		screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		image = new BufferedImage(screenSize.width, screenSize.height,
				BufferedImage.TYPE_INT_ARGB);
		g2Image = image.createGraphics();
		
		int gridX = screenSize.width / SNAKE_SIZE - 1;
		int gridY = (screenSize.height - PANEL_HEIGHT) / SNAKE_SIZE - 1;
		
		bounds = new Rectangle();
		bounds.width = gridX * SNAKE_SIZE;
		bounds.height = gridY * SNAKE_SIZE;
		
		if ((bounds.width / 2) % SNAKE_SIZE != SNAKE_SIZE / 2) bounds.width -= SNAKE_SIZE;
		if ((bounds.height / 2) % SNAKE_SIZE != SNAKE_SIZE / 2) bounds.height -= SNAKE_SIZE;
		
		bounds.x = (screenSize.width - bounds.width) / 2;
		bounds.y = (screenSize.height - PANEL_HEIGHT - bounds.height) / 2 + PANEL_HEIGHT;
		
		colorLime = new Color(192, 255, 0, 255);
		
		apples = new ArrayList<Apple>();
		
		mode = MODE_MAIN_MENU;
		menuId = 0;
		running = true;
	}
	
	private void makeScreenShot() {
		backgroundImage = new BufferedImage(screenSize.width, screenSize.height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = backgroundImage.createGraphics();
		g2.drawImage(image, 0, 0, screenSize.width, screenSize.height, null);
		g2.dispose();
	}
	
	private void initGame() {
		player = new Player(this, bounds.width / 2, bounds.height / 2, SNAKE_SIZE, colorLime, bounds);
		
		if (doResetScoreOnDeath) score = 0;
		
		appleCount[Apple.APPLE_GOOD] = 0;
		appleCount[Apple.APPLE_BAD] = 0;
		appleCount[Apple.APPLE_TELE] = 0;

		apples.clear();
		createApples(APPLE_START_COUNT_GOOD, Apple.APPLE_GOOD);
		createApples(APPLE_START_COUNT_BAD, Apple.APPLE_BAD);
		createApples(APPLE_COUNT_TELE, Apple.APPLE_TELE);

		mode = MODE_GAME_PLAY;
	}
	
	private void balanceApples(int typeEaten) {
		if (typeEaten != Apple.APPLE_GOOD
		||  appleCount[Apple.APPLE_GOOD] < APPLE_TARGET_COUNT_GOOD) {
			createApples(1, typeEaten);
		} else {
			int goodAppleDelta = APPLE_START_COUNT_GOOD - APPLE_TARGET_COUNT_GOOD;
			int  badAppleDelta = APPLE_TARGET_COUNT_BAD - APPLE_START_COUNT_BAD;

			int applesToCreate = (int)Math.ceil((float)badAppleDelta / (float)goodAppleDelta);

			if (appleCount[Apple.APPLE_BAD] + applesToCreate > APPLE_TARGET_COUNT_BAD)
			        applesToCreate = APPLE_TARGET_COUNT_BAD - appleCount[Apple.APPLE_BAD];

			createApples(applesToCreate, Apple.APPLE_BAD);
		}
	}

	private void createApples(int count, int appleType) {
		Apple apple;
		int x, y;
		int loop = 0;
		int index;
		boolean error;
		
		while (loop < count) {
			do {				
				x = new Random().nextInt(bounds.width / SNAKE_SIZE) * SNAKE_SIZE + SNAKE_SIZE / 2;
				y = new Random().nextInt(bounds.height / SNAKE_SIZE) * SNAKE_SIZE + SNAKE_SIZE / 2;
				
				error = player.contains(x, y);
				index = 0;
				while (!error && index < apples.size()) {
					apple = apples.get(index);
					error = apple.contains(x, y);
					index++;
				}
			} while (error);
			apple = new Apple(this, x, y, appleType, bounds);
			apples.add(apple);
			loop++;
		}
		appleCount[appleType] += count;
	}
	
	public void run() {
		long startTime;
		long elapsedTime;
		long waitTime;
		
		int index;
		Apple apple;
		boolean found;
		
		while (running) {
			if (mode == MODE_MAIN_MENU) {
				draw();
				drawToScreen();
			} else if (mode == MODE_GAME_START || mode == MODE_GAME_PLAY) {
				if (mode == MODE_GAME_START) initGame();
				
				while(mode != MODE_GAME_PAUSE && player.isAlive()) {
					startTime = System.currentTimeMillis();
					
					player.update();
					
					index = 0;
					found = false;
					apple = null;
					while (!found && index < apples.size()) {
						apple = apples.get(index);
						if (apple.contains(player.getX(), player.getY())) {
							found = true; 
						} else {
							index++;
						}
					}
					
					if (found) {
						player.eat(apple);
						apples.remove(index);
						appleCount[apple.getType()]--;
						balanceApples(apple.getType());
					}
					
					if (!player.collision()) {
						draw();
						drawToScreen();
					}
					
					elapsedTime = System.currentTimeMillis() - startTime;
					waitTime = targetTime - elapsedTime;
					
					if (waitTime > 0) {
						try {
							Thread.sleep(waitTime);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					
				}
				if (mode != MODE_GAME_PAUSE) {
					if (lives <= 0) {
						makeScreenShot();				
						mode = MODE_GAME_OVER;
						lives = START_LIVES;
						doResetScoreOnDeath = true;
					} else {
						doResetScoreOnDeath = false;
						lives--;
						mode = MODE_GAME_START;
					}
				}
			} else if (mode == MODE_GAME_OVER) {
				gameWait = 1 - gameWait;		

				draw();
				drawToScreen();
				
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} else if (mode == MODE_GAME_PAUSE) {
				draw();
				drawToScreen();
			}
		}
		
		dispose();
	}
	
	private void draw() {
		if (mode == MODE_MAIN_MENU) {
			String strTitle = "SNAKE";
			String strItems[] = {"Start", "Exit"};
			
			g2Image.setColor(Color.black);
			g2Image.fillRect(0, 0, screenSize.width, screenSize.height);
			
			g2Image.setFont(new Font("System", Font.PLAIN, 72));
			g2Image.setColor(colorLime);
			
			g2Image.drawString(strTitle, (screenSize.width - g2Image.getFontMetrics().stringWidth(strTitle)) / 2, 200);
			
			g2Image.setFont(new Font("System", Font.PLAIN, 36));
			
			for (int i = 0; i < strItems.length; i++) {
				if (menuId == i) {
					g2Image.setColor(colorLime);
				} else {
					g2Image.setColor(Color.lightGray);
				}
				
				g2Image.drawString(strItems[i], (screenSize.width - g2Image.getFontMetrics().stringWidth(strItems[i])) / 2, 340 + i*60);
			}
		} else if (mode == MODE_GAME_PLAY) {
			g2Image.setColor(colorLime);
			g2Image.fillRect(0, 0, getWidth(), getHeight());
			
			g2Image.setColor(Color.black);
			g2Image.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
			
			player.draw(g2Image);
			
			for (int index = 0; index < apples.size(); index++) apples.get(index).draw(g2Image);
			
			String scoreStr = "SCORE: 00000"; 
			int x;
			
			g2Image.setFont(new Font("system", Font.PLAIN, 36));
			x = screenSize.width - g2Image.getFontMetrics().stringWidth(scoreStr) - 20;
			
			scoreStr = String.valueOf(score); // "13"
			for (int index = 5 - scoreStr.length(); index > 0; index--) { // "00013"
				scoreStr = "0" + scoreStr;
			}
			scoreStr = "SCORE: " + scoreStr; // "SCORE: 00013"
			
			g2Image.setColor(Color.black);
			g2Image.drawString(scoreStr, x, 
					(int)((PANEL_HEIGHT - g2Image.getFontMetrics().getStringBounds(scoreStr, g2Image).getHeight()) / 2
					+ g2Image.getFontMetrics().getAscent()));
			
			String livesStr = "LIVES: 3"; 
			
			g2Image.setFont(new Font("system", Font.PLAIN, 36));
			x = 20;
			
			livesStr = String.valueOf(lives); // "3"
			livesStr = "LIVES: " + livesStr; // "LIVES: 3"
			
			g2Image.setColor(Color.black);
			g2Image.drawString(livesStr, x, 
					(int)((PANEL_HEIGHT - g2Image.getFontMetrics().getStringBounds(scoreStr, g2Image).getHeight()) / 2
					+ g2Image.getFontMetrics().getAscent()));
		} else if (mode == MODE_GAME_OVER) {
			String strGameOver = "GAME OVER";
			
			g2Image.drawImage(backgroundImage, 0, 0, screenSize.width, screenSize.height, null);
			
			g2Image.setFont(new Font("System", Font.PLAIN, 72));
			if (gameWait == 0) {
				g2Image.setColor(Color.red);
			} else {
				g2Image.setColor(Color.lightGray);
			}
			
			g2Image.drawString(strGameOver,
					(screenSize.width - g2Image.getFontMetrics().stringWidth(strGameOver)) / 2,
					(int) ((screenSize.height - g2Image.getFontMetrics().getStringBounds(strGameOver, g2Image).getHeight()) / 2
					+ g2Image.getFontMetrics().getAscent()));
			
			g2Image.setFont(new Font("System", Font.PLAIN, 36));
		} else if (mode == MODE_GAME_PAUSE) {
			String strTitle = "PAUSE";
			String strItems[] = {"Continue", "Restart", "Exit"};
			
			g2Image.setColor(Color.black);
			g2Image.fillRect(0, 0, screenSize.width, screenSize.height);
			
			g2Image.drawImage(backgroundImage, 0, 0, screenSize.width, screenSize.height, null);
			
			g2Image.setFont(new Font("System", Font.PLAIN, 72));
			g2Image.setColor(colorLime);
			
			g2Image.drawString(strTitle, (screenSize.width - g2Image.getFontMetrics().stringWidth(strTitle)) / 2, 200);
			
			g2Image.setFont(new Font("System", Font.PLAIN, 36));
			
			for (int i = 0; i < strItems.length; i++) {
				if (pauseId == i) {
					g2Image.setColor(colorLime);
				} else {
					g2Image.setColor(Color.lightGray);
				}
				
				g2Image.drawString(strItems[i], (screenSize.width - g2Image.getFontMetrics().stringWidth(strItems[i])) / 2, 340 + i*60);
			}
		}
	}
	
	private void drawToScreen() {
		Graphics2D g2 = (Graphics2D) getGraphics();
		g2.drawImage(image, 0, 0, screenSize.width, screenSize.height, null);
		g2.dispose();
	}

	@Override
	public void keyPressed(KeyEvent event) {
		if (mode == MODE_MAIN_MENU) {
			if (event.getKeyCode() == KeyEvent.VK_UP) {
				menuId--;
				if (menuId < 0) menuId = MENU_EXIT;
			}
			if (event.getKeyCode() == KeyEvent.VK_DOWN) {
				menuId++;
				if (menuId > MENU_EXIT) menuId = 0;
			}
			if (event.getKeyCode() == KeyEvent.VK_ENTER) {
				switch (menuId) {
				case MENU_START:
					mode = MODE_GAME_START;
					break;
				case MENU_EXIT:
					running = false;
					break;
				}
			}
		} else if (mode == MODE_GAME_PLAY) {
			if (event.getKeyCode() == KeyEvent.VK_UP) player.up();
			if (event.getKeyCode() == KeyEvent.VK_DOWN) player.down();
			if (event.getKeyCode() == KeyEvent.VK_LEFT) player.left();
			if (event.getKeyCode() == KeyEvent.VK_RIGHT) player.right();
			if (event.getKeyCode() == KeyEvent.VK_P) {	pauseId = 0; mode = MODE_GAME_PAUSE; makeScreenShot(); }
		} else if (mode == MODE_GAME_OVER) {
			if (event.getKeyCode() == KeyEvent.VK_ENTER) mode = MODE_MAIN_MENU;
		} else if (mode == MODE_GAME_PAUSE) {
			if (event.getKeyCode() == KeyEvent.VK_UP) {
				pauseId--;
				if (pauseId < 0) pauseId = MENU_PAUSE_EXIT;
			}
			if (event.getKeyCode() == KeyEvent.VK_DOWN) {
				pauseId++;
				if (pauseId > MENU_PAUSE_EXIT) pauseId = 0;
			}
			if (event.getKeyCode() == KeyEvent.VK_ENTER) {
				switch (pauseId) {
				case MENU_PAUSE_CONTINUE:
					mode = MODE_GAME_PLAY;
					break;
				case MENU_PAUSE_RESTART:
					mode = MODE_GAME_START;
					break;
				case MENU_PAUSE_EXIT:
					mode = MODE_MAIN_MENU;
					break;
				}
			}
		}	
	}

	@Override
	public void keyReleased(KeyEvent arg0) {}

	@Override
	public void keyTyped(KeyEvent arg0) {}
	
	public ArrayList<Apple> getApples() {
		return apples;
	}
	
}
