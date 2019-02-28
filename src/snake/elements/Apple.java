package snake.elements;

import java.awt.Color;
import java.awt.Rectangle;

import snake.game.Game;

public class Apple extends Element{
	
	public static final int APPLE_GOOD = 0;
	public static final int APPLE_BAD = 1;
	public static final int APPLE_TELE = 2;

	private int appleType;
	
	public Apple(Game game, int x, int y, int appleType, Rectangle bounds) {
		super(game, x, y, Game.SNAKE_SIZE, null, bounds);
		this.appleType = appleType;
		switch (appleType) {
		case APPLE_GOOD:
			this.color = Color.RED;
			break;
		case APPLE_BAD:
			this.color = new Color(50,255,120);
			break;
		case APPLE_TELE:
			this.color = Color.MAGENTA;
			break;
		}
	}
	
	public int getType() {
		return appleType;
	}
	
}
