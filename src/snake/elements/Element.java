package snake.elements;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import snake.game.Game;

public class Element {
	
	protected Game game;
	protected Rectangle bounds;
	protected int x, y;
	protected int size;
	protected Color color;
	
	public Element(Game game, int x, int y, int size, Color color, Rectangle bounds) {
		this.game = game;
		this.x = x;
		this.y = y;
		this.size = size;
		this.color = color;
		this.bounds = bounds;
	}
	
	public void draw(Graphics2D g2) {
		g2.setColor(color);
		g2.fillRect(bounds.x + x - size / 2 + 1, bounds.y + y - size / 2 + 1, size - 2, size - 2);
	}
	
	public void update() {
		
	}
	
	public boolean collision() {
		return (x < 0 || y < 0 || x > bounds.width || y > bounds.height);
	}
	
	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}
	
	public boolean contains(int x, int y) {
		return (this.x == x && this.y == y);
	}
}
