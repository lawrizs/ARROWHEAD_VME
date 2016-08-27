package org.arrowhead.wp5.agg.api;

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




/**
 * An interface that defines a factory for creation of an object of a ChangeRecOfObject subclass.
 * This is needed due to Java does not handle covariant and contravariant types well.   
 * 
 * @author Laurynas Siksnys (siksnys@cs.aau.dk), AAU
 *
 * @param <OT> type of an objects, e.g., FlexOffer, FGroup.
 * @param <CT> type of an objects's change recored, e. g., ChangeRecOfFlexOffer, ChangeRecOfGroup 
 */
public interface IChangeRecFactory<OT, CT extends ChangeRecOfObject> {
	public CT getNewChRecInstance(OT obj);
}
