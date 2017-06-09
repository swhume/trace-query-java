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
import java.io.*;
import java.net.URISyntaxException;
import java.util.HashMap;

import org.basex.core.*;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;

/**
 * The TraceQuery prototype queries the Trace-XML graph using XQuery to produce a full life-cycle trace
 * for any given variable OID. It captures each step as a file because some are useful
 * and to make each step more transparent to ease understanding and evolving the application.
 * @version 0.1
*/
public class TraceQuery {
    private static final Context context = new Context();
    private static String cfgFile;
    private static Boolean isQuiet = Boolean.FALSE;
    private static Boolean isFilter = Boolean.FALSE;
    private static String nodeOID = "";
    private static String graphMlFileName;
    
    /**
     * The TraceQuery application takes the following command-line arguments:
     * @param args command-line arguments including:
     * "cfg=path" path to configuration file,
     * "oid" identifies the variable on which the trace query will run,
     * "quiet" tells TraceQuery not to load the trace into the browser (batch mode)
     * "help" requests that the program display the application usage options 
    */
    public static void main(String[] args) {
        cfgFile = getConfigFileDir() + "trace-xml.cfg";        
        // process command line arguments
        setCommandLineOptions(args);
        
        if (nodeOID.isEmpty()) noContentFound("No OID provided for this query.");
        loadConfiguration(cfgFile);
        
        // generate the graph node trace for the variable
        String outputFileName = runNodeTrace();
        // get the file and oid details for each node    
        outputFileName = runGetNodeOIDs(outputFileName);
        // get the medata for each node
        outputFileName = runGetNodeDetails(outputFileName);
        // filter out Forms and IGs not referenced in the TraceItems
        if (isFilter) outputFileName = runGetFilteredNodes(outputFileName);
        // run the xslt to transform the node details file into an html trace visualization
        String xsltFileName = ConfigReader.getXmlPath() + ConfigReader.getTraceXsl();
        String htmlFileName = ConfigReader.getXmlPath() + ConfigReader.getTraceHtml();
        XsltTrace xslTrace = new XsltTrace(outputFileName, xsltFileName);
        xslTrace.transformXMLFile(htmlFileName, !isQuiet);
        context.close();
    }

    /* get the origin for each node in the trace - used for reporting untraceables */
    private static String runGetNodeDetails(String outputFileName) {
        String detailOutputFileName = ConfigReader.getXmlPath() + ConfigReader.getTraceNodeDetails();
        HashMap<String, String> qryParm = new HashMap<>();
        qryParm.put("trace-doc-name", outputFileName);
        Integer count = runQuery(detailOutputFileName, "trace-node-details.xql", qryParm);
        if (count == 0) noContentFound("Unable to retrieve the node details for this trace for oid = " + nodeOID);
        return detailOutputFileName;
    }
    
    /* filter out Form and IG nodes not refrenced by the TraceItem for tabulation and data collection */
    private static String runGetFilteredNodes(String nodeDetailsFileName) {
        String filteredOutputFileName = ConfigReader.getXmlPath() + "filtered-" + ConfigReader.getTraceNodeDetails();
        HashMap<String, String> qryParm = new HashMap<>();
        qryParm.put("trace-doc-name", nodeDetailsFileName);
        Integer count = runQuery(filteredOutputFileName, "trace-node-filters.xql", qryParm);
        if (count == 0) noContentFound("Unable to retrieve the node OIDs for this trace for oid = " + nodeOID);
        return filteredOutputFileName;
    }

    /* using the results of the GraphML trace look up the nodes in the appropriate XML file including the file name and path */
    private static String runGetNodeOIDs(String nodeFileName) {
        String oidOutputFileName = ConfigReader.getXmlPath() + ConfigReader.getTraceNodeOid();
        String xmlFileName = ConfigReader.getXmlPath() + "xml-files.xml";
        HashMap<String, String> qryParm = new HashMap<>();
        qryParm.put("trace-doc-name", nodeFileName);
        qryParm.put("graph-doc-name", graphMlFileName);
        qryParm.put("l1-doc-name", xmlFileName);
        Integer count = runQuery(oidOutputFileName, "trace-node-oid.xql", qryParm);
        if (count == 0) noContentFound("Unable to retrieve the node OIDs for this trace for oid = " + nodeOID);
        return oidOutputFileName;
    }
 
    /* run the trace query on the GraphML file and return the nodes in the trace */
    private static String runNodeTrace() {
        String outputFileName = ConfigReader.getXmlPath() + ConfigReader.getTraceNode();
        graphMlFileName = ConfigReader.getXmlPath() + ConfigReader.getL3Graph();
        HashMap<String, String> qryParm = new HashMap<>();
        qryParm.put("input", graphMlFileName);
        qryParm.put("oid", nodeOID);
        Integer count = runQuery(outputFileName, "trace-node.xql", qryParm);
        if (count == 0) noContentFound("No trace was found for oid = " + nodeOID);
        return outputFileName;
    }

    private static Integer runQuery(String outputFileName, String qryName, HashMap<String, String> qp) {
        Integer count = 0;
        String query = readFile(ConfigReader.getXqueryPath() + qryName);
        try {
            QueryProcessor proc = new QueryProcessor(query, context);
            // Store the pointer to the result in an iterator:
            for (String parmName : qp.keySet()) {
                proc.bind(parmName, qp.get(parmName));
            }            
            // Store the pointer to the result in an iterator
            Iter iter = proc.iter();
            OutputStream os = new FileOutputStream(outputFileName);
            // Create a serializer instance
            Serializer ser = proc.getSerializer(os);
            // Iterate through all items and serialize contents
            for(Item item; (item = iter.next()) != null;) {
                ser.serialize(item);
                if (!"<nodes/>".equalsIgnoreCase(item.toString())) count++;
              }
        } catch (FileNotFoundException ex) {
            System.err.println("Unable to locate or read the trace-node.xql. " + ex.getMessage());
            System.exit(0);
        } catch (IOException ex) {
            System.err.println("Unable to read the trace-node-unique.xql or write the ouput to " + outputFileName + ". " + ex.getMessage());
        } catch (QueryException ex) {
            System.err.println("Error reading results from the trace-node XQuery. " + ex.getMessage());
        }
        return count;
    }

  private static String readFile(String file) {
    BufferedReader f = null;
    Boolean ioException = Boolean.FALSE;
    StringBuilder xml = new StringBuilder();
      try {
          f = new BufferedReader(new FileReader(file));
          String line;
          while((line = f.readLine()) != null)
              xml.append(line);
      } catch (FileNotFoundException ex) {
          System.out.println("Unable to locate the XQuery file " + file + ". " + ex.getMessage());
          ioException = Boolean.TRUE;
      } catch (IOException ex) {
          System.out.println("Unable to read the XQuery file " + file + ". " + ex.getMessage());
          ioException = Boolean.TRUE;
      } finally {
          try {
              if (f != null) f.close();
          } catch (IOException ex) {
              System.err.println("Unable to close the XQuery file " + file + ". " + ex.getMessage());
          } finally {
              if (ioException) System.exit(0);
          }
      }
      return xml.toString();
    }
  
    private static void loadConfiguration(String cfgFile) {
        if (cfgFile.isEmpty()) noContentFound("Missing config file. The query was not executed.");
        ConfigReader.loadConfigProperties(cfgFile);
        checkForMetadataFiles();
    }

    /* ensure each of the expected metadata files can be found */
    private static void checkForMetadataFiles() {
        Boolean isFail;
        if (ConfigReader.getXmlPath() == null || ConfigReader.getXmlPath().isEmpty()) {
            System.out.println("Error: No XML path in the configuration file.");
            isFail = Boolean.TRUE;
        } else {
            isFail = checkMetadataFileNotFound(ConfigReader.getXmlPath() + ConfigReader.getL3Graph(), 
                    "Error: Missing Trace-XML graph file in configuration file or the file listed is not found.");
        }
        
        if (ConfigReader.getXqueryPath() == null || ConfigReader.getXqueryPath().isEmpty()) {
            System.err.println("Error: No XQuery path in the configuration file.");
            isFail = Boolean.TRUE;
        }        
        if (isFail) System.exit(0);
    }
    
    /* performs the actual test to ensure the metadata files can be found */
    private static Boolean checkMetadataFileNotFound(String metadataFileName, String missingFileMsg) {
        Boolean isFail = Boolean.FALSE;
        if (metadataFileName.isEmpty() ||  !(new File(metadataFileName).isFile())) {
            System.err.println(missingFileMsg);
            isFail = Boolean.TRUE;
        }
        return isFail;
    }
        
    /* get the current directory of the jar file as the default for the config file */
    private static String getConfigFileDir() {
        String jarPath = "";
        try {
            File jarFile = new File(TraceQuery.class.getProtectionDomain().getCodeSource().get‌​Location().toURI());
            String jarFilePath = jarFile.getAbsolutePath();
            jarPath = jarFilePath.replace(jarFile.getName(), "");
        } catch (URISyntaxException ex) {
            System.err.println("Unexpected error determining the current path. Please include the config file as a command-line argument. " 
                    + ex.toString());
        }
        return jarPath;
    }
    
    private static void noContentFound(String errMsg) {
        System.err.println(errMsg);
        System.exit(0);
    }
    
    // process command line arguments
    private static void setCommandLineOptions(String[] args) {
        for (int i = 0; i < args.length; i++) {
            String argument = args[i];
            if (argument.startsWith("cfg=")) {
                cfgFile = argument.substring(argument.indexOf("=")+1);
            } else if (argument.startsWith("oid=")) {
                nodeOID = argument.substring(argument.indexOf("=")+1);
            } else if (argument.contains("quiet")) {
                isQuiet = Boolean.TRUE;
            } else if (argument.contains("filter")) {
                isFilter = Boolean.TRUE;
            } else if (argument.equals("help")) {
                usage();
                System.exit(0);                
            } else {
                System.err.println("Unknown argument in TraceQuery.main: " +argument);
                usage();
                System.exit(0);
            }
        }                        
    }
    
    /* print the usage directions that include the command-line arguments */
    private static void usage() {
        System.err.println("Usage: java -jar TraceQuery.jar cfg=<config file> oid=<variable oid> [quiet] [filter] [help]");        
    }
}
