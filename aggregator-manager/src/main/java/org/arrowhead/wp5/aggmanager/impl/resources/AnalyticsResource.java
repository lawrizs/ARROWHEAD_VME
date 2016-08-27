package org.arrowhead.wp5.aggmanager.impl.resources;

/*-
 * #%L
 * ARROWHEAD::WP5::Aggregator Manager
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


import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.Collection;
import java.util.Scanner;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.arrowhead.wp5.agg.api.AggregationException;
import org.arrowhead.wp5.agg.api.FOAggParameters;
import org.arrowhead.wp5.agg.impl.Aggregator;
import org.arrowhead.wp5.aggmanager.impl.resources.entities.AggregationInput;
import org.arrowhead.wp5.aggmanager.impl.resources.entities.AnalyticsResourceSettings;
import org.arrowhead.wp5.application.entities.StringWrapper;
import org.arrowhead.wp5.core.entities.AggregatedFlexOffer;
import org.arrowhead.wp5.core.entities.FlexOffer;
import org.arrowhead.wp5.core.entities.FlexOfferException;
import org.arrowhead.wp5.core.wrappers.FlexOfferKey;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.postgresql.util.PGobject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/analytics")
public class AnalyticsResource {
	final static Logger logger = LoggerFactory.getLogger(AnalyticsResource.class);
	
	private Aggregator agg;
	private AnalyticsResourceSettings settings = new AnalyticsResourceSettings();

	// private String solveDB_URL =
	// "jdbc:postgresql://192.168.56.101/postgres?user=laurynas&password=XXXXX";

	public AnalyticsResource(Aggregator agg) {
		this.agg = agg;
	}

	/* Analytics settings */
	@GET
	@Path("settings")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public AnalyticsResourceSettings getSettings() {
		return this.settings;
	}

	@POST
	@Path("settings")
	public void setSettings(AnalyticsResourceSettings settings) {
		this.settings = settings;
	}

	/* Aggregated a set of flex-offers */
	@POST
	@Path("aggregate")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public AggregatedFlexOffer[] aggregate(AggregationInput input) {

		if (input == null) {
			logger.error("Empty aggregation input");
			return new AggregatedFlexOffer[] {};
		}

		Collection<FlexOffer> fc = input.getFlexOffers();
		FOAggParameters par = input.getParams();

		if (fc == null || fc.isEmpty()) {
			return new AggregatedFlexOffer[] {};
		}

		/* Setup default aggregation parameters */
		if (par == null) {
			par = this.agg.getAggParameters();
		}

		Collection<AggregatedFlexOffer> agg_fos;
		try {
			agg_fos = this.agg.getAggregator()
					.Aggregate(par, fc);
		} catch (AggregationException e) {
			logger.error("Aggregation error:", e.fillInStackTrace());
			return new AggregatedFlexOffer[] {}; 
		}

		return agg_fos.toArray(new AggregatedFlexOffer[] {});
	}

	/* Disaggregate flexOffer schedules */
	@POST
	@Path("disaggregate")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public FlexOffer[] disaggregate(AggregatedFlexOffer afo) {
		if (afo == null || afo.getFlexOfferSchedule() == null) {
			return new FlexOffer[] {};
		}

		try {
			return this.agg.getAggregator().Disaggregate(afo)
					.toArray(new FlexOffer[] {});
		} catch (AggregationException e) {
			logger.error("Disaggregation error", e.fillInStackTrace());
		}
		return new FlexOffer [] {};
	}
	
	/* Disaggregate flexOffer schedules */
	@POST
	@Path("execute")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public FlexOffer execute(FlexOffer fo) {
		if (fo == null) {
			logger.error("Cannot execute the NULL flexoffer");
			return null;
		}

		FlexOfferKey fk = new FlexOfferKey(fo.getOfferedById(),
				Integer.toString(fo.getId()));

		FlexOffer org = this.agg.getSimpleFlexOffer(fk);

		if (org == null) {
			logger.error("Cannot find the simple flexoffer to execute!");
			return null;
		}
		
		org.setFlexOfferSchedule(fo.getFlexOfferSchedule());
		
		if (fo.getFlexOfferSchedule() != null && fo.getFlexOfferSchedule().isCorrect(org)) {	
			try {
				this.agg.executeFlexOffer(org);
			} catch (FlexOfferException e) {
				logger.error("Error executing flexoffer:", e.fillInStackTrace());
			}
		} else {
			logger.warn("Trying to execute a flexoffer with an invalid schedule");
		}
		return fo;
	}
	
	@POST
	@Path("executeA")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public AggregatedFlexOffer executeA(AggregatedFlexOffer fo) {
		/*if (fo == null) {
			throw new Exception("Cannot execute the NULL aggragated flexoffer");
		}
		*/		
		if (fo.getFlexOfferSchedule() == null || !fo.getFlexOfferSchedule().isCorrect(fo)) {			
			logger.warn("Trying to execute an aggregated flexoffer with an invalid schedule");
		}		
		Collection<FlexOffer> fos;
		try {
			fos = this.agg.getAggregator().Disaggregate(fo);	
			for(FlexOffer f : fos) {
				this.execute(f);
			}
		} catch (AggregationException e) {
			logger.error("Error disaggregating a flexoffer schedule");
		}	
		return fo;
	}
	


	/* Convert a user query to a valid SQL query */
	private String getSQLquery(String userquery) {
		userquery = userquery.trim();

		if (userquery.startsWith("installAnalytics()")) {
			StringBuilder result = new StringBuilder("");
			result.append("SELECT 'The analytical engine has been successfully installed and configured in SolveDB!' AS message;");

			// Get file from resources folder
			ClassLoader classLoader = getClass().getClassLoader();
			try {
				InputStream s = classLoader
						.getResourceAsStream("solvedb/AggregatorSolver.sql");

				if (s == null) {
					throw new Exception("No resource file could be loaded.");
				}

				Scanner scanner = new Scanner(s);

				while (scanner.hasNextLine()) {
					String line = scanner.nextLine();
					result.append(line).append("\n");
				}
				scanner.close();
			} catch (Exception e) {
				JSONObject jobj = new JSONObject();
				jobj.put("error", e.getMessage());
				return jobj.toString();
			}

			/* Configure the SolveDB dynamically */
			result.append(String.format(
					"UPDATE settings SET value='%s' WHERE param='agg_url';\n",
					this.settings.getAggregator_URL()));

			return result.toString();
		}
		;

		return userquery;
	}

	/* Query the SolveDB database */
	@POST
	@Path("query")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public String query(StringWrapper queryString) throws JSONException {
		JSONObject jobj = new JSONObject();
		JSONArray jsarr = new JSONArray();
		Connection conn;
		try {
			conn = DriverManager.getConnection(this.settings.getSolveDB_URL());
			try {
				Statement st = conn.createStatement();
				if (st.execute(this.getSQLquery(queryString.getValue()))) {
					ResultSet rs = st.getResultSet();
					ResultSetMetaData rsmd = rs.getMetaData();
					/* Get warnings */
					SQLWarning warning = st.getWarnings();
					if (warning != null) {
						StringBuilder sb = new StringBuilder();
						while (warning != null) {
							sb.append(warning.getMessage());
							warning = warning.getNextWarning();
							if (warning != null) {
								sb.append(System.getProperty("line.separator"));
							}
						}
						jobj.put("warnings", sb.toString());
					}
					/* Get results */
					while (rs.next()) {
						int numColumns = rsmd.getColumnCount();
						JSONObject obj = new JSONObject();

						for (int i = 1; i < numColumns + 1; i++) {
							String column_name = rsmd.getColumnName(i);

							switch (rsmd.getColumnType(i)) {
							case java.sql.Types.ARRAY:
								obj.put(column_name, rs.getArray(column_name));
								break;
							case java.sql.Types.BIGINT:
								obj.put(column_name, rs.getInt(column_name));
								break;
							case java.sql.Types.BOOLEAN:
								obj.put(column_name, rs.getBoolean(column_name));
								break;
							case java.sql.Types.BLOB:
								obj.put(column_name, rs.getBlob(column_name));
								break;
							case java.sql.Types.DOUBLE:
								obj.put(column_name, rs.getDouble(column_name));
								break;
							case java.sql.Types.FLOAT:
								obj.put(column_name, rs.getFloat(column_name));
								break;
							case java.sql.Types.INTEGER:
								obj.put(column_name, rs.getInt(column_name));
								break;
							case java.sql.Types.NVARCHAR:
								obj.put(column_name, rs.getNString(column_name));
								break;
							case java.sql.Types.VARCHAR:
								obj.put(column_name, rs.getString(column_name));
								break;
							case java.sql.Types.TINYINT:
								obj.put(column_name, rs.getInt(column_name));
								break;
							case java.sql.Types.SMALLINT:
								obj.put(column_name, rs.getInt(column_name));
								break;
							case java.sql.Types.DATE:
								obj.put(column_name, rs.getDate(column_name));
								break;
							case java.sql.Types.TIMESTAMP:
								obj.put(column_name,
										rs.getTimestamp(column_name));
								break;
							default:
								Object o = rs.getObject(column_name);
								if ((o instanceof PGobject)
										&& (((PGobject) o).getType()
												.equalsIgnoreCase("json") || ((PGobject) o)
												.getType().equalsIgnoreCase("jsonb"))) 
								{
									Object json = new JSONTokener(((PGobject) o).getValue()).nextValue();
									obj.put(column_name, json);
								}
								else
									obj.put(column_name, o);
								break;
							}
						}

						jsarr.put(obj);
					}
					rs.close();
				};
				
				if (jsarr.length() == 0) {
					jobj.put("warnings", "No results were returned by the query.");
				}
				st.close();
			} catch (Exception e) {
				jobj.put("error", e.getMessage());
			} finally {
				conn.close();
			}
		} catch (Exception e) {
			jobj.put("error", e.getMessage());
		}
		jobj.put("result", jsarr);

		return jobj.toString();
	}

}
