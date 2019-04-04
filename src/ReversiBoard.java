import java.io.Serializable;

/**
 * @author Lucia Wang
 * @author Alan Cheng
 * 
 *         ReversiBoard class encapsulates the board independent of the Model. A
 *         ReversiBoard object is created every time a move is made, and passed
 *         to the view's update() method through the Object arg parameter, so
 *         that ReversiView can also be updated.
 *
 */
public class ReversiBoard implements Serializable {
	static final long serialVersionUID = 1L;

	/**
	 * Used to set ReversiBoard in ReversiModel and ReversiView
	 * 
	 * Colors aren't serializable so they're saved as numbers
	 */
	public static int WHITE = 1;
	public static int BLACK = 2;
	public static int BLANK = 0;

	/**
	 * Dimensions of board
	 */
	public static int DIM = 8;

	/**
	 * 2d array to store colors of the board
	 */
	private int[][] board;

	/**
	 * Constructs ReversiBoard object as 2d array of integers that represent colors
	 */
	public ReversiBoard() {
		this.board = new int[DIM][DIM];
	}

	/**
	 * Sets the tile at row/col to color
	 * 
	 * @param row   Row to set color
	 * @param col   Column to set color
	 * @param color color (0 blank, 1 white, 2 black)
	 */
	public void setAt(int row, int col, int color) {
		board[row][col] = color;
	}

	/**
	 * Gets color at row/col
	 * 
	 * @param row Row to get color
	 * @param col Column to get color
	 * @return color (0 blank, 1 white, 2 black)
	 */
	public int getAt(int row, int col) {
		return board[row][col];
	}
}