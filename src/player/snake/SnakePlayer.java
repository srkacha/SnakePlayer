package player.snake;

import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.PriorityQueue;
import java.util.TreeSet;

public class SnakePlayer {
	
	//Variables used for setting up the capture
	private static int SCREEN_X_OFFSET = 87; // screen horizontal offset from top side when capturing the game state
	private static int SCREEN_Y_OFFSET = 120; // screen vertical offset from left side when capturing the game state
	private static int BLOCK_SIZE = 20; //size of one field block in pixels
	private static int SCREEN_SCALE_FACTOR = 150; // windows screen scaling factor, for monitors with high ppi
	
	//Game state useful variables for tracking the current snake and food position 
	private Snake mySnake = new Snake(); 
	private int mySnakeHeadRow = 0;
	private int mySnakeHeadColumn = 0;
	private int myFoodRow = 0;
	private int myFoodColumn = 0;
	
	//Variables used for capturing and comparing the captures
	private boolean firstCaptureFlag = true; //we only need to create the global snake once
	private int previousHeadStateRow = 0; //useful to ignore new captures of the same frame
	private int previousHeadStateColumn = 0;; //useful to ignore new captures of the same frame
	private boolean foodFoundFlag = false; //useful for the first capture to ignore it if the food is not on the field
	
	//Instance variables
	private int fieldSize;
	private Robot myRobot;
	
	public SnakePlayer(int fieldSize) {
		try {
			this.fieldSize = fieldSize;
			myRobot = new Robot();
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	// A-Star based algorithm for suggesting the next snake movement based on the current game state matrix
	private void suggestSnakeMovement(int[][] matrix) {
		// if the game is not loaded or the food is not yet loaded
		if(!isCaptureValid(matrix) ||  !isFoodFound()) return; 
		
		//we signal that the game started so the global snake does not get cleared anymore
		setFirstCapture(false); 
		
		boolean closedList[][] = new boolean[fieldSize][fieldSize];
		PathNode nodes[][] = new PathNode[fieldSize][fieldSize];
		//we are using a priority queue because we want the queue to be sorted after adding the next node to it
		PriorityQueue<PathNode> openList = new PriorityQueue<>();
		
		//initialising graph nodes used for
		for(int i=0;i<fieldSize;i++) {
			for(int j=0;j<fieldSize;j++) {
				nodes[i][j] = new PathNode(i, j, -1, -1);
			}
		}
		
		//setting up the goal
		Pair goal = determineGoal(matrix);
		
		//setting up the starting node, where the snake head lies
		nodes[mySnakeHeadRow][mySnakeHeadColumn].setfValue(0);
		nodes[mySnakeHeadRow][mySnakeHeadColumn].setgValue(0);
		nodes[mySnakeHeadRow][mySnakeHeadColumn].sethValue(0);
		nodes[mySnakeHeadRow][mySnakeHeadColumn].setParentRow(mySnakeHeadRow);
		nodes[mySnakeHeadRow][mySnakeHeadColumn].setParentColumn(mySnakeHeadColumn);
		nodes[mySnakeHeadRow][mySnakeHeadColumn].setCurrentStateMatrix(matrix);
		nodes[mySnakeHeadRow][mySnakeHeadColumn].setCurrentSnake(mySnake);
		
		openList.add(nodes[mySnakeHeadRow][mySnakeHeadColumn]);
		
		PathNode current = null;
		
		//Iterating through all nodes found in the open list
		while(!openList.isEmpty()) {
			//we get the node with the lowest f value
			current = openList.poll();
			int currentRow = current.getRow();
			int currentColumn = current.getColumn();
			int[][] currentMatrix = current.getCurrentStateMatrix();
			Snake currentSnake = current.getCurrentSnake();
			
			//we mark the node as visited
			closedList[currentRow][currentColumn] = true;
			
			//now we generate the 4 successors
			
			//trying to generate the top successor, adding it to the open list and checking if it's the goal of the search
			if(isFieldValid(currentRow - 1, currentColumn)) {
				if(closedList[currentRow - 1][currentColumn] == false) {
					if(!isFieldAnObstacle(currentMatrix, currentRow - 1, currentColumn) || isFieldSnakeTail(currentRow - 1, currentColumn, currentSnake)) {
						//Generating the node, setting the g, h and f values, predicted snake position and game state
						nodes[currentRow - 1][currentColumn].setgValue(nodes[currentRow][currentColumn].getgValue() + 1);
						nodes[currentRow - 1][currentColumn].setParentRow(currentRow);
						nodes[currentRow - 1][currentColumn].setParentColumn(currentColumn);
						nodes[currentRow - 1][currentColumn].setNextMove(0);
						Snake newSnake = currentSnake.moveUp();
						nodes[currentRow - 1][currentColumn].setCurrentSnake(newSnake);
						int[][] newMatrix = generateNextStateMatrix(newSnake);
						nodes[currentRow - 1][currentColumn].setCurrentStateMatrix(newMatrix);
						if(isFoodTheGoal(goal)) {
							nodes[currentRow - 1][currentColumn].sethValue(distanceFromFood(currentRow - 1, currentColumn, newMatrix));
						}
						else nodes[currentRow - 1][currentColumn].sethValue(distanceFromTail(newSnake, newMatrix));
						nodes[currentRow - 1][currentColumn].setfValue(nodes[currentRow - 1][currentColumn].getgValue() + nodes[currentRow - 1][currentColumn].gethValue());
						
						//we add the created node to the list of those which need to be visited
						openList.add(nodes[currentRow - 1][currentColumn]);
						
						//we check if the new node is actually the goal, if it is, we trace the path and move the snake
						if(currentRow - 1 == goal.getRow() && currentColumn == goal.getColumn()) {
							tracePathAndMove(nodes, currentRow - 1, currentColumn);
							if(isFoodTheGoal(goal)) growGlobalSnake(currentSnake);
							return;
						}
					}
				}
			}
			
			//trying to generate the right successor, adding it to the open list and checking if it's the goal of the search
			if(isFieldValid(currentRow , currentColumn + 1)) {
				if(closedList[currentRow][currentColumn + 1] == false) {
					if(!isFieldAnObstacle(currentMatrix, currentRow , currentColumn + 1) || isFieldSnakeTail(currentRow, currentColumn + 1, currentSnake)) {
						//Generating the node, setting the g, h and f values, predicted snake position and game state
						nodes[currentRow][currentColumn+ 1].setgValue(nodes[currentRow][currentColumn].getgValue() + 1);
						nodes[currentRow][currentColumn+ 1].setParentRow(currentRow);
						nodes[currentRow][currentColumn+ 1].setParentColumn(currentColumn);
						nodes[currentRow][currentColumn+ 1].setNextMove(1);
						Snake newSnake = currentSnake.moveRight();
						nodes[currentRow][currentColumn+ 1].setCurrentSnake(newSnake);
						int[][] newMatrix = generateNextStateMatrix(newSnake);
						nodes[currentRow][currentColumn + 1].setCurrentStateMatrix(newMatrix);
						if(isFoodTheGoal(goal)) {
							nodes[currentRow][currentColumn + 1].sethValue(distanceFromFood(currentRow, currentColumn + 1, newMatrix));
						}
						else nodes[currentRow][currentColumn + 1].sethValue(distanceFromTail(newSnake, newMatrix));
						nodes[currentRow][currentColumn+ 1].setfValue(nodes[currentRow][currentColumn+ 1].getgValue() + nodes[currentRow][currentColumn+1].gethValue());
						
						//we add the created node to the list of those which need to be visited
						openList.add(nodes[currentRow][currentColumn+ 1]);
						
						//we check if the new node is actually the goal, if it is, we trace the path and move the snake
						if(currentRow == goal.getRow() && currentColumn+ 1 == goal.getColumn()) {
							tracePathAndMove(nodes, currentRow, currentColumn+ 1);
							if(isFoodTheGoal(goal)) growGlobalSnake(currentSnake);
							return; 
						}
					}
				}
			}
			
			
			//trying to generate the bottom successor, adding it to the open list and checking if it's the goal of the search
			if(isFieldValid(currentRow + 1, currentColumn)) {
				if(closedList[currentRow + 1][currentColumn] == false) {
					if(!isFieldAnObstacle(currentMatrix, currentRow + 1, currentColumn) || isFieldSnakeTail(currentRow + 1, currentColumn, currentSnake)) {
						//Generating the node, setting the g, h and f values, predicted snake position and game state
						nodes[currentRow + 1][currentColumn].setgValue(nodes[currentRow][currentColumn].getgValue() + 1);
						nodes[currentRow + 1][currentColumn].setParentRow(currentRow);
						nodes[currentRow + 1][currentColumn].setParentColumn(currentColumn);
						nodes[currentRow + 1][currentColumn].setNextMove(2);
						Snake newSnake = currentSnake.moveDown();
						nodes[currentRow + 1][currentColumn].setCurrentSnake(newSnake);
						int[][] newMatrix = generateNextStateMatrix(newSnake);
						nodes[currentRow + 1][currentColumn].setCurrentStateMatrix(newMatrix);
						if(isFoodTheGoal(goal)) {
							nodes[currentRow + 1][currentColumn].sethValue(distanceFromFood(currentRow + 1, currentColumn, newMatrix));
						}
						else nodes[currentRow + 1][currentColumn].sethValue(distanceFromTail(newSnake, newMatrix));
						nodes[currentRow + 1][currentColumn].setfValue(nodes[currentRow + 1][currentColumn].getgValue() + nodes[currentRow + 1][currentColumn].gethValue());
						
						//we add the created node to the list of those which need to be visited
						openList.add(nodes[currentRow + 1][currentColumn]);
						
						//we check if the new node is actually the goal, if it is, we trace the path and move the snake
						if(currentRow + 1 == goal.getRow() && currentColumn == goal.getColumn()) {
							tracePathAndMove(nodes, currentRow + 1, currentColumn);
							if(isFoodTheGoal(goal)) growGlobalSnake(currentSnake);
							return;
						}
					}
				}
			}
			
			
			//trying to generate the left successor, adding it to the open list and checking if it's the goal of the search
			if(isFieldValid(currentRow , currentColumn - 1)) {
				if(closedList[currentRow][currentColumn - 1] == false) {
					if(!isFieldAnObstacle(currentMatrix, currentRow , currentColumn - 1)  || isFieldSnakeTail(currentRow, currentColumn - 1, currentSnake)) {
						//Generating the node, setting the g, h and f values, predicted snake position and game state
						nodes[currentRow][currentColumn- 1].setgValue(nodes[currentRow][currentColumn].getgValue() + 1);
						nodes[currentRow][currentColumn- 1].setParentRow(currentRow);
						nodes[currentRow][currentColumn- 1].setParentColumn(currentColumn);
						nodes[currentRow][currentColumn- 1].setNextMove(3);
						Snake newSnake = currentSnake.moveLeft();
						nodes[currentRow][currentColumn - 1].setCurrentSnake(newSnake);
						int[][] newMatrix = generateNextStateMatrix(newSnake);
						nodes[currentRow][currentColumn - 1].setCurrentStateMatrix(newMatrix);
						if(isFoodTheGoal(goal)) {
							nodes[currentRow][currentColumn - 1].sethValue(distanceFromFood(currentRow, currentColumn - 1, newMatrix));
						}
						else nodes[currentRow][currentColumn - 1].sethValue(distanceFromTail(newSnake, newMatrix));
						nodes[currentRow][currentColumn- 1].setfValue(nodes[currentRow][currentColumn- 1].getgValue() + nodes[currentRow][currentColumn-1].gethValue());
						
						//we add the created node to the list of those which need to be visited
						openList.add(nodes[currentRow][currentColumn- 1]);
						
						//we check if the new node is actually the goal, if it is, we trace the path and move the snake
						if(currentRow == goal.getRow() && currentColumn- 1 == goal.getColumn()) {
							tracePathAndMove(nodes, currentRow, currentColumn- 1);
							if(isFoodTheGoal(goal)) growGlobalSnake(currentSnake);
							return;
						}
					}
				}
			}
		}
		//if the path was not found we move the snake to the best possible destination
		tracePathAndMove(nodes, current.getRow(), current.getColumn());
	}
	
	//generates the new matrix for the predicted state of the game
	private int[][] generateNextStateMatrix(Snake snake){
		int[][] newMatrix = new int[fieldSize][fieldSize];
		//setting the snake body
		for(SnakeBody part: snake.getBody()) {
				newMatrix[part.getRow()][part.getColumn()] = 1;
		}
		//setting the snake head
		newMatrix[snake.getBody().getFirst().getRow()][snake.getBody().getFirst().getColumn()] = 2;
		//setting the food
		newMatrix[myFoodRow][myFoodColumn] = 3;
		return newMatrix;
	}
	
	
	// determines if the snake should chase after food or its tail
	// if the snake can reach the food, and then it can reach its tail, then it goes for the food
	// if the snake can't do that, it checks if it can reach its tail, to at least stay alive
	// if it can reach the tail, it goes for the tail
	// finally if the snake can not reach the tail it will go for the food because that's the only valid option left to survive
	private Pair determineGoal(int[][] matrix) {
		int snakeHeadRow = mySnake.getBody().getFirst().getRow();
		int snakeHeadCol = mySnake.getBody().getFirst().getColumn();
		int snakeTailRow = mySnake.getBody().getLast().getRow();
		int snakeTailCol = mySnake.getBody().getLast().getColumn();
		
		boolean headToFood = pathAvailable(snakeHeadRow, snakeHeadCol, myFoodRow, myFoodColumn, matrix);
		boolean foodToTail = pathAvailable(myFoodRow, myFoodColumn, snakeTailRow, snakeTailCol, matrix);
		boolean headToTail = pathAvailable(snakeHeadRow, snakeHeadCol, snakeTailRow, snakeTailCol, matrix);
		
		if(headToFood && foodToTail) {
			return new Pair(myFoodRow, myFoodColumn);
		}
		else if(headToTail) {
			return new Pair(snakeTailRow, snakeTailCol);
		}
		else return new Pair(myFoodRow, myFoodColumn);
	}
	
	//traces back the path and gives the input to the program
	private void tracePathAndMove(PathNode[][] nodes, int currentRow, int currentColumn) {
		PathNode currentNode = nodes[currentRow][currentColumn];
		int movement = -1;
		
		//determining where the snake should move
		//we do it by iterating to the root parent node and memorise the first movement we did on that path
		while(currentNode.getParentRow()!= currentNode.getRow() || currentNode.getParentColumn() != currentNode.getColumn()) {
			movement = currentNode.getNextMove();
			mySnake = currentNode.getCurrentSnake();
			currentNode = nodes[currentNode.getParentRow()][currentNode.getParentColumn()];
		}
		
		//moving the snake
		if(movement == 0) {
			myRobot.keyPress(KeyEvent.VK_UP);
			myRobot.keyRelease(KeyEvent.VK_UP);
		}else if(movement == 1) {
			myRobot.keyPress(KeyEvent.VK_RIGHT);
			myRobot.keyRelease(KeyEvent.VK_RIGHT);
		}else if(movement == 2) {
			myRobot.keyPress(KeyEvent.VK_DOWN);
			myRobot.keyRelease(KeyEvent.VK_DOWN);
		}else if(movement == 3){
			myRobot.keyPress(KeyEvent.VK_LEFT);
			myRobot.keyRelease(KeyEvent.VK_LEFT);
		}
	}
	
	//HEURISTIC FUNCTIONS FOR CHASING FOOD OR TAIL
	
	//calculates heuristic path cost value for a given field to food
	//combines Manhattan distance from field to food, minimal distance for the head to be in the same row/column as the food and surrounding blocks around snake
	private int distanceFromFood(int row, int column, int[][] matrix) {
		return Math.abs(row - myFoodRow) + Math.abs(column - myFoodColumn) + Math.min(Math.abs(row - myFoodRow), Math.abs(column - myFoodColumn)) + surroundingBlocks(row, column, matrix);
	}
	
	//function for counting the number of snake body parts around a given field
	private int surroundingBlocks(int row, int  col, int[][] matrix) {
		int blockCount = 0;
		for(int i = -1;i<2;i++) {
			for(int j = -1;j<2;j++) {
				if(isFieldValid(row + i, col + j) && matrix[row + i][col + j] == 1) blockCount++;
			}
		}
		return blockCount;
	}
	
	//calculates heuristic path cost value for a given field to snake tail
	private int distanceFromTail(Snake snake, int matrix[][]) {
		int headRow = snake.getBody().getFirst().getRow();
		int headCol = snake.getBody().getFirst().getColumn();
		int tailRow = snake.getBody().getLast().getRow();
		int tailCol = snake.getBody().getLast().getColumn();
		return Math.abs(headRow - tailRow) + Math.abs(headCol - tailCol) + Math.min(Math.abs(headRow - tailRow), Math.abs(headCol - tailCol) +  surroundingBlocks(headRow, headCol, matrix));
	}
	
	//checks if there is a path from the start to the end, used for checking if the food can reach the tail etc.
	private boolean pathAvailable(int beginRow, int beginColumn, int endRow, int endColumn, int[][]matrix) {
		
		boolean[][] visited = new boolean[matrix.length][matrix.length];
		TreeSet<Pair> notVisited = new TreeSet<>();
		
		Pair start = new Pair(beginRow, beginColumn);
		notVisited.add(start);
		
		while(!notVisited.isEmpty()) {
			Pair current = notVisited.pollFirst();
			int currentRow = current.getRow();
			int currentColumn = current.getColumn();
			
			visited[currentRow][currentColumn] = true;
			
			if(isFieldValid(currentRow - 1, currentColumn)) {
				if(currentRow - 1 == endRow && currentColumn == endColumn) return true;
				if(!isFieldAnObstacle(matrix, currentRow - 1, currentColumn) && !visited[currentRow - 1][currentColumn]) {
					notVisited.add(new Pair(currentRow - 1, currentColumn));
				}
			}
			
			if(isFieldValid(currentRow + 1, currentColumn)) {
				if(currentRow + 1 == endRow && currentColumn == endColumn) return true;
				if(!isFieldAnObstacle(matrix, currentRow + 1, currentColumn) && !visited[currentRow + 1][currentColumn]) {
					notVisited.add(new Pair(currentRow + 1, currentColumn));
				}
			}
			
			if(isFieldValid(currentRow, currentColumn - 1)) {
				if(currentRow == endRow && currentColumn - 1 == endColumn) return true;
				if(!isFieldAnObstacle(matrix, currentRow, currentColumn - 1) && !visited[currentRow][currentColumn - 1]) {
					notVisited.add(new Pair(currentRow, currentColumn - 1));
				}
			}
			
			if(isFieldValid(currentRow, currentColumn + 1)) {
				if(currentRow == endRow && currentColumn + 1 == endColumn) return true;
				if(!isFieldAnObstacle(matrix, currentRow, currentColumn + 1) && !visited[currentRow][currentColumn + 1]) {
					notVisited.add(new Pair(currentRow, currentColumn + 1));
				}
			}
		}
		
		return false;
	}
	
	

	
	//checks if the cords are out of bounds of the game field
	private boolean isFieldValid(int row, int column) {
		return row>=0 && row<fieldSize && column>=0 && column<fieldSize;
	}
	
	//checks if the field is a snake part
	private boolean isFieldAnObstacle(int[][] matrix, int row, int column) {
		return matrix[row][column] == 1 || matrix[row][column] == 2;
	}
	
	//checks if the field is tail of the snake
	private boolean isFieldSnakeTail(int row, int column, Snake snake) {
		SnakeBody tail = snake.getBody().peekLast();
		return row == tail.getRow() && column == tail.getColumn();
	}
	
	//checks if the given goal is where the food is 
	private boolean isFoodTheGoal(Pair goal) {
		return myFoodRow == goal.getRow() && myFoodColumn == goal.getColumn();
	}
	
	//used for capturing the current frame of the game
	private BufferedImage captureSnakeScreen() {
		try {
			return myRobot.createScreenCapture(new Rectangle(SCREEN_X_OFFSET, SCREEN_Y_OFFSET, fieldSize*BLOCK_SIZE*SCREEN_SCALE_FACTOR/100, fieldSize*BLOCK_SIZE*SCREEN_SCALE_FACTOR/100));
		}catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	//checks if any of the fields on the captured screen are -1, because -1 means that one of the fields is not valid
	private boolean isCaptureValid(int[][] matrix) {
		for(int i = 0;i<matrix.length;i++) {
			for(int j = 0;j<matrix.length;j++) {
				if(matrix[i][j] == -1) return false;
			}
		}
		return true;
	}
	
	//pretty self-explanatory
	private boolean isFoodFound() {
		return foodFoundFlag;
	}
	
	//also pretty self-explanatory
	private void setFirstCapture(boolean value) {
		firstCaptureFlag = value;
	}
	
	
	//used for converting the captured game state image to an integer matrix representing the same game state
	private int[][] getGameState(BufferedImage imageState){
		try {
			int gameState[][] = new int[fieldSize][fieldSize];
			int currentPixel = 0;
			int rowMultiplier = fieldSize*BLOCK_SIZE*SCREEN_SCALE_FACTOR/100;
			int realBlockSize = BLOCK_SIZE*SCREEN_SCALE_FACTOR/100;
			int columnMultiplier = BLOCK_SIZE*SCREEN_SCALE_FACTOR/100;
			int pixelCenterOffset = BLOCK_SIZE*SCREEN_SCALE_FACTOR/200;
			
			//we clear the global snake until we finally make the capture of the game
			if(firstCaptureFlag) mySnake.getBody().clear(); 
			
			//first we convert the image to a byte array for faster iterating speed
			int[] pixels = ((DataBufferInt) imageState.getRaster().getDataBuffer()).getData();
			
			//iterating through the pixel info and getting the snake and food info
			for(int i=0;i<fieldSize;i++) {
				for(int j=0;j<fieldSize;j++) {
					//getting the pixel we want from the image
					currentPixel = pixels[(i * realBlockSize + pixelCenterOffset)*rowMultiplier + j*columnMultiplier + pixelCenterOffset];
					if(currentPixel == -1) { // -1 value represents white colour, that is an empty field 
						gameState[i][j] = 0;
					}else if(currentPixel == -16777216) { // -16777216 value represents black colour, that is the snake body
						gameState[i][j] = 1;
						
						//we only do this the first time we capture a valid game state
						if(firstCaptureFlag) {
							mySnake.addBodyPart(new SnakeBody(i, j)); //adding the snake part to the main snake
						}
					}else if(currentPixel == -16744448) { // -16744448 value represents green colour, that is the snakes head
						gameState[i][j] = 2;
						mySnakeHeadRow = i;
						mySnakeHeadColumn = j;
						
						//we only do this the first time we capture a valid game state
						if(firstCaptureFlag) {
							mySnake.addBodyPart(new SnakeBody(i, j)); //adding the snake head to the main snake	
						}
					} else if(currentPixel == -65536) { // -65536 value represents red colour, that is the food
						gameState[i][j] = 3;
						myFoodRow = i;
						myFoodColumn = j;
						foodFoundFlag = true;
					}else gameState[i][j] = -1; //if the player captures something that is not the game, for example when starting
				}
			}
			
			return gameState;
		}catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	//returns true if the game states are from the same game frame
	//useful to skip suggesting a move for a state if the move has been already suggested
	private boolean compareGameStates() {
		return previousHeadStateRow == mySnakeHeadRow && previousHeadStateColumn == mySnakeHeadColumn;
	}
	
	//used for checking if the captured game state is in sync with the global snake object, if it is not then the simulation will not be continued
	private boolean gameStateAndGlobalSnakeSynchronized(int[][] gameState) {
		int counter = 0;
		//for comparing every body part position of the snake
		for(SnakeBody part: mySnake.getBody()) {
			if(gameState[part.getRow()][part.getColumn()] != 1 && gameState[part.getRow()][part.getColumn()] != 2) return false;
		}
		//for comparing length of the global snake and captured snake
		for(int i = 0;i<gameState.length;i++) {
			for(int j = 0;j<gameState.length;j++) {
				if(gameState[i][j] == 1 || gameState[i][j] == 2) counter++;
			}
		}
		return counter == mySnake.getBody().size();
	}
	
	//again, pretty self-explanatory
	private void saveGameState() {
		previousHeadStateRow = mySnakeHeadRow;
		previousHeadStateColumn = mySnakeHeadColumn;
	}
	
	//captures the screen, converts the game state to a matrix and if possible and needed suggests a move 
	public void makeNextMove() {
		BufferedImage image = captureSnakeScreen();
		int[][] gameState = getGameState(image);
		if(!compareGameStates() && gameStateAndGlobalSnakeSynchronized(gameState)) {
			saveGameState();
			suggestSnakeMovement(gameState);
		}
		
	}
	
	//used for growing the global snake object if the next move is going to eat the food
	private void growGlobalSnake(Snake snake) {
		if(mySnakeHeadRow - 1 == myFoodRow && mySnakeHeadColumn == myFoodColumn) mySnake = snake.eatUp();
		else if(mySnakeHeadRow == myFoodRow && mySnakeHeadColumn + 1 == myFoodColumn) mySnake = snake.eatRight();
		else if(mySnakeHeadRow + 1 == myFoodRow && mySnakeHeadColumn == myFoodColumn) mySnake = snake.eatDown();
		else if(mySnakeHeadRow == myFoodRow && mySnakeHeadColumn - 1 == myFoodColumn) mySnake = snake.eatLeft();
	}
	
	//used for setting the offsets and windows scale factor when the application is run
	public static void setScreenCaptureFactors(int xOffset, int yOffset, int scaleFactor) {
		SCREEN_X_OFFSET = xOffset;
		SCREEN_Y_OFFSET = yOffset;
		SCREEN_SCALE_FACTOR = scaleFactor;
	}
}