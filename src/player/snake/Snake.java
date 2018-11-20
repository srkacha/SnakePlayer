package player.snake;

import java.util.LinkedList;


//A snake object encapsulating all the necessary info needed to represent a snake state from the game
public class Snake {
	
	private LinkedList<SnakeBody> body = new LinkedList<>();
	
	public Snake() {}
	
	public LinkedList<SnakeBody> getBody() {
		return body;
	}

	public void setBody(LinkedList<SnakeBody> body) {
		this.body = body;
	}

	//Copy constructor needed for cloning snake objects
	public Snake(Snake snake) {
		for(SnakeBody part: snake.getBody()) {
			this.body.add(new SnakeBody(part.getRow(), part.getColumn()));
		}
	}
	
	public void addBodyPart(SnakeBody part){
		this.body.add(part);
	}
	
	
	//Functions used for simulating the movement of the snake
	//We add a snake body part to the from of the list representing the new head
	//We then remove the tail part
	
	public Snake moveUp() {
		Snake newSnake = new Snake(this);
		SnakeBody head = newSnake.body.get(0);
		SnakeBody newHead = new SnakeBody(head.getRow() - 1, head.getColumn());
		newSnake.body.addFirst(newHead);
		newSnake.body.removeLast();
		return newSnake;
	}
	
	public Snake moveRight() {
		Snake newSnake = new Snake(this);
		SnakeBody head = newSnake.body.get(0);
		SnakeBody newHead = new SnakeBody(head.getRow(), head.getColumn() + 1);
		newSnake.body.addFirst(newHead);
		newSnake.body.removeLast();
		return newSnake;
	}
	
	public Snake moveDown() {
		Snake newSnake = new Snake(this);
		SnakeBody head = newSnake.body.get(0);
		SnakeBody newHead = new SnakeBody(head.getRow() + 1, head.getColumn());
		newSnake.body.addFirst(newHead);
		newSnake.body.removeLast();
		return newSnake;
	}
	
	public Snake moveLeft() {
		Snake newSnake = new Snake(this);
		SnakeBody head = newSnake.body.get(0);
		SnakeBody newHead = new SnakeBody(head.getRow(), head.getColumn() - 1);
		newSnake.body.addFirst(newHead);
		newSnake.body.removeLast();
		return newSnake;
	}
	
	//Functions used for simulating the growth of the snake when she consumes food
	//We add a snake body part to the from of the list representing the new head
	//We don't remove the tail because that becomes the food that the snake just consumed
	
	public Snake eatUp() {
		Snake newSnake = new Snake(this);
		SnakeBody head = newSnake.body.get(0);
		SnakeBody newHead = new SnakeBody(head.getRow() - 1, head.getColumn());
		newSnake.body.addFirst(newHead);
		return newSnake;
	}
	
	public Snake eatRight() {
		Snake newSnake = new Snake(this);
		SnakeBody head = newSnake.body.get(0);
		SnakeBody newHead = new SnakeBody(head.getRow(), head.getColumn() + 1);
		newSnake.body.addFirst(newHead);
		return newSnake;
	}
	
	public Snake eatDown() {
		Snake newSnake = new Snake(this);
		SnakeBody head = newSnake.body.get(0);
		SnakeBody newHead = new SnakeBody(head.getRow() + 1, head.getColumn());
		newSnake.body.addFirst(newHead);
		return newSnake;
	}
	
	public Snake eatLeft() {
		Snake newSnake = new Snake(this);
		SnakeBody head = newSnake.body.get(0);
		SnakeBody newHead = new SnakeBody(head.getRow(), head.getColumn() - 1);
		newSnake.body.addFirst(newHead);
		return newSnake;
	}
	
	@Override
	public String toString() {
		String snake = "";
		for(SnakeBody part: body) {
			snake += part.getRow() + "," + part.getColumn() + " ";
		}
		return snake;
	}

}
