package snake.elements;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Random;

import snake.game.Game;

public class Player extends Element {
	
	private static final int MOVE_NONE = -1;
	private static final int MOVE_UP = 0;
	private static final int MOVE_DOWN = 1;
	private static final int MOVE_LEFT = 2;
	private static final int MOVE_RIGHT = 3;
	
	private int movingDirection;
	private boolean alive;
	
	private Apple apple;
	
	private ArrayList<Point> tail;
	
	public int score = 0;
	
	public Player(Game game, int x, int y, int size, Color color, Rectangle bounds) {
		super(game, x, y, size, color, bounds);
		
		movingDirection = MOVE_NONE;
		alive = true;
		
		apple = null;
		
		tail = new ArrayList<Point>();
	}
	
	public void update() {
		int oldX = this.x;
		int oldY = this.y;
		int tempX, tempY;
		
		switch (movingDirection) {
		case MOVE_UP:
			y -= size;
			break;
		case MOVE_DOWN:
			y += size;
			break;
		case MOVE_LEFT:
			x -= size;
			break;
		case MOVE_RIGHT:
			x += size;
		}
		
		for (int index = 0; index < tail.size(); index++) {
			Point point = tail.get(index);
			tempX = point.x;
			tempY = point.y;
			point.x = oldX;
			point.y = oldY;
			oldX = tempX;
			oldY = tempY;
		}
		
		if (apple != null) {
			switch (apple.getType()) {
			case Apple.APPLE_GOOD:
				tail.add(new Point(oldX, oldY));
				break;
			
			}			
			apple = null;
		}
	}
	
	public void draw(Graphics2D g2) {
		super.draw(g2);
		
		for (int index = 0; index < tail.size(); index++) {
			Point point = tail.get(index);
			
			g2.fillRect(bounds.x + point.x - size / 2 + 1, bounds.y + point.y - size / 2 + 1, size - 2, size - 2);
		}
	}
	
	public void die() {
		alive = false;
		movingDirection = MOVE_NONE;
	}
	
	public void up() {
		if (movingDirection != MOVE_DOWN) {
			movingDirection = MOVE_UP;
		}
	}
	
	public void down() {
		if (movingDirection != MOVE_UP) {
			movingDirection = MOVE_DOWN;
		}
	}
	
	public void left() {
		if (movingDirection != MOVE_RIGHT) {
			movingDirection = MOVE_LEFT;
		}
	}
	
	public void right() {
		if (movingDirection != MOVE_LEFT) {
			movingDirection = MOVE_RIGHT;
		}
	}
	
	public boolean isAlive() {
		return alive;
	}
	
	private boolean hitTail(int x, int y) {
		boolean result = false;
		
		for (int i = 0; i < tail.size(); i++) {
			Point point = tail.get(i);
			if (x == point.x && y == point.y) result = true;
		}
		
		return result;
	}
	
	public boolean collision() {
		boolean result = false;
		
		if (x < 0 || y < 0 || x > bounds.width || y > bounds.height || hitTail(x, y)) {
			result = true;
			die();
		}
		
		return result;
	}
	
	public void eat(Apple apple) {
		switch (apple.getType()) {
		case Apple.APPLE_GOOD:
			score++;
			this.apple = apple;
			break;
		case Apple.APPLE_BAD:
			if (score >= 5) {
				score -= 5;
			} else {
				die();
			}
			break;
		}
	}
	
	public boolean contains(int x, int y) {
		return super.contains(x, y) || hitTail(x, y);
	}
	
	public int getLength() {
		return tail.size();
	}
	
	@SuppressWarnings("unchecked")
	public boolean teleport() {
		int newX = 0, newY = 0;
		int xOffset = 0, yOffset = 0;
		boolean telePossible = false;
		Random random = new Random();
		Player playerNew = new Player(game, 0, 0, Game.SNAKE_SIZE, null, bounds);
		
		while (!telePossible) {
			telePossible = true;
						
			newX = random.nextInt(bounds.width / Game.SNAKE_SIZE) * Game.SNAKE_SIZE + (Game.SNAKE_SIZE / 2);
			newY = random.nextInt(bounds.height / Game.SNAKE_SIZE) * Game.SNAKE_SIZE + (Game.SNAKE_SIZE / 2);
			
			xOffset = newX - x;
			yOffset = newY - y;
			
			playerNew.x = newX;
			playerNew.y = newY;
			playerNew.tail = (ArrayList<Point>) tail.clone();
			for (int i = 0; i < playerNew.tail.size(); i++) {
				Point point = playerNew.tail.get(i);
				
				point.x += xOffset;
				point.y += yOffset;
				
				if (point.x < 0 || point.x < 0 || point.x > bounds.width || point.y > bounds.height) {
					telePossible = false;
					break;
				}
			}
			for (int i = 0; i < game.getApples().size(); i++) {
				Apple apple = game.getApples().get(i);
				if (playerNew.contains(apple.x, apple.y)) telePossible = false;
			}
		}
		
		if (telePossible) {
			x = newX;
			y = newY;
			for (int i = 0; i < tail.size(); i++) {
				Point point = tail.get(i);
				point.x += xOffset;
				point.y += yOffset;
			}
		}
		
		return telePossible;
	}
}
