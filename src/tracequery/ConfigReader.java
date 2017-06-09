/*
 * Copyright 2017 Sam Hume.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tracequery;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * ConfigReader loads the contents of the Trace-XML config file and provides accessors
 *   to retrieve the information
 *   @version 0.1
 */
class ConfigReader {
  private static String xqueryPath = "";
  private static String xmlPath = "";
  private static String traceNode = "";
  private static String traceNodeUnique = "";
  private static String traceNodeOid = "";
  private static String traceNodeDetails = "";
  private static String l3Graph = "";
  private static String traceXsl = "";
  private static String textTraceXsl = "";
  private static String traceHtml = "";
  private static String unreachableXsl = "";
  private static String unreachableXml = "";
  private static String unreachableHtml = "";
  private static String unreachableText = "";
  private static String dataCollection;
  private static String dataTabulation;
  private static String dataAnalysis;
  private static Boolean verbose;
  private static String odmXsdFile;
  private static String defineXsdFile;
   
  /** 
   * loadConfigProperties reads the configuration file and loads them into property variables
   * @param cfgFileName String for the configuration file name and path
   */
  public static void loadConfigProperties(String cfgFileName)  {
    Properties prop = new Properties();
    InputStream input = null;
    
    try {
	input = new FileInputStream(cfgFileName);
	// load a properties file
	prop.load(input);

	// get the property value and print it out
	xqueryPath = prop.getProperty("xquery-path");
	xmlPath = prop.getProperty("xml-path");
	traceNode = prop.getProperty("trace-node");
	traceNodeUnique = prop.getProperty("trace-node-unique");
	traceNodeOid = prop.getProperty("trace-node-oid");
	traceNodeDetails = prop.getProperty("trace-node-details");
	l3Graph = prop.getProperty("L3-graph");
	traceXsl = prop.getProperty("trace-xsl");
	textTraceXsl = prop.getProperty("text-trace-xsl");
	traceHtml = prop.getProperty("trace-html");
        unreachableXsl = prop.getProperty("unreachable-xsl");
        unreachableXml = prop.getProperty("unreachable-xml");
        unreachableHtml = prop.getProperty("unreachable-html");
        unreachableText = prop.getProperty("unreachable-text");
        dataCollection = prop.getProperty("data-collection-file");
        dataTabulation = prop.getProperty("data-tabulation-file");
        dataAnalysis = prop.getProperty("data-analysis-file");
        odmXsdFile = prop.getProperty("odm-xsd-file");
        defineXsdFile = prop.getProperty("define-xsd-file");

    } catch (FileNotFoundException ex) {
        System.out.println("Configuration file not found: " + cfgFileName);
        System.exit(0);
    }catch (IOException ex) {
        System.out.println("Unable to load the configuration file " + cfgFileName + ". " + ex.toString());
        System.exit(0);
    } finally {
	if (input != null) {
            try {
		input.close();
            } catch (IOException e) {
		System.out.println("Unable to close configuration file.");
			}
		}
	}
  }

    public static String getUnreachableXsl() {
        if (unreachableXsl == null) {
            return "";
        }
        return unreachableXsl;
    }

    public static String getUnreachableXml() {
        if (unreachableXml == null) {
            return "";
        }
        return unreachableXml;
    }

    public static String getUnreachableHtml() {
        if (unreachableHtml == null) {
            return "";
        }
        return unreachableHtml;
    }

    public static String getTraceNodeUnique() {
        if (traceNodeUnique == null) {
            return "";
        }
        return traceNodeUnique;
    }

    public static String getXqueryPath() {
        if (xqueryPath == null) {
            return "";
        } else if (!xqueryPath.endsWith(File.separator)) {
            xqueryPath += File.separator;
        }
        return xqueryPath;
    }

    public static String getXmlPath() {
        if (xmlPath == null) {
            return "";
        } else if (!xmlPath.endsWith(File.separator)) {
            xmlPath += File.separator;
        }
        return xmlPath;
    }

    public static String getTraceNode() {
        if (traceNode == null) {
            return "";
        }
        return traceNode;
    }

    public static String getTraceNodeOid() {
        if (traceNodeOid == null) {
            return "";
        }
        return traceNodeOid;
    }

    public static String getTraceNodeDetails() {
        if (traceNodeDetails == null) {
            return "";
        }
        return traceNodeDetails;
    }

    public static String getL3Graph() {
        if (l3Graph == null) {
            return "";
        }
        return l3Graph;
    }

    public static String getTraceXsl() {
        if (traceXsl == null) {
            return "";
        }
        return traceXsl;
    }
    
    public static String getTextTraceXsl() {
        if (textTraceXsl == null) {
            return "";
        }
        return textTraceXsl;
    }

    public static String getUnreachableText() {
        if (unreachableText == null) {
            return "";
        }
        return unreachableText;
    }

    public static String getTraceHtml() {
        if (traceHtml == null) {
            return "";
        }
        return traceHtml;
    }  

    public static String getDataCollection() {
        if (dataCollection == null) {
            return "";
        }
        return dataCollection;
    }

    public static String getDataTabulation() {
        if (dataTabulation == null) {
            return "";
        }
        return dataTabulation;
    }

    public static String getDataAnalysis() {
        if (dataAnalysis == null) {
            return "";
        }
        return dataAnalysis;
    }    

    public static String getOdmXsdFile() {
        if (odmXsdFile == null) {
            return "";
        }
        return odmXsdFile;
    }
    public static String getDefineXsdFile() {
        if (defineXsdFile == null) {
            return "";
        }
        return defineXsdFile;
    }

    public static Boolean getVerbose() {
        return verbose;
    }

    public static void setVerbose(Boolean verbose) {
        ConfigReader.verbose = verbose;
    }
}   
