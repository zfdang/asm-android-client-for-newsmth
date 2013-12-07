/*
 * Copyright (c) 2009-2011 Graham Edgecombe.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

package com.athena.asm.util.vt100;

/**
 * Represents an ANSI control sequence.
 * @author Graham Edgecombe
 */
class AnsiControlSequence {

	/**
	 * The command character.
	 */
	private final char command;

	/**
	 * The parameters.
	 */
	private final String[] parameters;

	/**
	 * Creates an ANSI control sequence with the specified command and
	 * parameters.
	 * @param command The command character.
	 * @param parameters The parameters array.
	 * @throws NullPointerException if the parameters array is {@code null}.
	 */
	public AnsiControlSequence(char command, String[] parameters) {
		if (parameters == null) {
			throw new NullPointerException("parameters");
		}
		this.command = command;
		if (parameters.length == 1 && parameters[0].equals("")) {
			this.parameters = new String[0];
		} else {
			this.parameters = parameters.clone();
		}
	}

	/**
	 * Gets the command character.
	 * @return The command character.
	 */
	public char getCommand() {
		return command;
	}

	/**
	 * Gets the parameters array.
	 * @return The parameters array.
	 */
	public String[] getParameters() {
		return parameters;
	}

}

