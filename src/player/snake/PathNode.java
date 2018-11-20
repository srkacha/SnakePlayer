package player.snake;

//A class encapsulating all the necessary info into a node structure needed for my A* implementation
public class PathNode implements Comparable{
	
	private int row;
	private int column;
	private int gValue;
	private int hValue;
	private int fValue;
	private int parentRow;
	private int parentColumn;
	private int nextMove;
	private int[][] currentStateMatrix; 
	private Snake currentSnake;  
	
	public Snake getCurrentSnake() {
		return currentSnake;
	}

	public void setCurrentSnake(Snake currentSnake) {
		this.currentSnake = currentSnake;
	}

	public int getNextMove() {
		return nextMove;
	}

	public void setNextMove(int nextMove) {
		this.nextMove = nextMove;
	}

	public int getRow() {
		return row;
	}

	public void setRow(int row) {
		this.row = row;
	}

	public int getColumn() {
		return column;
	}

	public void setColumn(int column) {
		this.column = column;
	}

	public int getgValue() {
		return gValue;
	}

	public void setgValue(int gValue) {
		this.gValue = gValue;
	}

	public int gethValue() {
		return hValue;
	}

	public void sethValue(int hValue) {
		this.hValue = hValue;
	}

	public int getfValue() {
		return fValue;
	}

	public void setfValue(int fValue) {
		this.fValue = fValue;
	}

	public int getParentRow() {
		return parentRow;
	}

	public void setParentRow(int parentRow) {
		this.parentRow = parentRow;
	}

	public int getParentColumn() {
		return parentColumn;
	}

	public void setParentColumn(int parentColumn) {
		this.parentColumn = parentColumn;
	}

	public PathNode(int row, int column, int parentRow, int parentColumn) {
		this.row = row;
		this.column = column;
		this.parentRow = parentRow;
		this.parentColumn = parentColumn;
		gValue = Integer.MAX_VALUE;
		hValue = Integer.MAX_VALUE;
		fValue = Integer.MAX_VALUE;
	}

	public int[][] getCurrentStateMatrix() {
		return currentStateMatrix;
	}

	public void setCurrentStateMatrix(int[][] currentStateMatrix) {
		this.currentStateMatrix = currentStateMatrix;
	}

	@Override
	public int compareTo(Object o) {
		PathNode obj = (PathNode)o;
		return this.fValue - obj.fValue;
		
	}
	
	
}
