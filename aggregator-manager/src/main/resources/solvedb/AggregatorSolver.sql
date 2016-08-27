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
-- Aggregator settings table
DROP TABLE IF EXISTS settings CASCADE;
CREATE TABLE settings (
 param 	varchar(50) NOT NULL CONSTRAINT firstkey PRIMARY KEY,
 descr  varchar(255),
 value 	varchar(255)
);
-- Insert default settings
INSERT INTO settings(param, descr, value)
VALUES ('agg_url', 'This URL points to the AggregatorManager''s web-service for analytics', 'http://192.168.56.101:9998/api/');

-- JSON utils for RESTful Web-server access
DROP FUNCTION IF EXISTS json_from_web(text, text) CASCADE;
CREATE OR REPLACE FUNCTION json_from_web(path text, base_url text) RETURNS jsonb AS E'#!/bin/sh\n curl -H "Accept: application/json" $2$1 2>/dev/null' LANGUAGE plsh;
DROP FUNCTION IF EXISTS json_from_web(text) CASCADE;
CREATE OR REPLACE FUNCTION json_from_web(path text) RETURNS jsonb AS $$ SELECT json_from_web(path, (SELECT value FROM settings WHERE param = 'agg_url')) $$ LANGUAGE sql;

DROP FUNCTION IF EXISTS json_to_web(text, text, jsonb) CASCADE;
CREATE OR REPLACE FUNCTION json_to_web(path text, base_url text, obj jsonb) RETURNS text AS 
E'#!/bin/sh\n echo "$3" | curl -H "Content-Type: application/json" -H "Accept: application/json" -X POST $2$1 -d @- 2>/dev/null' LANGUAGE plsh;
DROP FUNCTION IF EXISTS json_to_web(text, jsonb) CASCADE;
CREATE OR REPLACE FUNCTION json_to_web(path text, obj jsonb) RETURNS text AS $$ SELECT json_to_web(path, (SELECT value FROM settings WHERE param = 'agg_url'), obj)$$ LANGUAGE sql;

-- Continous/discrete datetime conversions
DROP FUNCTION IF EXISTS dt_interval_duration() CASCADE;
CREATE OR REPLACE FUNCTION dt_interval_duration() RETURNS interval AS $$ SELECT INTERVAL '15 minute'; $$ LANGUAGE sql STABLE STRICT;
COMMENT ON FUNCTION dt_interval_duration() IS 'Returns the duration of a discrete time interval.';

DROP FUNCTION IF EXISTS dt_to_timestamp(dsTime int8) CASCADE;
CREATE OR REPLACE FUNCTION dt_to_timestamp(dsTime int8) RETURNS timestamptz AS $$ SELECT ('2000-01-01'::timestamptz + dsTime * dt_interval_duration()) $$ LANGUAGE sql STABLE STRICT;
COMMENT ON FUNCTION dt_to_timestamp(int8) IS 'Convert discrete time stamp into a continuous time stamp';

DROP FUNCTION IF EXISTS dt_to_discrete(absTime timestampz) CASCADE;
CREATE OR REPLACE FUNCTION dt_to_discrete(absTime timestamptz) RETURNS int8 AS $$ 
   SELECT floor((EXTRACT(EPOCH FROM (absTime)) - EXTRACT(EPOCH FROM '2000-01-01'::timestamptz)) / extract(EPOCH FROM dt_interval_duration()))::int8;
$$ LANGUAGE sql STABLE STRICT;
COMMENT ON FUNCTION dt_to_discrete(timestamptz) IS 'Convert timestamp to a discrete time stamp';

DROP FUNCTION IF EXISTS dt_sec_to_duration(int) CASCADE;
CREATE OR REPLACE FUNCTION dt_sec_to_duration(durSeconds int) RETURNS int8 AS $$
   SELECT durSeconds / (EXTRACT (EPOCH FROM dt_interval_duration()))::int8
$$ LANGUAGE sql STABLE STRICT;
COMMENT ON FUNCTION dt_sec_to_duration(int) IS 'Get a number of discrete intervals for a given number of seconds';

-- TESTS: 
--
-- SELECT Now(), extract(EPOCH FROM dt_interval_duration());
-- SELECT Now(), dt_to_timestamp(dt_to_discrete(Now()));

-- ********************************************************* FlexOffer Aggregation Parameter Routines *****************************************

-- Gets aggregation parameters 
DROP FUNCTION IF EXISTS aggpars_get() CASCADE;
CREATE OR REPLACE FUNCTION aggpars_get() RETURNS jsonb AS $$ SELECT json_from_web('manager/aggparams'); $$ LANGUAGE sql STABLE STRICT;
COMMENT ON FUNCTION aggpars_get() IS 'Get the currently set agent aggregation parameters.';

DROP VIEW IF EXISTS aggpars_defaults CASCADE;
CREATE OR REPLACE VIEW aggpars_defaults AS 
	SELECT 'FULL' AS type, json_build_object('constraintPair', json_build_object('startAfterTolerance', 1E8, 'startAfterToleranceType', 'acSet'))::jsonb AS pars
	UNION ALL
	SELECT 'EST0', json_build_object('constraintPair', json_build_object('startAfterTolerance', 0, 'startAfterToleranceType', 'acSet'))::jsonb
	UNION ALL
	SELECT 'EST0TFT0', json_build_object('constraintPair', json_build_object('startAfterTolerance', 0, 'startAfterToleranceType', 'acSet',
										 'timeFlexibilityTolerance', 0, 'timeFlexibilityToleranceType', 'acSet'))::jsonb;
COMMENT ON VIEW aggpars_defaults IS 'Return the table of standard aggregation parameters.';

DROP FUNCTION IF EXISTS aggpars_defaults() CASCADE;
CREATE OR REPLACE FUNCTION aggpars_defaults() RETURNS SETOF aggpars_defaults AS $$ SELECT * FROM aggpars_defaults; $$ LANGUAGE sql STABLE STRICT;
COMMENT ON VIEW aggpars_defaults IS 'Return a set of standard aggregation parameters.';

/*
 TESTS: 
 
 SELECT * FROM aggpars_get();
 SELECT * FROM aggpars_defaults;
 SELECT * FROM aggpars_defaults();
*/

-- ********************************************************* FlexOffer Identification Routines **************************************

/* Is the json object a flex-offer */
DROP FUNCTION IF EXISTS json_is_fo(jsonb) CASCADE;
CREATE OR REPLACE FUNCTION json_is_fo(obj jsonb) RETURNS boolean AS $$ 
   SELECT (obj ? 'slices') AND (obj ? 'startAfterTime') AND (obj ? 'startBeforeTime')
$$ LANGUAGE sql STABLE;

/* Is the json object an aggregated flex-offer */
DROP FUNCTION IF EXISTS json_is_afo(jsonb) CASCADE;
CREATE OR REPLACE FUNCTION json_is_afo(obj jsonb) RETURNS boolean AS $$ 
   SELECT json_is_fo(obj) AND (obj ? 'subFoMetas')
$$ LANGUAGE sql STABLE;

/* Is the json object a flexoffer with schedule */
DROP FUNCTION IF EXISTS json_is_fo_with_schedule(jsonb) CASCADE;
CREATE OR REPLACE FUNCTION json_is_fo_with_schedule(obj jsonb) RETURNS boolean AS $$ 
   SELECT json_is_fo(obj) AND ((obj->'flexOfferSchedule') IS NOT NULL)
$$ LANGUAGE sql STABLE;


/* 
 TESTS: 
  SELECT fo, json_is_fo(fo) FROM fo_get_simple;
  SELECT json_is_afo(unnest(aggregate(fo))) FROM fo_get_simple;
  SELECT json_is_foschedule(schedule(sum(fo))) FROM fo_get_simple;
*/

-- ********************************** Flexoffer Aggregation routines ****************************

DROP TYPE IF EXISTS fo_agg_state CASCADE;
CREATE TYPE fo_agg_state AS (
  fos jsonb[],
  agg_params jsonb
);
COMMENT ON TYPE fo_agg_state IS 'This is a flexoffer aggregation state data type';

DROP FUNCTION IF EXISTS fo_agg_state(fo_agg_state, jsonb) CASCADE;
CREATE OR REPLACE FUNCTION fo_agg_state(state fo_agg_state, fo jsonb) RETURNS fo_agg_state AS
$$ SELECT ROW(array_append(state.fos, fo), NULL)::fo_agg_state $$ LANGUAGE sql STABLE;
COMMENT ON FUNCTION fo_agg_state(fo_agg_state, jsonb) IS 'This is a flexoffer aggregation state transition function, when aggregation parameters unspecified';

DROP FUNCTION IF EXISTS fo_agg_state(fo_agg_state, jsonb, jsonb) CASCADE;
CREATE OR REPLACE FUNCTION fo_agg_state(state fo_agg_state, fo jsonb, agg_params jsonb) RETURNS fo_agg_state AS
$$ SELECT ROW(array_append(state.fos, fo), agg_params)::fo_agg_state $$ LANGUAGE sql STABLE;
COMMENT ON FUNCTION fo_agg_state(fo_agg_state, jsonb, jsonb) IS 'This is a flexoffer aggregation state transition function, when aggregation parameters specified';

DROP FUNCTION IF EXISTS fo_agg_final(fo_agg_state) CASCADE;
CREATE OR REPLACE FUNCTION fo_agg_final(state fo_agg_state) RETURNS jsonb AS $$ 
   SELECT json_to_web('analytics/aggregate', json_build_object('flexOffers', state.fos, 'params', state.agg_params)::jsonb)::jsonb;
$$ LANGUAGE sql STABLE STRICT;
COMMENT ON FUNCTION fo_agg_final(fo_agg_state) IS 'The final function that invokes flexoffer aggregation';

-- User defined aggregates and their synonims 
DROP AGGREGATE IF EXISTS aggregate(jsonb) CASCADE;
CREATE AGGREGATE aggregate(jsonb) (SFUNC=fo_agg_state, STYPE=fo_agg_state, FINALFUNC=fo_agg_final, INITCOND='({}, null)' );
COMMENT ON AGGREGATE aggregate(jsonb) IS 'Aggregates a set of flexoffers using default aggregation parameters';

DROP AGGREGATE IF EXISTS aggregate(jsonb, jsonb) CASCADE;
CREATE AGGREGATE aggregate(jsonb, jsonb) (SFUNC=fo_agg_state, STYPE=fo_agg_state, FINALFUNC=fo_agg_final, INITCOND='({}, null)' );
COMMENT ON AGGREGATE aggregate(jsonb, jsonb) IS 'Aggregates a set of flexoffers using user-specified aggregation parameters';

DROP FUNCTION IF EXISTS unnest(jsonb) CASCADE;
CREATE OR REPLACE FUNCTION unnest(fos jsonb) RETURNS SETOF jsonb AS $$ SELECT CASE WHEN jsonb_typeof(fos)='array' THEN jsonb_array_elements(fos) ELSE fos END $$ LANGUAGE sql STABLE;
COMMENT ON FUNCTION unnest(jsonb) IS 'Unnests an array of jsons';

DROP FUNCTION IF EXISTS fo_unnest(jsonb) CASCADE;
CREATE OR REPLACE FUNCTION fo_unnest(fos jsonb) RETURNS SETOF jsonb AS $$ SELECT unnest(fos)  $$ LANGUAGE sql STABLE;
COMMENT ON FUNCTION fo_unnest(jsonb) IS 'Unnests an array of flexoffers';

/* 
  TESTS:

  SELECT unnest(aggregate(fo)) FROM fo_get_simple;
  SELECT aggregate(fo, (aggpars_get())) FROM fo_get_simple;
  SELECT unnest(aggregate(fo, (aggpars_get()))) FROM fo_get_simple;
  SELECT unnest(unnest(aggregate(fo, (aggpars_get())))) FROM fo_get_simple;  

*/

-- Alternative aggregation approach, using set union and aggregation

-- Simple JSON union user-defined aggregate
DROP FUNCTION IF EXISTS json_sum_state(jsonb[], jsonb) CASCADE;
CREATE OR REPLACE FUNCTION json_sum_state(state jsonb[], obj jsonb) RETURNS jsonb[] AS
$$ SELECT array_append(state, obj) $$ LANGUAGE sql STABLE;
COMMENT ON FUNCTION json_sum_state(jsonb[], jsonb) IS 'The state function of the json sum aggregate';

DROP FUNCTION IF EXISTS json_sum_final(jsonb[]) CASCADE;
CREATE OR REPLACE FUNCTION json_sum_final(state jsonb[]) RETURNS jsonb AS
$$ SELECT array_to_json(state)::jsonb $$ LANGUAGE sql STABLE STRICT;
COMMENT ON FUNCTION json_sum_final(jsonb[]) IS 'The final function of the json sum aggregate';

-- Assemble UDA and is synonims
DROP AGGREGATE IF EXISTS json_sum(jsonb);
CREATE AGGREGATE json_sum(jsonb) (SFUNC=json_sum_state, STYPE=jsonb[], FINALFUNC=json_sum_final, INITCOND='{}' );
COMMENT ON AGGREGATE json_sum(jsonb) IS 'This assembles individual JSON objects into a JSON of array of objects.';

-- Two synonims 
DROP AGGREGATE IF EXISTS sum(jsonb);
CREATE AGGREGATE sum(jsonb) (SFUNC=json_sum_state, STYPE=jsonb[], FINALFUNC=json_sum_final, INITCOND='{}' );
COMMENT ON AGGREGATE sum(jsonb) IS 'This assembles individual JSON objects into a JSON of array of objects.';

-- Aggregate a set of flex-offers using default parameters
CREATE OR REPLACE FUNCTION aggregate_set(fos jsonb) RETURNS SETOF jsonb AS $$  
   SELECT jsonb_array_elements(json_to_web('analytics/aggregate', json_build_object('flexOffers', fos)::jsonb)::jsonb);
$$ LANGUAGE sql STABLE STRICT;

-- Aggregate using a user-defined parameters
CREATE OR REPLACE FUNCTION aggregate_set(fos jsonb, agg_params jsonb) RETURNS SETOF jsonb AS $$
   SELECT jsonb_array_elements(json_to_web('analytics/aggregate', json_build_object('flexOffers', fos, 'params', agg_params)::jsonb)::jsonb);
$$ LANGUAGE sql STABLE STRICT;

-- Aggregated flex-offer disaggregation function
DROP FUNCTION IF EXISTS disaggregate(jsonb);
CREATE OR REPLACE FUNCTION disaggregate(afo jsonb) RETURNS SETOF jsonb AS $$ 
   SELECT jsonb_array_elements(json_to_web('analytics/disaggregate', afo)::jsonb);
$$ LANGUAGE sql STABLE STRICT;

-- Execute the simple or aggregated flexoffer
DROP FUNCTION IF EXISTS execute(jsonb);
CREATE OR REPLACE FUNCTION execute(fo jsonb) RETURNS jsonb AS $$ 
   SELECT CASE WHEN NOT json_is_fo_with_schedule(fo) THEN '"Error: FlexOffer has no schedule assigned!"'::jsonb
	       WHEN json_is_afo(fo) THEN json_to_web('analytics/executeA', fo)::jsonb
	       ELSE json_to_web('analytics/execute', fo)::jsonb END;
$$ LANGUAGE sql STABLE STRICT;
    
/* TESTS: 

SELECT aggregate_set(sum(fo)) FROM fo_get_simple;
SELECT aggregate_set(sum(fo), (SELECT pars FROM aggpars_defaults WHERE type='EST0TFT0')) FROM fo_get_simple;

*/

-- ********************************************************* FlexOffer Scheduling Routines ******************************************

-- Time series managements 
DROP TYPE IF EXISTS timeseries_d CASCADE;
CREATE TYPE timeseries_d AS
(
	start_time	int,
	values		float8[]
);

CREATE OR REPLACE FUNCTION timeseries_d_add(t1 timeseries_d, t2 timeseries_d) RETURNS timeseries_d AS $$
   WITH add_profiles AS 
    (SELECT t, SUM(v) AS v FROM (
				  SELECT (t1.start_time + i - 1) AS t, (t1.values[i]) AS v FROM generate_subscripts(t1.values, 1) AS i
				  UNION ALL
				  SELECT (t2.start_time + j - 1) AS t, (t2.values[j]) AS v FROM generate_subscripts(t2.values, 1) AS j
				  UNION ALL 
				  SELECT t, 0 FROM generate_series(LEAST   (t1.start_time, t2.start_time), 
				                                   GREATEST(t1.start_time + array_length(t1.values,1)-1,
								            t2.start_time + array_length(t2.values,1)-1)) AS t
                                ) AS S
     GROUP BY t
     ORDER BY t ASC
    )
    SELECT CASE WHEN COALESCE(array_length(t1.values,1),0)=0 THEN t2
	        WHEN COALESCE(array_length(t2.values,1),0)=0 THEN t1
	        ELSE
			ROW((SELECT min(t) FROM add_profiles),
		            (SELECT array_agg(v) FROM add_profiles))::timeseries_d
           END
$$ LANGUAGE sql STABLE STRICT;

-- Utility functions
DROP AGGREGATE IF EXISTS sum (timeseries_d);
CREATE AGGREGATE sum (timeseries_d)
(
    sfunc = timeseries_d_add,
    stype = timeseries_d,
    initcond = '(0,{})'
);

CREATE OR REPLACE FUNCTION timeseries_d_integral(t timeseries_d) RETURNS float8 AS $$
    SELECT sum(abs(t.values[i])) FROM generate_subscripts(t.values, 1) AS i
$$ LANGUAGE sql STABLE STRICT;

DROP FUNCTION IF EXISTS timeseries_d_to_json(timeseries_d, text);;
CREATE OR REPLACE FUNCTION timeseries_d_to_json(t timeseries_d, name text DEFAULT 'No name time series') RETURNS jsonb AS $$
   SELECT json_build_object('name', name,
			    'series', json_build_object('startTime', dt_to_timestamp((t).start_time),
							'endTime', dt_to_timestamp((t).start_time + array_length((t).values,1)),
							'data', (t).values))::jsonb;
$$ LANGUAGE sql STABLE STRICT;

CREATE OR REPLACE FUNCTION timeseries_d_from_json(ts jsonb) RETURNS timeseries_d AS $$
   SELECT ROW(dt_to_discrete((ts->'series'->>'startTime')::timestamptz), 
	      (SELECT array_agg(value::text::float8) FROM jsonb_array_elements(ts->'series'->'data') AS d(value)))::timeseries_d;
$$ LANGUAGE sql STABLE STRICT;

-- Build time series from raw time value pairs

-- Time series building routines
DROP TYPE IF EXISTS timestamp_value_pair CASCADE;
CREATE TYPE timestamp_value_pair AS
(
	tm		timestamptz,
	value		float8
);

-- State function
DROP FUNCTION IF EXISTS timeseries_d_build_state(timestamp_value_pair[], timestamptz, float8) CASCADE;
CREATE OR REPLACE FUNCTION timeseries_d_build_state(state timestamp_value_pair[], tm timestamptz, value float8) RETURNS timestamp_value_pair[] AS $$ 
SELECT array_append(state, ROW(tm, value)::timestamp_value_pair); 
$$ LANGUAGE sql STABLE STRICT;

-- Final state 
DROP FUNCTION IF EXISTS timeseries_d_build_final(timestamp_value_pair[]) CASCADE;
CREATE OR REPLACE FUNCTION timeseries_d_build_final(state timestamp_value_pair[]) RETURNS timeseries_d AS $$ 
 WITH min_tm AS (SELECT MIN(tm) AS tm FROM unnest(state) AS t(tm, value)),
      max_tm AS (SELECT MAX(tm) AS tm FROM unnest(state) AS t(tm, value)),
      all_values AS (SELECT tm_d, COALESCE(avg(value), 0) AS value 
		     FROM unnest(state) AS t(tm, value) 
		     RIGHT OUTER JOIN generate_series(dt_to_discrete((SELECT tm FROM min_tm)), dt_to_discrete((SELECT tm FROM max_tm))) AS s(tm_d) 
				   ON tm_d = dt_to_discrete(t.tm)
		     GROUP BY tm_d
		     ORDER BY tm_d ASC)
      SELECT ROW(dt_to_discrete((SELECT tm FROM min_tm)), 
                 (SELECT array_agg(value) FROM all_values))::timeseries_d      
$$ LANGUAGE sql STABLE STRICT;

DROP AGGREGATE IF EXISTS timeseries_d_build (timestamptz, float8);
CREATE AGGREGATE timeseries_d_build(timestamptz, float8) (
    stype = timestamp_value_pair[],
    sfunc = timeseries_d_build_state,
    finalfunc= timeseries_d_build_final,
    initcond = '{}'
);

/* TESTS:

   SELECT timeseries_d_build(tm, v) FROM (VALUES (TIMESTAMP '2004-10-19 10:23:54', 123), (TIMESTAMP '2004-10-19  10:30:54', 125)) AS s(tm, v)
*/

-- ******************* Continuos (JSON) time series *********************************************

-- Time series addition 
CREATE OR REPLACE FUNCTION ts_add_state(state timeseries_d, ts jsonb) RETURNS timeseries_d AS $$ 
SELECT timeseries_d_add(state, timeseries_d_from_json(ts)); 
$$ LANGUAGE sql STABLE STRICT;

CREATE OR REPLACE FUNCTION ts_add_final(state timeseries_d) RETURNS jsonb AS $$ 
SELECT timeseries_d_to_json(state, 'Aggregated timeseries')
$$ LANGUAGE sql STABLE STRICT;

DROP AGGREGATE IF EXISTS ts_add (jsonb);
CREATE AGGREGATE ts_add(jsonb) (
    stype = timeseries_d,
    sfunc = ts_add_state,
    finalfunc= ts_add_final,
    initcond = '(0,{})'
);

DROP FUNCTION IF EXISTS ts_from_raw_final(timestamp_value_pair[]) CASCADE;
CREATE OR REPLACE FUNCTION ts_from_raw_final(state timestamp_value_pair[]) RETURNS jsonb AS $$
   SELECT timeseries_d_to_json(timeseries_d_build_final(state));
$$ LANGUAGE sql STABLE STRICT;

DROP AGGREGATE IF EXISTS ts_from_raw (timestamptz, float8);
CREATE AGGREGATE ts_from_raw(timestamptz, float8) (
    stype = timestamp_value_pair[],
    sfunc = timeseries_d_build_state,
    finalfunc= ts_from_raw_final,
    initcond = '{}'
);

/* TESTS:

 SELECT ts_add(ts) FROM (  SELECT fo_to_ts(schedule(sum(fo))) AS ts FROM fo_get_simple ) AS T;
 SELECT ts_from_raw(tm, v) FROM (VALUES (TIMESTAMP '2004-10-19 10:23:54', 123), (TIMESTAMP '2004-10-19  10:30:54', 125)) AS s(tm, v)

*/


-- (Discrete) flex-offer data type 
DROP TYPE IF EXISTS slice_d CASCADE;
CREATE TYPE slice_d AS
(	
	d	int, -- Duration, should always be 1 for analysis
	el	float8, -- Energy low
	eh	float8  -- Energy high
);

-- This is used internally by the solver's - to simplify the processsing of JSON objects
DROP TYPE IF EXISTS flexoffer_d CASCADE;
CREATE TYPE flexoffer_d AS
(
	est		int8,       -- earliest start time
	lst		int8, 	    -- latest start time
	slices		slice_d[],  -- All flex-offer slices
        schedule	timeseries_d, -- An active flex-offer schedule
	baseline	timeseries_d, -- Default flexoffer schedule
	foc		jsonb       -- Underlying continuous flex-offer
);

/* Generates a discrete flex-offer with the slice durations of 1 */
CREATE OR REPLACE FUNCTION flexoffer_d_get(fo jsonb) RETURNS flexoffer_d AS $$
 WITH slices AS (SELECT ROW( 1, -- Accept only the duration 1 slices
			    (s->'energyConstraint'->>'lower')::float8 / sdur,
			    (s->'energyConstraint'->>'upper')::float8 / sdur
			   )::slice_d AS sd,
			av::text::float8 / sdur AS av,
			bv::text::float8 / sdur AS bv
		 FROM jsonb_array_elements(fo->'slices') WITH ORDINALITY AS S(s, snr)
		      LEFT JOIN LATERAL dt_sec_to_duration((s->>'durationSeconds')::int) AS D(sdur) ON TRUE
		      LEFT JOIN LATERAL generate_series(1, sdur) AS SS(ssnr) ON TRUE
		      LEFT OUTER JOIN LATERAL jsonb_array_elements(fo->'flexOfferSchedule'->'energyAmounts') WITH ORDINALITY AS A(av, anr) ON A.anr = S.snr
		      LEFT OUTER JOIN LATERAL jsonb_array_elements(fo->'defaultSchedule'->'energyAmounts') WITH ORDINALITY AS B(bv, bnr) ON B.bnr = S.snr
		 ORDER BY snr, ssnr)
 SELECT ROW(dt_to_discrete((fo->>'startAfterTime')::timestamptz),  -- EST
	    dt_to_discrete((fo->>'startBeforeTime')::timestamptz), -- LST
	    (SELECT array_agg(sd) FROM slices),	    		   -- Slices
	    CASE WHEN ((fo->'flexOfferSchedule') IS NOT NULL) AND (fo->'flexOfferSchedule' != 'null') THEN 
		 ROW(dt_to_discrete(((fo->'flexOfferSchedule')->>'startTime')::timestamptz), 
		     (SELECT array_agg(av) FROM slices))::timeseries_d
		 ELSE NULL END,					-- Schedule
	    CASE WHEN ((fo->'defaultSchedule') IS NOT NULL) AND (fo->'defaultSchedule' != 'null') THEN 
		 ROW(dt_to_discrete(((fo->'defaultSchedule')->>'startTime')::timestamptz), 
		     (SELECT array_agg(bv) FROM slices))::timeseries_d
		 ELSE NULL END,					-- Baseline (default profile)
	    fo							-- Continuous flex-offer
	    )::flexoffer_d
$$ LANGUAGE sql STABLE STRICT;

/* Create a continuous flex-offer from a flexoffer_d */
DROP FUNCTION IF EXISTS flexoffer_d_to_json(flexoffer_d);
CREATE OR REPLACE FUNCTION flexoffer_d_to_json(f flexoffer_d) RETURNS jsonb AS $$
    SELECT JSON_OBJECT_AGG(key, value)::JSONB 
    FROM (
	SELECT key, value FROM JSONB_EACH((f).foc)		-- Take the original attributes, if any
	UNION ALL
	SELECT key, value FROM JSONB_EACH(			-- Override the attributes from a template
	        json_build_object('id', 0,
				 'state', 'Initial',
				 'offeredById', 'SELF',
				 'acceptanceBeforeTime', dt_to_timestamp((f).est),
				 'assignmentBeforeDurationSeconds', 0,
				 'assignmentBeforeTime', dt_to_timestamp((f).est),
				 'creationTime', current_timestamp,
				 'durationSeconds', (EXTRACT(EPOCH FROM dt_interval_duration())) * array_length((f).slices, 1),
				 'endAfterTime', dt_to_timestamp((f).est + array_length((f).slices, 1)),
				 'endBeforeTime', dt_to_timestamp((f).lst + array_length((f).slices, 1)),
				 'numSecondsPerInterval', (EXTRACT(SECONDS FROM dt_interval_duration())),
				 'startAfterTime', dt_to_timestamp((f).est),
				 'startBeforeTime', dt_to_timestamp((f).lst),	
				 'slices', (SELECT array_agg(json_build_object('durationSeconds', EXTRACT(EPOCH FROM dt_interval_duration()),
									       'costPerEnergyUnitLimit', 1,
									       'energyConstraint', json_build_object('lower', el, 'upper', eh)))
					    FROM unnest((f).slices) AS P(d, el, eh)),
				 'flexOfferSchedule', CASE WHEN (f).schedule IS NULL THEN NULL
							   ELSE json_build_object('startTime', dt_to_timestamp(((f).schedule).start_time),
										  'energyAmounts', ((f).schedule).values)
							   END,
				 'defaultSchedule', CASE WHEN (f).baseline IS NULL THEN NULL
							   ELSE json_build_object('startTime', dt_to_timestamp(((f).baseline).start_time),
										  'energyAmounts', ((f).baseline).values)
							   END							   
				)::jsonb) AS n
	) AS o
$$ LANGUAGE sql STABLE STRICT;

/* TESTS: 

SELECT flexoffer_d_get(fo) FROM fo_get_simple;
SELECT flexoffer_d_to_json(flexoffer_d_get(fo))->'defaultSchedule' FROM fo_get_simple;
SELECT EXTRACT (EPOCH FROM (INTERVAL '1 minute'))

*/

/* Update a schedule of a continuous flex-offer */
CREATE OR REPLACE FUNCTION fo_update_schedule(fo flexoffer_d) RETURNS jsonb AS $$
   WITH  slices AS  (SELECT snr, (row_number() over ()) AS vnr, (s->'energyConstraint'->>'upper')::float8 AS upper, (s->'energyConstraint'->>'lower')::float8 AS lower
		    FROM jsonb_array_elements((fo).foc->'slices') WITH ORDINALITY AS S(s, snr)
		    LEFT JOIN LATERAL generate_series(1, dt_sec_to_duration((s->>'durationSeconds')::int)) AS SS(ssnr) ON TRUE
		    ORDER BY snr, ssnr),
	amounts AS (SELECT snr, sum(v) as totalAmount
		    FROM unnest(((fo).schedule).values) WITH ORDINALITY AS S(v, vnr)
			 LEFT JOIN slices ON slices.vnr = S.vnr
		    GROUP BY snr
		    ORDER BY snr),
	amount_bnd AS (SELECT amounts.snr, GREATEST(lower, LEAST(upper, totalAmount)) AS totalAmount
		       FROM amounts INNER JOIN slices ON amounts.snr = slices.snr),
	merged_obj AS (SELECT key, value FROM JSONB_EACH((fo).foc)
		       UNION ALL
		       SELECT 'flexOfferSchedule', json_build_object('startTime'::text, dt_to_timestamp(((fo).schedule).start_time), 
								     'energyAmounts', array_to_json((SELECT array_agg(totalAmount) FROM amount_bnd)))::JSONB)
	SELECT JSON_OBJECT_AGG(key, value)::JSONB FROM merged_obj;   
$$ LANGUAGE sql STABLE STRICT;

-- Convert a timeseries_d to a (non-flexible) flexoffer_d
CREATE OR REPLACE FUNCTION timeseries_d_to_flexoffer_d(ts timeseries_d) RETURNS flexoffer_d AS $$
  SELECT ROW((ts).start_time, -- est
   	     (ts).start_time, -- lst
	     (SELECT array_agg(ROW(1, v, v)::slice_d) FROM unnest((ts).values) AS V(v)), -- slices
	     ts, -- schedule
	     ts, -- baseline
	     NULL -- foc, the underlying continuous flex-offer
	     )::flexoffer_d
$$ LANGUAGE sql STABLE STRICT;

-- Convert a time series to flexoffer
DROP FUNCTION IF EXISTS ts_to_fo(jsonb);
CREATE OR REPLACE FUNCTION ts_to_fo(ts jsonb) RETURNS jsonb AS $$
	SELECT flexoffer_d_to_json(timeseries_d_to_flexoffer_d(timeseries_d_from_json(ts)))
$$ LANGUAGE sql STABLE STRICT;


-- Convert a flexoffer (schedule) to time series
CREATE OR REPLACE FUNCTION fo_to_ts(fo jsonb, type varchar(255) DEFAULT 'schedule') RETURNS jsonb AS $$
 SELECT CASE WHEN type='schedule' THEN timeseries_d_to_json((flexoffer_d_get(fo)).schedule, format('Flexoffer %s schedule', fo->>'id'))
	     WHEN type='default' THEN timeseries_d_to_json((flexoffer_d_get(fo)).baseline, format('Flexoffer %s default schedule (baseline)', fo->>'id')) END
$$ LANGUAGE sql STABLE STRICT;


-- SELECT id, (fo_update_schedule(flexoffer_d_get(fo))->'flexOfferSchedule') FROM fo_get_simple;
-- SELECT fo_to_ts(fo) FROM fo_get_simple;

/* Schedules the flexoffers in the given set */
DROP FUNCTION IF EXISTS schedule_baseline(jsonb) CASCADE;
CREATE OR REPLACE FUNCTION schedule_baseline(fo jsonb) RETURNS SETOF jsonb AS $$
 SELECT JSON_OBJECT_AGG(key, value)::JSONB FROM (
	  SELECT key, value FROM JSONB_EACH(fo)
		  UNION ALL
	  SELECT 'flexOfferSchedule', fo->'defaultSchedule'
 ) AS S  					        
$$ LANGUAGE sql STABLE STRICT;


-- ****************** Flexoffer balancing solver ******************************

DELETE FROM sl_solver
WHERE (name = 'fosolver');

-- Registers the solver and 1 method.
WITH 
     -- Registers the solver and its parameters.
     solver AS   (INSERT INTO sl_solver(name, version, author_name, author_url, description)
                  values ('fosolver', 1.0, '', '', 'A solver for a flex-offer scheduling (by Laurynas Siksnys)') 
                  returning sid),     

     -- Registers the BASIC balancing/scheduling method. It has no parameters.
     methodSch AS  (INSERT INTO sl_solver_method(sid, name, name_full, func_name, prob_name, description)
                    SELECT sid, 'balance', 'The MIP-based method for flex-offer scheduling', 'fo_solve_schedule', 'Flex-offer scheduling problem', 'Solves the scheduling problem utilizing the LP solver'
		    FROM solver RETURNING mid)
     -- Perform the actual insert
     SELECT count(*) FROM solver, methodSch;

-- Set the default method
UPDATE sl_solver s
SET default_method_id = mid
FROM sl_solver_method m
WHERE (s.sid = m.sid) AND (s.name = 'fosolver') AND (m.name='balance');


-- -- A pair that binds unknown variable number and a flex-offer 

-- Install the solver method
CREATE OR REPLACE FUNCTION fo_solve_schedule(arg sl_solver_arg) RETURNS setof record AS $$
  DECLARE
     foundAttFO		boolean = false;-- Was the "fo" attribute found?     
  BEGIN
     -- Check if the required columns are specified
     foundAttFO    = COALESCE((SELECT count(*)=1 FROM sl_get_attributes(arg) WHERE att_kind = 'unknown'::sl_attribute_kind AND att_name = 'fo' AND att_type = 'jsonb'), false);

     -- Report and error, if the schema mismatch
     IF NOT foundAttFO THEN
	 RAISE EXCEPTION 'The input relation must comply to the following schema: (fo jsonb); and the attribute "fo" must be unknown.';
     END IF;

     -- Create the flex-offer view over the input relation
     PERFORM sl_create_view(sl_build_out_defcols(arg, ARRAY[['tmp_flexoffer_id','(row_number() OVER ())'], 
							    ['tmp_flexoffer_d', 'flexoffer_d_get(fo)']]), 'fo_solver_input');

      -- Solves the overall problem
     CREATE TEMP VIEW fo_solver_solution AS 
	WITH solution AS (
		SOLVESELECT e, ep, en, s IN (
			SELECT fid, 		
			       NULL::boolean AS s,			-- Scheduling option: was it scheduled to this point?	       	       	       
			       NULL::float8 AS e,  			-- Energy, we want to find
			       NULL::float8 AS ep,			-- e+ for absolute deviations
			       NULL::float8 AS en,			-- e- for absolute deviations
			       tid,					-- Absolute TID
			       (tid - (f).est) AS tnr, 			-- TID offset number
			       array_length((f).slices, 1) AS s_cnt, 	-- Slices count
			       snr,					-- Slice Nr.
			       el,					-- Slice low energy 
			       eh,					-- Slice high energy
			       (tid<=(f).lst) AS can_schedule		-- Can schedule start_time to this TID	       
			FROM (SELECT  tmp_flexoffer_id AS fid, tmp_flexoffer_d FROM fo_solver_input) AS F(fid, f)
			     LEFT OUTER JOIN LATERAL generate_series((f).est, array_length((f).slices, 1) + (f).lst - 1) AS T(tid) ON true	     
			     LEFT JOIN LATERAL unnest((f).slices) WITH ORDINALITY AS S(d, el, eh, snr) ON (f).est + snr = T.tid + 1			 
			) AS t
		MINIMIZE(SELECT sum(ep+en) FROM (SELECT DISTINCT ON (tid) ep, en FROM t ORDER BY tid, fid) AS o)
		SUBJECTTO	
			-- Absolute deviations problem
			(SELECT DISTINCT ON (tid) ep>=0, en>=0, ep - en = (SELECT sum(t2.e) FROM t AS t2 WHERE (t1.tid = t2.tid))
			 FROM t AS t1
			 ORDER BY tid, fid),
			-- Constraint of scheduling possibilities 
			(SELECT sum(s) = 1  FROM t WHERE can_schedule GROUP BY fid),
			-- Energy lower bound constraint	 
			(SELECT e >= (SELECT sum(t2.s*t3.el) 
					 FROM t AS t2, t AS t3 
					 WHERE (t2.fid = t1.fid) AND (t3.fid = t1.fid) AND
					       (t1.tid>=t2.tid)  AND (t2.can_schedule) AND 
					       (t3.tid = t1.tid - t2.tnr))
			FROM t AS t1),
			-- Energy upper bound constraint	 
			(SELECT e <= (SELECT sum(t2.s*t3.eh) 
					 FROM t AS t2, t AS t3 
					 WHERE (t2.fid = t1.fid) AND (t3.fid = t1.fid) AND
					       (t1.tid>=t2.tid)  AND (t2.can_schedule) AND 
					       (t3.tid = t1.tid - t2.tnr ))
			 FROM t AS t1)
		WITH solverlp.cbc(args:='-maxNodes 10')),
	  sol_packed AS (SELECT fid, tid, s, s_cnt, (array_agg(e) OVER (PARTITION BY fid ORDER BY tid ROWS BETWEEN CURRENT ROW AND UNBOUNDED FOLLOWING)) AS prof FROM solution)
	SELECT fid, ROW(tid, prof[1 : s_cnt])::timeseries_d AS ts
	FROM sol_packed
	WHERE s;

     --    Output the solver result
     RETURN QUERY EXECUTE sl_return(arg, sl_build_out_defcols(arg, 
				ARRAY[['fo','(SELECT fo_update_schedule(ROW((tmp_flexoffer_d).est, (tmp_flexoffer_d).lst, (tmp_flexoffer_d).slices, ts, ts, (tmp_flexoffer_d).foc)::flexoffer_d) 
					      FROM fo_solver_solution WHERE fid = tmp_flexoffer_id)']],
				ROW('SELECT * FROM fo_solver_input')::sl_viewsql_out));
    
     -- Performs the clean-up
     DROP VIEW fo_solver_solution;
     PERFORM sl_drop_view('fo_solver_input');	
  END;
$$ LANGUAGE plpgsql VOLATILE STRICT;

/* Schedules the flexoffers in the given set */
DROP FUNCTION IF EXISTS schedule(jsonb) CASCADE;
CREATE OR REPLACE FUNCTION schedule(fos jsonb) RETURNS SETOF jsonb AS $$
BEGIN   
   CREATE TEMP TABLE tmp_fos AS SELECT fo_unnest(fos) AS fo;
   RETURN QUERY SOLVESELECT fo IN (SELECT fo FROM tmp_fos) AS T WITH fosolver();
   DROP TABLE tmp_fos;
END   
$$ LANGUAGE plpgsql VOLATILE STRICT;


/* TESTS: 

SELECT fo, flexoffer_d_get(fo), disaggregate(fo) FROM (
SOLVESELECT fo IN (SELECT aggregate_set(sum(fo)) as fo FROM fo_get_simple) AS T
WITH fosolver) AS s; 


SELECT disaggregate(fo) 
FROM (	SOLVESELECT fo IN (SELECT aggregate_set(sum(fo)) AS fo 
			   FROM fo_get_simple) AS T
	WITH fosolver
     ) AS SA

SELECT schedule(aggregate(fo)) FROM fo_get_simple;     
SELECT execute(schedule(aggregate(fo))) FROM fo_get_simple;     

*/

-- ******************* Core views of the Aggregator ***************************

-- Gets active flex-offers
DROP VIEW IF EXISTS fo_get_simple;
CREATE VIEW fo_get_simple AS 
SELECT (f->>'id')::int4 AS id, fo FROM jsonb_array_elements(json_from_web('flexoffers')) AS F(fo);

DROP VIEW IF EXISTS fo_get_aggregated;
CREATE VIEW fo_get_aggregated AS 
SELECT (f->>'id')::int4 AS id, fo FROM jsonb_array_elements(json_from_web('aggfos')) AS F(fo);

/* TESTS:

   SELECT * FROM fo_get_simple;
   SELECT * FROM fo_get_aggregated;
*/


-- ******************* TESTS ***************************************************

/*

-- Just schedule

SELECT fo, get_flexoffer_d(fo), fo->'flexOfferSchedule' FROM (
SOLVESELECT fo IN (SELECT aggregate_set(sum(fo)) as fo FROM fo_get_simple) AS T
WITH fosolver) AS s; 


-- Aggregate/schedule/disaggregate

SELECT disaggregate(fo) 
FROM (	SOLVESELECT fo IN (SELECT aggregate_set(sum(fo)) AS fo 
			   FROM fo_get_simple) AS T
	WITH fosolver
     ) AS SA


*/
-- *****************************************************************************

-- SET client_min_messages TO NOTICE;

-- select * from jsonb_array_elements(json_from_web('flexoffers'));

-- select jsonb_array_length(f->>'slices') from jsonb_array_elements(json_to_web('analytics/aggregate', (SELECT json_from_web('flexoffers')))) AS A(f);

-- select (f->>'startAfterTime')::timestamptz FROM jsonb_array_elements(json_from_web('flexoffers')) AS F(f);

-- SELECT json_from_web('flexoffers');




