/*
jWeather(TM) is a Java library for parsing raw weather data
Copyright (C) 2004 David Castro

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

For more information, please email arimus@users.sourceforge.net
*/
package net.sf.jweather.metar;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.logging.Logger;

/**
 * Responsible for downloading the METAR reports 
 *
 * <p>
 * <code>
 * String metarData = MetarFetcher.fetch("KLAX");<br>
 * </code>
 * </p>
 *
 * @author David Castro, dcastro@apu.edu
 * @version $Revision: 1.4 $
 * @see <a href="Metar.html">Metar</a>
 */
public class MetarFetcher {
	private static Logger log = null;
	private static String metarData = null;
	static final Charset defaultCharset = Charset.forName("UTF-8");
	final static String httpMetarURL = "http://weather.noaa.gov/pub/data/observations/metar/stations/";
	//final static String httpMetarHostname =  "weather.noaa.gov";
	//final static int    httpMetarPort     =  80;
	//final static String httpMetarPath     = "/pub/data/observations/metar/stations/";

	static {
    	log = Logger.getLogger("net.sf.jweather");
		log.fine("MetarFetcher: instantiated");
	}

	public static String fetch(String station)throws Exception {
		return fetch(station, 0);
	}

	public static String fetch(String station, int timeout) throws Exception{
		metarData = null;

		HttpURLConnection openConnection = null;
		try{
		URL url = new URL(httpMetarURL + station + ".TXT");
		 openConnection =(HttpURLConnection)url.openConnection();
		
			// set the timeout is specified
			if (timeout != 0) {
				log.fine("MetarFetch: setting timeout to '"+timeout+"' milliseconds");
				long start = System.currentTimeMillis();
				openConnection.setConnectTimeout(timeout);
				long end = System.currentTimeMillis();
				if (end - start < timeout) {
					openConnection.setReadTimeout((int)(end - start));
				} else {
					return null;
				}
			}
		

		// check that we didn't run out of retries
		if (openConnection.getResponseCode() != HttpURLConnection.HTTP_OK) {
			log.severe("failed to download station data for '"+station+"'");
			return null;
		} else {
			
			InputStream is = url.openStream();
			int l = openConnection.getContentLength();
			ByteArrayOutputStream bos = new ByteArrayOutputStream(l);
			int read = 0; byte[] b = new byte[l];
			while( -1 !=( read= is.read(b))){
				bos.write(b,0, read);
			}
			is.close();
			metarData = new String(bos.toByteArray(),getCharset( openConnection.getContentType())) + "\n";
			log.fine("MetarFetcher: metar data: " + metarData);
		}
		}finally{
			if(openConnection!=null)openConnection.disconnect();
		}
		return metarData;
	}
	static Charset getCharset(String contentType){
		
		String charset = "UTF-8";
		if(contentType==null || contentType.length()<2)
			return defaultCharset;
		String[] values = contentType.split(";");
		if(values.length>5)return defaultCharset;
		
		for (String value : values) {
		    value = value.trim();		    
		    if (value.toLowerCase().startsWith("charset=")) {
		        charset = value.substring("charset=".length());
		    }
		}

		if ("".equals(charset)) {
		    charset = "UTF-8"; //Assumption
		}
		return Charset.isSupported(charset)?Charset.forName(charset):defaultCharset;
		

	}
}
