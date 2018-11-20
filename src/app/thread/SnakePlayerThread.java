package app.thread;

import player.snake.SnakePlayer;


//A thread extending class which main purpose is to play the game
public class SnakePlayerThread extends Thread{

	private static boolean active;
	
	private SnakePlayer snakePlayer;
	
	public SnakePlayerThread(int fieldSize) {
		snakePlayer = new SnakePlayer(fieldSize);
	}
	
	public void run() {
		active = true;
		while(active) {
			snakePlayer.makeNextMove();
		}
	}
	
	public static void setActive(boolean value) {
		active = value;
	}
}
