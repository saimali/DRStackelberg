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

package edu.stanford.multiagent.gamer.functions;

import edu.stanford.multiagent.gamer.*;

/**
 * An abstract class to support functions on vectors of values.
 */
public abstract class VectorFunction extends ParameterizedObject
{

    /**
     * Constructor for a vector function.
     */
    public VectorFunction()
	throws Exception
    {
	super();
    }

    /**
     * Returns the arity of the function
     */
    public abstract int getArity();

    /**
     * Returns the value of the function evaluated on x.
     *
     * @param x an array of values on which the function should
     * be executed
     */
    public abstract double eval(double[] x);


    /**
     * Initializes the vector function.
     */
    public void initialize()
	throws Exception
    {
	super.initialize();
    }
}
