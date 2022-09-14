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

import java.util.*;
import edu.stanford.multiagent.gamer.graphs.*;
import edu.stanford.multiagent.gamer.functions.*;

/**
 * Generate a Local Effect Game with a given graphical 
 * structure and a given function structure such that
 * every edge from b to a has the same local effect as 
 * an edge from c to a.  (This is opposite notation from
 * the LEG literature.)
 *
 * Very similar to RandomLEG except for the generation
 * function.
 */


public class UniformLEG extends RandomLEG
{

    // --------------------------------------------------
    // Parameters: Parameters carry over from RandomLEG
    // and from the graph and function being used.  No new
    // parameters are introduced here.
    //

    static {
	Global.registerParams(UniformLEG.class, rlegParam);
    }
    
    // -------------------------------------------------------


    public UniformLEG() 
	throws Exception
    {
	super();
    }



    protected String getGameHelp()
    {
	return "Creates a Uniform Local-Effect Game using the " +
	    "specified graph class and specified function class.\n\n" +
	    "A Uniform Local-Effect Game is a LEG with a graphical " +
	    "structure in which every edge from b to a has the same " +
	    "local-effect function as the edge from c to a.  (This " +
	    "notation is slightly different from what is used in the " +
	    "local-effect literature, but is equivalent.)\n\n" +
	    "Please note " +
	    "that you should be careful when you choose the graph " +
	    "class and set the graph parameters here.  The graph " +
	    "chosen should be symmetric (i.e. whenever there is an " +
	    "edge from a to b there is also an edge from b to a) and " +
	    "should not have reflexive edges.  (Each node will have a " +
	    "local effect on itself, but this is handled outside of the " +
	    "graph.)  Set the parameters for the graph accordingly!";
    }



    /**
     * Generates the graph and for each node and edge of the graph,
     * a function, making sure that functions are the same on every
     * edge from b to a as they are on the edge from c to a.
     */
    public void doGenerate()
    {
	setDescription("Uniform Local-Effect Game\n"  + 
		       getDescription());
	setName("Uniform Local-Effect Game");

	// Generate the graph for the game.
	graph.doGenerate();

	setDescription(getDescription() + "\nGraph Params:\n" + graph.getDescription());

	// First loop through all of the nodes creating their
	// node functions and an array containing their 
	// effect functions for other nodes
	int numNodes = graph.getNNodes();
	Function effectFunctions[] = new Function[numNodes];

	String funcName = parameters.getStringParameter("func");
	ParamParser funcParams = parameters.getParserParameter("func_params");
       
	for (int i = 0; i < numNodes; i++) {
	    Function fNode = (Function) Global.getObjectOrDie(funcName,
							      Global.FUNC);
	    try {
		fNode.setDomain(0, getNumPlayers());
		fNode.setParameters(funcParams, randomize);
		fNode.initialize();
		fNode.doGenerate();
	    } catch (Exception e) {
	  System.err.println(getHelp());
	      System.err.println(fNode.getHelp());
		Global.handleError(e, "Error initializing function");
	    }

	    graph.setNodeData(i, fNode);

	    effectFunctions[i] = (Function) Global.getObjectOrDie
		(funcName, Global.FUNC);
	    
	    try {
		effectFunctions[i].setDomain(0, getNumPlayers());
		effectFunctions[i].setParameters(funcParams, randomize);
		effectFunctions[i].initialize();
		effectFunctions[i].doGenerate();
	    } catch (Exception e) {
	      System.err.println(getHelp());
	      System.err.println(effectFunctions[i].getHelp());
		Global.handleError(e, "Error initializing function");
	    }
	}


	// Now loop through the edges of the graph and add the
	// appropriate function to each
	for (int i = 0; i < graph.getNNodes(); i++) {

	    Iterator edgeIter = graph.getEdges(i);
	    while (edgeIter.hasNext()) {
		Edge edge = (Edge) edgeIter.next();		
		Function fEdge = effectFunctions[edge.getDest()];
		edge.setData(fEdge);
	    }
	}
    }
}
