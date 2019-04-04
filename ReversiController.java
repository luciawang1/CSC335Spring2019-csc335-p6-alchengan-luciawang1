import java.io.*;
import javafx.scene.paint.Color;

/**
 * @author Lucia Wang
 * @author Alan Cheng
 * 
 *         ReversiController validates player moves, communicates with model to
 *         make them.
 */
public class ReversiController {
	/**
	 * model to store pieces on the board
	 */
	public ReversiModel model;

	/**
	 * score of B
	 */
	public int bScore;

	/**
	 * score of W
	 */
	public int wScore;

	/**
	 * String version of board, "B" = black, "W" = white, "_" = empty
	 */
	String[][] board;

	/**
	 * dimensions of board
	 */
	int dimension;

	/**
	 * load saved file if it exists
	 */
	FileInputStream load;

	/**
	 * Constructs ReversiModel object. If a file named "save_game.dat" exists, load
	 * it into the model and show it in the view, if it doesn't, make a new
	 * ReversiModel for a new game
	 */
	public ReversiController() {
		// if "save_game.dat" exists, load into model, and show in view
		try {
			load = new FileInputStream("save_game.dat");
			ObjectInputStream in = new ObjectInputStream(load);
			ReversiBoard b = (ReversiBoard) in.readObject();
			this.model = new ReversiModel(b);
			in.close();
			load.close();
			// no saved file, construct new ReversiModel
		} catch (FileNotFoundException fnfe) {
			this.model = new ReversiModel();
		} catch (IOException ioe) {
			ioe.printStackTrace();
			return;
		} catch (ClassNotFoundException cnfe) {
			cnfe.printStackTrace();
			return;
		}

		// Instantiate instance variables
		this.board = model.getStringBoard();
		this.dimension = model.getDimension();
		this.bScore = 2;
		this.wScore = 2;
	}

	/**
	 * getter for ReversiModel object
	 * 
	 * @return model
	 */
	public ReversiModel getModel() {
		return model;
	}

	/**
	 * Resets the controller's board to the model's, used to create a new game
	 */
	public void resetBoard() {
		board = model.getStringBoard();
	}

	/**
	 * Calculate score of W
	 * 
	 * @return wScore Score of W
	 */
	public int getWScore() {
		wScore = 0;
		for (int i = 0; i < board.length; i++) {
			for (int j = 0; j < board.length; j++) {
				if (model.getAt(i, j).equals("W"))
					wScore += 1;
			}
		}
		return wScore;
	}

	/**
	 * Calculate score of B
	 * 
	 * @return bScore Score of B
	 */
	public int getBScore() {
		bScore = 0;
		for (int i = 0; i < board.length; i++) {
			for (int j = 0; j < board.length; j++) {
				if (model.getAt(i, j).equals("B"))
					bScore += 1;
			}
		}
		return bScore;
	}

	/**
	 * Puts the right piece at specified row/col for specified player
	 * 
	 * @param col    Column of move
	 * @param row    Row of move
	 * @param player W or B
	 */
	public void move(int row, int col, String player) {
		if (player == "W")
			model.setBoard(row, col, Color.WHITE);
		if (player == "B")
			model.setBoard(row, col, Color.BLACK);
		board[row][col] = player;
	}

	/**
	 * Determines if game is over
	 * 
	 * Game is over if there are no more empty spaces, or if there are no more valid
	 * moves
	 * 
	 * @return true if game is over; false if there are more valid moves
	 */
	public boolean gameOver() {
		boolean emptySpace = false;
		for (int i = 0; i < ReversiBoard.DIM; i++) {
			for (int j = 0; j < ReversiBoard.DIM; j++) {
				if (board[i][j] == "_")
					emptySpace = true;
			}
		}
		// board is full
		if (!emptySpace)
			return true;

		boolean validMove = false;
		for (int i = 0; i < ReversiBoard.DIM; i++) {
			for (int j = 0; j < ReversiBoard.DIM; j++) {
				if (checkValid(i, j, "W", false) || checkValid(i, j, "B", false))
					validMove = true;
			}
		}
		// no valid moves
		if (!validMove)
			return true;
		return false;
	}

	/**
	 * Determines if the move at row, col for specified player is valid
	 * 
	 * @param row    Row the player wants to move to
	 * @param col    Column the player wants to move to
	 * @param player "B" or "W"
	 * @param        actuallyMove: true if you're actually trying to move the piece,
	 *               false if you're just trying to check if the move is valid
	 *               without moving anything
	 * @return isValid true if the move is valid, false otherwise
	 */
	public boolean checkValid(int row, int col, String player, boolean actuallyMove) {
		boolean isValid = false;
		if (model.getAt(row, col) != "_")
			return false;
		for (int dX = -1; dX < 2; dX++) {
			for (int dY = -1; dY < 2; dY++) {
				// continue if it is checking itself
				if (dX == 0 && dY == 0) {
					continue;
				}

				// check 8 directions
				int checkRow = row + dX;
				int checkCol = col + dY;

				// needs to be within board dimensions
				if (checkRow >= 0 && checkCol >= 0 && checkRow < dimension && checkCol < dimension) {

					// if it is white, check for black, and vice versa
					if (model.getAt(checkRow, checkCol) == (player == "W" ? "B" : "W")) {

						// distance
						for (int d = 0; d < dimension; d++) {
							int r = row + d * dX;
							int c = col + d * dY;

							// continue if it is not on the board
							if (r < 0 || c < 0 || r > dimension - 1 || c > dimension - 1)
								continue;

							// if you find a valid move
							if (model.getAt(r, c).equals(player)) {

								// are you actually moving or just checking
								if (actuallyMove) {

									// flip if actuallyMove is true
									for (int d2 = 1; d2 < d; d2++) {
										int rFlip = row + d2 * dX;
										int cFlip = col + d2 * dY;
										if (player == "W")
											model.setBoard(rFlip, cFlip, Color.WHITE);
										if (player == "B")
											model.setBoard(rFlip, cFlip, Color.BLACK);
										board[rFlip][cFlip] = player;
									}
								}
								isValid = true;
								break;
							}
						}
					}
				}
			}
		}
		return isValid;
	}

	/**
	 * checks to see if the player has valid moves
	 * 
	 * @param player "W" or "B"
	 * @return true if player has valid moves, false otherwise
	 */
	public boolean hasValidMoves(String player) {
		for (int i = 0; i < dimension; i++) {
			for (int j = 0; j < dimension; j++) {
				if (model.getAt(i, j) == "_")
					if (checkValid(i, j, player, false))
						return true;
			}
		}
		return false;
	}
}