import java.io.*;
import java.net.*;
import java.util.Observable;

import javafx.application.Platform;
import javafx.event.*;
import javafx.geometry.*;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 * @author Lucia Wang
 * @author Alan Cheng
 * 
 *         ReversiView Creates Reversi GUI with JavaFX to allow users to
 *         interact with the board. Implements Observer interface so that
 *         changing the model can also change the view
 */
public class ReversiView extends javafx.application.Application implements java.util.Observer {

	/**
	 * ReversiController to allow view to communicate changes to model
	 */
	public ReversiController controller;

	/**
	 * Number of rows/columns
	 */
	public int dimension = 8;

	/**
	 * Graphics context to draw board
	 */
	private GraphicsContext gc;

	/**
	 * Label to display score
	 */
	private Label score;

	/**
	 * 8 rows, pieces have 20px radius, 2px insets, border is 2px, edge is 8px
	 */
	private int rowPixels = 384;
	private int colPixels = 384;
	private Socket socket;
	private boolean serverOn;

	public boolean GAMEOVER;
	private int c;
	private String SERVER = "localhost";
	private int PORT = 4000;
	private boolean SERVER_ON = false;
	private boolean CLIENT_ON = false;

	private RadioButton rbServer;
	private RadioButton rbClient;
	private RadioButton rbHuman;
	private RadioButton rbComputer;
	private TextField tfServer;
	private TextField tfPort;
	private Button bOK;
	private Button bCancel;

	private int isServer; // 1 if Server selected; 2 if Client selected; 0 if neither selected
	private int isHuman; // 1 if Human selected; 2 if Computer selected; 0 if neither selected

	/**
	 * Constructs ReversiView object with new ReversiController object whose model
	 * is an observer. ReversiView needs a no-parameter constructor in order to
	 * launch
	 * 
	 * @param ReversiModel model
	 */

	/**
	 * Generages GUI for Reversi game, allows user to interact with the board
	 * through clicks on the tiles
	 * 
	 * @param stage Stage to display GUI
	 */
	@Override
	public void start(Stage stage) {
		// title
		stage.setTitle("Reversi");
		BorderPane window = new BorderPane();

		// new game
		MenuBar menuBar = new MenuBar();
		Menu menuFile = new Menu("File");
		Label label = new Label("New Game");
		MenuItem newGame = new CustomMenuItem(label);
		menuFile.getItems().add(newGame);

		Label networkedGameLabel = new Label("Networked Game");
		MenuItem networkedGame = new CustomMenuItem(networkedGameLabel);
		menuFile.getItems().add(networkedGame);

		menuBar.getMenus().add(menuFile);

		controller = new ReversiController();
		controller.model.addObserver(this);

		score = new Label(scoreString()); // score on bottom
		Canvas board = new Canvas(rowPixels, colPixels); // game board
		gc = board.getGraphicsContext2D();
		reset(board, stage, label); // reset canvas

		// lets user move
		clicking(board, stage, label);
		play(board, stage, label, networkedGameLabel);

		// set up window
		window.setTop(menuBar);
		window.setCenter(board);
		window.setBottom(score);

		// display board
		Group group = new Group();
		group.getChildren().add(window);
		Scene scene = new Scene(group);
		stage.setScene(scene);
		stage.show();
	}

	/**
	 * Resets visuals of the board (green background, black and white circle pieces
	 * in the middle, transparent circle pieces everywhere else, black lines to
	 * separate squares)
	 * 
	 * @param board Canvas displays and updates board
	 * @param stage Stage displays the canvas
	 * @param label Label to show score
	 */
	private void reset(Canvas board, Stage stage, Label label) {
		// set background green
		gc.setFill(Color.GREEN);
		gc.fillRect(0, 0, rowPixels, colPixels);

		// set grid
		gc.setFill(Color.BLACK);
		// horizontal
		for (int y = 9; y < rowPixels; y += 46) {
			gc.setLineWidth(2);
			gc.strokeLine(9, y, 377, y);
		}
		// vertical
		for (int x = 9; x < colPixels; x += 46) {
			gc.strokeLine(x, 9, x, 377);
		}
		// set circles clear initially
		gc.setFill(Color.TRANSPARENT);
		for (int i = 0; i < dimension; i++) {
			for (int j = 0; j < dimension; j++) {
				gc.fillOval(getPixels(i), getPixels(j), 40, 40);
			}
		}

		// ReversiBoard independent of model
		ReversiBoard rb = controller.getModel().getBoard();
		// set colors based on the board
		for (int i = 0; i < ReversiBoard.DIM; i++) {
			for (int j = 0; j < ReversiBoard.DIM; j++) {
				if (rb.getAt(i, j) == ReversiBoard.BLANK)
					gc.setFill(Color.TRANSPARENT);
				else if (rb.getAt(i, j) == ReversiBoard.WHITE)
					gc.setFill(Color.WHITE);
				else if (rb.getAt(i, j) == ReversiBoard.BLACK)
					gc.setFill(Color.BLACK);

				gc.fillOval(this.getPixels(i), this.getPixels(j), 40, 40);
			}
		}
		score.setText(scoreString());
	}

	/**
	 * Given the row/col, calculate pixel location
	 * 
	 * @param i Row/Col we want the pixels of
	 * @return corresponding pixels
	 */
	private int getPixels(int i) {
		return 12 + 46 * i;
	}

	/**
	 * Given pixel, determine col/row
	 * 
	 * @param pixel Pixels on the canvas
	 * @return Row/Col of pixel
	 */
	private int getRowCol(double pixel) {
		return (int) (pixel - 9) / 46;
	}

	/**
	 * Determine if move at Mouse Click Location is valid, update view/model through
	 * ReversiBoard object if move is valid; keeps track of user/CPU turn, updates
	 * score every turn
	 * 
	 * If the user clicks a square that is not legal, it is ignored
	 * 
	 * @param: mouse click MouseEvent
	 */
	private void clicking(Canvas board, Stage stage, Label label) {
		board.setOnMouseClicked(mouse -> {
			boolean gg = controller.gameOver();
			if (gg) { // return if game over
				gameOver(board, stage, label);
				return;
			}
			int row = getRowCol(mouse.getX());
			int col = getRowCol(mouse.getY());

			if (row >= 0 && row < 8 && col >= 0 && col < 8) {
				// call controller to make desired move
				if (controller.checkValid(row, col, "W", false)) {
					controller.checkValid(row, col, "W", true);
					controller.move(row, col, "W");
					score.setText(scoreString());

					boolean bValid = false;
					while (!bValid) {
						row = (int) (Math.random() * dimension);
						col = (int) (Math.random() * dimension);
						bValid = controller.checkValid(row, col, "B", false);
					}
					controller.checkValid(row, col, "B", true);
					controller.move(row, col, "B");
					score.setText(scoreString());

				}
				score.setText(scoreString());
				if (gg = controller.gameOver()) {
					board.setOnMouseClicked(mouse2 -> {
					});
				}
			} else if (gg) {
				board.setOnMouseClicked(mouse2 -> {
				});
			}
		});
	}

	/**
	 * Handle user mouse clicks
	 * 
	 * Validates the user's move and updates the board when a valid move has been
	 * made. Alternates turns with CPU, who moves randomly. Updates score, and
	 * terminates game when neither CPU nor user has any valid moves
	 * 
	 * @param board Canvas object to handle user mouse click events and update GUI
	 * @param stage Stage to display GUI
	 * @param label Label to show score
	 */
	private void play(Canvas board, Stage stage, Label label, Label networkedGameLabel) {

		// clicking exit
		stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
			/**
			 * Handles WindowClosing event and saves the current game to "save_game.dat" by
			 * writing out the serialized ReversiBoard class
			 * 
			 * @param wc WindowEvent
			 */
			public void handle(WindowEvent wc) {
				try {
					FileOutputStream save = new FileOutputStream("save_game.dat");
					ObjectOutputStream out = new ObjectOutputStream(save);
					out.writeObject(controller.getModel().getBoard());
					out.close();
					save.close();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});

		// allow user to start a new game

		/**
		 * Starts a new game with a new model, resets view, deletes the save_game.dat
		 * file if it exists
		 */
		label.setOnMouseClicked(event -> {
			try {
				File file = new File("save_game.dat");
				file.delete();
			} catch (Exception e) {
				e.printStackTrace();
			}
			newGame();
			reset(board, stage, label);
			update(controller.model, controller.model.getBoard());

		});

		networkedGameLabel.setOnMouseClicked(event -> {
			NetworkSetup dialog = new NetworkSetup();
			bOK.setOnAction(event1 -> {
				// clicking OK won't do anything unless options have been selected
				if (rbServer.isSelected() || rbClient.isSelected()) {
					if (rbHuman.isSelected() || rbComputer.isSelected()) {
						// could check if server and port are legal but i'm lazy
						isServer = (rbServer.isSelected() ? 1 : 2);
						System.out.println("server is " + isServer);
						isHuman = (rbHuman.isSelected() ? 1 : 2);
						System.out.println("human is " + isHuman);
						dialog.close();

						// start new game
						newGame();
						clicking(board, stage, networkedGameLabel);

						reset(board, stage, label);
						update(controller.model, controller.model.getBoard());

						// start server/client
						if (isServer == 1) {
							if (!serverOn) {
								ServerSocket serverSocket = null;
								try {
									serverSocket = new ServerSocket(PORT);
									socket = serverSocket.accept();
									System.out.println("Connected to Server");

								} catch (IOException e) {
									System.out.println("Can't connect to server");

									// e.printStackTrace();
								}

								// player type is cpu
								if (isHuman == 2) {
									while (!controller.gameOver() && controller.hasValidMoves("B")) {
										boolean bValid = false;
										int row = 0;
										int col = 0;

										String turn = "B";
										while (!bValid) {
											row = (int) (Math.random() * dimension);
											col = (int) (Math.random() * dimension);
											bValid = controller.checkValid(row, col, turn, false);
										}
										controller.checkValid(row, col, turn, true);
										controller.move(row, col, turn);
									}

								}
								serverOn = true;
							} else {
								System.out.println("the else part");
							}

						} else if (isServer == 2) {
							try {
								socket = new Socket(SERVER, PORT);
								System.out.println("Connected to Server");

							} catch (IOException e) {
								System.out.println("Can't connect to server");
								// e.printStackTrace();
							}
							// player is cpu
							if (isHuman == 2) {
								Thread clientThread = new ClientThread();
								clientThread.run();
								while (!controller.gameOver() && controller.hasValidMoves("W")) {
									boolean wValid = false;
									int row = 0;
									int col = 0;

									String turn = "W";
									while (!wValid) {
										row = (int) (Math.random() * dimension);
										col = (int) (Math.random() * dimension);
										wValid = controller.checkValid(row, col, turn, false);
									}
									controller.checkValid(row, col, turn, true);
									controller.move(row, col, turn);
								}
							}
						}
					}

				}

			});

			bCancel.setOnAction(event3 -> {
				// doesn't do anything
				dialog.close();

			});
			dialog.initModality(Modality.APPLICATION_MODAL);
			dialog.showAndWait();

		});
	}

	class NetworkSetup extends Stage {

		public NetworkSetup() {
			isServer = 0;
			isHuman = 0;
			setTitle("Network Setup");

			// just puts all the shit where they're supposed to go
			Label lCreate = new Label("Create:");
			rbServer = new RadioButton("Server");
			rbClient = new RadioButton("Client");
			ToggleGroup tgCreate = new ToggleGroup();
			rbServer.setToggleGroup(tgCreate);
			rbClient.setToggleGroup(tgCreate);
			HBox create = new HBox();
			create.getChildren().addAll(lCreate, rbServer, rbClient);
			create.setSpacing(10);

			Label lPlayAs = new Label("Play as:");
			rbHuman = new RadioButton("Human");
			rbComputer = new RadioButton("Computer");
			ToggleGroup tgPlayAs = new ToggleGroup();
			rbHuman.setToggleGroup(tgPlayAs);
			rbComputer.setToggleGroup(tgPlayAs);
			HBox playAs = new HBox();
			playAs.getChildren().addAll(lPlayAs, rbHuman, rbComputer);
			playAs.setSpacing(10);

			Label lServerAdd = new Label("Server");
			tfServer = new TextField("localhost");
			Label lPort = new Label("Port");
			tfPort = new TextField("4000");
			SERVER = getServer();
			PORT = getPort();
			HBox connectTo = new HBox();
			connectTo.getChildren().addAll(lServerAdd, tfServer, lPort, tfPort);
			connectTo.setSpacing(10);

			bOK = new Button("OK");

			bCancel = new Button("Cancel");

			HBox buttons = new HBox();
			buttons.getChildren().addAll(bOK, bCancel);
			buttons.setSpacing(10);

			VBox vbox = new VBox();
			vbox.getChildren().addAll(create, playAs, connectTo, buttons);
			VBox.setMargin(create, new Insets(20, 20, 20, 20));
			VBox.setMargin(playAs, new Insets(20, 20, 20, 20));
			VBox.setMargin(connectTo, new Insets(20, 20, 20, 20));
			VBox.setMargin(buttons, new Insets(20, 20, 20, 20));

			Group group = new Group(vbox);
			Scene scene = new Scene(group);
			setScene(scene);
		}

		// idk what else to call it lol

		public int getIsServer() {
			return isServer;
		}

		public int getIsHuman() {
			return isHuman;
		}

		public String getServer() {
			return tfServer.getText();
		}

		public int getPort() {
			return Integer.parseInt(tfPort.getText());
		}
	}

	private class ServerNetwork extends Thread {
		private ReversiBoard rb;

		private ServerNetwork(ReversiBoard rb) {
			rb = rb;
		}

		@Override
		public void run() {

			try {

				// out
				// ServerSocket server = new ServerSocket(PORT);
				while (controller.gameOver()) {
					ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
					out.writeObject(rb);

					// in
					ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

					ReversiBoard move = (ReversiBoard) in.readObject();
					System.out.println("sever in: " + move);
					// update the board?????

					// check if game over, if game over, close server and break out
					GAMEOVER = controller.gameOver();
				}

				// output game info

			} catch (IOException e) {
				System.out.println(" asdf io exception ");
			} catch (ClassNotFoundException e) {
				System.out.println("server class notfound");
			}
		}
	}

	private class ClientThread extends Thread {
		@Override
		public void run() {
			// test run
			try {
				// in
				ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
				ReversiBoard rb = (ReversiBoard) in.readObject();
				System.out.println("client in : " + rb);

				// idk update board

				// out
				// send server info
				ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());

				ReversiBoard rb2 = new ReversiBoard();// wrong
				out.writeObject(rb2);
				System.out.println("in Run method 2->");

			} catch (IOException e) {
				System.out.println("io exception" + e.getMessage());
			} catch (ClassNotFoundException e) {
				System.out.println("ServerClassNotFoundError: " + e.getMessage());
			}

		}
	}

	/**
	 * Creates new model, adds view as observer, resets ReversiBoard object
	 */
	private void newGame() {
		controller.model = new ReversiModel();
		controller.model.addObserver(this);
		controller.resetBoard();
	}

	/**
	 * Updates view if changes have been made to model by switching appropriate
	 * circles to white and black
	 * 
	 * @param model  Model that indicates whether changes have been made
	 * @param oBoard ReversiBoard object that contains the most recent move's col,
	 *               row and color
	 */
	public void update(Observable model, Object oBoard) {
		ReversiBoard rb = (ReversiBoard) oBoard;
		for (int i = 0; i < ReversiBoard.DIM; i++) {
			for (int j = 0; j < ReversiBoard.DIM; j++) {
				if (rb.getAt(i, j) == ReversiBoard.BLANK) {
					gc.setFill(Color.TRANSPARENT);
				} else if (rb.getAt(i, j) == ReversiBoard.WHITE)
					gc.setFill(Color.WHITE);
				else if (rb.getAt(i, j) == ReversiBoard.BLACK)
					gc.setFill(Color.BLACK);
				gc.fillOval(this.getPixels(i), this.getPixels(j), 40, 40);
			}
		}
		score.setText(scoreString());
		if (c == 0) {
			c = 1;
			Thread serverThread = new ServerNetwork(rb);
			serverThread.run();
		} else {
			c = 0;
		}
	}

	/**
	 * Builds a string object to display the score underneath the board
	 * 
	 * @return scoreSB StringBuilder that contains the score
	 */
	private String scoreString() {
		StringBuilder scoreSB = new StringBuilder();
		scoreSB.append("White: ");
		scoreSB.append(controller.getWScore());
		scoreSB.append(" - Black: ");
		scoreSB.append(controller.getBScore());

		return scoreSB.toString();
	}

	/**
	 * Determines win/lose/tie, display info through Alert, starts a new game when
	 * user acknowledges Alert by deleting "save_game.dat"
	 * 
	 * @param board Canvas for Reversi
	 * @param stage Stage to display GUI
	 * @param label Label for "New Game"
	 */
	private void gameOver(Canvas board, Stage stage, Label label) {
		if (controller.getBScore() > controller.getWScore())
			new Alert(Alert.AlertType.INFORMATION, "You lose :(").showAndWait();
		else if (controller.getWScore() > controller.getBScore())
			new Alert(Alert.AlertType.INFORMATION, "You win! :)").showAndWait();
		else
			new Alert(Alert.AlertType.INFORMATION, "It's a tie!").showAndWait();
		try {
			File file = new File("save_game.dat");
			file.delete();
		} catch (Exception e) {
			e.printStackTrace();
		}
		// starts new game after user acknowledges game over (no further moves allowed)
		newGame();
		reset(board, stage, label);
		update(controller.model, controller.model.getBoard());
	}
}