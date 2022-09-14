/*
 * Copyright (C) 2004 Jennifer Wortman, Eugene Nudelman, Kevin Leyton-Brown, Yoav Shoham.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.stanford.multiagent.gamer;

/**
 * Returns an instance of the two player location game
 * based on Hotelling's original model.
 *
 */


public class LocationGame extends Game
{

    // Parameters: The location game is parameterized by the
    // length l of the street, the length a of the street on the 
    // far side of player 1, the length b of the street on the 
    // far side of player 2, the cost c per distance of 
    // transporting the product, and min and max prices that
    // the players can offer.

    protected static Parameters.ParamInfo pADist;
    protected static Parameters.ParamInfo pBDist;
    protected static Parameters.ParamInfo pLDist;
    protected static Parameters.ParamInfo pCost;
    protected static Parameters.ParamInfo pLowAct;
    protected static Parameters.ParamInfo[] lgParams;

    static {
	pLowAct = new Parameters.ParamInfo("price_low", Parameters.ParamInfo.LONG_PARAM, new Long(1), new Long(5000), "lowest price each player can choose.  Must be > 0 and <= 5000.  The highest price each player can choose will then be price_low + actions - 1.");

	pADist = new Parameters.ParamInfo("a", Parameters.ParamInfo.DOUBLE_PARAM, new Double(0), new Double (1000), "distance between the location of player 1's store and his end of the street.  Must fall between 0 and 1000.");

	pBDist = new Parameters.ParamInfo("b", Parameters.ParamInfo.DOUBLE_PARAM, new Double(0), new Double(1000), "distance between the location of player 2's store and his end of the street.  Must fall between 0 and 1000.");

	pLDist = new Parameters.ParamInfo("l", Parameters.ParamInfo.DOUBLE_PARAM, new Double(0), new Double(1000), "length of the entire street.  Must be >= a + b but <= 1000.");
	
	pCost = new Parameters.ParamInfo("c", Parameters.ParamInfo.DOUBLE_PARAM, new Double(1), new Double(100), "cost per unit of transporting the goods.  Must fall between 1 and 100.  See the note in the help string about randomization and values of this parameter.");

	lgParams = new Parameters.ParamInfo[] {Game.symActions, pADist, pBDist, 
					       pLDist, pCost, pLowAct};
	Global.registerParams(LocationGame.class, lgParams); 
    }


    // ----------------------------------------------------------

    
    public LocationGame()
	throws Exception
    {
	super();
    }


    public void initialize() throws Exception
    {
	super.initialize();

	// Set the number of players (always 2) and actions
	setNumPlayers(2);
	parseSameNumberActions();
    }

    
    // 
    // Make sure parameters are in the correct range.  Does not 
    // need to check price_low and price_high as this is already
    // taken care of by the initialize function.
    //
    protected void checkParameters() throws Exception
    {
	if (getDoubleParameter(pADist.name) < 0)
	    throw new Exception("a < 0");

	if (getDoubleParameter(pBDist.name) < 0)
	    throw new Exception("b < 0");

	if (getDoubleParameter(pLDist.name) <= 0)
	    throw new Exception("l <= 0");

	if (getDoubleParameter(pCost.name) <= 0)
	    throw new Exception("c <= 0");

	if (getDoubleParameter(pADist.name) + getDoubleParameter(pBDist.name) >
	    getDoubleParameter(pLDist.name))
	    throw new Exception("a + b > l");
    }


    //
    // Randomize parameters that were not set by the user
    //
    public void randomizeParameters() {

	// The low price is straight-forward to randomize
	if (!(parameters.setByUser(pLowAct.name))) {
	    parameters.randomizeParameter(pLowAct.name);
	}

	// The cost per unit good does not directly depend on
	// anything, but it is best if the cost is less than the
	// price the player can sell for, thus when we randomize, we
	// force the cost to be less.
	if (!(parameters.setByUser(pCost.name))) {
	    pCost.high = new Double((double)getLongParameter(pLowAct.name));
	    parameters.randomizeParameter(pCost.name);
	}

	// The user must set all or none of l, a, and b to avoid
	// over complication of the randomization.
	if ((parameters.setByUser(pLDist.name) != 
	     parameters.setByUser(pADist.name)) ||
	    (parameters.setByUser(pLDist.name) !=
	     parameters.setByUser(pBDist.name))) {
	    Global.handleError("Randomization Error: Please set all or " +
			       "none of parameters a, b, and l.");
	}
	
	if (!parameters.setByUser(pLDist.name)) {
	    // Need to randomize all three
	  
	  parameters.randomizeParameter(pLDist.name);
	  double sum=Global.randomDouble(0, getDoubleParameter(pLDist.name));
	  double a=Global.randomDouble(0, sum);
	  try {
	    parameters.setParameter(pADist.name, new Double(a));
	    parameters.setParameter(pBDist.name, new Double(sum-a));
	  } catch (Exception e) {
	    Global.handleError(e, "Randomization Error");
	  }

	  /*
	    parameters.randomizeParameter(pADist.name);
	    parameters.randomizeParameter(pBDist.name);
	    pLDist.low = new Double(getDoubleParameter(pADist.name) 
				    + getDoubleParameter(pBDist.name));
	    parameters.randomizeParameter(pLDist.name);		    
	  */
	}

    }	


    protected String getGameHelp() {

	return "Creates an instance of the two person Location " +
	    "Game based on Hotelling's original model.\n\nIn this " +
	    "game there is a street of length l.  Player one has " +
	    "a shop set up distance a from one end of the street " +
	    "and player 2 has a shop set up distance b from the " +
	    "other end.  Customers are uniformly distributed along " +
	    "the street and the cost of getting a good from a shop " +
	    "to a home on the street is c times the distance.  The " +
	    "players must pick a price at which to sell their goods " +
	    "in order to maximize their profit assuming that " +
	    "production is free and customers will always choose " + 
	    "the shop for which the combined good price and " +
	    "transportation cost is smaller.\n\nProfits may be " +
	    "scaled if normalization is used, but relations between " +
	    "the parameters will remain the same and are thus important.\n\n" +
	    "Be very careful randomizing parameters in this game.  If the " +
	    "cost of transporting goods is too high, it will always be " +
	    "a dominant strategy for both players to choose their highest " +
	    "action and the game will lose some of its intended interesting " +
	    "properties.";

    }


    /**
     * Return the payoff for the given player at the given outcome.
     */
    public double getPayoff(int[] outcome, int player) 
    {
	double dist[] = new double[2];
	dist[0] = getDoubleParameter(pADist.name);
	dist[1] = getDoubleParameter(pBDist.name);

	double l = getDoubleParameter(pLDist.name);
	double c = getDoubleParameter(pCost.name);

	// Action counting always starts at 1 so need to
	// adjust the index
	long offset = getLongParameter(pLowAct.name) - 1;

	double price[] = new double[2];
	for (int i = 0; i < getNumPlayers(); i++)
	    price[i] = (double) (outcome[i] + (int) offset);

	// First check if either player has priced their item
	// so much higher that the other player makes all of the
	// profits.
	for (int i = 0; i < getNumPlayers(); i++) {
	    if (price[i] > price[1-i] + c * (l - dist[0] - dist[1])) {
	
		if (player == i) {
		    // The player has charged too much and
		    // will receive no profit
		    return 0;
		} else {
		    // The opponent has charged too much so all
		    // customers will go to the player
		    return (l * price[1-i]);
		}
	    }
	}


	// If this is not the case, then player 1 will get all 
	// of the customers in the a section, player 2 will get all
	// of the customers in the b section, and the middle
	// section will be split up according to price and distance
	// according to the formulas in Hotelling's work.
	return (double) ((0.5 * (l + dist[player] - dist[1-player]) 
			  * price[player])
			 - ((price[player] * price[player]) / (2.0 * c))
			 + ((price[0] * price[1]) / (2.0 * c)));
    }



    /**
     * All information for generating payoffs for the location
     * game is contained in the parameter set.  There is no
     * additional game generation necessary.
     */
    public void doGenerate() {
	setDescription("Location Game\n" + getDescription());
	setName("Location Game");
	return;
    }


}
