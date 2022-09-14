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

package edu.stanford.multiagent.gamer.graphs;

import java.util.*;
import edu.stanford.multiagent.gamer.*;

/**
 * Generates a graph on an n-dimensional grid that wraps 
 * around at the edges, i.e. 1D is a ring, 2D is a torus, and so 
 * on.  Currently all dimensions are the same length to keep 
 * parameterization from getting out of hand.
 *
 * NDimensionalWrappedGrid is built as a subclass of 
 * NDimensionalGrid as it uses many of the same functions 
 * (initialize, checkParameters, translateIndices, etc.)
 * but more edges are generated.
 */

public class NDimensionalWrappedGrid extends NDimensionalGrid
{

    // ---------------------------------------------------
    // Parameters: For now all parameters which are used are
    // inherited from the NDimensionalGrid class so no new
    // parameters are necessary.

  static {
    Global.registerParams(NDimensionalWrappedGrid.class, ndgParam);
  }
    // ---------------------------------------------------


    //
    // Constructed in same way as NDimensionalGrid
    //
    public NDimensionalWrappedGrid()
	throws Exception
    {
	super();
    }

  protected  String getGraphHelp()
  {
    return "NDimensionalWrappedGrid: Generates an n-dimensional grid on a sphere with a given number of points in each dimension. Each node is connected to its neighbors.";
  }


    // 
    // Generate an N-dimensional wrapped grid graph.  Is
    // generated in exactly the same way as NDimensionaGrid
    // except that edges that wrap around the outsides of the
    // grids are also added.
    //
    public void doGenerate() {

	int n = (int) getLongParameter("num_dimensions");
	long dimSize = getLongParameter("dim_size");

	long currEdges = 0;

	boolean hasMore = true;
	int currNode[] = new int[n];

	// Set the index in each dimension to be 0 first
	for (int i = 0; i < n; i++)
	    currNode[i] = 0;

	// Systematically loop through all nodes indexed by their
	// position in the n-dimensional grid, adding edges
	// between adjacent nodes
	while (hasMore) {

	    // First find the next node
	    for (int i = 0; i < n; i++) {
		if (currNode[i] < dimSize - 1) {
		    currNode[i]++;
		    break;
		} else {
		    currNode[i] = 0;
		    if(i == n - 1)
			hasMore = false;
		}
	    }

	    int node1 = translateIndices(currNode, dimSize, n);

	    // Now add one to each dimension in the node and
	    // add edges back and forth
	    for (int i = 0; i < n; i++) {

		if (currNode[i] + 1 < dimSize) {
		    currNode[i]++;
		} else {
		    // wrap around
		    currNode[i] = 0;
		}

		int node2 = translateIndices(currNode, dimSize, n);
		
		// Add the edges to the graph
		addEdge(node1, node2);
		addEdge(node2, node1);
		
		// Reset currNode to the currentNode
		if (currNode[i] > 0) {
		    currNode[i]--;
		} else {
		    // for the case of the wrap-arounds
		    currNode[i] = (int) dimSize - 1;
		}
	    }
	}
    }
}


