/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.apache.vysper.console;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;

import org.apache.vysper.mina.TCPEndpoint;
import org.apache.vysper.storage.StorageProviderRegistry;
import org.apache.vysper.storage.inmemory.MemoryStorageProviderRegistry;
import org.apache.vysper.xmpp.addressing.Entity;
import org.apache.vysper.xmpp.addressing.EntityImpl;
import org.apache.vysper.xmpp.authorization.AccountCreationException;
import org.apache.vysper.xmpp.authorization.AccountManagement;
import org.apache.vysper.xmpp.modules.extension.xep0049_privatedata.PrivateDataModule;
import org.apache.vysper.xmpp.modules.extension.xep0050_adhoc_commands.AdhocCommandsModule;
import org.apache.vysper.xmpp.modules.extension.xep0054_vcardtemp.VcardTempModule;
import org.apache.vysper.xmpp.modules.extension.xep0092_software_version.SoftwareVersionModule;
import org.apache.vysper.xmpp.modules.extension.xep0119_xmppping.XmppPingModule;
import org.apache.vysper.xmpp.modules.extension.xep0133_service_administration.ServiceAdministrationModule;
import org.apache.vysper.xmpp.modules.extension.xep0202_entity_time.EntityTimeModule;
import org.apache.vysper.xmpp.server.XMPPServer;

/**
 * starts the server as a standalone application
 *
 * @author The Apache MINA Project (dev@mina.apache.org)
 */
public class ServerMain {

    /**
     * boots the server as a standalone application
     * 
     * adding a module from the command line:
     * using a runtime property, one or more modules can be specified, like this:
     * -Dvysper.add.module=org.apache.vysper.xmpp.modules.extension.xep0060_pubsub.PublishSubscribeModule,... more ...
     * 
     * @param args
     */
    public static void main(String[] args) throws AccountCreationException, FileNotFoundException {

        String domain = "vysper.org";
        
        // choose the storage you want to use
        StorageProviderRegistry providerRegistry = new MemoryStorageProviderRegistry();

        final Entity adminJID = EntityImpl.parseUnchecked("admin@" + domain);
        final AccountManagement accountManagement = (AccountManagement) providerRegistry
                .retrieve(AccountManagement.class);

        if (!accountManagement.verifyAccountExists(adminJID)) {
            accountManagement.addUser(adminJID, "password");
        }

        XMPPServer server = new XMPPServer(domain);
        server.addEndpoint(new TCPEndpoint());
        //server.addEndpoint(new StanzaSessionFactory());
        server.setStorageProviderRegistry(providerRegistry);

        server.setTLSCertificateInfo(new File("src/main/resources/bogus_mina_tls.cert"), "boguspw");

        try {
            server.start();
            System.out.println("vysper server is running...");
        } catch (Exception e) {
            e.printStackTrace();
        }

        server.addModule(new SoftwareVersionModule());
        server.addModule(new EntityTimeModule());
        server.addModule(new VcardTempModule());
        server.addModule(new XmppPingModule());
        server.addModule(new PrivateDataModule());
        server.addModule(new AdhocCommandsModule());
        final ServiceAdministrationModule serviceAdministrationModule = new ServiceAdministrationModule();
        // unless admin user account with a secure password is added, this will be not become effective
        serviceAdministrationModule.setAddAdminJIDs(Arrays.asList(adminJID)); 
        server.addModule(serviceAdministrationModule);
    }
}
