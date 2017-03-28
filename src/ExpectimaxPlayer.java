/**
 * ExpectimaxPlayer - a simple example implementation of the player interface for PokerSquares that 
 * ADD MORE STUFF HERE.
 * Ordering of suits in the play grid are as follows:
 * Column 0: Clubs, Column 1: Diamonds, Column 2: Hearts, Column 3: Spades, Column 4: Overflow
 * Author: Danny Elliott, based on code provided by Todd W. Neller and Michael Fleming
 */
public class ExpectimaxPlayer implements PokerSquaresPlayer {

    private final int SIZE = 5; // number of rows/columns in square grid
    private final int NUM_POS = SIZE * SIZE; // number of positions in square grid
    private final int NUM_CARDS = Card.NUM_CARDS; // number of cards in deck
    private Card[][] grid = new Card[SIZE][SIZE]; // grid with Card objects or null (for empty positions)
    private PokerSquaresPointSystem system; // point system
    private final int DEPTH_LIMIT = 2;
    private Card[] cardsInDeck = new Card[NUM_CARDS];
    private int cardsOnGrid = 0;
    private double chanceNodeValue = 0.0;

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

        cardsOnGrid++;

        int[] bestPosition = depthSearch(card);

        placeCard(card, bestPosition[0], bestPosition[1], grid); // Place card on main grid
        int[] playPosition = {bestPosition[0], bestPosition[1]};

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

    // This method gets hit for every terminal node
    private double scoreGrid(Card[][] grid, double terminalHighScore){
        double stateScore = system.getScore(grid);

        if(terminalHighScore < stateScore){
            terminalHighScore = stateScore;
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

//        System.out.println("NEXT PLAY GRID STATE WITH CARD " + card);

        // remove current card from remaining cards in deck
        removeCardFromRemaining(card, cardsInDeck);

        int[] rootPosition = new int[2];
        int[] bestSpotFound = findFirstEmptySpot(tempGrid, false);


        if(DEPTH_LIMIT==1){
            rootPosition = bestSpotFound;
            placeAndScore(card, tempGrid, 1, rootPosition, 0.0);
        }
        else{
            double bestScore = 0.0;
            // iterate over all possible root card positions
            for(int row = 0; row < SIZE; row++) {
                for (int col = 0; col < SIZE; col++) {
                    if (grid[row][col] == null) {
                        chanceNodeValue = 0.0;
                        removeCard(rootPosition[0], rootPosition[1], tempGrid);
                        rootPosition[0] = row;
                        rootPosition[1] = col;
                        placeAndScore(card, tempGrid, 1, rootPosition, 0.0);

                        if(bestScore < chanceNodeValue){
                            bestScore = chanceNodeValue;
                            bestSpotFound = rootPosition;
                        }
                    }
                }
            }

        }
        return bestSpotFound;

    }


    private void placeAndScore(Card card, Card[][] grid, int currentDepth, int[] rootPosition, double terminalHighScore){
        double termHighScore = terminalHighScore;
        if(currentDepth >= DEPTH_LIMIT){
            // Skip evaluation for first and last card placed on grid
            if(cardsOnGrid < 2 || cardsOnGrid == NUM_POS){
                return;
            }
            else {
                // play possible next card in each empty position and take best score
                for (int row = 0; row < SIZE; row++) {
                    for (int col = 0; col < SIZE; col++) {
                        if (grid[row][col] == null && card != null) {
                            placeCard(card, row, col, grid); // MAX NODE

                            // This effects performance so don't use this for real.
                            // You'll need to lower the number of chance cards or player will run out of time
//                            system.printGrid(grid);
//                            System.out.println("");

                            termHighScore = scoreGrid(grid, termHighScore); // MAX VALUE OF TERMING NODE
                            removeCard(row, col, grid);
                        }
                    }
                }
                chanceNodeValue += termHighScore * (1.0/(NUM_CARDS-cardsOnGrid)); // BUILD UP VALUE OF CHANCE NODE
            }
        }
        else{
            placeCard(card, rootPosition[0], rootPosition[1], grid); // place root card: INITIAL MAX NODE
            currentDepth ++;
            for (int i = 0; i < cardsInDeck.length; i++) {
                placeAndScore(cardsInDeck[i], grid, currentDepth, rootPosition, 0.0); // CHANCE NODE for next card picked
            }

        }

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
    public String getName() { return "ExpectimaxPlayer"; }

    /**
     * Demonstrate ExpectimaxPlayer play with British point system.
     * @param args (not used)
     */
    public static void main(String[] args) {
        PokerSquaresPointSystem system = PokerSquaresPointSystem.getBritishPointSystem();
        System.out.println(system);
        new PokerSquares(new ExpectimaxPlayer(), system).play(); // play a single game
    }

}
