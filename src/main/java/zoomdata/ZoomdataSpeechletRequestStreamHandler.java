/**
    Copyright 2014-2015 Amazon.com, Inc. or its affiliates. All Rights Reserved.

    Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file except in compliance with the License. A copy of the License is located at

        http://aws.amazon.com/apache2.0/

    or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */
package zoomdata;

import com.amazon.speech.speechlet.lambda.SpeechletRequestStreamHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zoomdata.util.MiscUtil;
import zoomdata.util.ZoomdataUtil;

import java.util.HashSet;
import java.util.Set;

/**
 * This class could be the handler for an AWS Lambda function powering an Alexa Skills Kit
 * experience. To do this, simply set the handler field in the AWS Lambda console to
 * "session.SessionWorldSpeechletRequestStreamHandler" For this to work, you'll also need to build
 * this project using the {@code lambda-compile} Ant task and upload the resulting zip file to power
 * your function.
 */
public class ZoomdataSpeechletRequestStreamHandler extends SpeechletRequestStreamHandler {
    private static final Set<String> supportedApplicationIds;
    private static final Logger logger = LoggerFactory.getLogger(ZoomdataSpeechletRequestStreamHandler.class);
    static {
        /*
         * This Id can be found on https://developer.amazon.com/edw/home.html#/ "Edit" the relevant
         * Alexa Skill and put the relevant Application Ids in this Set.
         */
        supportedApplicationIds = new HashSet<>();
        // application id from aws console: amzn1.echo-sdk-ams.app.5eda19bc-7f82-4e42-beb2-36364a563539

        supportedApplicationIds.add("amzn1.echo-sdk-ams.app.5eda19bc-7f82-4e42-beb2-36364a563539");
    }

    public ZoomdataSpeechletRequestStreamHandler() {
        super(new ZoomdataSpeechlet(), supportedApplicationIds);
    }

    public static void main(String args[]) {
        try {
            final String data = MiscUtil.GetDataFromFileName("/Dashboard_Template_BAR.json");
            logger.info("data=" + data);
            System.out.println("data=" + data);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
