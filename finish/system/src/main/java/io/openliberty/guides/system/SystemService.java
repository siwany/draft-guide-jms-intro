// tag::copyright[]
/*******************************************************************************
 * Copyright (c) 2024 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
// end::copyright[]
package io.openliberty.guides.system;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Logger;
import jakarta.jms.JMSConnectionFactory;
import jakarta.jms.JMSContext;
import jakarta.jms.Queue;

import jakarta.annotation.Resource;
import jakarta.ejb.Schedule;
import jakarta.ejb.Singleton;

import io.openliberty.guides.models.SystemLoad;
import jakarta.inject.Inject;

@Singleton
public class SystemService {

    private static final OperatingSystemMXBean OS_MEAN =
            ManagementFactory.getOperatingSystemMXBean();
    private static String hostname = null;

    private static Logger logger = Logger.getLogger(SystemService.class.getName());
    
    @Inject
    @JMSConnectionFactory("InventoryQueueConnectionFactory")
    private JMSContext context;

    //tag::jms/InventoryQueue[]
    @Resource(lookup = "jms/InventoryQueue")
    private Queue queue;
    //end::jms/InventoryQueue[]

    private static String getHostname() {
        if (hostname == null) {
            try {
                return InetAddress.getLocalHost().getHostName();
            } catch (UnknownHostException e) {
                return System.getenv("HOSTNAME");
            }
        }
        return hostname;
    }

    // tag::schedule[]
    @Schedule(second = "*/15", minute = "*", hour = "*", persistent = false)
    // end::schedule[]
    // tag::sendSystemLoad[]
    public void sendSystemLoad() {
        //tag::SystemLoad[]
      SystemLoad systemLoad = new SystemLoad(getHostname(),
              Double.valueOf(OS_MEAN.getSystemLoadAverage()));
        //end::SystemLoad[]
      //tag::createProducer[]
      context.createProducer().send(queue, systemLoad.toString());
      //end::createProducer[]
      logger.info(systemLoad.toString());   
     }
    // end::sendSystemLoad[]
}
