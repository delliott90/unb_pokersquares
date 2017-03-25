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
    private boolean rootOnBoard = false;
	
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

        int[] best = depthSearch(card, DEPTH_LIMIT);
        placeCard(card, best[0], best[1], grid); // Place card on main grid

        int[] playPosition = {best[0], best[1]};
//        System.out.println("Card: " + card + " Position: " + best[0] + ":" + best[1]);
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

    private Card[] setRemainingCards(){
        Card[] remainingCards = new Card[cardsInDeck.length];
        System.arraycopy(cardsInDeck, 0, remainingCards, 0, cardsInDeck.length);
        return remainingCards;
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

    private int[] depthSearch(Card card, int depthLimit){

        Card[][] tempGrid = copyGrid(grid);

        System.out.println("NEXT PLAY GRID STATE WITH CARD " + card);
        system.printGrid(tempGrid);

        int[] rootPosition = findFirstEmptySpot(grid, true);

        Card[] remainingCards = setRemainingCards();
        remainingCards = removeCardFromRemaining(card, remainingCards);

        int[] bestSpotFound = placeAndScore(card, card, tempGrid, 1, remainingCards, rootPosition);
//        int[] bestSpotFound ={0, 0};
//        for(int row = 0; row < SIZE; row++) {
//            for (int col = 0; col < SIZE; col++) {
//                if (grid[row][col] == null) {
//                    rootPosition[0] = row;
//                    rootPosition[1] = col;
//                    bestSpotFound = placeAndScore(card, card, tempGrid, 1, remainingCards, rootPosition);
//                }
//            }
//        }


        int bestRowFound = bestSpotFound[0];
        int bestColFound = bestSpotFound[1];
        int[] bestSpot = {bestRowFound, bestColFound};

        // remove current card from remaining cards in deck
        removeCardFromRemaining(card, cardsInDeck);

        return bestSpot;
    }


    private int[] placeAndScore(Card card, Card rootCard, Card[][] grid, int currentDepth, Card[] remainingCards, int[] rootPosition){
        int bestRow = rootPosition[0];
        int bestCol = rootPosition[1];

        int rootRow = rootPosition[0];
        int rootCol = rootPosition[1];
        if(currentDepth >= DEPTH_LIMIT){
            for(int row = 0; row < SIZE; row++) {
                for (int col = 0; col < SIZE; col++) {
                    if (grid[row][col] == null && card != null) {
                        placeCard(card, row, col, grid);
                        system.printGrid(grid);
                        removeCard(row, col, grid);
                    }
                }
            }

        }
        else{
            for(int row = 0; row < SIZE; row++) {
                for (int col = 0; col < SIZE; col++) {
                    if (grid[row][col] == null) {

                        if(card != null && !rootOnBoard && card.equals(rootCard)){
                            placeCard(card, rootRow, rootCol, grid);
                            system.printGrid(grid);
                            rootOnBoard = true;
                        }
                        else if(card != null && rootOnBoard && !card.equals(rootCard)){
                            placeCard(card, row, col, grid);
                            system.printGrid(grid);
                        }

                        if (currentDepth < DEPTH_LIMIT) {
                            currentDepth ++;
                            for (int i = 0; i < remainingCards.length; i++) {
                                placeAndScore(remainingCards[i], rootCard, grid, currentDepth, remainingCards, rootPosition);
                            }
                        }
                        removeCard(row, col, grid);
                    }

                }
            }
        }

        int score = 0;
        int[] bestSpot = {bestRow, bestCol, score};
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
