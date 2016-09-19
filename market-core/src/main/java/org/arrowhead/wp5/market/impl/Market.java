package org.arrowhead.wp5.market.impl;

/*-
 * #%L
 * ARROWHEAD::WP5::Market Core
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.linear.LinearConstraint;
import org.apache.commons.math3.optim.linear.LinearConstraintSet;
import org.apache.commons.math3.optim.linear.LinearObjectiveFunction;
import org.apache.commons.math3.optim.linear.NoFeasibleSolutionException;
import org.apache.commons.math3.optim.linear.Relationship;
import org.apache.commons.math3.optim.linear.SimplexSolver;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.arrowhead.wp5.core.entities.Bid;
import org.arrowhead.wp5.core.entities.MarketInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Market {
	private MarketInfo info;
	private List<Bid> supplyUpBids;
	private List<Bid> demandUpBids;
	private List<Bid> supplyDownBids;
	private List<Bid> demandDownBids;
	
	private List<Bid> winningSupplyUpBids;
	public List<Bid> getWinSupplyUp() {
		return winningSupplyUpBids;
	}

	public List<Bid> getWinDemandUp() {
		return winningDemandUpBids;
	}

	public List<Bid> getWinSupplyDown() {
		return winningSupplyDownBids;
	}

	public List<Bid> getWinDemandDown() {
		return winningDemandDownBids;
	}

	private List<Bid> winningDemandUpBids;
	private List<Bid> winningSupplyDownBids;
	private List<Bid> winningDemandDownBids;
	
	final Logger logger = LoggerFactory.getLogger(this.getClass());

	public static void main(String[] argv) {
		Market market = new Market();
		market.clear(market.demandUpBids, market.supplyUpBids, market.winningDemandUpBids, market.winningSupplyUpBids);
		market.clear(market.demandDownBids, market.supplyDownBids, market.winningDemandDownBids, market.winningSupplyDownBids);

		market.logger.debug("supplyUp: {}", market.supplyUpBids);
		Collections.sort(market.supplyUpBids);
		market.logger.debug("supplyUp: {}", market.supplyUpBids);

	}
	
	public Market() {
		supplyUpBids = new ArrayList<Bid>();
		demandUpBids = new ArrayList<Bid>();
		supplyDownBids = new ArrayList<Bid>();
		demandDownBids = new ArrayList<Bid>();
		winningSupplyUpBids = new ArrayList<Bid>();
		winningSupplyDownBids = new ArrayList<Bid>();
		winningDemandUpBids = new ArrayList<Bid>();
		winningDemandDownBids = new ArrayList<Bid>();
		info = new MarketInfo("Market V1", "Area 52", 15, new Date());
	}
	
	public void clear() {
		winningSupplyUpBids = new ArrayList<Bid>();
		winningSupplyDownBids = new ArrayList<Bid>();
		winningDemandUpBids = new ArrayList<Bid>();
		winningDemandDownBids = new ArrayList<Bid>();

		clear(demandUpBids, supplyUpBids, winningDemandUpBids, winningSupplyUpBids);
		clear(demandDownBids, supplyDownBids, winningDemandDownBids, winningSupplyDownBids);
		
		supplyUpBids = new ArrayList<Bid>();
		demandUpBids = new ArrayList<Bid>();
		supplyDownBids = new ArrayList<Bid>();
		demandDownBids = new ArrayList<Bid>();
	}
	
	public MarketInfo getInfo() {
		return info;
	}
	
	public void bidSupply(Bid bid) {
		logger.debug("new supply bud: {}", bid);
		if (bid.isUp()) {
			supplyUpBids.add(bid);
		} else {
			supplyDownBids.add(bid);
		}
	}
	
	public void bidDemand(Bid bid) {
		logger.debug("new demand bud: {}", bid);
		if (bid.isUp()) {
			demandUpBids.add(bid);
		} else {
			demandDownBids.add(bid);
		}
	}
	
	public Set<Bid> getSupplyUp() {
		Set<Bid> res = new HashSet<Bid>();
		res.addAll(supplyUpBids);
		return res;
	}
	public Set<Bid> getDemandUp() {
		Set<Bid> res = new HashSet<Bid>();
		res.addAll(demandUpBids);
		return res;
	}
	
	public Set<Bid> getSupplyDown() {
		Set<Bid> res = new HashSet<Bid>();
		res.addAll(supplyDownBids);
		return res;
	}
	public Set<Bid> getDemandDown() {
		Set<Bid> res = new HashSet<Bid>();
		res.addAll(demandDownBids);
		return res;
	}
	public Set<Bid> getSupply() {
		Set<Bid> res = new HashSet<Bid>();
		res.addAll(supplyUpBids);
		res.addAll(supplyDownBids);
		return res;
	}
	public Set<Bid> getDemand() {
		Set<Bid> res = new HashSet<Bid>();
		res.addAll(demandUpBids);
		res.addAll(demandDownBids);
		return res;
	}
		
	public void clear(List<Bid> demList, List<Bid> supList, List<Bid> winningDemandBids, List<Bid> winningSupplyBids) {
		if (demList.isEmpty() || supList.isEmpty()) {
			logger.debug("Empty list, no clearing.");
			return;
		}
		List<Bid> demandList = new ArrayList<Bid>();
		demandList.addAll(demList);
		List<Bid> supplyList = new ArrayList<Bid>();
		supplyList.addAll(supList);
		
		long totalDemand = getTotalQuantity(demandList);
		long totalSupply = getTotalQuantity(supplyList);
		long minQuantity = Math.min(totalDemand, totalSupply);
		Bid fakeBid = new Bid(0, totalSupply-totalDemand, true, "", "");
		if (totalDemand < totalSupply) {
			demandList.add(fakeBid);
			totalDemand = getTotalQuantity(demandList);
			totalSupply = getTotalQuantity(supplyList);
			minQuantity = Math.min(totalDemand, totalSupply);
		}
		
		int numDemand = demandList.size();
		int numSupply = supplyList.size();
		int demandIdx = 0;
		int supplyIdx = numDemand;
		int total = numDemand + numSupply;
		logger.debug("demand: {} supply: {} total: {}", numDemand, numSupply, total);
		
		double[] demand = getPrice(demandList);
		double[] supply = getPrice(supplyList);
		
		logger.debug("demand: {}", Arrays.toString(demand));
		logger.debug("supply: {}", Arrays.toString(supply));
		
		double[] empty = new double[total];
		
		double[] objective = empty.clone();
		System.arraycopy(demand, 0, objective, demandIdx, numDemand);
		System.arraycopy(supply, 0, objective, supplyIdx, numSupply);
		for (int i = 0; i < supply.length; i++) {
			objective[supplyIdx+i] = -supply[i];
		}
		logger.debug("Empty: {} - obj: {}", Arrays.toString(empty), Arrays.toString(objective));
		
		// describe the optimization problem
		LinearObjectiveFunction f = new LinearObjectiveFunction(objective, 0);
		Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
		
		for (int i = 0; i < demandList.size(); i++) {
			generateConstraint(demandList, demandIdx, total, constraints, i, Relationship.LEQ);
		}
		
		for (int i = 0; i < supplyList.size(); i++) {
			generateConstraint(supplyList, supplyIdx, total, constraints, i, Relationship.GEQ);
		}
		
		double[] constraint = empty.clone();
		for (int i = 0; i < demandList.size(); i++) {
			constraint[i+demandIdx] = 1;
		}
		logger.debug("Total quantity constraint: {} {} {}", Arrays.toString(constraint), Relationship.LEQ, minQuantity);
		constraints.add(new LinearConstraint(constraint, Relationship.LEQ, minQuantity));
		
		constraint = empty.clone();
		for (int i = 0; i < supplyList.size(); i++) {
			constraint[i+supplyIdx] = 1;
		}
		logger.debug("Total quantity constraint: {} {} {}", Arrays.toString(constraint), Relationship.LEQ, minQuantity);
		constraints.add(new LinearConstraint(constraint, Relationship.LEQ, minQuantity));
		
		for (int i = 0; i < demandList.size(); i++) {
			constraint = empty.clone();
			constraint[i+demandIdx] = 1;
			logger.debug("Non zero constraint: {} {} {}", Arrays.toString(constraint), Relationship.GEQ, 0);
			constraints.add(new LinearConstraint(constraint, Relationship.GEQ, 0));
		}
		
		for (int i = 0; i < supplyList.size(); i++) {
			constraint = empty.clone();
			constraint[i+supplyIdx] = 1;
			logger.debug("Non zero constraint: {} {} {}", Arrays.toString(constraint), Relationship.GEQ, 0);
			constraints.add(new LinearConstraint(constraint, Relationship.GEQ, 0));
		}
		
		constraint = empty.clone();
		for (int i = 0; i < demandList.size(); i++) {
			constraint[i+demandIdx] = 1;
		}
		for (int i = 0; i < supplyList.size(); i++) {
			constraint[i+supplyIdx] = -1;
		}
//		logger.debug("Equilibrium constraint: {} {} {}", Arrays.toString(constraint), Relationship.LEQ, totalSupply-totalDemand);
//		constraints.add(new LinearConstraint(constraint, Relationship.LEQ, totalSupply-totalDemand));
		
		LinearConstraintSet set = new LinearConstraintSet(constraints);
		// create and run the solver
		PointValuePair solution;
		try {
		solution = new SimplexSolver().optimize(f, set, GoalType.MAXIMIZE);
		} catch (NoFeasibleSolutionException e) {
			logger.info("No feasuble solution!!");
			return;
		}

		// get the solution
		logger.debug("solution: ");
		double[] solutionPoint = solution.getPoint();
		List<Bid> all = new ArrayList<Bid>();
		all.addAll(demandList);
		all.addAll(supplyList);

		for (int i = 0; i < solutionPoint.length; i++) {
			logger.debug("{} - {} - {}", solutionPoint[i], all.get(i).getQuantity(), all.get(i).getPrice());
		}
		double max = solution.getValue();
		logger.debug("value: {}", max);

		demandList.remove(fakeBid);
		double price = findPrice(demandList, supplyList, solutionPoint);
		logger.debug("win price: {}", price);
		
		for (int i = 0; i < demandList.size(); i++) {
			if (solutionPoint[i+demandIdx] > 0) {
				Bid d = demandList.get(i);
				if (d == fakeBid) {
					continue;
				}
				if (price <= d.getPrice()) {
					d.setWinPrice(price);
//					d.setWinQuantity(solutionPoint[i+demandIdx]);
					logger.debug("Win d: p: {} - q: {} - wq: {}", d.getWinPrice(), d.getQuantity(), d.getWinQuantity());
					winningDemandBids.add(d);
				}
			}
		}
		for (int i = 0; i < supplyList.size(); i++) {
			if (solutionPoint[i+supplyIdx] > 0) {
				Bid d = supplyList.get(i);
				if (price >= d.getPrice()) {
					d.setWinPrice(price);
//					d.setWinQuantity(solutionPoint[i+supplyIdx]);
					logger.debug("Win s: p: {} - q: {} - wq: {}", d.getWinPrice(), d.getQuantity(), d.getWinQuantity());
					winningSupplyBids.add(d);
				}
			}
		}
		logger.debug("winning demand: {} - total quantity: {}", winningDemandBids, getTotalWinQuantity(winningDemandBids));

		logger.debug("winning supply: {} - total quantity: {}", winningSupplyBids, getTotalWinQuantity(winningSupplyBids));
	}

	private double findPrice(List<Bid> demList, List<Bid> supList, double[] solutionPoint) {
		List<Bid> demandList = new ArrayList<Bid>();
		demandList.addAll(demList);
		List<Bid> supplyList = new ArrayList<Bid>();
		supplyList.addAll(supList);
		
		Collections.sort(demandList, Collections.reverseOrder());
		Collections.sort(supplyList);
		
		Collections.reverse(demandList);
		long[] demandCum = cumulative(demandList);
		Collections.reverse(demandList);
		long[] supplyCum = cumulative(supplyList);
		
		logger.debug("sorted: {}", demandList);
		logger.debug("sorted: {}", supplyList);
		
		if (demandList.get(0).getPrice() < supplyList.get(0).getPrice()) {
			return supplyList.get(0).getPrice()-(supplyList.get(0).getPrice()-demandList.get(0).getPrice())/2;
		}
		
		int i = 0;
		int j = 0;
		long demQuantity = demandCum[i];
		long supQuantity = supplyCum[j];
		long curDemQuantity = demandList.get(i).getQuantity();
		long curSupQuantity = supplyList.get(j).getQuantity();
		while (i < demandList.size() && j < supplyList.size() && demandList.get(i).getPrice() > supplyList.get(j).getPrice()) {
			if (curDemQuantity > curSupQuantity) {
				demQuantity -= supQuantity;
				curDemQuantity -= curSupQuantity;
				demandList.get(i).incrWinQuantity(curSupQuantity);
				if (demQuantity == 0 && i < demandList.size()) {
					assert(false);
					demQuantity = demandCum[i];
				}
				supplyList.get(j).incrWinQuantity(curSupQuantity);
				j++;
				if (j < supplyList.size()) {
					supQuantity = supplyCum[j];
					curSupQuantity = supplyList.get(j).getQuantity();
				}
			} else {
				supQuantity -= demQuantity;
				curSupQuantity -= curDemQuantity;
				supplyList.get(j).incrWinQuantity(curDemQuantity);
				if (supQuantity == 0 && j < supplyList.size()) {
					assert(false);
					supQuantity = supplyCum[j];
				}
				demandList.get(i).incrWinQuantity(curDemQuantity);
				i++;
				if (i < demandList.size()) {
					demQuantity = demandCum[i];
					curDemQuantity = demandList.get(i).getQuantity();
				}
			}
		}
		
		if (i < demandList.size() && j < supplyList.size()) {
			if (curSupQuantity > curDemQuantity) {
				supplyList.get(j).incrWinQuantity(curDemQuantity);
				return demandList.get(i).getPrice();
			} else {
				demandList.get(i).incrWinQuantity(curSupQuantity);
				return supplyList.get(j).getPrice();
			}
		}
		
		i = Math.min(i, demandList.size()-1);
		j = Math.min(j, supplyList.size()-1);
//		while (i < demandList.size() && j < supplyList.size()) {
//			logger.debug("while: i: {} j: {} td: {} ts: {}", i, j, totalDemand, totalSupply);
//			long demPrice = demandList.get(i).getPrice();
//			long supPrice = supplyList.get(j).getPrice();
//			if (demPrice > supPrice) {
//				if (supQuantity<totalDemand && supQuantity<demQuantity) {
//					totalDemand -= supQuantity;
//					demQuantity -= supQuantity;
//					j++;
//					supQuantity = supplyList.get(j).getQuantity();
//				} else {
//					return supPrice;
//				}
//			} else {
//				if (demQuantity<totalSupply && demQuantity<supQuantity) {
//					totalSupply -= demQuantity;
//					supQuantity -= demQuantity;
//					i++;
//					demQuantity = demandList.get(i).getQuantity();
//				} else {
//					return demPrice;
//				}
//			}
//		}
//		for (int i = 0; i < demandList.size()-1; i++) {
//			for (int j = 0; j < supplyList.size()-1; j++) {
//				if (demandList.get(i+1).getPrice() > supplyList.get(j).getPrice());
//			}
//		}
		return demandList.get(i).getPrice()<supplyList.get(j).getPrice()?demandList.get(i).getPrice():supplyList.get(j).getPrice();
	}

	private long[] cumulative(List<Bid> list) {
		logger.debug("cum: {}", list);
		long[] result = new long[list.size()];
		long total = 0;
		for (int i = 0; i < list.size(); i++) {
			Bid b = list.get(i);
			total += b.getQuantity();
//			b.setQuantity(total);
			result[i] = total;
		}
		logger.debug("res: {}", result);
		return result;
	}

	private void generateConstraint(List<Bid> list, int idx,
			int total, Collection<LinearConstraint> constraints, int i, Relationship rel) {
		double[] constraint = new double[total];
		long quantity = list.get(i).getQuantity();
		constraint[i+idx] = 1;
//		Relationship rel = Relationship.LEQ;
		logger.debug("Individual quantity constraint: {} {} {}", Arrays.toString(constraint), rel, quantity);
		constraints.add(new LinearConstraint(constraint, rel, quantity));
	}
	
	private long getTotalQuantity(List<Bid> list) {
		long result = 0;
		for (Bid b : list) {
			result += b.getQuantity();
		}
		return result;
	}

	private long getTotalWinQuantity(List<Bid> list) {
		long result = 0;
		for (Bid b : list) {
			result += b.getWinQuantity();
		}
		return result;
	}

	private double[] getPrice(List<Bid> set) {
		double[] res = new double[set.size()];
		
		for (int i = 0; i<res.length; i++) {
			res[i] = (double)set.get(i).getPrice();
		}
		
		return res;
	}
}
