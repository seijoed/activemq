/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.activemq.camel.component;

import static org.apache.activemq.camel.component.ActiveMQComponent.activeMQComponent;

import java.util.Date;
import java.util.List;

import javax.jms.Destination;
import javax.jms.Message;

import org.apache.activemq.command.ActiveMQQueue;
import org.apache.camel.CamelContext;
import org.apache.camel.ContextTestSupport;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jms.JmsMessage;
import org.apache.camel.component.mock.AssertionClause;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @version $Revision$
 */
public class ActiveMQJmsHeaderRouteTest extends ContextTestSupport {
    private static final transient Log LOG = LogFactory.getLog(ActiveMQJmsHeaderRouteTest.class);

    protected Object expectedBody = "<time>" + new Date() + "</time>";
    protected ActiveMQQueue replyQueue = new ActiveMQQueue("test.reply.queue");
    protected String correlationID = "ABC-123";
    protected String messageType = getClass().getName();

    public void testForwardingAMessageAcrossJMSKeepingCustomJMSHeaders() throws Exception {
        MockEndpoint resultEndpoint = resolveMandatoryEndpoint("mock:result", MockEndpoint.class);

        resultEndpoint.expectedBodiesReceived(expectedBody);
        AssertionClause firstMessageExpectations = resultEndpoint.message(0);
        firstMessageExpectations.header("cheese").isEqualTo(123);
        firstMessageExpectations.header("JMSReplyTo").isEqualTo(replyQueue);
        firstMessageExpectations.header("JMSCorrelationID").isEqualTo(correlationID);
        firstMessageExpectations.header("JMSType").isEqualTo(messageType);

        template.sendBodyAndHeader("activemq:test.a", expectedBody, "cheese", 123);

        resultEndpoint.assertIsSatisfied();

        List<Exchange> list = resultEndpoint.getReceivedExchanges();
        Exchange exchange = list.get(0);
        Object replyTo = exchange.getIn().getHeader("JMSReplyTo");
        LOG.info("Reply to is: " + replyTo);
        Destination destination = assertIsInstanceOf(Destination.class, replyTo);
        assertEquals("ReplyTo", replyQueue.toString(), destination.toString());
    }

    protected CamelContext createCamelContext() throws Exception {
        CamelContext camelContext = super.createCamelContext();

        // START SNIPPET: example
        camelContext.addComponent("activemq", activeMQComponent("vm://localhost?broker.persistent=false"));
        // END SNIPPET: example

        return camelContext;
    }

    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            public void configure() throws Exception {
                from("activemq:test.a").process(new Processor() {
                    public void process(Exchange exchange) throws Exception {
                        // lets set the custom JMS headers using the JMS API
						JmsMessage jmsMessage = assertIsInstanceOf(JmsMessage.class, exchange.getIn());
						
                        jmsMessage.getJmsMessage().setJMSReplyTo(replyQueue);
                        jmsMessage.getJmsMessage().setJMSCorrelationID(correlationID);
                        jmsMessage.getJmsMessage().setJMSType(messageType);
                    }
                }).to("activemq:test.b?preserveMessageQos=true");

                from("activemq:test.b").to("mock:result");
            }
        };
    }
}