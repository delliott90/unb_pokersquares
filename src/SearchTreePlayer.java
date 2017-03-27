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
    private final int DEPTH_LIMIT = 2;
    private Card[] cardsInDeck = new Card[NUM_CARDS];
    private int[] bestScoreAndPosition = new int[3]; // row, col, score
    private int cardsOnGrid = 0;
	
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
        initializeBestScorePosition(grid);
        cardsOnGrid = 0;
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

        initializeBestScorePosition(grid);
        cardsOnGrid++;

        // EXPECTIMAX otherwise don't need return value
        int[] bestPosition = depthSearch(card);

        placeCard(card, bestScoreAndPosition[0], bestScoreAndPosition[1], grid); // Place card on main grid
        int[] playPosition = {bestScoreAndPosition[0], bestScoreAndPosition[1]};

// EXPECTIMAX
//        placeCard(card, bestPosition[0], bestPosition[1], grid); // Place card on main grid
//        int[] playPosition = {bestPosition[0], bestPosition[1]};

        return playPosition;

    }

    private Card[][] copyGrid(Card[][] inputGrid){
	    Card[][] outputGrid = new Card[inputGrid.length][inputGrid.length];
	    for(int i=0; i< inputGrid.length; i++){
	        System.arraycopy(inputGrid[i], 0, outputGrid[i], 0, inputGrid.length);
        }
        return outputGrid;
    }

    private void placeCard(Card card, int row, int col, Card[][] grid){
        grid[row][col] = card;
    }

    private void removeCard(int row, int col, Card[][] grid){
        grid[row][col] = null;
    }

    private void initializeBestScorePosition(Card[][] grid){
        int[] emptySpot = findFirstEmptySpot(grid, false);
        bestScoreAndPosition[0] = emptySpot[0];
        bestScoreAndPosition[1] = emptySpot[1];
        bestScoreAndPosition[2] = 0;
    }

    private void scoreGridDirectly(int row, int col, Card[][] grid){
        int stateScore = system.getScore(grid);
        if (bestScoreAndPosition[2] < stateScore){
            bestScoreAndPosition[0] = row;
            bestScoreAndPosition[1] = col;
            bestScoreAndPosition[2] = stateScore;
        }
    }

    // This method gets hit for every terminal node
    // EXPECTIMAX
    private int scoreGrid(int row, int col, Card[][] grid, int terminalHighScore){
        int stateScore = system.getScore(grid);

        if(terminalHighScore < stateScore){
            terminalHighScore = stateScore;
        }


        if (bestScoreAndPosition[2] < stateScore){
            bestScoreAndPosition[0] = row;
            bestScoreAndPosition[1] = col;
            bestScoreAndPosition[2] = stateScore;
        }

        return terminalHighScore;
    }

    private int[] findFirstEmptySpot(Card[][] grid, boolean random){
        int emptyRow = 0;
        int emptyCol = 0;
        boolean spotFound = false;
        if(random){
            randomCoordinateInitialization:
            for(int i=0; i < SIZE; i++){
                int row = (int)(Math.random() * 5);
                int col = (int)(Math.random() * 5);
                if(grid[row][col] == null){
                    emptyRow = row;
                    emptyCol = col;
                    spotFound = true;
                    break randomCoordinateInitialization;
                }
            }
        }
        if(!spotFound){
            firstCoordinateInitialization:
            for(int row = 0; row < SIZE; row++) {
                for (int col = 0; col < SIZE; col++) {
                    if(grid[row][col] == null){
                        emptyRow = row;
                        emptyCol = col;
                        break firstCoordinateInitialization;
                    }
                }
            }
        }
        int[] emptySpot = {emptyRow, emptyCol};
        return emptySpot;
    }

    private Card[] removeCardFromRemaining(Card card, Card[] remainingCards){
        int i=0;
        while(!card.equals(remainingCards[i]) && i < remainingCards.length){
            i++;
        }
        remainingCards[i] = null;
        return remainingCards;
    }

    private int[] depthSearch(Card card){

        Card[][] tempGrid = copyGrid(grid);

        System.out.println("NEXT PLAY GRID STATE WITH CARD " + card);

        // remove current card from remaining cards in deck
        removeCardFromRemaining(card, cardsInDeck);

//        int[] rootPosition = findFirstEmptySpot(grid, true);
//        int[] bestSpotFound = placeAndScore(card, card, tempGrid, 1, remainingCards, rootPosition);

        int[] rootPosition = new int[2];
        int[] bestSpotFound = findFirstEmptySpot(tempGrid, false);
        double bestScore = 0.0; // EXPECTIMAX

        if(DEPTH_LIMIT==1){
            rootPosition = bestSpotFound;
            // EXPECTIMAX otherwise terminalHighScore and chanceCardValue not needed
            placeAndScore(card, tempGrid, 1, rootPosition, true, 0, 0);
        }
        else{
            double chanceCardValue = 0.0;
            for(int row = 0; row < SIZE; row++) {
                for (int col = 0; col < SIZE; col++) {
                    if (grid[row][col] == null) {
                        removeCard(rootPosition[0], rootPosition[1], tempGrid);
                        rootPosition[0] = row;
                        rootPosition[1] = col;
                        System.out.println("OUTER CHECK FOR " + card + " at " + row + ":" + col);
                        // EXPECTIMAX otherwise don't need returned value
                        chanceCardValue = placeAndScore(card, tempGrid, 1, rootPosition, true, 0, 0);
                        // EXPECTIMAX
                        if(bestScore < chanceCardValue){
                            bestScore = chanceCardValue;
                            bestSpotFound = rootPosition;
                        }
                    }
                }
            }

        }
        return bestSpotFound; // EXPECTIMAX

    }


    private double placeAndScore(Card card, Card[][] grid, int currentDepth, int[] rootPosition, boolean placingRoot, int terminalHighScore, double chanceCardValue){

        if(currentDepth >= DEPTH_LIMIT){
            if(cardsOnGrid < 2 || cardsOnGrid == NUM_POS){
                // EXPECTIMAX otherwise empty return
                return chanceCardValue;
            }
            else {
                for (int row = 0; row < SIZE; row++) {
                    for (int col = 0; col < SIZE; col++) {
                        if (grid[row][col] == null && card != null) {
                            placeCard(card, row, col, grid); // MAX NODE
                            system.printGrid(grid);
                            System.out.println("");
                            if(placingRoot){
                                // EXPECTIMAX otherwise can use scoreGrid()
                                scoreGridDirectly(row, col, grid); // Only when DEPTH_LIMIT == 1
                            }
                            else{
                                terminalHighScore = scoreGrid(rootPosition[0], rootPosition[1], grid, terminalHighScore); // EXPECTIMAX
                            }
                            removeCard(row, col, grid);
                        }
                    }
                }
                chanceCardValue += terminalHighScore * (1/(NUM_CARDS-cardsOnGrid)); // EXPECTIMAX
            }
        }
        else{
            placeCard(card, rootPosition[0], rootPosition[1], grid); // place root card: INITIAL MAX NODE if DEPTH_LIMIT > 1
            currentDepth ++;
            for (int i = 0; i < cardsInDeck.length/3; i++) {
                placeAndScore(cardsInDeck[i], grid, currentDepth, rootPosition, false, 0, chanceCardValue); // CHANCE NODE for next card picked
            }
        }
        return chanceCardValue; // EXPECTIMAX
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
