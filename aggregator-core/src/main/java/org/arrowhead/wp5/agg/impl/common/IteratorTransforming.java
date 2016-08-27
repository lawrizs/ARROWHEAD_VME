package org.arrowhead.wp5.agg.impl.common;

/*-
 * #%L
 * ARROWHEAD::WP5::Aggregator Core
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


import java.util.Iterator;

/**
 * An abstract iterator class, which can cast elements of type T to an 
 * elements of type U during an iteration.
 *  
 * @author Laurynas Siksnys (siksnys@cs.aau.dk), AAU
 *
 * @param <T> some type
 * @param <U> some type
 */
public abstract class IteratorTransforming<T, U> implements Iterable<U>, Iterator<U> {
	public abstract U apply(T object);

	private final Iterator<T> source;

	public IteratorTransforming(Iterable<T> source) {
		if (source == null)
			this.source = new IteratorEmpty<T>();
		else 
			this.source = source.iterator();
	}
	
	public IteratorTransforming(Iterator<T> source) {
		if (source == null)
			this.source = new IteratorEmpty<T>();
		else 
			this.source = source;
	}

	@Override
	public boolean hasNext() {
		return source.hasNext();
	}

	@Override
	public U next() {
		return apply(source.next());
	}

	@Override
	public void remove() {
		source.remove();
	}

	@Override
	public Iterator<U> iterator() {
		return this;
	}
}
