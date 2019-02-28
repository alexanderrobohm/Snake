package snake;

import snake.game.Game;

public class Snake {

	public static void main(String[] args) {
		Game game = new Game();
		game.init();
		game.run();
	}

}
