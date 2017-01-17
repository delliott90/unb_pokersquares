import java.util.Collections;
import java.util.Stack;

/**
 * FlushPlayer - a simple example implementation of the player interface for PokerSquares that 
 * attempts to get flushes in the first four columns.
 * Ordering of suits in the play grid are as follows:
 * Column 0: Clubs, Column 1: Diamonds, Column 2: Hearts, Column 3: Spades, Column 4: Overflow
 * Author: Danny Elliott, based on code provided by Todd W. Neller and Michael Fleming
 */
public class FlushPlayer implements PokerSquaresPlayer {

    private final int SIZE = 5; // number of rows/columns in square grid
    private final int NUM_POS = SIZE * SIZE; // number of positions in square grid
    private final int NUM_CARDS = Card.NUM_CARDS; // number of cards in deck
    private Card[][] grid = new Card[SIZE][SIZE]; // grid with Card objects or null (for empty positions)

	
	/* (non-Javadoc)
	 * @see PokerSquaresPlayer#setPointSystem(PokerSquaresPointSystem, long)
	 */
	@Override
	public void setPointSystem(PokerSquaresPointSystem system, long millis) {
		// The FlushPlayer, like the RandomPlayer, does not worry about the scoring system.	
	}
	
	/* (non-Javadoc)
	 * @see PokerSquaresPlayer#init()
	 */
	@Override
	public void init() { 
        // clear grid
        for (int row = 0; row < SIZE; row++)
            for (int col = 0; col < SIZE; col++)
                grid[row][col] = null;

	}

	/* (non-Javadoc)
	 * @see PokerSquaresPlayer#getPlay(Card, long)
	 */
	@Override
	public int[] getPlay(Card card, long millisRemaining) {
		int cardrow = 0;
		int cardcol = 0;

		int cardrank = card.getRank();
		int cardsuit = card.getSuit(); // 0-3

		int[] emptyRowPosition;
		int columnToSearch = cardsuit;

		// check card suit column
		emptyRowPosition = findEmptyRowPosition(columnToSearch);
        boolean spotFound = (emptyRowPosition[0] == 1);
		if (spotFound){
			cardrow = emptyRowPosition[1];
			cardcol = columnToSearch;
		}
		else {
			// check overflow column
			columnToSearch = SIZE - 1;
			emptyRowPosition = findEmptyRowPosition(columnToSearch);
			spotFound = (emptyRowPosition[0] == 1);

			if (spotFound){
				cardrow = emptyRowPosition[1];
				cardcol = columnToSearch;
			}
		}

		if (!spotFound){
			// check remaining columns
			for (int i = 0; i < SIZE-1; i++){
				if (i != cardsuit){
					emptyRowPosition = findEmptyRowPosition(i);
					spotFound = (emptyRowPosition[0] == 1);
					if (spotFound){
						cardrow = emptyRowPosition[1];
						cardcol = i;
					}
				}
			}
		}
        grid[cardrow][cardcol] = card;
        return new int[] {cardrow, cardcol};
	}

    /**
     * Given a column index, this method attempts to find an empty space in the 2-D grid array in the given column.
     * Method returns a length 2 array. The first entry contains a true (1) false (0) flag representing in a blank
     * space was successfully found. The second entry contains the row index of the empty space if one was found,
     * otherwise it contains 0.
     * @param columnIndex index of the grid column to search
     * @return length 2 array containing succeess flag and row index of empty space
     */
    private int[] findEmptyRowPosition (int columnIndex) {
        int[] responseArray = new int[2];
        for (int row = 0; row < SIZE; row++){
            if (grid[row][columnIndex] == null){
                responseArray[0] = 1; // success
                responseArray[1] = row;
                return responseArray;
            }
        }
        responseArray[0] = 0; // failed
        responseArray[1] = 0;
        return responseArray;
    }

	/* (non-Javadoc)
	 * @see PokerSquaresPlayer#getName()
	 */
	@Override
	public String getName() {
		return "FlushPlayer";
	}

	/**
	 * Demonstrate FlushPlayer play with British point system.
	 * @param args (not used)
	 */
	public static void main(String[] args) {
		PokerSquaresPointSystem system = PokerSquaresPointSystem.getBritishPointSystem();
		System.out.println(system);
		new PokerSquares(new FlushPlayer(), system).play(); // play a single game
	}

}
