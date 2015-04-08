package bbdn.util.xml;

import java.io.StringReader;
import java.io.StringWriter;

import org.jdom2.Document;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

public class XMLHelper {
	/**
     * Pretty print the XML string given to this method.
     * 
     * @param xml String to be pretty printed
     * @return null if it's a malformed document, pretty printed XML otherwise
     */
    public static String prettyPrint(String xml){
            Document document = null;
            SAXBuilder parser = new SAXBuilder();
            StringWriter sw = new StringWriter();
            
            try {
                    document = parser.build(new StringReader(xml));
                    XMLOutputter out = new XMLOutputter();
                    out.setFormat(Format.getPrettyFormat());
                    
                    out.output(document, sw);
                    
            } catch (Exception e) {
                    return null;
            }
            
            return (sw == null ? null : sw.toString());
    }
}