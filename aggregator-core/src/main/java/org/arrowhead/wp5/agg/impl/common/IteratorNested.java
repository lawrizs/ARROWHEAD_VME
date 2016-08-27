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
 * A nested iterator. It iterates over elements of B (of T2 type), where B is some element of A (of T1 type).     
 * 
 * @author Laurynas Siksnys (siksnys@cs.aau.dk), AAU
 *
 * @param <T1> some type
 * @param <T2> some type
 */
public class IteratorNested<T1, T2> implements Iterator<T2> {
	private InnerGenerator<T1, T2> innerGenerator;
	private Iterator<T1> t1Iter = null;
	private Iterator<T2> t2Iter = null;
	private T2 t2Item = null;

	public IteratorNested(Iterator<T1> t1Iter,
			InnerGenerator<T1, T2> innerGenerator) {
		this.innerGenerator = innerGenerator;
		this.t1Iter = t1Iter;
		this.t2Iter = null;
		this.getNextItem();
	}

	private void getNextItem() {
		this.t2Item = null;
		if (this.t2Iter != null && this.t2Iter.hasNext()) {
			this.t2Item = t2Iter.next();
			return;
		}
		;

		this.t2Iter = null;

		if (t1Iter != null)
			while (this.t1Iter.hasNext()) {
				this.t2Iter = this.innerGenerator.generateItems(this.t1Iter
						.next());
				if (this.t2Iter != null && this.t2Iter.hasNext()) {
					this.t2Item = t2Iter.next();
					return;
				}
			}
	}

	@Override
	public boolean hasNext() {

		return this.t2Item != null;
	}

	@Override
	public T2 next() {
		T2 t2copy = this.t2Item;
		this.getNextItem();
		return t2copy;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

	public interface InnerGenerator<T1, T2> {
		public Iterator<T2> generateItems(T1 innerItem);
	}
}
