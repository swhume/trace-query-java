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

import java.awt.Desktop;
import java.io.File;
import java.net.URI;
import javax.xml.transform.Transformer;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
/**
 * XsltTrace runs style sheets to transform XML output into html and other formats.
 * @version 0.1
 */
class XsltTrace {
    private final String xmlFileNameIn;
    private final String xslFileNameIn;
    private String fileNameOut;
    
    /**
     * Constructor for XsltTrace
     * @param xmlFileName String that contains the XML path and file to be transformed
     * @param xslFileName String that contains the XSL path and file to perform the transformation
     */
    public XsltTrace(String xmlFileName, String xslFileName) {
        this.xmlFileNameIn = xmlFileName;
        this.xslFileNameIn = xslFileName;
    }

    /**
     * transformXMLFile executes the XSL transformation on the XML file 
     * @param fileNameOutput String that contains the path and file for the transformed output
     * @param isShowResult Boolean that indicates if an html output file should be loaded into the browser
     */    
    public void transformXMLFile(String fileNameOutput, Boolean isShowResult) {
        fileNameOut = fileNameOutput;
        TransformerFactory factory = TransformerFactory.newInstance();
        StreamSource xslStream = new StreamSource(xslFileNameIn);
        StreamSource xmlIn = new StreamSource(xmlFileNameIn);
        StreamResult fileOut = new StreamResult(fileNameOut);
        try {
            Transformer transformer = factory.newTransformer(xslStream);
            transformer.transform(xmlIn, fileOut);
        } catch (TransformerException e) {
            System.out.println("Error transforming XML file to " + fileNameOutput + ". " + e.getMessage());
        }               
        if (isShowResult) {
            try {
                File sFileOut = new File(fileNameOut);
                URI fileUri = sFileOut.toURI();
                Desktop desktop = Desktop.getDesktop();
                desktop.browse(fileUri);
            }
            catch (Exception e) {
                System.out.println("Unable to load HTML file in browser: " + e.getMessage());
            }

        }
    }
}