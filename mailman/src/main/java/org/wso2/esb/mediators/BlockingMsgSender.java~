package org.wso2.esb.mediators;
/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.OperationClient;
import org.apache.axis2.client.Options;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.context.ServiceGroupContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisServiceGroup;
import org.apache.axis2.description.WSDL2Constants;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.SynapseException;
import org.apache.synapse.commons.json.JsonUtil;
import org.apache.synapse.core.axis2.AnonymousServiceFactory;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.endpoints.AbstractEndpoint;
import org.apache.synapse.endpoints.Endpoint;
import org.apache.synapse.endpoints.EndpointDefinition;
import org.apache.synapse.transport.nhttp.NhttpConstants;
import org.apache.synapse.util.MessageHelper;

import javax.xml.namespace.QName;

public class BlockingMsgSender {
    public final static String DEFAULT_CLIENT_REPO = "./repository/deployment/client";
    public final static String DEFAULT_AXIS2_XML = "./repository/conf/axis2/axis2_blocking_client.xml";

    private static Log log = LogFactory.getLog(BlockingMsgSender.class);
    private String clientRepository = null;
    private String axis2xml = null;
    private ConfigurationContext configurationContext = null;
    boolean initClientOptions = true;

    public void init() {
        try {
            if (configurationContext == null) {
                configurationContext
                        = ConfigurationContextFactory.createConfigurationContextFromFileSystem(
                        clientRepository != null ? clientRepository : DEFAULT_CLIENT_REPO,
                        axis2xml != null ? axis2xml : DEFAULT_AXIS2_XML);
            }
        } catch (AxisFault e) {
            handleException("Error initializing BlockingMessageSender", e);
        }
    }

    public MessageContext send(Endpoint endpoint, MessageContext synapseInMsgCtx)
            throws Exception {
        System.out.println("######1");
        if (log.isDebugEnabled()) {
            log.debug("Start Sending the Message ");
        }
        System.out.println("######2");
        AbstractEndpoint abstractEndpoint = (AbstractEndpoint) endpoint;
        if (!abstractEndpoint.isLeafEndpoint()) {
            handleException("Endpoint Type not supported");
        }
        abstractEndpoint.executeEpTypeSpecificFunctions(synapseInMsgCtx);
        EndpointDefinition endpointDefinition = abstractEndpoint.getDefinition();

        org.apache.axis2.context.MessageContext axisInMsgCtx =
                ((Axis2MessageContext) synapseInMsgCtx).getAxis2MessageContext();
        org.apache.axis2.context.MessageContext axisOutMsgCtx =
                new org.apache.axis2.context.MessageContext();

        String endpointReferenceValue = null;
        if (endpointDefinition.getAddress() != null) {
            endpointReferenceValue = endpointDefinition.getAddress();
        } else if (axisInMsgCtx.getTo() != null) {
            endpointReferenceValue = axisInMsgCtx.getTo().getAddress();
        } else {
            handleException("Service url, Endpoint or 'To' header is required");
        }
        axisOutMsgCtx.setTo(new EndpointReference(endpointReferenceValue));
        System.out.println("######3");
        if (endpointReferenceValue != null &&
            endpointReferenceValue.startsWith(Constants.TRANSPORT_LOCAL)) {
            configurationContext = axisInMsgCtx.getConfigurationContext();
        }

        axisOutMsgCtx.setConfigurationContext(configurationContext);
        axisOutMsgCtx.setEnvelope(axisInMsgCtx.getEnvelope());

        // Fill MessageContext
        BlockingMsgSenderUtils.fillMessageContext(endpointDefinition, axisOutMsgCtx, synapseInMsgCtx);
       if (axisInMsgCtx.getEnvelope() != null) {
        if (JsonUtil.hasAJsonPayload(axisInMsgCtx)) {
            System.out.println("######90");
            JsonUtil.cloneJsonPayload(axisInMsgCtx, axisOutMsgCtx);
        }
        }
        Options clientOptions;
        if (initClientOptions) {
            clientOptions = new Options();
        } else {
            clientOptions = axisInMsgCtx.getOptions();
        }
        // Fill Client options
        BlockingMsgSenderUtils.fillClientOptions(endpointDefinition, clientOptions, synapseInMsgCtx);
        System.out.println("######4");
        AxisService anonymousService =
                AnonymousServiceFactory.getAnonymousService(
                        null,
                        configurationContext.getAxisConfiguration(),
                        endpointDefinition.isAddressingOn() | endpointDefinition.isReliableMessagingOn(),
                        endpointDefinition.isReliableMessagingOn(),
                        endpointDefinition.isSecurityOn(),
                        false);
        anonymousService.getParent().addParameter(SynapseConstants.HIDDEN_SERVICE_PARAM, "true");
        ServiceGroupContext serviceGroupContext =
                new ServiceGroupContext(configurationContext,
                                        (AxisServiceGroup) anonymousService.getParent());
        ServiceContext serviceCtx = serviceGroupContext.getServiceContext(anonymousService);
        axisOutMsgCtx.setServiceContext(serviceCtx);
         System.out.println("######5");
        // Invoke
        boolean isOutOnly = isOutOnly(synapseInMsgCtx, axisOutMsgCtx);
        try {
            if (isOutOnly) {
                sendRobust(axisOutMsgCtx, clientOptions, anonymousService, serviceCtx);
            } else {
            	System.out.println("######6");
                org.apache.axis2.context.MessageContext result =
                        sendReceive(axisOutMsgCtx, clientOptions, anonymousService, serviceCtx);
                System.out.println("######7");
                synapseInMsgCtx.setEnvelope(result.getEnvelope());
                System.out.println("#####8#");
                if (result.getEnvelope() != null) {
                if (JsonUtil.hasAJsonPayload(result)) {
                System.out.println("#####560");
               	JsonUtil.cloneJsonPayload(result, ((Axis2MessageContext) synapseInMsgCtx).getAxis2MessageContext());
                }
                }                
                System.out.println("######9");
                synapseInMsgCtx.setProperty(SynapseConstants.HTTP_SENDER_STATUSCODE,
                                            result.getProperty(SynapseConstants.HTTP_SENDER_STATUSCODE));
               System.out.println(result.getProperty(SynapseConstants.HTTP_SENDER_STATUSCODE));
                System.out.println("######10");
                synapseInMsgCtx.setProperty(SynapseConstants.BLOCKING_SENDER_ERROR, "false");
                System.out.println("######11");
                return synapseInMsgCtx;
            }
            } catch (Exception ex) {
            if (!isOutOnly) {
                System.out.println("######0000000000001");
                //axisOutMsgCtx.getTransportOut().getSender().cleanup(axisOutMsgCtx);
                synapseInMsgCtx.setProperty(SynapseConstants.BLOCKING_SENDER_ERROR, "true");
                System.out.println(ex);
                if (ex instanceof AxisFault) {
                    System.out.println("######000000000000234");
                    AxisFault fault = (AxisFault) ex;
                    System.out.println(ex);
                    synapseInMsgCtx.setProperty(SynapseConstants.ERROR_CODE,
                                                fault.getFaultCode() != null ?
                                                fault.getFaultCode().getLocalPart() : "");
                    System.out.println("######000000000000234");
                    synapseInMsgCtx.setProperty(SynapseConstants.ERROR_MESSAGE, fault.getMessage());
                    System.out.println("######000000000000234");
                    synapseInMsgCtx.setProperty(SynapseConstants.ERROR_DETAIL,
                                                fault.getDetail() != null ?
                                                fault.getDetail().getText() : "");
                    System.out.println("######000000000000234");
                    synapseInMsgCtx.setProperty(SynapseConstants.ERROR_EXCEPTION, ex);
                    System.out.println("######000000000000234");
                    org.apache.axis2.context.MessageContext faultMC = fault.getFaultMessageContext();
                    System.out.println("######000000000000234");
                    //System.out.println(faultMC.getProperty(SynapseConstants.HTTP_SENDER_STATUSCODE));
                    if (faultMC != null) {
                        System.out.println("######0000000000002");
                        synapseInMsgCtx.setProperty(NhttpConstants.HTTP_SC,
                                                    faultMC.getProperty(
                                                            SynapseConstants.HTTP_SENDER_STATUSCODE));
                        synapseInMsgCtx.setEnvelope(faultMC.getEnvelope());
                  
  }
                }
                return synapseInMsgCtx;
            }
            handleException("Error sending Message to url : " +
                            ((AbstractEndpoint) endpoint).getDefinition().getAddress(), ex);
        }
        return null;
    }

    private void sendRobust(org.apache.axis2.context.MessageContext axisOutMsgCtx,
                            Options clientOptions, AxisService anonymousService,
                            ServiceContext serviceCtx) throws AxisFault {

        AxisOperation axisAnonymousOperation =
                anonymousService.getOperation(new QName(AnonymousServiceFactory.OUT_ONLY_OPERATION));
        OperationClient operationClient =
                axisAnonymousOperation.createClient(serviceCtx, clientOptions);
        
        Options chunk_options = new Options();
        chunk_options.setProperty(org.apache.axis2.transport.http.HTTPConstants.CHUNKED, Boolean.FALSE);        
        chunk_options.setProperty(Constants.Configuration.MESSAGE_TYPE,HTTPConstants.MEDIA_TYPE_APPLICATION_ECHO_XML);
        chunk_options.setProperty(Constants.Configuration.DISABLE_SOAP_ACTION,Boolean.TRUE);
        operationClient.setOptions(chunk_options);
        
        operationClient.addMessageContext(axisOutMsgCtx);
        axisOutMsgCtx.setAxisMessage(
                axisAnonymousOperation.getMessage(WSDLConstants.MESSAGE_LABEL_OUT_VALUE));
        operationClient.execute(true);
        axisOutMsgCtx.getTransportOut().getSender().cleanup(axisOutMsgCtx);

    }

    private org.apache.axis2.context.MessageContext sendReceive(
            org.apache.axis2.context.MessageContext axisOutMsgCtx,
            Options clientOptions,
            AxisService anonymousService,
            ServiceContext serviceCtx)
            throws AxisFault {
         System.out.println("######21");
        AxisOperation axisAnonymousOperation =
                anonymousService.getOperation(new QName(AnonymousServiceFactory.OUT_IN_OPERATION));
        OperationClient operationClient =
                axisAnonymousOperation.createClient(serviceCtx, clientOptions);
        System.out.println("######22");

        Options chunk_options = new Options();
        chunk_options.setProperty(org.apache.axis2.transport.http.HTTPConstants.CHUNKED, Boolean.FALSE);        
        chunk_options.setProperty(Constants.Configuration.MESSAGE_TYPE,HTTPConstants.MEDIA_TYPE_APPLICATION_ECHO_XML);
        chunk_options.setProperty(Constants.Configuration.DISABLE_SOAP_ACTION,Boolean.TRUE);
        operationClient.setOptions(chunk_options);
        


       System.out.println("######24");
        operationClient.addMessageContext(axisOutMsgCtx);
        axisOutMsgCtx.setAxisMessage(
                axisAnonymousOperation.getMessage(WSDLConstants.MESSAGE_LABEL_OUT_VALUE));
        operationClient.execute(true);
        System.out.println("######25");
        org.apache.axis2.context.MessageContext resultMsgCtx =
                operationClient.getMessageContext(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
        System.out.println("######27");
        org.apache.axis2.context.MessageContext returnMsgCtx =
                new org.apache.axis2.context.MessageContext();
        if (resultMsgCtx.getEnvelope() != null) {
            System.out.println("######899");
            returnMsgCtx.setEnvelope(MessageHelper.cloneSOAPEnvelope(resultMsgCtx.getEnvelope()));
        }
         System.out.println("######28");
      if (resultMsgCtx.getEnvelope() != null) {
       if (JsonUtil.hasAJsonPayload(resultMsgCtx)) {
           System.out.println("######900");
           JsonUtil.cloneJsonPayload(resultMsgCtx, returnMsgCtx);
       }        
      }
        System.out.println("######29");
        returnMsgCtx.setProperty(SynapseConstants.HTTP_SENDER_STATUSCODE,
                                 resultMsgCtx.getProperty(SynapseConstants.HTTP_SENDER_STATUSCODE));
        axisOutMsgCtx.getTransportOut().getSender().cleanup(axisOutMsgCtx);
        System.out.println("######29");
        return returnMsgCtx;
    }

    private boolean isOutOnly(MessageContext messageIn,
                              org.apache.axis2.context.MessageContext axis2Ctx) {
        return "true".equals(messageIn.getProperty(SynapseConstants.OUT_ONLY)) ||
               axis2Ctx.getOperationContext() != null &&
               WSDL2Constants.MEP_URI_IN_ONLY.equals(axis2Ctx.getOperationContext().
                       getAxisOperation().getMessageExchangePattern());
    }

    public void setClientRepository(String clientRepository) {
        this.clientRepository = clientRepository;
    }

    public void setAxis2xml(String axis2xml) {
        this.axis2xml = axis2xml;
    }

    public void setConfigurationContext(ConfigurationContext configurationContext) {
        this.configurationContext = configurationContext;
    }

    public void setInitClientOptions(boolean initClientOptions) {
        this.initClientOptions = initClientOptions;
    }

    private void handleException(String msg, Exception e) {
        log.error(msg, e);
        throw new SynapseException(msg, e);
    }

    private void handleException(String msg) {
        log.error(msg);
        throw new SynapseException(msg);
    }

}
