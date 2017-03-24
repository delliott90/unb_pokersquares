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
    private int depthLimit = 2;
    private Card[] cardsInDeck = new Card[NUM_CARDS];
	
	/* (non-Javadoc)
	 * @see PokerSquaresPlayer#init()
	 */
	@Override
	public void init() { 
        // clear grid
        for (int row = 0; row < SIZE; row++)
            for (int col = 0; col < SIZE; col++)
                grid[row][col] = null;
        cardsInDeck = Card.getAllCards();
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

        int[] best = depthSearch(card, depthLimit);
        placeCard(card, best[0], best[1], grid); // Place card on main grid

        int[] playPosition = {best[0], best[1]};
        System.out.println("Card: " + card + " Position: " + best[0] + ":" + best[1]);
        return playPosition;

    }

    private Card[][] copyGrid(Card[][] inputGrid){
	    Card[][] outputGrid = new Card[inputGrid.length][inputGrid.length];
	    for(int i=0; i< inputGrid.length; i++){
	        System.arraycopy(inputGrid[i], 0, outputGrid[i], 0, inputGrid.length);
        }
        return outputGrid;
    }
    private void placeCard(Card card, int row, int col, Card[][] grid){ grid[row][col] = card; }
    private void removeCard(int row, int col, Card[][] grid){
        grid[row][col] = null;
    }

    private int[] depthSearch(Card card, int depthLimit){

        Card[][] tempGrid = copyGrid(grid);
        Card[] remainingCards = new Card[cardsInDeck.length];
        System.arraycopy(cardsInDeck, 0, remainingCards, 0, cardsInDeck.length);

        System.out.println("TEMP GRID");
        system.printGrid(tempGrid);

        int[] bestSpotFound = combinedRecursiveDepthSearch(card, 1, remainingCards, tempGrid, system.getScore(tempGrid));
        int bestRowFound = bestSpotFound[0];
        int bestColFound = bestSpotFound[1];
        int[] bestSpot = {bestRowFound, bestColFound};

        // remove current card from remaining cards in deck
        int i=0;
        while(!card.equals(cardsInDeck[i])){
            i++;
        }
        cardsInDeck[i] = null;

        return bestSpot;
    }


    private int[] combinedRecursiveDepthSearch(Card card, int currentDepth, Card[] remainingCards, Card[][] tempGrid, int bestScore){
        // Todo: How to track the best spots? These seem to get overwritten.
        int bestRow = 0;
        int bestCol = 0;
        // Default best position to first empty spot on grid
        bestCoordinateInitialization:
        for(int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                if(tempGrid[row][col] == null){
                    bestRow = row;
                    bestCol = col;
                    break bestCoordinateInitialization;
                }
            }
        }
        int currentScore = 0;

        System.out.println("Current Deck: " + printRemainingCards(remainingCards));

        // iterate over each empty grid space and choose the one with the best score
        for(int row = 0; row < SIZE; row++){
            for(int col = 0; col < SIZE; col++){
                if(tempGrid[row][col] == null){
                    placeCard(card, row, col, tempGrid);

                    System.out.println("Trying card " + card + " at " + row + ":" + col);
                    system.printGrid(tempGrid);


                    if(currentDepth < depthLimit){
                        // remove current card from the temp deck
                        int i=0;
                        while(!card.equals(remainingCards[i])){
                            i++;
                        }
                        remainingCards[i] = null;
                        Card nextCard = remainingCards[0];
                        // Find the next card in the temp deck
                        for(int x=0; x<remainingCards.length; x++){
                            if(remainingCards[x] != null){
                                nextCard = remainingCards[x];
                                break;
                            }
                        }
                        currentDepth++;
                        // Recursive call
                        return combinedRecursiveDepthSearch(nextCard, currentDepth, remainingCards, tempGrid, bestScore);


                    }
                    // Score current grid state
                    currentScore = system.getScore(tempGrid);

                    System.out.println("Grid Score: " + currentScore);

                    // Find best spot based on current grid score
                    if(currentScore > bestScore){
                        bestScore = currentScore; // Todo: This will have to be bubbled up from below
                        bestRow = row;
                        bestCol = col;
                        removeCard(row, col, tempGrid);
                    }
                    else{
                        removeCard(row, col, tempGrid);
                    }
                }
            }
        }
        int[] bestSpot = {bestRow, bestCol, bestScore};
        return bestSpot;
    }


    private String printRemainingCards(Card[] deck){
        String cardList = "";
        for(int i=0; i<deck.length; i++){
            cardList += deck[i] + ":";
        }
        return cardList;
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
