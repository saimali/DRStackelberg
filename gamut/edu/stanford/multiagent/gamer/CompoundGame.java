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

import edu.stanford.multiagent.gamer.graphs.*;


/**
 * Class implements any compound game.  Can be extended to
 * generate N-player version of any symmetric 2x2 game.
 */


public abstract class CompoundGame extends GraphicalGame
{

    /**
     * Construct a new Compound game.
     */
    public CompoundGame()
	throws Exception
    {
	super();
    }


    /**
     * Sets the number of players from the command line parameter,
     * sets the number of actions to 2 for each player, and 
     * initializes the graph.  All child classes should call
     * this function for initialization.
     *
     * @throws Exception if, for example, there is a problem 
     * initializing the graph
     */
    public void initialize()
	throws Exception
    {
	super.initialize();

	// The number of players is extensible but the number
	// of actions must always be 2
	int players = (int) getLongParameter(Game.players.name);
	setNumPlayers(players);
	
	int numActions[];
	numActions = new int[players];
	for (int i = 0; i < players; i++)
	    numActions[i] = 2;
	setNumActions(numActions);

	initGraph();
    }



    /**
     * Initializes a complete graph with the number of nodes
     * equal to the number of players in the game.
     */
    protected void initGraph() {
	try {
	    graph = new CompleteGraph(getNumPlayers(), false);
	    graph.initialize();
	} catch (Exception e) {
	  System.err.println(getHelp());
	  System.err.println(graph.getHelp());
	  Global.handleError(e, "Error initializing graph:");
	}
    }



    /**
     * Sets up the compound game as a graphical game (polymatrix,
     * really) with each submatrix being in the form
     *               R, R     S, T
     *               T, S     P, P
     * for whatever values of R, S, T, and P are passed in.
     *
     * @param R
     * @param S
     * @param T
     * @param P
     */
    protected void generatePolymatrixGame(double R, double S, 
					  double T, double P) {

	// Generate the complete graph.
	graph.doGenerate();

	// The same DoubleTensor will be used for all subgames 
	// in the polymatrix game
	int[] dimensions = new int[2];
	int[] indices = new int[2];
	dimensions[0] = dimensions[1] = 2;

	// Create a new DoubleTensor to hold the symmetric matrix
	// and fill in the values of R, S, T, and P in the
	// appropriate spots
	DoubleTensor symMatrix = new DoubleTensor(dimensions);

	indices[0] = 1;
	indices[1] = 1;
	symMatrix.setValue(R, indices);

	indices[0] = 1;
	indices[1] = 2;
	symMatrix.setValue(S, indices);

	indices[0] = 2;
	indices[1] = 1;
	symMatrix.setValue(T, indices);

	indices[0] = 2;
	indices[1] = 2;
	symMatrix.setValue(P, indices);

	// Now add edges to the graph for the game between each
	// pair of distinct players in each direction using the
	// symmetric matrix as the edge object
	for (int i = 0; i < getNumPlayers(); i++) {
	    for (int j = 0; j < getNumPlayers(); j++) {
		if (i != j) {
		    try {
			graph.setEdgeData(i, j, symMatrix);
		    } catch (Exception e) {
			System.out.println("Error setting graph values");
			e.printStackTrace();
			System.exit(1);
		    }
		}
	    }
	}
    }

}
