package bbdn.lis.gradejourney;

import java.io.StringWriter;
import java.net.URL;

import javax.xml.bind.DatatypeConverter;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import bbdn.util.security.SSLCertificateHelper;
import bbdn.util.xml.XMLHelper;

/*
 * @class: 	GradeJourney
 * @author: Scott Hurrey
 * 
 * Purpose: This class provides the user the ability to interact with the LIS 2.0 implementation of the Grade Journey Building Block.
 * 			This Building Block provides a third-party application the ability to interact with the Blackboard Grade Center at a more
 * 			advanced level than is available through standard Web Services, without having to build a Building Block themselves.
 */
public class GradeJourney {

	// Blackboard Developer VMs contain self-signed certificates, which cause the connection to fail. 
	// When using these, we must override the SSL certificate check.
	// DO NOT USE IN PRODUCTION
	private static final boolean IGNORE_SSL_CERTIFICATE = true;
	
	// Username and Password from the Blackboard Learn SIS integration
	// DO NOT HARD-CODE IN YOUR APPLICATION
	private static final String username = "d1aad219-5c48-439e-99af-fe46b956d8dd";
	private static final String password = "password";
	
	/*
	 * @Method:	invoke
	 * 
	 * Purpose: This method will request all grades from a SourcedId.
	 * 			This is sample code, so many things are hard-coded that shouldn't be.
	 */
	public static String invoke (String action){
		SOAPMessage message = null;
		SOAPMessage response = null;
		
		try {
			
			// Instantiate SOAP Connection Factory and create a Connection object
			SOAPConnectionFactory soapFactory = SOAPConnectionFactory.newInstance();
			SOAPConnection soapConnection = soapFactory.createConnection();
			
			// Instantiate the message factory and create a message. This will be the object where we store our SOAP message.
			MessageFactory messageFactory = MessageFactory.newInstance();
			message = messageFactory.createMessage();
			
			// Encode username and password and add as a HTTP auth header to our SOAP message
			String authString = username+":"+password;
			String authorization = DatatypeConverter.printBase64Binary(authString.getBytes());
			MimeHeaders hd = message.getMimeHeaders();
			hd.addHeader("Authorization", "Basic " + authorization);
			
			// This is the URL for the endpoint in Learn
			URL soapEndpoint = new URL("https://services-uswest.skytap.com:28848/webapps/bb-data-integration-lis-final-BBLEARN/services/OutcomesManagementServiceSyncService/");
			
			// Initialize the SOAP part of the HTTP method
			SOAPPart soapPart = message.getSOAPPart();
		
			// Create SOAP Envelope. Generates:
			// <soapenv:Envelope 
			SOAPEnvelope soapEnvelope = soapPart.getEnvelope();
			
			// Add name space declaration to the soap envelope tag. Generates: 
			//  xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
			soapEnvelope.addNamespaceDeclaration("soapenv", "http://schemas.xmlsoap.org/soap/envelope/	");
			
			// Add ims namespace declaration to the soap envelope tag. Generates:
			// xmlns:ims="http://www.imsglobal.org/services/lis/oms1p0/wsdl11/sync/imsoms_v1p0">
			soapEnvelope.addNamespaceDeclaration("ims", "http://www.imsglobal.org/services/lis/oms1p0/wsdl11/sync/imsoms_v1p0");
			
			// Add a SOAP header tag. Generates:
			// <soapenv:Header>
			SOAPHeader soapHeader = soapEnvelope.getHeader();
			
			// Add an element to the headers that contains sub-elements for the IMS version and a random/unique message identifier. Generates the following
			// <ims:imsx_syncRequestHeaderInfo>
			// 		<ims:imsx_version>V2.0</ims:imsx_version>
			//		<ims:imsx_messageIdentifier>?</ims:imsx_messageIdentifier>
			// </ims:imsx_syncRequestHeaderInfo>
			SOAPElement syncRequestHeaderInfo = soapHeader.addChildElement("imsx_syncRequestHeaderInfo", "ims");
			
			// <ims:imsx_version>V2.0</ims:imsx_version>
			SOAPElement imsx_version = syncRequestHeaderInfo.addChildElement("imsx_version", "ims");
			imsx_version.addTextNode("V2.0");
			
			//<ims:imsx_messageIdentifier>?</ims:imsx_messageIdentifier>
			SOAPElement imsx_messageIdentifier = syncRequestHeaderInfo.addChildElement("imsx_messageIdentifier", "ims");
			imsx_messageIdentifier.addTextNode("1234567890");
			
			// Close the SOAP Header tag and open the soap body tag.
			// <soapenv:Body>
			SOAPBody soapBody = soapEnvelope.getBody();
			
			switch(action) {
			case "readResultsRequest":
				soapBody = readResultsRequest(soapBody);
				break;
			case "replaceLineItemRequest":
				soapBody = replaceLineItemRequest(soapBody);
				break;
			case "manualReplaceLineItemRequest":
				soapBody = manualReplaceLineItemRequest(soapBody);
				break;
			case "readResultIdsForCourseSectionRequest":
				soapBody = readResultIdsForCourseSectionRequest(soapBody);
				break;
			default:
				return("Method " + action + " is invalid or not yet implemented.");
			}
			
			// If using a test system with self-signed SSL certificate, ignore the SSL Certificate check
			if(IGNORE_SSL_CERTIFICATE)
				SSLCertificateHelper.ignoreCertificates();
			
			// Send the SOAP Message we just created and retrieve the response.
			response = soapConnection.call(message, soapEndpoint);
			
			response.writeTo(System.out);
			
			// Parse the XML response into a string and return the string to the original calling Object.
			return(parseResponse(response, true));
			
		} catch (NullPointerException npe) {
			// Didn't get a valid response
			return ("NullPointerException: " + npe.getMessage());
		} catch (SOAPException se) {
			return("SOAPException: " + se.getMessage());
		} catch (Exception e) {
			// Catch any random exception not a NullPointer
			return("Exception: " + e.getMessage());
		}
	}
	
	private static SOAPBody readResultsRequest(SOAPBody soapBody) throws SOAPException {
					
					
					// Add a readResultsRequest element to tell Learn what we want to do. Generates:
					// <ims:readResultsRequest>
					SOAPElement readResultsRequest = soapBody.addChildElement("readResultsRequest", "ims");
					
					// Add a sourcedIdSet element inside the readResultsRequest element to tell Learn what we grades we want. Generates:
					// <ims:sourcedIdSet>
					SOAPElement sourcedIdSet = readResultsRequest.addChildElement("sourceIdSet", "ims");
					
					// Add a sourceId element inside the sourcesIdSet element to tell Learn which specific gradebook columns to return. There can be many of these. Generates:
					// <ims:sourcedId>WC|LIS_TEST_001|LIS_TEST_001_GC001|jstudent</ims:sourcedId>
					SOAPElement sourcedId = sourcedIdSet.addChildElement("sourcedId", "ims");
					sourcedId.addTextNode("WC|LIS_TEST_001|LIS_TEST_001_GC001|jstudent");
					
					return(soapBody);
	}
	
	private static SOAPBody readResultIdsForCourseSectionRequest(SOAPBody soapBody) throws SOAPException {
		
		
		// Add a readResultsRequest element to tell Learn what we want to do. Generates:
		// <ims:readResultsRequest>
		SOAPElement readResultIdsForCourseSectionRequest = soapBody.addChildElement("readResultIdsForCourseSectionRequest", "ims");
		
		// Add a sourcedIdSet element inside the readResultsRequest element to tell Learn what we grades we want. Generates:
		// <ims:sourcedIdSet>
		SOAPElement courseSectionSourcedId = readResultIdsForCourseSectionRequest.addChildElement("courseSectionSourcedId", "ims");
		courseSectionSourcedId.addTextNode("LIS_TEST_001");
		
		return(soapBody);
	}

	private static SOAPBody manualReplaceLineItemRequest(SOAPBody soapBody) throws SOAPException {
					// Add a replaceLineItemRequest element to tell Learn what we want to do. Generates:
					// <ims:replaceLineItemRequest>
					SOAPElement readResultsRequest = soapBody.addChildElement("replaceLineItemRequest", "ims");
					
					// Add a sourcedId element inside the replaceLineItemRequest element to tell Learn what we grades we want. Generates:
					// <ims:sourcedId>LIS_TEST_001_GC001</ims:sourcedId>
					SOAPElement sourcedId = readResultsRequest.addChildElement("sourcedId", "ims");
					sourcedId.addTextNode("LIS_TEST_001_GC001");
					
					SOAPElement lineItemRecord = readResultsRequest.addChildElement("lineItemRecord", "ims");
					
					SOAPElement sourcedGUID = lineItemRecord.addChildElement("sourcedGUID", "ims");
					
					//<!--Optional:-->
                    // <ims:refAgentInstanceID>?</ims:refAgentInstanceID>
                    // <ims:sourcedId>LIS_TEST_001_GC001</ims:sourcedId>
					SOAPElement refAgentInstanceID = sourcedGUID.addChildElement("refAgentInstanceID", "ims");
					refAgentInstanceID.addTextNode("?");
					
					SOAPElement sourcedId2 = sourcedGUID.addChildElement("sourcedId", "ims");
					sourcedId2.addTextNode("LIS_TEST_001_GC001");
					
					SOAPElement lineItem = lineItemRecord.addChildElement("lineItem", "ims");
					
					SOAPElement context = lineItem.addChildElement("context", "ims");
					
					//<ims:contextIdentifier>LIS_TEST_001</ims:contextIdentifier>
                    //<ims:contextType>CourseOffering</ims:contextType>
					SOAPElement contextIdentifier = context.addChildElement("contextIdentifier", "ims");
					contextIdentifier.addTextNode("LIS_TEST_001");
					
					SOAPElement contextType = context.addChildElement("contextType", "ims");
					contextType.addTextNode("CourseSection");
					
					SOAPElement lineItemType = lineItem.addChildElement("lineItemType", "ims");
					
					SOAPElement lineItemTypeVocab = lineItemType.addChildElement("lineItemTypeVocabulary", "ims");
					lineItemTypeVocab.addTextNode("http://www.imsglobal.org/vdex/lis/omsv1p0/lineitemtypevocabularyv1p0.xml");
					
					/*
					 * <ims:lineItemTypeValue>
                           <ims:language>en</ims:language>
                           <ims:textString>Interim</ims:textString>
                       </ims:lineItemTypeValue>
					 */
					SOAPElement lineItemTypeValue = lineItemType.addChildElement("lineItemTypeValue", "ims");
					
					SOAPElement language = lineItemTypeValue.addChildElement("language", "ims");
					language.addTextNode("en");
					
					SOAPElement textString = lineItemTypeValue.addChildElement("textString", "ims");
					textString.addTextNode("Interim");
					
					/*
					 * <ims:resourceHandlerId>http://fake/LTI/handler/uri</ims:resourceHandlerId>
                       <!--Optional:-->
                       <ims:localeKey>some.resource.bundle.key.lineitem</ims:localeKey>
                       <!--Optional:-->
                       <ims:defaultDisplayName>testDefaultDisplayNameLineItem</ims:defaultDisplayName>
					 */
					
					SOAPElement resourceHandler = lineItemType.addChildElement("resourceHandler", "ims");
					resourceHandler.addTextNode("http://fake/LTI/handler/uri");
					
					SOAPElement localeKey = lineItemType.addChildElement("localeKey", "ims");
					localeKey.addTextNode("some.resource.bundle.key.lineitem");

					SOAPElement defaultDisplayName = lineItemType.addChildElement("defaultDisplayName", "ims");
					defaultDisplayName.addTextNode("testDefaultDisplayNameLineItem");
					
					/* 
					 * <ims:label>
                             <ims:language>en</ims:language>
                             <ims:textString>SIS Assign 2</ims:textString>
                       </ims:label>
					 */
					SOAPElement label = lineItem.addChildElement("label", "ims");
					
					SOAPElement labelLang = label.addChildElement("language", "ims");
					labelLang.addTextNode("en");
					
					SOAPElement labelText = label.addChildElement("textString", "ims");
					labelText.addTextNode("GC Manual 1");
					
					/*
					 * 	<ims:extension>
	                            <ims:extensionField>
	                                    <ims:fieldName>PointsPossible</ims:fieldName>
	                                    <ims:fieldType>Decimal</ims:fieldType>
	                                    <ims:fieldValue>20.0</ims:fieldValue>
	                            </ims:extensionField>
	                    </ims:extension>
					 */
					SOAPElement extension = lineItem.addChildElement("extension", "ims");
					
					SOAPElement extensionField = extension.addChildElement("extensionField", "ims");
					
					SOAPElement fieldName = extensionField.addChildElement("fieldName", "ims");
					fieldName.addTextNode("PointsPossible");
					
					SOAPElement fieldType = extensionField.addChildElement("fieldType", "ims");
					fieldType.addTextNode("Decimal");
					
					SOAPElement fieldValue = extensionField.addChildElement("fieldValue", "ims");
					fieldValue.addTextNode("20.0");
					
					return(soapBody);
	}
	
	private static SOAPBody replaceLineItemRequest(SOAPBody soapBody) throws SOAPException {
					
					// Add a replaceLineItemRequest element to tell Learn what we want to do. Generates:
					// <ims:replaceLineItemRequest>
					SOAPElement readResultsRequest = soapBody.addChildElement("replaceLineItemRequest", "ims");
					
					// Add a sourcedId element inside the replaceLineItemRequest element to tell Learn what we grades we want. Generates:
					// <ims:sourcedId>LIS_TEST_001_GC002</ims:sourcedId>
					SOAPElement sourcedId = readResultsRequest.addChildElement("sourcedId", "ims");
					sourcedId.addTextNode("LIS_TEST_001_GC002");
					
					SOAPElement lineItemRecord = readResultsRequest.addChildElement("lineItemRecord", "ims");
					
					SOAPElement sourcedGUID = lineItemRecord.addChildElement("sourcedGUID", "ims");
					
					//<!--Optional:-->
                    // <ims:refAgentInstanceID>?</ims:refAgentInstanceID>
                    // <ims:sourcedId>LIS_TEST_001_GC002</ims:sourcedId>
					SOAPElement refAgentInstanceID = sourcedGUID.addChildElement("refAgentInstanceID", "ims");
					refAgentInstanceID.addTextNode("?");
					
					SOAPElement sourcedId2 = sourcedGUID.addChildElement("sourcedId", "ims");
					sourcedId2.addTextNode("LIS_TEST_001_GC002");
					
					SOAPElement lineItem = lineItemRecord.addChildElement("lineItem", "ims");
					
					SOAPElement context = lineItem.addChildElement("context", "ims");
					
					//<ims:contextIdentifier>LIS_TEST_001</ims:contextIdentifier>
                    //<ims:contextType>CourseOffering</ims:contextType>
					SOAPElement contextIdentifier = context.addChildElement("contextIdentifier", "ims");
					contextIdentifier.addTextNode("LIS_TEST_001");
					
					SOAPElement contextType = context.addChildElement("contextType", "ims");
					contextType.addTextNode("CourseOffering");
					
					SOAPElement lineItemType = lineItem.addChildElement("lineItemType", "ims");
					
					SOAPElement lineItemTypeVocab = lineItemType.addChildElement("lineItemTypeVocabulary", "ims");
					lineItemTypeVocab.addTextNode("http://www.imsglobal.org/vdex/lis/omsv1p0/lineitemtypevocabularyv1p0.xml");
					
					/*
					 * <ims:lineItemTypeValue>
                           <ims:language>en</ims:language>
                           <ims:textString>Interim</ims:textString>
                       </ims:lineItemTypeValue>
					 */
					SOAPElement lineItemTypeValue = lineItemType.addChildElement("lineItemTypeValue", "ims");
					
					SOAPElement language = lineItemTypeValue.addChildElement("language", "ims");
					language.addTextNode("en");
					
					SOAPElement textString = lineItemTypeValue.addChildElement("textString", "ims");
					textString.addTextNode("Interim");
					
					/*
					 * <ims:resourceHandlerId>http://fake/LTI/handler/uri</ims:resourceHandlerId>
                       <!--Optional:-->
                       <ims:localeKey>some.resource.bundle.key.lineitem</ims:localeKey>
                       <!--Optional:-->
                       <ims:defaultDisplayName>testDefaultDisplayNameLineItem</ims:defaultDisplayName>
					 */
					
					SOAPElement resourceHandler = lineItemType.addChildElement("resourceHandler", "ims");
					resourceHandler.addTextNode("http://fake/LTI/handler/uri");
					
					SOAPElement localeKey = lineItemType.addChildElement("localeKey", "ims");
					localeKey.addTextNode("some.resource.bundle.key.lineitem");

					SOAPElement defaultDisplayName = lineItemType.addChildElement("defaultDisplayName", "ims");
					defaultDisplayName.addTextNode("testDefaultDisplayNameLineItem");
					
					/* 
					 * <ims:label>
                             <ims:language>en</ims:language>
                             <ims:textString>SIS Assign 2</ims:textString>
                       </ims:label>
					 */
					SOAPElement label = lineItem.addChildElement("label", "ims");
					
					SOAPElement labelLang = label.addChildElement("language", "ims");
					labelLang.addTextNode("en");
					
					SOAPElement labelText = label.addChildElement("textString", "ims");
					labelText.addTextNode("SIS Assign 2");
					
					return(soapBody);
	}
	
	/*
	 * method: parseResponse
	 * 
	 * Purpose: take the XML from the readResultsResponse, turn it into a String, and return it to the calling method.
	 */
	private static String parseResponse(SOAPMessage response, Boolean prettyPrint) {
		SOAPPart part;
		
		try {
			// Get the SOAP message portion of the reponse
			part = response.getSOAPPart();
		
			// Create a DOM source from the SOAP message
			DOMSource source = new DOMSource(part);
			//Instantiate an empty StringWriter
			StringWriter stringResult = new StringWriter();
		
			//Transform the XML document into a String 
			TransformerFactory.newInstance().newTransformer().transform(source, new StreamResult(stringResult));
			
			// If pretty print is requested by the calling method, run through XMLHelper.prettyPrint and return the results, otherwise, return as-is.
			if(prettyPrint) {
				return(XMLHelper.prettyPrint(stringResult.toString()));
			}
			else {
				return(stringResult.toString());
			}
		} catch (TransformerConfigurationException e) {
			return(e.getMessage());
		} catch (TransformerException e) {
			return(e.getMessage());
		} catch (TransformerFactoryConfigurationError e) {
			return(e.getMessage());
		}
	}
}