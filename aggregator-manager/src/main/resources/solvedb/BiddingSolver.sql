---
-- #%L
-- ARROWHEAD::WP5::Aggregator Manager
-- %%
-- Copyright (C) 2016 The ARROWHEAD Consortium
-- %%
-- Permission is hereby granted, free of charge, to any person obtaining a copy
-- of this software and associated documentation files (the "Software"), to deal
-- in the Software without restriction, including without limitation the rights
-- to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
-- copies of the Software, and to permit persons to whom the Software is
-- furnished to do so, subject to the following conditions:
-- 
-- The above copyright notice and this permission notice shall be included in
-- all copies or substantial portions of the Software.
-- 
-- THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
-- IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
-- FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
-- AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
-- LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
-- OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
-- THE SOFTWARE.
-- #L%
---
﻿-- ****************** Aggregator's Virtual Energy Market (VEM) bidding solver ******************************

/*
  This is a Prosumer <-> Aggregator contract. 
  See details in: package org.arrowhead.wp5.agg.impl.billing.AggregatorContract;
*/
DROP TYPE IF EXISTS fo_contract_d CASCADE;
CREATE TYPE fo_contract_d AS (
  fixedReward 			float8,
  timeFlexReward 		float8,
  energyFlexReward		float8,
  schedulingFixedReward 	float8,
  schedulingStartTimeReward 	float8,
  schedulingEnergyReward	float8
);
COMMENT ON TYPE fo_contract_d IS 'This is a contract associated to a flexoffer';


-- This is utility function returning true, when default profile is followed
CREATE OR REPLACE FUNCTION flexoffer_d_is_schedule_baseline(f flexoffer_d) RETURNS boolean AS $$
  SELECT CASE WHEN ((f).schedule IS NULL) OR ((f).baseline IS NULL) THEN False
         ELSE (((f).schedule).start_time = ((f).baseline).start_time) AND (SELECT bool_and(abs(v - b)<=1e-5) 
									   FROM unnest(((f).baseline).values) WITH ORDINALITY B(v, snr) LEFT OUTER JOIN
									        unnest(((f).schedule).values) WITH ORDINALITY C(b, snr) ON B.snr = C.snr) 
         END;
$$ LANGUAGE sql STABLE STRICT;

CREATE OR REPLACE FUNCTION flexoffer_d_set_schedule_baseline(f flexoffer_d) RETURNS flexoffer_d AS $$
 SELECT ROW((f).est, (f).lst, (f).slices, (f).baseline, (f).baseline, (f).foc)::flexoffer_d; 
$$ LANGUAGE sql STABLE STRICT;

/* 
 SELECT flexoffer_d_is_schedule_baseline(flexoffer_d_set_schedule_baseline(flexoffer_d_get(fo))) FROM fo_get_simple;
*/

-- This is the function that calculated flexoffer value based on contract
CREATE OR REPLACE FUNCTION flexoffer_d_value(fo flexoffer_d, contract fo_contract_d) RETURNS float8 AS $$  
   SELECT (contract).timeFlexReward * ((fo).lst - (fo).est) +
          (contract).energyFlexReward * (SELECT sum((s).eh - (s).el) FROM unnest((fo).slices) AS s) +
	      (SELECT CASE WHEN ((fo).schedule IS NULL) OR (flexoffer_d_is_schedule_baseline(fo)) THEN 0
	                   ELSE (contract).schedulingFixedReward +
				(contract).schedulingStartTimeReward * abs(((fo).schedule).start_time - ((fo).baseline).start_time) + 
				(contract).schedulingEnergyReward * (SELECT sum(abs(b - v))
				   				     FROM unnest(((fo).baseline).values) WITH ORDINALITY B(v, snr) LEFT OUTER JOIN
									  unnest(((fo).schedule).values) WITH ORDINALITY C(b, snr) ON B.snr = C.snr) 
	                   END)
$$ LANGUAGE sql STABLE STRICT;

CREATE OR REPLACE FUNCTION fo_value(fo jsonb, contract fo_contract_d) RETURNS float8 AS $$
  SELECT flexoffer_d_value(flexoffer_d_get(fo), contract);
$$  LANGUAGE sql STABLE STRICT;
/* 
  SELECT sum(fo_value(fo, ROW(10, 0.1, 0.001, 0.1, 0.002, 0.002)::fo_contract_d)) FROM fo_get_simple;  
*/


-- The utility functions for finding EST and LET of the flexoffer set
DROP FUNCTION IF EXISTS flexoffer_d_get_est(flexoffer_d[]);
CREATE OR REPLACE FUNCTION flexoffer_d_get_est(fos flexoffer_d[]) RETURNS int8 AS $$
   SELECT min((f).est) FROM unnest(fos) AS f;   
$$ LANGUAGE sql STABLE STRICT;

DROP FUNCTION IF EXISTS flexoffer_d_get_let(flexoffer_d[]);
CREATE OR REPLACE FUNCTION flexoffer_d_get_let(fos flexoffer_d[]) RETURNS int8 AS $$
   SELECT max(array_length((f).slices, 1) + (f).lst - 1) FROM unnest(fos) AS f;   
$$ LANGUAGE sql STABLE STRICT;


/* Generated a (dummy) "imbalance" flexoffer with no time flexibility, spanning the specified region and having very wide energy bounds. */
CREATE OR REPLACE FUNCTION get_imbalance_fo_d(period_from int8, period_to int8) RETURNS flexoffer_d AS $$
 WITH slices AS (SELECT ROW(1, -1e9, 1e9)::slice_d AS slice FROM generate_series(period_from, period_to))
 SELECT ROW(period_from, -- EST
            period_from, -- LST
            (SELECT array_agg(slice) FROM slices),	-- Slices
            NULL, 	 -- Schedule
			 -- Baseline
            ROW(period_from, (SELECT array_agg(0) FROM generate_series(period_from, period_to)))::timeseries_d,
            NULL	 -- FOC
            )::flexoffer_d;
$$ LANGUAGE sql STABLE STRICT;
/*
 SELECT get_imbalance_fo_d(1, 10);
*/

-- This returns an expensive contract for penalizing "imbalances" from the imbalance flexoffer
CREATE OR REPLACE FUNCTION get_imbalance_contract() RETURNS fo_contract_d AS $$
 SELECT ROW(0, 0, 0, 0, 0, 1000 /* Energy deviations are expensive */)::fo_contract_d;
$$ LANGUAGE sql STABLE STRICT;
-- SELECT get_imbalance_contract();



/* This function normalizes flexoffer amount with respect to the default profile (baseline) */
CREATE OR REPLACE FUNCTION flexoffer_d_normalize_amounts(fo flexoffer_d) RETURNS flexoffer_d AS $$
  WITH slices AS (SELECT ROW(d, S.el - B.v, S.eh - B.v)::slice_d AS norm_d, 
		         C.v - B.v AS norm_s
   		  FROM unnest((fo).slices) WITH ORDINALITY AS S(d, el, eh, snr) INNER JOIN 
		       unnest(((fo).baseline).values) WITH ORDINALITY B(v, snr) ON S.snr = B.snr LEFT OUTER JOIN
		       unnest(((fo).schedule).values) WITH ORDINALITY C(v, snr) ON S.snr = C.snr) 
  SELECT CASE WHEN (fo).baseline IS NULL THEN NULL
	      ELSE
			  ROW((fo).est,					 -- EST
			      (fo).lst,					 -- LST
			      (SELECT array_agg(norm_d) FROM slices),	 -- Slices
			      (CASE WHEN (fo).schedule IS NULL THEN NULL
				   ELSE  ROW(((fo).schedule).start_time, 
					     (SELECT array_agg(norm_s) FROM slices))::timeseries_d
					 END), 		-- Schedule
			      (CASE WHEN (fo).baseline IS NULL THEN NULL
				   ELSE  ROW(((fo).baseline).start_time, 
					     (SELECT array_agg(0) FROM slices))::timeseries_d
					 END),		-- Baseline
			      NULL			-- FOC
			   )::flexoffer_d
	 END;
$$ LANGUAGE sql STABLE STRICT;

/* TESTS: 

SELECT (fo->'flexOfferSchedule'='null') FROM fo_get_simple;
SELECT flexoffer_d_to_json(flexoffer_d_normalize_amounts(flexoffer_d_get(fo))) FROM fo_get_simple;
*/

-- *************************************** Aggregato's cost-based optimizer ************************************************************

DELETE FROM sl_solver
WHERE (name = 'costsolver');

-- Registers the solver and 1 method.
WITH 
     -- Registers the solver and its parameters.
     solver AS   (INSERT INTO sl_solver(name, version, author_name, author_url, description)
                  values ('costsolver', 1.0, '', '', 'A solver that minimizes Aggregator costs (by Laurynas Siksnys)') 
                  returning sid),     

     -- Registers the BASIC balancing/scheduling method. It has no parameters.
     methodSch AS  (INSERT INTO sl_solver_method(sid, name, name_full, func_name, prob_name, description)
                    SELECT sid, 'default', 'The MIP-based method for minimizing Aggregator costs (by Laurynas Siksnys)', 'cost_solve_default', 'Flex-offer cost-based balancing problem', 'Solves the bid generation problem utilizing the LP solver'
		    FROM solver RETURNING mid)
     -- Perform the actual insert
     SELECT count(*) FROM solver, methodSch;

-- Set the default method
UPDATE sl_solver s
SET default_method_id = mid
FROM sl_solver_method m
WHERE (s.sid = m.sid) AND (s.name = 'costsolver') AND (m.name='default');


CREATE OR REPLACE FUNCTION cost_solve_default(arg sl_solver_arg) RETURNS setof record AS $$
  DECLARE
     foundAttFO		boolean = false;-- Was the "fo" attribute found?     
     foundAttContract	boolean = false;-- Was the "contract" attribute found?   
  BEGIN
     -- Check if the required columns are specified
     foundAttFO    = COALESCE((SELECT count(*)=1 FROM sl_get_attributes(arg) WHERE att_kind = 'unknown'::sl_attribute_kind AND att_name = 'fo' AND att_type = 'jsonb'), false);
     foundAttContract = COALESCE((SELECT count(*)=1 FROM sl_get_attributes(arg) WHERE att_kind = 'known'::sl_attribute_kind AND att_name = 'contract' AND att_type = 'fo_contract_d'), false);

     -- Report and error, if the schema mismatch
     IF (NOT foundAttFO) OR (NOT foundAttContract) THEN
	 RAISE EXCEPTION 'The input relation must comply to the following schema: (fo jsonb, contract fo_contract_d); and the attribute "fo" must be unknown, and "contract" must be known.';
     END IF;

     -- Create the flex-offer view over the input relation
     PERFORM sl_create_view(sl_build_out_defcols(arg, ARRAY[['tmp_flexoffer_id','(row_number() OVER ())'], 
							    ['tmp_flexoffer_d', 'flexoffer_d_get(fo)']]), 'fo_solver_input');

/*
     DROP VIEW fo_solver_input;
     CREATE TEMP VIEW fo_solver_input AS 
      WITH fosd AS (SELECT flexoffer_d_get(fo) AS fo FROM fo_get_simple),
           fosa AS (SELECT array_agg(fo) AS a FROM fosd)
      SELECT (row_number() OVER ()) AS tmp_flexoffer_id, fo AS tmp_flexoffer_d, ROW(10, 0.1, 0.001, 0.1, 0.002, 0.002)::fo_contract_d AS contract FROM fosd
      UNION ALL
      SELECT 0,  get_imbalance_fo_d(flexoffer_d_get_est((SELECT a FROM fosa)),
				    flexoffer_d_get_let((SELECT a FROM fosa))), get_imbalance_contract();
*/

      -- Solves the overall problem
     CREATE TEMP VIEW fo_solver_solution AS 
	WITH solution AS (

	SOLVESELECT e, s, sea, sea_p, sea_n, den_p, den_n, dt_p, dt_n IN (

			WITH flexOffers AS (SELECT  tmp_flexoffer_id AS fid, tmp_flexoffer_d AS f, contract FROM fo_solver_input)
			SELECT COALESCE(F.fid, FOJ.fid) AS fid,		-- Fid
			       S.snr,					-- Slice Nr.
			       NULL::float8 AS e,  			-- Slice allocated energy
			       el,					-- Slice low energy 
			       ed,					-- Slice default energy
			       eh,					-- Slice high energy
			       (f).est,					-- EST
			       ((f).baseline).start_time AS dst,	-- Default start time
			       (f).lst,					-- LST
			       tid,					-- Tid			       
			       start_time,				-- Start_time			       
			       NULL::boolean AS s,			-- Is this start_time selected? 
			       NULL::float8  AS sea,			-- Start-time at no TID / Energy at specific tid
			       NULL::float8  AS sea_p,
			       NULL::float8  AS sea_n,
			       -- Baseline delta energy/ start_time
			       NULL::float8 AS den_p,			
			       NULL::float8 AS den_n,			       
			       NULL::float8 AS dt_p,
			       NULL::float8 AS dt_n,
			       -- Flex-offer raward/cost attributes			       
			       (contract).schedulingStartTimeReward AS r_start,
			       (contract).schedulingEnergyReward AS r_energy
			FROM flexOffers AS F
			     LEFT JOIN LATERAL unnest((f).slices) WITH ORDINALITY AS S(d, el, eh, snr) ON TRUE
			     LEFT JOIN LATERAL unnest(((f).baseline).values) WITH ORDINALITY AS D(ed, snr) ON S.snr = D.snr
			     FULL OUTER JOIN (			     
					      SELECT fid, tid, start_time
					      FROM flexOffers AS F
						   LEFT JOIN LATERAL (SELECT generate_series((f).est, (f).lst) 
								       UNION
								      SELECT NULL 		-- Null for absolute start_time allocations
								      ) AS C(start_time) ON True 			      
					      LEFT OUTER JOIN LATERAL generate_series(COALESCE(C.start_time, (f).est), COALESCE(C.start_time, (f).lst) + array_length((f).slices, 1) - 1) AS tid ON True
					      ) AS FOJ ON FALSE			      
			
			) AS t
		MINIMIZE(SELECT (SELECT sum((den_p + den_n) * r_energy) FROM t WHERE snr IS NOT NULL)  +	-- Energy deviation cost 
		                (SELECT sum((dt_p + dt_n) * r_start) FROM t WHERE snr = 1)    			-- Time shift deviation cost*/
		                -- Add insignificant value of the absolute variables
		                -- (SELECT 1e-9 * sum(dt_p + dt_n) FROM t WHERE snr = 1) 
		                -- SELECT sum(-1*sea) FROM t WHERE (snr IS NULL) AND (start_time IS NULL)
			)			
		SUBJECTTO 
			
			-- Start time constraint: Exacly 1 start-time must be selected
			(SELECT sum(s) = 1 FROM t WHERE (snr IS NULL) AND (start_time IS NOT NULL) AND (tid=start_time) GROUP BY fid),
			
			--     Start times are equal accross TIDs
			(SELECT t1.s=t2.s FROM t AS t1, t AS t2 
			 WHERE (t1.fid=t2.fid) AND  (t1.start_time = t2.start_time) AND (t1.tid != t2.tid) AND
			       (t1.snr IS NULL) AND (t2.snr IS NULL)),
			       
			-- Absolute time allocation
			(SELECT t1.sea = (SELECT sum(t2.s * t2.start_time) FROM t AS t2 WHERE (t1.fid = t2.fid) AND (t2.snr IS NULL) AND (t2.tid = t2.start_time))
			 FROM t AS t1
			 WHERE snr = 1),

			-- The main energy constraint, at factorized level	
			(SELECT el <= e <= eh FROM t WHERE snr IS NOT NULL),

			-- Absolute energy allocation constracts
			(SELECT 0<=sea_p<=1e6, 0<=sea_n<=1e6, sea_p - sea_n = sea, sea_p + sea_n <= 1e6 * s
			 FROM t WHERE (snr IS NULL) AND (start_time IS NOT NULL)),
			 
			-- Absolute energy allocation
			(SELECT e = (SELECT sum(t2.sea) FROM t AS t2 WHERE (t1.fid=t2.fid) AND (t1.snr = t2.tid - t2.start_time + 1))
			 FROM t AS t1
			 WHERE t1.snr IS NOT NULL),
	 
			-- Absolute sum energy, at Start_time = NULL
			(SELECT sea = (SELECT sum(sea) FROM t AS t2 WHERE (t1.fid = t2.fid) AND (t1.tid = t2.tid) AND (t2.start_time IS NOT NULL) AND (t2.snr IS NULL))
			 FROM t AS t1
			 WHERE (snr IS NULL) AND (start_time IS NULL)),

			 -- Absolute sum energy global balance constraint
			(SELECT sum(sea) = 0 FROM t
			 WHERE (snr IS NULL) AND (start_time IS NULL)
			 GROUP BY tid),
			 
			 -- Energy BASELINE delta calculations
			(SELECT 0<=den_p<=1e6, 0<=den_n<=1e6, den_p - den_n = ed - e FROM t WHERE snr IS NOT NULL),
			
			-- Time shifting BASELINE delta calculations			
			(SELECT 0<=dt_p<=1e6, 0<=dt_n<=1e6, dt_p - dt_n = dst - sea FROM t WHERE snr = 1)			

		WITH solverlp.cbc(args:='-maxNodes 5')  ),
	  solution_filtered AS (SELECT fid, CASE WHEN snr = 1 THEN sea ELSE NULL END AS start_time, e FROM solution WHERE snr IS NOT NULL ORDER BY fid, snr),
	  sol_packed AS (SELECT fid, max(start_time) AS start_time, array_agg(e) AS prof FROM (SELECT fid, e, start_time FROM solution_filtered) AS s GROUP BY fid)
	SELECT fid, ROW(start_time, prof)::timeseries_d AS ts
	FROM sol_packed;

     --    Output the solver result
     RETURN QUERY EXECUTE sl_return(arg, sl_build_out_defcols(arg, 
				ARRAY[['fo','(SELECT fo_update_schedule(ROW((tmp_flexoffer_d).est, (tmp_flexoffer_d).lst, (tmp_flexoffer_d).slices, ts, (tmp_flexoffer_d).baseline, (tmp_flexoffer_d).foc)::flexoffer_d) 
					      FROM fo_solver_solution WHERE fid = tmp_flexoffer_id)']],
				ROW('SELECT * FROM fo_solver_input')::sl_viewsql_out));
    
     -- Performs the clean-up
     DROP VIEW fo_solver_solution;
     PERFORM sl_drop_view('fo_solver_input');	
  END;
$$ LANGUAGE plpgsql VOLATILE STRICT;

/*
 TESTING:

SELECT sum(fo_value(fo, ROW(10, 0.1, 0.001, 0.1, 0.002, 0.002)::fo_contract_d)) FROM (

	SOLVESELECT fo IN (
		-- SELECT fo,  ROW(10, 0.1, 0.001, 0.1, 0.002, 0.002)::fo_contract_d AS contract FROM fo_get_simple
		
		 WITH fos  AS (SELECT fo FROM fo_get_simple),
		      fosa AS (SELECT array_agg(flexoffer_d_get(fo)) AS a FROM fos)
		      SELECT TRUE AS is_real_fo, fo,  ROW(10, 0.1, 0.001, 0.1, 0.002, 0.002)::fo_contract_d AS contract FROM fos
		      UNION ALL
		      SELECT FALSE, flexoffer_d_to_json(get_imbalance_fo_d(flexoffer_d_get_est((SELECT a FROM fosa)),
								    flexoffer_d_get_let((SELECT a FROM fosa)))), 
			     get_imbalance_contract()
	) AS T	WITH costsolver

	) AS s
WHERE is_real_fo; 




/* ******************************************* Bid generation routines ****************************************






*/

/* This function generates a bid flexoffer, based on a 2 phase flexoffer scheduling */
/*
DROP FUNCTION IF EXISTS generate_bid_flexoffer(jsonb, timestamptz, timestamptz);
CREATE OR REPLACE FUNCTION generate_bid_flexoffer(fos jsonb, timeFrom timestamptz, timeTo timestamptz) RETURNS jsonb AS $$ 
   WITH foBalance AS (SELECT unnest(schedule(fos)) AS fo),
        tsFOS 	  AS (SELECT fo_to_ts(fo, 'schedule') AS ts FROM foBalance),
	tsSUM	  AS (SELECT sum(timeseries_d_from_json(ts)) AS ts FROM tsFOS)
	tsFilt	  AS (SELECT ROW(dt_to_discrete(timeFrom),
   SELECT ts_to_fo(timeseries_d_to_json(ts)) FROM tsSUM
$$ LANGUAGE sql STABLE STRICT;
*/

 


/* TESTS:

SELECT sum(timeseries_d_from_json(ts)) FROM (
	SELECT fo_to_ts(unnest(schedule((SELECT aggregate(fo) FROM fo_get_simple))), 'schedule') AS ts
	) AS T

SELECT generate_bid_flexoffer((SELECT sum(flexoffer_d_to_json(flexoffer_d_normalize_amounts(flexoffer_d_get(fo)))) FROM fo_get_simple), '2015-01-01'::timestamptz, '2015-01-01 01:00:00'::timestamptz)

*/
