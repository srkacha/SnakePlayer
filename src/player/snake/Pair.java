package player.snake;

//Useful class when we need to represent a state in the matrix as an object
public class Pair  implements Comparable{
	
	private int row;
	private int column;
	
	public Pair(int row, int column) {
		super();
		this.row = row;
		this.column = column;
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
	@Override
	public int compareTo(Object o) {
		Pair obj = (Pair)o;
		return Math.abs(this.row - obj.row) + Math.abs(this.column - obj.column);
	}
	
	

}
