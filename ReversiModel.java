import java.util.Observable;
import javafx.scene.paint.Color;

/**
 * @author Lucia Wang
 * @author Alan Cheng
 * 
 *         ReversiModel extends Observable and is used to notify observers (the
 *         view) when the model changes (when a move is made). It saves the
 *         pieces on the board: B for black(cpu), W for white(user)
 *
 */
public class ReversiModel extends Observable {
	/**
	 * used to update ReversiView
	 */
	private ReversiBoard board;

	/**
	 * String representation of board
	 */
	private String[][] stringBoard;

	/**
	 * Dimensions of board
	 */
	private int dimension = 8;

	/**
	 * Construct ReversiModel object with ReversiBoard version and String version of
	 * the board
	 */
	public ReversiModel() {
		board = new ReversiBoard();
		stringBoard = new String[dimension][dimension];
		for (int i = 0; i < stringBoard.length; i++) {
			for (int j = 0; j < stringBoard.length; j++) {
				stringBoard[i][j] = "_";
			}
		}

		board.setAt(3, 3, ReversiBoard.WHITE);
		board.setAt(3, 4, ReversiBoard.BLACK);
		board.setAt(4, 4, ReversiBoard.WHITE);
		board.setAt(4, 3, ReversiBoard.BLACK);

		stringBoard[3][3] = "W";
		stringBoard[3][4] = "B";
		stringBoard[4][4] = "W";
		stringBoard[4][3] = "B";
	}

	/**
	 * Construct ReversiModel object whose stringBoard is based on the ReversiBoard
	 * board
	 * 
	 * @param board: ReversiBoard that is used to update stringBoard
	 */
	public ReversiModel(ReversiBoard board) {
		this.board = board;

		stringBoard = new String[dimension][dimension];
		for (int i = 0; i < stringBoard.length; i++) {
			for (int j = 0; j < stringBoard.length; j++) {
				if (this.board.getAt(i, j) == ReversiBoard.BLANK)
					stringBoard[i][j] = "_";
				else if (this.board.getAt(i, j) == ReversiBoard.WHITE)
					stringBoard[i][j] = "W";
				else if (this.board.getAt(i, j) == ReversiBoard.BLACK)
					stringBoard[i][j] = "B";
			}
		}
	}

	/**
	 * Get the piece at the specified row and column
	 * 
	 * @param row Row
	 * @param col Column
	 * @return String at the given row/col
	 */
	public String getAt(int row, int col) {
		return stringBoard[row][col];
	}

	/**
	 * Getter for ReversiBoard object
	 * 
	 * @return board ReversiBoard
	 */
	public ReversiBoard getBoard() {
		return board;
	}

	/**
	 * Getter for string version of the board
	 * 
	 * @return 2d array of current board
	 */
	public String[][] getStringBoard() {
		return stringBoard;
	}

	/**
	 * Getter for dimensions of board
	 * 
	 * @return: dimension Board's dimensions
	 */
	public int getDimension() {
		return dimension;
	}

	/**
	 * Updates board after a player has moved with the right color, and the right
	 * color flips on the board, then notifies view that changes have been made
	 * 
	 * @param row: the row to be updated
	 * @param col: the column to be updated
	 * @param color: the color the board should be updated to
	 */
	public void setBoard(int row, int col, Color color) {
		int player = (color.equals(Color.WHITE) ? ReversiBoard.WHITE : ReversiBoard.BLACK);
		board.setAt(row, col, player);
		setChanged();
		// instance of ReversiBoardbecomes arg parameter of update
		notifyObservers(board);
	}
}