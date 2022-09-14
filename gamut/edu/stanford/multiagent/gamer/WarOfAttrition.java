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
 * Return an instance of the game War of Attrition
 */ 


public class WarOfAttrition extends TimingGame
{

    // Parameters: War Of Attrition is parameterized first by the number
    // of actions (in other words, the maximum number of time steps),
    // and finally by low and high boundaries on the valuations for
    // each player for the item, and low and high boundaries on how
    // much the the utility of each player  will decrease at each time 
    // step (each player has a different decrement value).

    private static Parameters.ParamInfo valLow;
    private static Parameters.ParamInfo valHigh;
    private static Parameters.ParamInfo decrementLow;
    private static Parameters.ParamInfo decrementHigh;
    private static Parameters.ParamInfo[] warParam;

    static {
	// The valLow, valHigh, decrementLow, and decrementHigh 
	// randomization params were chosen somewhat arbitrarily.  The 
	// user can hand set these parameters to be out of the randomization 
	// range, though it is recommended that the valuation stays 
	// significantly higher than the decrement.  If the user sets either
	// the decrements or the values but not both then the range for the
	// other will be updated automatically.

	valLow = new Parameters.ParamInfo("valuation_low", Parameters.ParamInfo.DOUBLE_PARAM, new Double(10), new Double(1000), "lower bound on the players' valuations for the item, should be between 10 and 1000.");

	valHigh = new Parameters.ParamInfo("valuation_high",  Parameters.ParamInfo.DOUBLE_PARAM, new Double(10), new Double(1000), "upper bound on the players' valuations for the item. Must be >= valuation_low.");

	decrementLow = new Parameters.ParamInfo("decrement_low", Parameters.ParamInfo.DOUBLE_PARAM, new Double(1), new Double(100), "lower bound on the amount that the worth of the object to a player is decremented by at each time step.  Note that each player has a different decrement value.  Should be <= valuation_low, and between 1 and 100.");

	decrementHigh = new Parameters.ParamInfo("decrement_high", Parameters.ParamInfo.DOUBLE_PARAM, new Double(1), new Double(100), "upper bound on the amount that the worth of the object to a player is decremented by at each time step.  Must be >= decrement_low.");

	warParam = new Parameters.ParamInfo[] {Game.symActions,
					       valLow, valHigh, 
					       decrementLow, decrementHigh};
	Global.registerParams(WarOfAttrition.class, warParam);
    }


    // ----------------------------------------------


    public WarOfAttrition() 
	throws Exception
    {
	super();
    }



    public void initialize()
	throws Exception
    {
	super.initialize();

	// The number of players must always be two, but the number
	// of actions is extensible
	setNumPlayers(2);
	parseSameNumberActions();
    }


    /**
     * Make sure that the parameters are in the proper range
     */
    protected void checkParameters() throws Exception 
    {
	if(getDoubleParameter(decrementLow.name) <= 0)
	    throw new Exception("decrement_low <= 0");

	if(getDoubleParameter(valLow.name) <= 0)
	    throw new Exception("valuation_low <= 0");
	
	if(getDoubleParameter(valLow.name)
	   > getDoubleParameter(valHigh.name))
	    throw new Exception("valuation_low > valuation_high");
	
	if(getDoubleParameter(decrementLow.name)
	   > getDoubleParameter(decrementHigh.name))
	    throw new Exception("decrement_low > decrement_high"); 

	if(getDoubleParameter(decrementHigh.name)
	   > getDoubleParameter(valLow.name))
	    throw new Exception("decrement_high > valuation_low"); 
    }


    /**
     * Randomize the parameters which were not filled in by
     * the user.  Since it would be extremely complicated to
     * allow random combinations of 4 variables which need to be
     * fully ordered to be set, we force that both decrements are
     * either set or not set, and same for both valuations.
     */
    public void randomizeParameters() 
    {
	// Check that either both or neither decrement values are set.
	if (parameters.setByUser(decrementLow.name) !=
	    parameters.setByUser(decrementHigh.name)) {
	    Global.handleError("Randomization Error: Please set either " +
			       "both or neither of parameters " +
			       "decrement_low and decrement_high");
	}	    

	// Check that either both or neither valuation values are set.
	if (parameters.setByUser(valLow.name) !=
	    parameters.setByUser(valHigh.name)) {
	    Global.handleError("Randomization Error: Please set either " +
			       "both or neither of parameters " +
			       "valuation_low and valuation_high");
	}

	// Set the decrements
	if (!(parameters.setByUser(decrementHigh.name))) {
	    
	    if (parameters.setByUser(valLow.name)) {
		if (getDoubleParameter(valLow.name) < 1.0) {
		    Global.handleError("Randomization Error: Parameter " +
				       "valuation_low should be >= 1.0 " +
				       "to leave room for decrement_low " +
				       "and decrement_high randomization.");
		}

		decrementLow.high = new 
		    Double(getDoubleParameter(valLow.name));
		decrementHigh.high = new 
		    Double(getDoubleParameter(valLow.name));
	    }
	    
	    parameters.randomizeParameter(decrementLow.name);
	    decrementHigh.low = 
		new Double(getDoubleParameter(decrementLow.name));
	    parameters.randomizeParameter(decrementHigh.name);

	}

	// Set the valuations
	if (!(parameters.setByUser(valHigh.name))) {
	    
	    valLow.low = new 
		Double(getDoubleParameter(decrementHigh.name));
	    valHigh.low = new 
		Double(getDoubleParameter(decrementHigh.name));

	    parameters.randomizeParameter(valLow.name);
	    valHigh.low = 
		new Double(getDoubleParameter(valLow.name));
	    parameters.randomizeParameter(valHigh.name);
	}
    }




    protected String getGameHelp()
    {
	return "Creates an instance of the War of Attrition.  In a " +
	    "War of Attrition, two players are in a dispute over an " +
	    "object, and each chooses a time to concede the object to " +
	    "the other player.  If both concede at the same time, they " +
	    "share the object.  Each player has a valuation of " +
	    "the object, and each player's utility is decremented at " +
	    "every time step.\n\n" +
	    "Payoffs are based on the ranges of valuations and decrements " +
	    "provided.  Although normalization may have the effect that " +
	    "the ranges of the payoffs will change, the ratio of the " +
	    "valuation amount to the decrement amount will still come " +
	    "into play.";
    }


    public void doGenerate()
    {

	// -- get Parameter values
	double lowVal = getDoubleParameter(valLow.name);
	double highVal = getDoubleParameter(valHigh.name);
	double lowDec = getDoubleParameter(decrementLow.name);
	double highDec = getDoubleParameter(decrementHigh.name);

	setDescription("War of Attrition\n" + getDescription());
	setName("War of Attrition");

	int players = getNumPlayers();

	// Generate valuations for the object in the range from
	// lowVal to highVal for each player
	double valuations[] = new double[players];
	for (int i = 0; i < players; i++) {
	    valuations[i] = Global.randomDouble(lowVal, highVal);
	}

	// Generate time step decrement amounts for each player
	// in the range from lowDec to highDec
	double decrement[] = new double[players];
	for (int i = 0; i < players; i++) {
	    decrement[i] = Global.randomDouble(lowDec, highDec);
	}

	// Create TimingGameParams objects which will be 
	// necessary to hold the function parameters for each player
	TimingGameParams low[] = new TimingGameParams[players];
	TimingGameParams notLow[] = new TimingGameParams[players];
	TimingGameParams tie[] = new TimingGameParams[players];

	for (int i = 0; i < players; i++) {
	    // Set up function parameters for when player chooses
	    // lowest time
	    low[i] = new TimingGameParams(0, -decrement[i], decrement[i]);

	    // Function params for when player ties for low time
	    tie[i] = new TimingGameParams(0, -decrement[i], 
					  valuations[i]/2 + decrement[i]);

	    // Function params for when player does not have low time
	    notLow[i] = new TimingGameParams(0, -decrement[i], 
					     valuations[i] + decrement[i]);

	    // note: In each of these, the additional add-on of
	    // decrement[i] is there to balance out the fact that the
	    // first time step should not be subtracting anything
	}

	// Finally, set these parameters so that the payoff can be
	// calculated using the timing game class
	setParamsWithTie(low, tie, notLow);
    }

}
