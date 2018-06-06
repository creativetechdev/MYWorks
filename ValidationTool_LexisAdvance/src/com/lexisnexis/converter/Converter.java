/**
 * 
 */
package com.lexisnexis.converter;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 * @author Sandeep Kumar
 *
 */
public class Converter {

	public static String currWorkingDIR = System.getProperty("user.dir");
	public static Logger logger = Logger.getLogger(Converter.class);
	static
	{
		PropertyConfigurator.configure(currWorkingDIR + System.getProperty("file.separator") + "resources" + System.getProperty("file.separator") + "log4j.properties");
	}
	public static void main(String[] args) throws IOException {
		
		UtilMethods.transformXml();

	}

}
