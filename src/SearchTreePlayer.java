/**
 * SearchTreePlayer - a simple example implementation of the player interface for PokerSquares that 
 * ADD MORE STUFF HERE.
 * Ordering of suits in the play grid are as follows:
 * Column 0: Clubs, Column 1: Diamonds, Column 2: Hearts, Column 3: Spades, Column 4: Overflow
 * Author: Danny Elliott, based on code provided by Todd W. Neller and Michael Fleming
 */
public class SearchTreePlayer implements PokerSquaresPlayer {

    private final int SIZE = 5; // number of rows/columns in square grid
    private final int NUM_POS = SIZE * SIZE; // number of positions in square grid
    private final int NUM_CARDS = Card.NUM_CARDS; // number of cards in deck
    private Card[][] grid = new Card[SIZE][SIZE]; // grid with Card objects or null (for empty positions)
    private PokerSquaresPointSystem system; // point system
    private int numberOfPlays = 0;
    private int depthLimit = 2;
	
	/* (non-Javadoc)
	 * @see PokerSquaresPlayer#init()
	 */
	@Override
	public void init() { 
        // clear grid
        for (int row = 0; row < SIZE; row++)
            for (int col = 0; col < SIZE; col++)
                grid[row][col] = null;
        numberOfPlays = 0;

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

        return findBestPosition(card);

    }

//  Todo: score isn't changing between tournament tests.
    private int[] findBestPosition(Card card){
	    int initialScore = system.getScore(grid);
        int currentScore = initialScore;
        int bestScore = initialScore;
//        System.out.println("Initial Scores starting at " + initialScore);
        int currentRow = 0;
        int currentCol = 0;
        int bestRow = 0;
        int bestCol = 0;
	    int[] playPostition = new int[2];

	    // place first card in top left corner
	    if(numberOfPlays == 0){
            placeCard(card, bestRow, bestCol);
            playPostition[0] = bestRow;
	        playPostition[1] = bestCol;
        }
        // best current score or last empty
        else{
	        for(int row = 0; row < SIZE; row++){
	            for(int col = 0; col < SIZE; col++){
	                if(grid[row][col] == null){
	                    currentRow = row;
	                    currentCol = col;
	                    placeCard(card, row, col);
                        currentScore = system.getScore(grid);
	                    if(currentScore > bestScore){
	                        bestScore = currentScore;
	                        bestRow = row;
	                        bestCol = col;
//                            System.out.println("Best score is now " + bestScore + " BR: " + bestRow + " BC: " + bestCol);
	                        removeCard(row, col);
                        }
                        else{
	                        removeCard(row, col);
                        }
                    }
                }
            }
            // best position has changed
            if(bestRow != 0 || bestCol != 0){
//	            System.out.println("Using the best: " + bestScore + " BR: " + bestRow + " BC: " + bestCol);
	            placeCard(card, bestRow, bestCol);
                playPostition[0] = bestRow;
                playPostition[1] = bestCol;
            }
            // user last empty position
            else{
                placeCard(card, currentRow, currentCol);
//                System.out.println("Using last position: " + system.getScore(grid) + " CR: " + currentRow + " CC: " + currentCol);
                playPostition[0] = currentRow;
                playPostition[1] = currentCol;
            }
        }
        numberOfPlays ++;
        return playPostition;

    }

    private void placeCard(Card card, int row, int col){
        grid[row][col] = card;
    }

    private void removeCard(int row, int col){
        grid[row][col] = null;
    }

    /* (non-Javadoc)
	 * @see PokerSquaresPlayer#setPointSystem(PokerSquaresPointSystem, long)
	 */
    @Override
    public void setPointSystem(PokerSquaresPointSystem system, long millis) {
        this.system = system;
    }

	/* (non-Javadoc)
	 * @see PokerSquaresPlayer#getName()
	 */
	@Override
	public String getName() { return "SearchTreePlayer"; }

	/**
	 * Demonstrate SearchTreePlayer play with British point system.
	 * @param args (not used)
	 */
	public static void main(String[] args) {
		PokerSquaresPointSystem system = PokerSquaresPointSystem.getBritishPointSystem();
		System.out.println(system);
		new PokerSquares(new SearchTreePlayer(), system).play(); // play a single game
	}

}