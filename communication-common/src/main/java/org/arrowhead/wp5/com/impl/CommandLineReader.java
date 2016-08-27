package org.arrowhead.wp5.com.impl;

/*-
 * #%L
 * ARROWHEAD::WP5::Communication Common
 * %%
 * Copyright (C) 2016 The ARROWHEAD Consortium
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */


import java.io.FilterReader;
import java.io.IOException;
import java.io.Reader;

/**
This is a quick hack to turn empty lines entered interactively on the 
command line into ';\n' empty lines for the interpreter.  It's just more 
pleasant to be able to hit return on an empty line and see the prompt 
reappear.
	
This is *not* used when text is sourced from a file non-interactively.
*/
class CommandLineReader extends FilterReader {

public CommandLineReader( Reader in ) {
	super(in);
}

static final int 
	normal = 0,
	lastCharNL = 1,
	sentSemi = 2;

int state = lastCharNL;

public int read() throws IOException {
	int b;

	if ( state == sentSemi ) {
		state = lastCharNL;
		return '\n';
	}

	// skip CR
    while ( (b = in.read()) == '\r' );

	if ( b == '\n' )
		if ( state == lastCharNL ) {
			b = ';';
			state = sentSemi;
		} else
			state = lastCharNL;
	else
		state = normal;

	return b;
}

/**
	This is a degenerate implementation.
	I don't know how to keep this from blocking if we try to read more
	than one char...  There is no available() for Readers ??
*/
public int read(char buff[], int off, int len) throws IOException 
{
	int b = read();
	if ( b == -1 )
		return -1;  // EOF, not zero read apparently
	else {
		buff[off]=(char)b;
		return 1;
	}
}
}