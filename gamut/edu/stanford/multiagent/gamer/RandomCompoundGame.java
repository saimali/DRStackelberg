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
 * Return a compound game based on a randomly generated
 * symmetric 2x2 matrix.
 */ 


public class RandomCompoundGame extends CompoundGame
{

    // Parameters: The number of players is a parameter.
    private static Parameters.ParamInfo[] rcgParam;

    static {
	rcgParam = new Parameters.ParamInfo[] {players};
	Global.registerParams(RandomCompoundGame.class, rcgParam);
    }


    // ----------------------------------------------


    public RandomCompoundGame() 
	throws Exception
    {
	super();
    }



    public void initialize()
	throws Exception
    {
	// Number of players and actions are initialized
	// by the CompoundGame superclass, as is the graph.
	super.initialize();
    }



    //
    // Make sure that the parameters are in the proper range
    //
    protected void checkParameters() throws Exception 
    {
	// None to check
    }



    //
    // Randomize the parameters which were not filled in by
    // the user.
    //
    public void randomizeParameters() 
    {
	// None to randomize
    }



    protected String getGameHelp()
    {
	return "Creates a Compound Game from a randomly " +
	    "generated symmetric 2x2 matrix.\n\n" +
	    "A Compound Game is a game in which the payoff " +
	    "for each player is calculated as if he were playing " +
	    "the same two by two game with each of the other players " +
	    "and summing the payoffs.\n\n" +
	    "The values in the 2x2 game matrix are always " +
	    "chosen at random from values between -100 and 100.  To " +
	    "change this range, use the normalization or integer " +
	    "payoff options.";
    }



    /**
     * Generate the symmetric 2x2 subgame and create a polymatrix
     * game with this 2x2 matrix at all edges except the edges
     * from nodes to themselves which will have stub 0 matrices. 
     */
    public void doGenerate()
    {
	double low = -100;
	double high = 100;

	setDescription("Random Compound Game\n" 
			 + getDescription());
	setName("Random Compound Game");

	double R, S, T, P;

	// Generate four subgame payoff values in the range 
	// from matrix_low to matrix_high.
	R = Global.randomDouble(low, high);
	S = Global.randomDouble(low, high);
	T = Global.randomDouble(low, high);
	P = Global.randomDouble(low, high);

	generatePolymatrixGame(R, S, T, P);
    }

}
