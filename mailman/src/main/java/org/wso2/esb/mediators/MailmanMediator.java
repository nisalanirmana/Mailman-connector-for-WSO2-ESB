package org.wso2.esb.mediators;

import org.apache.synapse.transport.nhttp.NhttpConstants;
import org.apache.synapse.Mediator;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.SynapseException;
import org.apache.synapse.SynapseLog;
import org.apache.synapse.commons.json.JsonUtil;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.endpoints.AddressEndpoint;
import org.apache.synapse.endpoints.Endpoint;
import org.apache.synapse.endpoints.EndpointDefinition;
import org.apache.synapse.mediators.AbstractMediator;
import org.apache.synapse.util.MessageHelper;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMDocument;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import java.util.HashMap;
import org.apache.axiom.soap.SOAPBody;
import java.util.Iterator;
import org.apache.axiom.soap.SOAPEnvelope;
import java.io.StringReader;
import java.util.Iterator;
import java.util.HashMap;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
public class MailmanMediator extends AbstractMediator implements Mediator  {
    
    private ConfigurationContext configCtx = null;
    private String serviceURL = null;    
    private String clientRepository = null;
    private String axis2xml = null;    
    private Endpoint endpoint;    
    public final static HashMap<String, String> HTTP_STATUS_CODE_DESCRIPTION = new HashMap<String, String>();
    public final static String DEFAULT_CLIENT_REPO = "./repository/deployment/client";
    public final static String DEFAULT_AXIS2_XML = "./repository/conf/axis2/axis2_blocking_client.xml";
    private boolean isWrappingEndpointCreated = false;
    BlockingMsgSender blockingMsgSender = null;
    
    static {
    	
    	HTTP_STATUS_CODE_DESCRIPTION.put("201","Created");
    	HTTP_STATUS_CODE_DESCRIPTION.put("202","Accepted");
    	HTTP_STATUS_CODE_DESCRIPTION.put("204","No Response");
    	HTTP_STATUS_CODE_DESCRIPTION.put("400","Bad Request");
    	HTTP_STATUS_CODE_DESCRIPTION.put("401","Unauthorised");
        HTTP_STATUS_CODE_DESCRIPTION.put("403","Forbidden");
    	HTTP_STATUS_CODE_DESCRIPTION.put("404","Not Found");
    	HTTP_STATUS_CODE_DESCRIPTION.put("500","Internal Error");
        HTTP_STATUS_CODE_DESCRIPTION.put("503","Service Unavailable");
    }
    

    public boolean mediate(MessageContext synCtx) {
           
    	
    	serviceURL=(String)synCtx.getProperty("serviceURL");      	
    	init();  	
        SynapseLog synLog = getLog(synCtx);

        if (synLog.isTraceOrDebugEnabled()) {
            synLog.traceOrDebug("Start : class mediator");

            if (synLog.isTraceTraceEnabled()) {
                synLog.traceTrace("Message : " + synCtx.getEnvelope());
            }
        }

        try {

                       
            if (synLog.isTraceOrDebugEnabled()) {               
                    if (serviceURL != null) {
                        synLog.traceOrDebug("Using the serviceURL : " + serviceURL);
                    } 
            }

   
            MessageContext synapseOutMsgCtx = MessageHelper.cloneMessageContext(synCtx);
             

            if (synLog.isTraceOrDebugEnabled()) {
                synLog.traceOrDebug("About to invoke the service");
                if (synLog.isTraceTraceEnabled()) {
                    synLog.traceTrace("Request message payload : " + synapseOutMsgCtx.getEnvelope());
                }
            }
                       
            MessageContext resultMsgCtx = null;
            try {
                if ("true".equals(synCtx.getProperty(SynapseConstants.OUT_ONLY))) {
                    blockingMsgSender.send(endpoint, synapseOutMsgCtx);
                } else {
                    resultMsgCtx = blockingMsgSender.send(endpoint, synapseOutMsgCtx);
                    if ("true".equals(resultMsgCtx.getProperty(SynapseConstants.BLOCKING_SENDER_ERROR))) {
                      System.out.println(resultMsgCtx.getProperty(SynapseConstants.ERROR_EXCEPTION).toString());
                        if(!(resultMsgCtx.getProperty(SynapseConstants.ERROR_EXCEPTION) instanceof AxisFault)){
                          handleFault(synCtx, (Exception) resultMsgCtx.getProperty(SynapseConstants.ERROR_EXCEPTION));                  
                       } 
                       org.apache.axis2.context.MessageContext axis2MessageContext = ((Axis2MessageContext)synCtx).getAxis2MessageContext();                        
                       System.out.println("++++++++++++");
                     //if(synCtx.getEnvelope()!=null){      
                       // System.out.println(synCtx.getEnvelope().toString());     
                       // System.out.println(synCtx.getEnvelope().getFirstElement().toString());     
                       // synCtx.getEnvelope().getHeader().getFirstElement().detach();  
                       // synCtx.getEnvelope().getBody().getFirstElement().detach();  
                       // System.out.println(synCtx.getEnvelope().toString()); 
                        //synCtx.setEnvelope(null);            
                    // }  
                      // SOAPBody soapBody = synCtx.getEnvelope().getBody();
                       //for (Iterator itr = soapBody.getChildElements(); itr.hasNext(); ) {
                        //  OMElement child = (OMElement) itr.next();
                        //  child.detach();
                       //}
                       //SOAPEnvelope envelope = OMAbstractFactory.getSOAP11Factory().getDefaultEnvelope();
                       //OMDocument omDoc = OMAbstractFactory.getSOAP11Factory().createOMDocument();
                       //omDoc.addChild(envelope);
                    
                       //XMLStreamReader parser = XMLInputFactory.newInstance().
                       //        createXMLStreamReader(new StringReader("lklsd"));
                       //StAXOMBuilder builder = new StAXOMBuilder(parser);
                    
                       // set a dummy static message
                      // envelope.getBody().addChild(builder.getDocumentElement());
                      // synCtx.setEnvelope(envelope);
                       System.out.println("++++++++++++");
                       System.out.println(synCtx.getEnvelope().toString());  
                       System.out.println("++++++++++++");
                       if(resultMsgCtx.getProperty(SynapseConstants.ERROR_EXCEPTION).toString().equals("org.apache.axis2.AxisFault: The input stream for an incoming message is null.")){
                          resultMsgCtx.setProperty(SynapseConstants.HTTP_SENDER_STATUSCODE,"400"); 
                          axis2MessageContext.setProperty(NhttpConstants.HTTP_SC,"400");                     
                          synLog.traceOrDebug("org.apache.axis2.AxisFault: The input stream for an incoming message is null.");
                       }
                       if(resultMsgCtx.getProperty(SynapseConstants.ERROR_EXCEPTION).toString().equals("org.apache.axis2.AxisFault: Transport error: 404 Error: Not Found")){
                           resultMsgCtx.setProperty(SynapseConstants.HTTP_SENDER_STATUSCODE,"404"); 
                           axis2MessageContext.setProperty(NhttpConstants.HTTP_SC,"404");                     
                           synLog.traceOrDebug("org.apache.axis2.AxisFault: Transport error: 404 Error: Not Found");
                        }
  
                          if(resultMsgCtx.getProperty(SynapseConstants.ERROR_EXCEPTION).toString().equals("org.apache.axis2.AxisFault: InputStream cannot be NULL.")){
                           resultMsgCtx.setProperty(SynapseConstants.HTTP_SENDER_STATUSCODE,"204");    
                           axis2MessageContext.setProperty(NhttpConstants.HTTP_SC,"204");                  
                           synLog.traceOrDebug("org.apache.axis2.AxisFault: InputStream cannot be NULL.");
                        } 
                       if(resultMsgCtx.getProperty(SynapseConstants.ERROR_EXCEPTION).toString().equals("org.apache.axis2.AxisFault: Transport error: 403 Error: Forbidden")){
                           resultMsgCtx.setProperty(SynapseConstants.HTTP_SENDER_STATUSCODE,"403");    
                           axis2MessageContext.setProperty(NhttpConstants.HTTP_SC,"403");
                           System.out.println("++++++++++++");
                           System.out.println(resultMsgCtx.getEnvelope().toString());
                           System.out.println(resultMsgCtx.getProperty(SynapseConstants.ERROR_EXCEPTION).toString());                  
                           synLog.traceOrDebug("org.apache.axis2.AxisFault: Transport error: 403 Error: Forbidden");
                        }
                         if(resultMsgCtx.getProperty(SynapseConstants.ERROR_EXCEPTION).toString().equals("org.apache.axis2.AxisFault: Transport error: 401 Error: Unauthorized")){
                           System.out.println(resultMsgCtx.getProperty(SynapseConstants.ERROR_EXCEPTION).toString());
                           System.out.println("++++++++++++");
                           System.out.println(resultMsgCtx.getEnvelope().toString());
                           resultMsgCtx.setProperty(SynapseConstants.HTTP_SENDER_STATUSCODE,"401");
                           axis2MessageContext.setProperty(NhttpConstants.HTTP_SC,"401");                      
                           synLog.traceOrDebug("org.apache.axis2.AxisFault: Transport error: 401 Error: Unauthorized");
                        }
                         if(resultMsgCtx.getProperty(SynapseConstants.ERROR_EXCEPTION).toString().equals("org.apache.axis2.AxisFault: Connection refused")){
                           System.out.println(resultMsgCtx.getProperty(SynapseConstants.ERROR_EXCEPTION).toString());
                           resultMsgCtx.setProperty(SynapseConstants.HTTP_SENDER_STATUSCODE,"503");
                           axis2MessageContext.setProperty(NhttpConstants.HTTP_SC,"503");                      
                           synLog.traceOrDebug("org.apache.axis2.AxisFault: Connection refused");
                        }
                    }
                }
            } catch (Exception ex) {
                handleFault(synCtx, ex);
            } 
                
            if (synLog.isTraceTraceEnabled()) {
                synLog.traceTrace("Response payload received : " + resultMsgCtx.getEnvelope());

            }
             
            if(!("200".equals(resultMsgCtx.getProperty(SynapseConstants.HTTP_SENDER_STATUSCODE).toString()))){                        
               // setPayload(synCtx,resultMsgCtx);
            }
     
            if("200".equals(resultMsgCtx.getProperty(SynapseConstants.HTTP_SENDER_STATUSCODE).toString())&&resultMsgCtx.getEnvelope() == null){            	
              //  setPayload(synCtx,resultMsgCtx);
            }       
                 
                           
            if (resultMsgCtx != null&&resultMsgCtx.getEnvelope() != null&&("200".equals(resultMsgCtx.getProperty(SynapseConstants.HTTP_SENDER_STATUSCODE).toString()))) {
              org.apache.axis2.context.MessageContext mc = ((Axis2MessageContext) resultMsgCtx).getAxis2MessageContext();
              if (JsonUtil.hasAJsonPayload(mc)) {
                  JsonUtil.cloneJsonPayload(mc, ((Axis2MessageContext) synCtx).getAxis2MessageContext());
              }else {
                       synCtx.setEnvelope(resultMsgCtx.getEnvelope());                     
                    }
            } else {
                synLog.traceOrDebug("Service returned a null response");
            }
             
        } catch (AxisFault e) {
            
            handleException("Error invoking service : " + serviceURL, e, synCtx);
            System.out.println(e);
        } 
        System.out.println("##########");       
        synLog.traceOrDebug("End : class mediator");
        return true;
    }

    private void handleFault(MessageContext synCtx, Exception ex) {
         
       synCtx.setProperty(SynapseConstants.SENDING_FAULT, Boolean.TRUE);

        if (ex instanceof AxisFault) {
            AxisFault axisFault = (AxisFault) ex;

            if (axisFault.getFaultCodeElement() != null) {
                synCtx.setProperty(SynapseConstants.ERROR_CODE,
                                   axisFault.getFaultCodeElement().getText());
            } else {
                synCtx.setProperty(SynapseConstants.ERROR_CODE,
                                   SynapseConstants.CALLOUT_OPERATION_FAILED);
            }

            if (axisFault.getMessage() != null) {
                synCtx.setProperty(SynapseConstants.ERROR_MESSAGE,
                                   axisFault.getMessage());
            } else {
                synCtx.setProperty(SynapseConstants.ERROR_MESSAGE, "Error while performing " +
                                                                   "the class mediator operation");
            }

            if (axisFault.getFaultDetailElement() != null) {
                if (axisFault.getFaultDetailElement().getFirstElement() != null) {
                    synCtx.setProperty(SynapseConstants.ERROR_DETAIL,
                                       axisFault.getFaultDetailElement().getFirstElement());
                } else {
                    synCtx.setProperty(SynapseConstants.ERROR_DETAIL,
                                       axisFault.getFaultDetailElement().getText());
                }
            }
        }

        synCtx.setProperty(SynapseConstants.ERROR_EXCEPTION, ex);
        throw new SynapseException("Error while performing the class mediator operation", ex);
    }

   
    public void init() {    	
        try {
             //Thread.sleep(40000);
             ClassLoader classLoader =Thread.currentThread().getContextClassLoader();
             java.net.URL Axis2ConfigUrl = classLoader.getResource("axis2_blocking_client_mailman.xml");
             axis2xml=Axis2ConfigUrl.getPath();

             this.configCtx = ConfigurationContextFactory.createConfigurationContextFromFileSystem(
             clientRepository != null ? clientRepository : DEFAULT_CLIENT_REPO,
             axis2xml != null ? axis2xml : DEFAULT_AXIS2_XML);
   
 
            blockingMsgSender = new BlockingMsgSender();
            blockingMsgSender.setConfigurationContext(configCtx);
            blockingMsgSender.init();          
            EndpointDefinition endpointDefinition = null;

            if (serviceURL != null) {                
                endpoint = new AddressEndpoint();
                endpointDefinition = new EndpointDefinition();
                endpointDefinition.setAddress(serviceURL);
                ((AddressEndpoint) endpoint).setDefinition(endpointDefinition);
                isWrappingEndpointCreated = true;
            } 
           
        } catch (AxisFault e) {
            String msg = "Error initializing class mediator : " + e.getMessage();
            log.error(msg, e);
            throw new SynapseException(msg, e);
        } catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
    }   
    public void setPayload(MessageContext synCtx,MessageContext resultMsgCtx){
    	  
          OMFactory factory = OMAbstractFactory.getOMFactory();          
	  OMNamespace ns = factory.createOMNamespace("http://org.wso2.esb.mediators.mailman", "ns");
	  OMElement response= factory.createOMElement("Response",ns);
	  OMElement result= factory.createOMElement("HTTP_Status",ns);
          OMElement result_code= factory.createOMElement("HTTP_Status_Code",ns);
          OMElement result_code_des= factory.createOMElement("HTTP_Status_Description",ns);
         
          result_code.setText(resultMsgCtx.getProperty(SynapseConstants.HTTP_SENDER_STATUSCODE).toString());
          result_code_des.setText(HTTP_STATUS_CODE_DESCRIPTION.get(resultMsgCtx.getProperty(SynapseConstants.HTTP_SENDER_STATUSCODE).toString()));
          
	  result.addChild(result_code);
          result.addChild(result_code_des);
          
          OMElement request=null;        
          System.out.println(synCtx.getEnvelope().toString());
          if(synCtx.getEnvelope()!=null){            
             request=synCtx.getEnvelope().getBody().getFirstElement();  
             if(request!=null){        
             request.detach(); 
             }
          }  
         //System.out.println(resultMsgCtx.getEnvelope().toString());
         // if(resultMsgCtx.getEnvelope()!=null){ 
         //    request=resultMsgCtx.getEnvelope().getBody().getFirstElement();                      
          //}          
          if(request!=null){
          response.addChild(request);
          }
          response.addChild(result);          
          synCtx.getEnvelope().getBody().setFirstChild(response);           
          System.out.println(synCtx.getEnvelope().toString());   	
    	
    }
    
    public String getServiceURL() {
        return serviceURL;
    }

    public void setServiceURL(String serviceURL) {
        this.serviceURL = serviceURL;
    }    
    

    public String getClientRepository() {
        return clientRepository;
    }

    public void setClientRepository(String clientRepository) {
        this.clientRepository = clientRepository;
    }

    public String getAxis2xml() {
        return axis2xml;
    }

    public void setAxis2xml(String axis2xml) {
        this.axis2xml = axis2xml;
    }   

    /**
     * Get the defined endpoint
     *
     * @return endpoint
     */
    public Endpoint getEndpoint() {
        if (!isWrappingEndpointCreated) {
            return endpoint;
        }
        return null;
    }

    /**
     * Set the defined endpoint
     *
     * @param endpoint defined endpoint
     */
    public void setEndpoint(Endpoint endpoint) {
        this.endpoint = endpoint;
    }

}

