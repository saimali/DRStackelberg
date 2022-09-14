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
 * Return a Minimum Effort Game
 *
 * The MEG was coded as a TimingGame because it was very
 * simple to add on using the TimingGame infrastructure.  The
 * downside is that the TimingGame class requires that
 * MinimumEffortGame store many copies of the same parameters.
 * It could be more efficient to set this up in another way.
 */ 


public class MinimumEffortGame extends TimingGame
{

    // Parameters: A Minimum Effort Game is parameterized first by the
    // number of players, next by the number of actions (in other words, 
    // the maximum level of effort which can be chosen), and finally by 
    // the values of the coefficients a, b, and c which are used in the 
    // payoff function a + bM - cE.

    private static Parameters.ParamInfo pA;
    private static Parameters.ParamInfo pB;
    private static Parameters.ParamInfo pC;
    private static Parameters.ParamInfo[] megParam;

    static {
	pA = new Parameters.ParamInfo("a",  Parameters.ParamInfo.DOUBLE_PARAM, new Double(-100), new Double(100), "constant a used in formula a + bM - cE.  Should be between -100 and 100.");

	pB = new Parameters.ParamInfo("b",  Parameters.ParamInfo.DOUBLE_PARAM, new Double(0), new Double(100), "coefficient b used in formula a + bM - cE.  Should be between 0 and 100.");

	pC = new Parameters.ParamInfo("c",  Parameters.ParamInfo.DOUBLE_PARAM, new Double(0), new Double(100), "coefficient used in formula a + bM - cE.  Should be between 0 and 100 but must be < b.");

	megParam = new Parameters.ParamInfo[] {Game.players, Game.symActions,
					       pA, pB, pC};
	Global.registerParams(MinimumEffortGame.class, megParam);
    }


    // ----------------------------------------------


    public MinimumEffortGame() 
	throws Exception
    {
	super();
    }


    public void initialize()
	throws Exception
    {
	super.initialize();

	// Set the number of players and the number of actions (time
	// steps) from the parameters
	parsePlayersSameNumberActions();
    }


    //
    // Make sure that the parameters are in the proper range
    //
    protected void checkParameters() throws Exception 
    {
	if(getDoubleParameter(pC.name)
	   >= getDoubleParameter(pB.name))
	    throw new Exception("c >= b");

    }


    public void randomizeParameters() 
    {
	// Force that user randomize all or none of a, b, and c
	if ((parameters.setByUser(pA.name) != parameters.setByUser(pB.name)) ||
	    (parameters.setByUser(pA.name) != parameters.setByUser(pC.name))) {
	    Global.handleError("Randomization Error: Please set all or " +
			       "none of parameters a, b, and c.");
	}

	if (!parameters.setByUser(pA.name)) {
	    // Need to randomize all three
	    parameters.randomizeParameter(pA.name);
	    parameters.randomizeParameter(pB.name);

	    // Reset range on c as it must always be less than b
	    pC.high = new Double(getDoubleParameter(pB.name));
	    parameters.randomizeParameter(pC.name);		    
	}
    }


    protected String getGameHelp()
    {
	return "Creates an instance of the Minimum Effort Game.\n\n" +
	    "In this game, the payoff for a player is determined by a " +
	    "formula a + bM - cE where E is the player's effort and M " +
	    "is the minimum effort of any player.";
    }


    public void doGenerate()
    {
	// Get parameter values
	double a = getDoubleParameter(pA.name);
	double b = getDoubleParameter(pB.name);
	double c = getDoubleParameter(pC.name);

	setDescription("Minimum Effort Game\n" + getDescription());
	setName("Minimum Effort Game");

	int players = getNumPlayers();
	
	// Create TimingGameParams objects which will be 
	// necessary to hold the function parameters for each player
	TimingGameParams low[] = new TimingGameParams[players];
	TimingGameParams notLow[] = new TimingGameParams[players];
	TimingGameParams tie[] = new TimingGameParams[players];

	// All players have the same parameters in their functions
	// all of the time
	for (int i = 0; i < players; i++) {
	    low[i] = new TimingGameParams(-c, b, a);
	    notLow[i] = new TimingGameParams(-c, b, a);
	    tie[i] = new TimingGameParams(-c, b, a);
	}

	// Finally, set these parameters so that the payoff can be
	// calculated using the timing game class
	setParamsWithTie(low, tie, notLow);
    }

}
