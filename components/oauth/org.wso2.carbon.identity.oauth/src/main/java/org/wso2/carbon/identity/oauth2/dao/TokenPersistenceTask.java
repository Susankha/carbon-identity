/*
*  Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.identity.oauth2.dao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.base.IdentityException;

import java.util.concurrent.BlockingDeque;

/**
 *
 */
public class TokenPersistenceTask implements Runnable {

    private static Log log = LogFactory.getLog(TokenPersistenceTask.class);
    private BlockingDeque<AccessContextTokenDO> accessContextTokenQueue;

    public TokenPersistenceTask(BlockingDeque<AccessContextTokenDO> accessContextTokenQueue) {
        this.accessContextTokenQueue = accessContextTokenQueue;
    }

    @Override
    public void run() {

        log.debug("Access Token context persist consumer is started");

        while (true) {
            AccessContextTokenDO accessContextTokenDO = null;
            try {
                accessContextTokenDO =  accessContextTokenQueue.take();
                if (accessContextTokenDO != null) {
                    if (accessContextTokenDO.getAccessToken() == null) {
                        log.debug("Access Token Data removing Task is started to run");
                        TokenMgtDAO tokenMgtDAO = new TokenMgtDAO();
                        tokenMgtDAO.removeAccessToken(accessContextTokenDO.getAccessToken());
                    } else {
                        log.debug("Access Token Data persisting Task is started to run");
                        TokenMgtDAO tokenMgtDAO = new TokenMgtDAO();
                        tokenMgtDAO.persistAccessToken(accessContextTokenDO.getAccessToken(), accessContextTokenDO.getConsumerKey(),
                                accessContextTokenDO.getAccessTokenDO(), accessContextTokenDO.getUserStoreDomain());
                    }
                }
            } catch (InterruptedException e) {
                log.error("Error occurred while persisting access token " +
                        accessContextTokenDO.getAccessToken(), e);
            } catch (IdentityException e) {
                log.error("Error occurred while persisting access token " +
                        accessContextTokenDO.getAccessToken(), e);
            }

        }
    }
}
