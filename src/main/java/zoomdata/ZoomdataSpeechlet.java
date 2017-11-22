/**
 Copyright 2014-2015 Amazon.com, Inc. or its affiliates. All Rights Reserved.

 Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file except in compliance with the License. A copy of the License is located at

 http://aws.amazon.com/apache2.0/

 or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */
package zoomdata;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.slu.Slot;
import com.amazon.speech.speechlet.*;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.amazon.speech.ui.Reprompt;
import com.amazon.speech.ui.SimpleCard;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zoomdata.util.*;

import javax.xml.bind.DatatypeConverter;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * This sample shows how to create a simple speechlet for handling intent requests and managing
 * session interactions.
 */
public class ZoomdataSpeechlet implements Speechlet {
    private static final Logger logger = LoggerFactory.getLogger(ZoomdataSpeechlet.class);


    final String sourceIdCommon = "5a0e1658e4b0e5f6a5084dd7";

    /*
    private static final String restUrl = "https://2-5-latest.zoomdata.com/zoomdata/service/";
    private static final String websocket = "wss://2-5-latest.zoomdata.com:8443/zoomdata/websocket";
    private static final String credential = "admin:Z00mda1a";
    */

    /*

    static private final String zoomdataHost = System.getenv("ZOOMDATA_HOST"); // "54.89.168.2";


    private static final String restUrl = "http://" + zoomdataHost + ":8080/zoomdata/service/";
    private static final String websocket = "ws://" + zoomdataHost + ":8443/zoomdata/websocket";
    private static final String credential = "admin:Z00mda1a";
    */

    private static final String startTime = "+2013-11-11 01:00:00.000";
    private static final String endTime = "+2013-11-11 03:59:59.999";


//    private static final String bookmarkUrl = restUrl + "bookmarks";


    // qluu/Changeme
//    private static final String websocket = "wss://customerdemo.zoomdata.com:8443/zoomdata/websocket";

//    private static final String bookmarkUrl = "https://customerdemo.zoomdata.com/zoomdata/service/bookmarks";


    //   private static final String credential = "admin:Z00mda1a";

    /*
    private static final Map<String, String> HEADERS = new HashMap<>();

    static {
        final String encoding = DatatypeConverter.printBase64Binary(credential.getBytes());
        HEADERS.put("Authorization", "Basic " + encoding);

    }*/

    private static final String COLOR_KEY = "COLOR";
    private static final String COLOR_SLOT = "Color";
    private static final String ATTRIBUTE_SLOT = "Attribute";
    private static final String METRIC_SLOT = "Metric";
    private static final String TIME_FILTER_SLOT = "TimeFilter";
    private static final String RANGE_TYPE_SLOT = "RangeType";
    private static final String CHART_TYPE_SLOT = "ChartType";
    private static final String COMPANY_SLOT = "Company";
    private static final String SYMBOL_SLOT = "Symbol";

    /*
    static public String GetSourceById(final String sourceId) throws Exception {
        try {
            final String serviceUrl = restUrl + "sources/" + sourceId;
            final String data = HttpUtil.sendGetWithHeader(serviceUrl, HEADERS);
            return data;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }*/


    static private final String REQUEST_TEMPLATE_BAR =
            "{\n" +
                    "  \"name\": \"__VIS_NAME__\",\n" +
                    "  \"description\": \"\",\n" +
                    "  \"layout\": \"unset\",\n" +
                    "  \"showDescription\": false,\n" +
                    "  \"visualizations\": [\n" +
                    "    {\n" +
                    "      \"id\": \"5a0e190ee4b0e5f6a5084e41\",\n" +
                    "      \"visId\": \"58b5e1c8e4b05f19ec13f9f4\",\n" +
                    "      \"name\": \"Bars\",\n" +
                    "      \"type\": \"UBER_BARS\",\n" +
                    "      \"widgetId\": \"a41f78dcbb2855db99c1ce3384e9ee3b\",\n" +
                    "      \"layout\": {\n" +
                    "        \"col\": 1,\n" +
                    "        \"row\": 1,\n" +
                    "        \"colSpan\": 4,\n" +
                    "        \"rowSpan\": 4\n" +
                    "      },\n" +
                    "      \"source\": {\n" +
                    "        \"variables\": {\n" +
                    "          \"Multi Group By\": [\n" +
                    "            {\n" +
                    "              \"name\": \"__ATTRIBUTE__\",\n" +
                    "              \"limit\": 50,\n" +
                    "              \"sort\": {\n" +
                    "                \"dir\": \"desc\",\n" +
                    "                \"name\": \"count\"\n" +
                    "              },\n" +
                    "              \"label\": \"__ATTRIBUTE__\",\n" +
                    "              \"type\": \"ATTRIBUTE\",\n" +
                    "              \"groupColorSet\": \"ZoomSequential\",\n" +
                    "              \"autoShowColorLegend\": false,\n" +
                    "              \"colorNumb\": 2,\n" +
                    "              \"autoColor\": true,\n" +
                    "              \"groupColors\": {}\n" +
                    "            }\n" +
                    "          ],\n" +
                    "          \"Chart Name\": \"__VIS_NAME__\",\n" +
                    "          \"Metric\": [\n" +
                    "            __METRIC_INFO__\n" +
                    "          ],\n" +
                    "          \"Bar Color\": [\n" +
                    "            {\n" +
                    "              \"name\": \"count\",\n" +
                    "              \"colorConfig\": {\n" +
                    "                \"colorNumb\": 3,\n" +
                    "                \"legendType\": \"palette\",\n" +
                    "                \"colors\": [\n" +
                    "                  {\n" +
                    "                    \"name\": \"Color 1\",\n" +
                    "                    \"color\": \"#ffc65f\"\n" +
                    "                  },\n" +
                    "                  {\n" +
                    "                    \"name\": \"Color 2\",\n" +
                    "                    \"color\": \"#9eb778\"\n" +
                    "                  },\n" +
                    "                  {\n" +
                    "                    \"name\": \"Color 3\",\n" +
                    "                    \"color\": \"#0096b6\"\n" +
                    "                  }\n" +
                    "                ],\n" +
                    "                \"colorSet\": \"ZoomSequential\",\n" +
                    "                \"autoShowColorLegend\": true,\n" +
                    "                \"autoColor\": true,\n" +
                    "                \"colorPositions\": null,\n" +
                    "                \"colorScaleType\": \"gradient\"\n" +
                    "              }\n" +
                    "            }\n" +
                    "          ],\n" +
                    "          \"Chart Description\": \"\",\n" +
                    "          \"UberBarsSettings\": {\n" +
                    "            \"showXGrid\": true,\n" +
                    "            \"showYGrid\": true,\n" +
                    "            \"chartType\": \"plain\",\n" +
                    "            \"chartOrientation\": \"vertical\",\n" +
                    "            \"thickness\": \"100\",\n" +
                    "            \"showAbsoluteValues\": false,\n" +
                    "            \"showRelativeValues\": false,\n" +
                    "            \"showGroupLabels\": true,\n" +
                    "            \"enableLogScale\": false\n" +
                    "          },\n" +
                    "          \"Font\": {\n" +
                    "            \"barLabels\": {\n" +
                    "              \"size\": \"normal\",\n" +
                    "              \"typeface\": \"sans\",\n" +
                    "              \"align\": \"left\",\n" +
                    "              \"style\": \"\"\n" +
                    "            },\n" +
                    "            \"title\": {\n" +
                    "              \"size\": \"normal\",\n" +
                    "              \"typeface\": \"sans\",\n" +
                    "              \"align\": \"left\",\n" +
                    "              \"style\": \"\"\n" +
                    "            }\n" +
                    "          },\n" +
                    "          \"Rulers\": {\n" +
                    "            \"gridlines\": {\n" +
                    "              \"X1grid\": false,\n" +
                    "              \"Y1grid\": true,\n" +
                    "              \"X2grid\": false,\n" +
                    "              \"Y2grid\": false\n" +
                    "            },\n" +
                    "            \"axis\": [\n" +
                    "              {\n" +
                    "                \"axis\": \"Metric\",\n" +
                    "                \"name\": \"Metric\",\n" +
                    "                \"metricsName\": \"__METRIC_NAME__\",\n" +
                    "                \"from\": 0,\n" +
                    "                \"to\": 10000000000,\n" +
                    "                \"step\": 2000000000,\n" +
                    "                \"fromAuto\": true,\n" +
                    "                \"toAuto\": true,\n" +
                    "                \"stepAuto\": true,\n" +
                    "                \"logScaleEnabled\": false,\n" +
                    "                \"type\": \"yAxis\"\n" +
                    "              }\n" +
                    "            ],\n" +
                    "            \"reflines\": []\n" +
                    "          },\n" +
                    "          \"_SubheadFiltersList\": true\n" +
                    "        },\n" +
                    "        \"filters\": [],\n" +
                    "        \"sourceId\": \"__SOURCE_ID__\",\n" +
                    "        \"sourceName\": \"Alexa_RTS_Source\",\n" +
                    "        \"sourceType\": \"EDC2\",\n" +
                    "        \"sparkIt\": false,\n" +
                    "        \"playbackMode\": false,\n" +
                    "        \"textSearchEnabled\": false,\n" +
                    "        \"live\": false\n" +
                    "      },\n" +
                    "      \"dashboardLink\": {\"inheritFilterCfg\": true},\n" +
                    "      \"controlsCfg\": {\n" +
                    "        \"id\": \"5a0e1a43e4b0e5f6a5084e46\",\n" +
                    "        \"dashboardId\": \"5a0e190ee4b0e5f6a5084e3f\",\n" +
                    "        \"visualizationDefId\": \"5a0e190ee4b0e5f6a5084e41\",\n" +
                    "        \"timeControlCfg\": {\n" +
                    "          \"from\": \"" + startTime + "\",\n" +
                    "          \"to\": \"" + endTime + "\",\n" +
                    "          \"timeField\": \"_ts\"\n" +
                    "        },\n" +
                    "        \"playerControlCfg\": {}\n" +
                    "      }\n" +
                    "    }\n" +
                    "  ],\n" +
                    "  \"shareState\": \"NOT_SHARED\",\n" +
                    "  \"rememberTime\": true,\n" +
                    "  \"unifiedBarCfgs\": [\n" +
                    "    {\n" +
                    "      \"id\": \"5a0e1a43e4b0e5f6a5084e46\",\n" +
                    "      \"dashboardId\": \"5a0e190ee4b0e5f6a5084e3f\",\n" +
                    "      \"timeControlCfg\": {\n" +
                    "        \"from\": \"" + startTime + "\",\n" +
                    "        \"to\": \"" + endTime + "\",\n" +
                    "        \"timeField\": \"_ts\"\n" +
                    "      },\n" +
                    "      \"playerControlCfg\": {},\n" +
                    "      \"widgetIds\": [\"a41f78dcbb2855db99c1ce3384e9ee3b\"],\n" +
                    "      \"visualizationDefId\": \"5a0e190ee4b0e5f6a5084e41\"\n" +
                    "    }\n" +
                    "  ],\n" +
                    "  \"type\": \"UBER_BARS\",\n" +
                    "  \"userId\": \"58b5e1c8e4b05f19ec13f9e1\",\n" +
                    "  \"viewsCount\": 0,\n" +
                    "  \"selectedWidgetId\": \"a41f78dcbb2855db99c1ce3384e9ee3b\",\n" +
                    "  \"ownerName\": \"admin\"\n" +
                    "}\n" +
                    "\n";


    static private final String REQUEST_TEMPLATE_PIE =
            "{\n" +
                    "  \"name\": \"__VIS_NAME__\",\n" +
                    "  \"description\": \"\",\n" +
                    "  \"layout\": \"unset\",\n" +
                    "  \"showDescription\": false,\n" +
                    "  \"visualizations\": [\n" +
                    "    {\n" +
                    "      \"id\": \"5a0e1cfce4b0e5f6a5084e55\",\n" +
                    "      \"visId\": \"58b5e1c8e4b05f19ec13fa0a\",\n" +
                    "      \"widgetId\": \"a41f78dcbb2855db99c1ce3384e9ee3b\",\n" +
                    "      \"dashboardLink\": {\"inheritFilterCfg\": true},\n" +
                    "      \"controlsCfg\": {\n" +
                    "        \"id\": \"5a0e1665e4b0e5f6a5084e28\",\n" +
                    "        \"visualizationDefId\": \"5a0e1665e4b0e5f6a5084e27\",\n" +
                    "        \"timeControlCfg\": {\n" +
                    "          \"from\": \"" + startTime + "\",\n" +
                    "          \"to\": \"" + endTime + "\",\n" +
                    "          \"timeField\": \"_ts\"\n" +
                    "        },\n" +
                    "        \"playerControlCfg\": {}\n" +
                    "      },\n" +
                    "      \"layout\": {\n" +
                    "        \"col\": 1,\n" +
                    "        \"row\": 1,\n" +
                    "        \"colSpan\": 4,\n" +
                    "        \"rowSpan\": 4\n" +
                    "      },\n" +
                    "      \"name\": \"Pie\",\n" +
                    "      \"type\": \"PIE\",\n" +
                    "      \"source\": {\n" +
                    "        \"variables\": {\n" +
                    "          \"Chart Name\": \"__VIS_NAME__\",\n" +
                    "          \"Size\": [\n" +
                    "            __METRIC_INFO__\n" +
                    "          ],\n" +
                    "          \"Chart Description\": \"\",\n" +
                    "          \"Group By\": {\n" +
                    "            \"name\": \"__ATTRIBUTE__\",\n" +
                    "            \"limit\": 50,\n" +
                    "            \"sort\": {\n" +
                    "              \"dir\": \"desc\",\n" +
                    "              \"name\": \"count\"\n" +
                    "            },\n" +
                    "            \"label\": \"__ATTRIBUTE__\",\n" +
                    "            \"type\": \"ATTRIBUTE\",\n" +
                    "            \"groupColorSet\": \"ZoomPalette\",\n" +
                    "            \"autoShowColorLegend\": true,\n" +
                    "            \"colorNumb\": 50,\n" +
                    "            \"autoColor\": true,\n" +
                    "            \"groupColors\": {\n" +
                    "              \"California\": \"#0095b7\",\n" +
                    "              \"Texas\": \"#a0b774\",\n" +
                    "              \"New York\": \"#f4c658\",\n" +
                    "              \"Florida\": \"#fe8b3e\",\n" +
                    "              \"Illinois\": \"#cf2f23\",\n" +
                    "              \"Pennsylvania\": \"#756c56\",\n" +
                    "              \"Ohio\": \"#007896\",\n" +
                    "              \"Georgia\": \"#47a694\",\n" +
                    "              \"Michigan\": \"#f9a94b\",\n" +
                    "              \"North Carolina\": \"#ff6b30\",\n" +
                    "              \"New Jersey\": \"#e94d29\",\n" +
                    "              \"Virginia\": \"#005b76\",\n" +
                    "              \"Washington\": \"#0095b7\",\n" +
                    "              \"Massachusetts\": \"#a0b774\",\n" +
                    "              \"Arizona\": \"#f4c658\",\n" +
                    "              \"Indiana\": \"#fe8b3e\",\n" +
                    "              \"Tennessee\": \"#cf2f23\",\n" +
                    "              \"Missouri\": \"#756c56\",\n" +
                    "              \"Maryland\": \"#007896\",\n" +
                    "              \"Wisconsin\": \"#47a694\",\n" +
                    "              \"Minnesota\": \"#f9a94b\",\n" +
                    "              \"Colorado\": \"#ff6b30\",\n" +
                    "              \"Alabama\": \"#e94d29\",\n" +
                    "              \"South Carolina\": \"#005b76\",\n" +
                    "              \"Louisiana\": \"#0095b7\",\n" +
                    "              \"Kentucky\": \"#a0b774\",\n" +
                    "              \"Oregon\": \"#f4c658\",\n" +
                    "              \"Oklahoma\": \"#fe8b3e\",\n" +
                    "              \"Connecticut\": \"#cf2f23\",\n" +
                    "              \"Iowa\": \"#756c56\",\n" +
                    "              \"Mississippi\": \"#007896\",\n" +
                    "              \"Arkansas\": \"#47a694\",\n" +
                    "              \"Kansas\": \"#f9a94b\",\n" +
                    "              \"Utah\": \"#ff6b30\",\n" +
                    "              \"Nevada\": \"#e94d29\",\n" +
                    "              \"New Mexico\": \"#005b76\",\n" +
                    "              \"West Virginia\": \"#0095b7\",\n" +
                    "              \"Nebraska\": \"#a0b774\",\n" +
                    "              \"Idaho\": \"#f4c658\",\n" +
                    "              \"Hawaii\": \"#fe8b3e\",\n" +
                    "              \"Maine\": \"#cf2f23\",\n" +
                    "              \"New Hampshire\": \"#756c56\",\n" +
                    "              \"Rhode Island\": \"#007896\",\n" +
                    "              \"Montana\": \"#47a694\",\n" +
                    "              \"Delaware\": \"#f9a94b\",\n" +
                    "              \"South Dakota\": \"#ff6b30\",\n" +
                    "              \"Alaska\": \"#e94d29\",\n" +
                    "              \"North Dakota\": \"#005b76\",\n" +
                    "              \"District of Columbia\": \"#0095b7\",\n" +
                    "              \"Vermont\": \"#a0b774\"\n" +
                    "            }\n" +
                    "          },\n" +
                    "          \"_SubheadFiltersList\": true,\n" +
                    "          \"UberBarsSettings\": {\n" +
                    "            \"showAbsoluteValues\": true,\n" +
                    "            \"showRelativeValues\": true,\n" +
                    "            \"showGroupLabels\": true\n" +
                    "          },\n" +
                    "          \"Font\": {\n" +
                    "            \"title\": {\n" +
                    "              \"size\": \"normal\",\n" +
                    "              \"typeface\": \"sans\",\n" +
                    "              \"align\": \"left\",\n" +
                    "              \"style\": \"\"\n" +
                    "            }\n" +
                    "          },\n" +
                    "          \"Rulers\": {\n" +
                    "            \"gridlines\": {\n" +
                    "              \"X1grid\": false,\n" +
                    "              \"Y1grid\": false,\n" +
                    "              \"X2grid\": false,\n" +
                    "              \"Y2grid\": false\n" +
                    "            },\n" +
                    "            \"axis\": [],\n" +
                    "            \"reflines\": []\n" +
                    "          }\n" +
                    "        },\n" +
                    "        \"sourceId\": \"__SOURCE_ID__\",\n" +
                    "        \"sourceName\": \"Alexa_RTS_Source\",\n" +
                    "        \"sourceType\": \"EDC2\",\n" +
                    "        \"sparkIt\": false,\n" +
                    "        \"playbackMode\": false,\n" +
                    "        \"textSearchEnabled\": false,\n" +
                    "        \"live\": false,\n" +
                    "        \"filters\": []\n" +
                    "      }\n" +
                    "    }\n" +
                    "  ],\n" +
                    "  \"shareState\": \"NOT_SHARED\",\n" +
                    "  \"rememberTime\": true,\n" +
                    "  \"unifiedBarCfgs\": [\n" +
                    "    {\n" +
                    "      \"id\": \"5a0e1665e4b0e5f6a5084e28\",\n" +
                    "      \"dashboardId\": \"5a0e1cfce4b0e5f6a5084e53\",\n" +
                    "      \"timeControlCfg\": {\n" +
                    "        \"from\": \"" + startTime + "\",\n" +
                    "        \"to\": \"" + endTime + "\",\n" +
                    "        \"timeField\": \"_ts\"\n" +
                    "      },\n" +
                    "      \"playerControlCfg\": {},\n" +
                    "      \"widgetIds\": [\"a41f78dcbb2855db99c1ce3384e9ee3b\"],\n" +
                    "      \"visualizationDefId\": \"5a0e1665e4b0e5f6a5084e27\"\n" +
                    "    }\n" +
                    "  ],\n" +
                    "  \"type\": \"UBER_BARS\",\n" +
                    "  \"userId\": \"58b5e1c8e4b05f19ec13f9e1\",\n" +
                    "  \"viewsCount\": 0,\n" +
                    "  \"selectedWidgetId\": \"a41f78dcbb2855db99c1ce3384e9ee3b\",\n" +
                    "  \"ownerName\": \"admin\"\n" +
                    "}";

    @Override
    public void onSessionStarted(final SessionStartedRequest request, final Session session)
            throws SpeechletException {
        logger.info("onSessionStarted requestId={}, sessionId={}", request.getRequestId(),
                session.getSessionId());
        // any initialization logic goes here
    }

    @Override
    public SpeechletResponse onLaunch(final LaunchRequest request, final Session session)
            throws SpeechletException {
        logger.info("onLaunch requestId={}, sessionId={}", request.getRequestId(),
                session.getSessionId());
        return getWelcomeResponse();
    }

    @Override
    public SpeechletResponse onIntent(final IntentRequest request, final Session session)
            throws SpeechletException {
        logger.info("onIntent requestId={}, sessionId={}", request.getRequestId(),
                session.getSessionId());

        // Get intent from the request object.
        Intent intent = request.getIntent();
        String intentName = (intent != null) ? intent.getName() : null;

        // Note: If the session is started with an intent, no welcome message will be rendered;
        // rather, the intent specific response will be returned.
        if ("ChartIntent".equals(intentName)) {
            //return getColorFromSession(intent, session);
            return doChartIntent(intent, session);

        } else if ("StockIntent".equals(intentName)) {
            return doStockIntent(intent, session);
            // throw new SpeechletException("Invalid Intent");

        } else if ("DistributionIntent".equals(intentName)) {
            return doDistributionIntent(intent, session);
            // throw new SpeechletException("Invalid Intent");

        } else if ("TopIntent".equals(intentName)) {
            return doTopIntent(intent, session);
            // throw new SpeechletException("Invalid Intent");

        } else if ("DebugIntent".equals(intentName)) {
            return doDebugIntent(intent, session);
            // throw new SpeechletException("Invalid Intent");

        } else if ("RealTimeIntent".equals(intentName)) {
            return doRealTimeIntent(intent, session);
            // throw new SpeechletException("Invalid Intent");

        } else if ("RealTimeIntentTrends".equals(intentName)) {
            return doRealTimeIntentTrends(intent, session);
            // throw new SpeechletException("Invalid Intent");

        } else if ("RealTimeIntentTweets".equals(intentName)) {
            return doRealTimeIntentTweets(intent, session);
            // throw new SpeechletException("Invalid Intent");

        } else if ("WelcomeIntent".equals(intentName)) {
            //return getColorFromSession(intent, session);
            return doWelcome(intent, session);
            //  return doWelcomeRealTime(intent, session);
            // return doWelcomeRealTweets(intent, session);
            // return doWelcomeRealTrends(intent,session);

        } else if ("WelcomeIntentRealtime".equals(intentName)) {
            //return getColorFromSession(intent, session);
            // return doWelcome(intent,session);
            return doWelcomeRealTime(intent, session);
            // return doWelcomeRealTweets(intent, session);
            // return doWelcomeRealTrends(intent,session);

        } else {
            throw new SpeechletException("Invalid Intent");
        }
    }

    @Override
    public void onSessionEnded(final SessionEndedRequest request, final Session session)
            throws SpeechletException {
        logger.info("onSessionEnded requestId={}, sessionId={}", request.getRequestId(),
                session.getSessionId());
        // any cleanup logic goes here
    }

    /**
     * Creates and returns a {@code SpeechletResponse} with a welcome message.
     *
     * @return SpeechletResponse spoken and visual welcome message
     */
    private SpeechletResponse getWelcomeResponse() {
        // Create the welcome message.
        String speechText =
                "Welcome to the Alexa-Zoomdata. You can ask me, for example, Alexa, tell zoomdata to build a bar chart for price based on gender";
        String repromptText = null;

        return getSpeechletResponse(speechText, repromptText, false);
    }

    /**
     * Creates a {@code SpeechletResponse} for the intent and stores the extracted color in the
     * Session.
     *
     * @param intent
     * intent for the request
     * @return SpeechletResponse spoken and visual response the given intent
     */

    static private Map<String, String> mapFieldToSpeech = new HashMap<>();
    static private Map<String, String> mapMetricToUnitSpeech = new HashMap<>();

    static {
        mapFieldToSpeech.put("usercity", "city");
        mapFieldToSpeech.put("userstate", "state");
        mapFieldToSpeech.put("plannedsales", "sales");
        mapFieldToSpeech.put("group_name", "group name");
        mapFieldToSpeech.put("*", "transactions");
        mapFieldToSpeech.put("sale_date", "sale date");
        mapFieldToSpeech.put("hasgtag_str", "trend");
        mapFieldToSpeech.put("usergender", "gender");


        mapMetricToUnitSpeech.put("sales", "dollars");
        mapMetricToUnitSpeech.put("plannedsales", "dollars");
        mapMetricToUnitSpeech.put("price", "dollars");

    }

    static private Map<String, String> mapSymbolToCompany = new HashMap<>();

    static {
        mapSymbolToCompany.put("fb", "Facebook");
        mapSymbolToCompany.put("aapl", "Apple");
        mapSymbolToCompany.put("msft", "Microsoft");
        mapSymbolToCompany.put("googl", "Google");
        mapSymbolToCompany.put("ua", "Under Armour");
        mapSymbolToCompany.put("twtr", "Twitter");

    }

    final String getMyStock(final String dateValue) throws Exception {

        final String url = "https://api.intrinio.com/prices?start_date=" + dateValue + "&end_date=" + dateValue + "&frequency=daily&identifier=";
        final String[] myStocks = {"fb", "ua", "twtr", "msft", "aapl", "googl"};

        final String credential = "4dc21a631a4e0f039382c4018a04d8a9:8b9577f457c8e2c108c005d7244f607d";

        final Map<String, String> HEADERS = new HashMap<>();

        StringBuilder speechBuilder = new StringBuilder();

        final String encoding = DatatypeConverter.printBase64Binary(credential.getBytes());
        HEADERS.put("Authorization", "Basic " + encoding);
        speechBuilder.append("For Date " + dateValue + ": ");
        for (final String symbol : myStocks) {
            final String companyName = mapSymbolToCompany.get(symbol);

            final String api = url + symbol.toUpperCase();
            try {

                final String data = HttpUtil.httpGetWithHeader(api, HEADERS);
                final JSONObject response = new JSONObject(data);
                final JSONArray jsonArrayData = response.getJSONArray("data");
                final JSONObject item = jsonArrayData.getJSONObject(0);
                item.remove("ex_dividend");
                item.remove("split_ratio");
                item.remove("adj_open");
                item.remove("adj_high");
                item.remove("adj_low");
                item.remove("adj_close");
                item.remove("adj_volume");
                item.remove("date");
                item.remove("volume");

                final Iterator it = item.keys();
                speechBuilder.append(" Company " + companyName).append(" ");
                while (it.hasNext()) {
                    final String key = it.next().toString();
                    speechBuilder.append(". ");
                    speechBuilder.append(key).append(" ").append(item.get(key));
                    speechBuilder.append(" . ");
                }
            } catch (Exception e) {
                throw new Exception("Got exception for api=" + api + ". e=" + e);
            }
            speechBuilder.append(" . ");
        }

        return speechBuilder.toString();
    }

    private SpeechletResponse doStockIntent(final Intent intent, final Session session) {
        // Get the slots from the intent.
        final Map<String, Slot> slots = intent.getSlots();

        // Get the color slot from the list of slots.
        Slot companySlot = slots.get(COMPANY_SLOT);
        Slot dateSlot = slots.get("DATE");
        String speechText;

        Slot symbolSlot = slots.get(SYMBOL_SLOT);

        String repromptText = null;
        String company = "Facebook";
        String companySpeech = "Face book";
        String symbol = "fb";

        if (dateSlot == null) {
            speechText = "Missing date value";
            return getSpeechletResponse(speechText, repromptText, false);

        }
        if (companySlot == null) {
            // Use default one.
            try {

                String dateValue = dateSlot.getValue();
                speechText = getMyStock(dateValue);

            } catch (Exception e) {
                speechText = "Got exception " + e;
            }
            return getSpeechletResponse(speechText, repromptText, false);
        }

        // Check for favorite color and create output to user.
        if (companySlot != null) {
            // Store the user's favorite color in the Session and create response.
            String companyName = companySlot.getValue();
            if (companyName.toLowerCase().contains("book")) {
                companyName = "facebook";
                symbol = "fb";
            } else if (companyName.toLowerCase().contains("apple")) {
                companyName = "apple";
                symbol = "appl";
            } else if (companyName.toLowerCase().contains("soft")) {
                companyName = "microsoft";
                symbol = "msft";
            } else if (companyName.toLowerCase().contains("google")) {

            }

            companySpeech = mapFieldToSpeech.get(companyName);
            if (StringUtils.isEmpty(companySpeech) == true) {
                companySpeech = companyName;
            }
        }

        speechText =
                String.format("You are asking about company %s & symbol %s ",
                        companySpeech, symbol);


        return getSpeechletResponse(speechText, repromptText, false);
    }

    private SpeechletResponse doChartIntent(final Intent intent, final Session session) {
        // Get the slots from the intent.
        final Map<String, Slot> slots = intent.getSlots();

        // Get the color slot from the list of slots.
        Slot attributeSlot = slots.get(ATTRIBUTE_SLOT);
        Slot metricSlot = slots.get(METRIC_SLOT);
        Slot chartTypeSlot = slots.get(CHART_TYPE_SLOT);
        String speechText;
        String repromptText = null;
        final String filters = null;
        final String timeRangeType = null;

        // Check for favorite color and create output to user.
        if (attributeSlot != null) {
            // Store the user's favorite color in the Session and create response.
            String attribute = attributeSlot.getValue();
            if (StringUtils.isEmpty(attribute)) {
                attribute = "usergender";
            } else if (attribute.contains("group")) {
                attribute = "group_name";
            } else if ((attribute.contains("city")) || (attribute.contains("citi")) || (attribute.contains("CD")) || (attribute.contains("ct"))) {
                attribute = "usercity";
            } else if ((attribute.contains("state")) || (attribute.contains("states"))) {
                attribute = "userstate";
            } else if (attribute.contains("income")) {
                attribute = "userincome";
            } else if (attribute.toLowerCase().contains("gender")) {
                attribute = "usergender";
            }
            String attributeSpeech = mapFieldToSpeech.get(attribute);
            if (StringUtils.isEmpty(attributeSpeech) == true) {
                attributeSpeech = attribute;
            }

            String metric = metricSlot.getValue();
            if (StringUtils.isEmpty(metric)) {
                metric = "sales";
            }

            String chartType = chartTypeSlot.getValue();
            if (StringUtils.isEmpty(chartType)) {
                chartType = "bar";
            }

            //  final String sourceName ="edc-smart-w-cratedb";
            session.setAttribute(COLOR_KEY, attribute);

            speechText =
                    String.format("You are asking to build %s chart for %s based on %s. ",
                            chartType, metric, attribute.replace("_", " "));
            String sourceId = sourceIdCommon;

            final String sourceIdEnv = System.getenv("SOURCE_ID");
            if (StringUtils.isEmpty(sourceIdEnv) == false) {
                sourceId = sourceIdEnv;
            }

            final ZoomdataEnv zoomdataEnv = getZoomdataEnv();
            try {

                if (metric.contains("sales")) {
                    metric = "plannedsales";
                }

                if (chartType.toLowerCase().equals("bar")) {
                    chartType = "BAR";
                } else {
                    chartType = "PIE";
                }
                final String function = "sum";

                final String bookmarkName = "Alexa_" + chartType + "_" + metric + "_" + attribute;

                zoomdataEnv.visPregen(bookmarkName, sourceId, chartType, metric, attribute);
                speechText += "Congratulations! Your chart is ready under the name  " + bookmarkName.replace("_", " ");

                final JSONObject jsonObject = zoomdataEnv.getData(metric, function, attribute, sourceId, filters, timeRangeType);
                final int size = jsonObject.getInt("available");

                String unitSpeech = mapMetricToUnitSpeech.get(metric);
                if (StringUtils.isEmpty(unitSpeech)) {
                    unitSpeech = metric;
                }

                speechText += " . I found " + size + " " + attributeSpeech.trim() + "s .";

                final JSONArray data = jsonObject.getJSONArray("data");
                int max = 3;
                if (data.length() < max) {
                    max = data.length();
                }
                speechText += ". Your top  " + max + " " + attributeSpeech + " based on " + metric + " are: ";

                for (int i = 0; i < max; i++) {

                    final Double groupValue = data.getJSONObject(i).getJSONObject("current").getJSONObject("metrics").getJSONObject(metric).getDouble("sum");
                    final String groupName = data.getJSONObject(i).getJSONArray("group").getString(0);
                    String groupNameSpeech = groupName;
                    if (groupName.contains("to")) {
                        groupNameSpeech = "from " + groupName;
                    }
                    if ((metric.contains("sales")) || (metric.contains("price"))) {
                        /*
                        final NumberFormat numberFormat = new DecimalFormat("#,###,###.##");
                        String centPartStr = numberFormat.format(groupValue);
                        final String number[] = centPartStr.split("\\.");
                        if (number.length > 1) {
                            try {
                                centPartStr = number[1];
                                if (centPartStr.length() == 1) {
                                    centPartStr += "0";
                                }
                                // centPart = bigDecimal.precision();
                            } catch (Exception e) {
                                centPartStr += " " + e.toString();
                            }
                        }*/

                        final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
                        currencyFormat.setMaximumFractionDigits(0);

                        final String currencyValue = currencyFormat.format(groupValue);

                        speechText += ". " + attributeSpeech + " " + groupNameSpeech + " has " + currencyValue + " in sales.";

                        //     speechText += ". " + attributeSpeech + " " + groupNameSpeech + " has " + groupValue.longValue() + " dollars and " + centPartStr + " cents in sales.";

                    } else {
                        speechText += ". " + attributeSpeech + " " + groupNameSpeech + " has " + groupValue.longValue() + " " + unitSpeech;
                    }

                }

                speechText += " . Please visit zoom data preview to visualize your chart for more details.";
                //   speechText += ".  Goodbye!";

            } catch (Exception e) {
                speechText += "Sorry I am not able to build your chart. I got exception " + e.toString();
            }

        } else {
            // Render an error since we don't know what the users favorite color is.
            speechText = "I'm not sure what chart you want me to build, please try again " +
                    "with, for example, Alexa, tell zoomdata to build a bar chart for price based on gender";
            repromptText = null;
            /*
                    "I'm not sure what your favorite color is. You can tell me your favorite "
                            + "color by saying, my favorite color is red";
                            */
        }

        return getSpeechletResponse(speechText, repromptText, false);
    }

    private SpeechletResponse doRealTimeIntent(final Intent intent, final Session session) {
        // Get the slots from the intent.
        final Map<String, Slot> slots = intent.getSlots();

        // Get the color slot from the list of slots.
        Slot attributeSlot = slots.get(ATTRIBUTE_SLOT);
        Slot metricSlot = slots.get(METRIC_SLOT);
        Slot chartTypeSlot = slots.get(CHART_TYPE_SLOT);
        String speechText;
        String repromptText = null;
        final String filters = null; // "2016-06-04 00:00:00";
        final String timeRangeType = "on";
        final String filterField = "updated_ts";
        String attribute = null;
        String metric = metricSlot.getValue();
        String attributeSpeech = null;

        // Check for favorite color and create output to user.
        if (attributeSlot != null) {
            // Store the user's favorite color in the Session and create response.
            attribute = attributeSlot.getValue();
            if (StringUtils.isEmpty(attribute)) {
                attribute = "";
            } else if (attribute.contains("group")) {
                attribute = "group_name";
            } else if ((attribute.contains("fruit")) || (attribute.contains("fruits"))) {
                attribute = "fruit";
            } else if ((attribute.contains("area")) || (attribute.contains("code")) || (attribute.contains("area code"))) {
                attribute = "country_area_code";
            } else if ((attribute.contains("phone")) || (attribute.contains("number")) || (attribute.contains("phone number"))) {
                attribute = "phone_num";
            }
            attributeSpeech = mapFieldToSpeech.get(attribute);
            if (StringUtils.isEmpty(attributeSpeech) == true) {
                attributeSpeech = attribute;
            }

            String chartType = chartTypeSlot.getValue();
            if (StringUtils.isEmpty(chartType)) {
                chartType = "bar";
            }

            //  final String sourceName ="edc-smart-w-cratedb";
            session.setAttribute(COLOR_KEY, attribute);

            // final String bookmarkName = "Alexa_" + chartType + "_for_" + metric + " by " + attribute;
        }


        if (StringUtils.isEmpty(metric) == false) {
            if (metric.equals("volume")) {
                metric = "";
            }
            speechText = "You are asking for the total order volume of fruits at the current hour. ";
        } else {
            speechText =
                    String.format("You are asking for the order distribution of %s at the current hour. ",
                            attribute.replace("_", " "));
        }
        if (StringUtils.isEmpty(metric)) {
            metric = "*";
        } else {

        }
        final ZoomdataEnv zoomdataEnv = getZoomdataEnv();

        try {
            final String function = "count";
            String sourceId = sourceIdCommon;
            final String sourceIdEnv = System.getenv("SOURCE_ID");
            if (StringUtils.isEmpty(sourceIdEnv) == false) {
                sourceId = sourceIdEnv;
            }


            //   visPregen(bookmarkName, sourceId, chartType, metric, attribute);
            //  speechText += "Congratulations! Your chart is ready under the name  " + bookmarkName.replace("_", " ");

            final JSONObject jsonObject = zoomdataEnv.getDataFruits(metric, function, attribute, sourceId, filters, filterField, timeRangeType);
            if (jsonObject == null) {
                speechText += " Sorry I found no " + attribute; // + ". Goodbye!";
                return getSpeechletResponse(speechText, repromptText, false);
            }

            int size = 0;
            try {
                size = jsonObject.getInt("available");
            } catch (Exception e) {
                e.printStackTrace();
            }

            String unitSpeech = mapMetricToUnitSpeech.get(metric);
            if (StringUtils.isEmpty(unitSpeech)) {
                unitSpeech = metric;
            }


            JSONArray data = null;
            try {
                data = jsonObject.getJSONArray("data");
            } catch (Exception e) {
                e.printStackTrace();
                speechText += ". Sorry I found no fruit in the system at this hour. Please try again later.";
                return getSpeechletResponse(speechText, repromptText, false);

            }

            if (StringUtils.isEmpty(attributeSpeech)) {
                int count = 0;
                if (data.length() > 0) {
                    count = data.getJSONObject(0).getJSONObject("current").getInt("count");
                }
                if (count > 1) {
                    speechText += ". Congratulations! I found ";
                    speechText += "a total of " + count;
                    speechText += " orders";
                } else {
                    speechText += ". Sorry! I found ";
                    speechText += "" + count;
                    speechText += " fruit.!";
                }

                //  speechText += " Goodbye";
                return getSpeechletResponse(speechText, repromptText, false);

            }

            int max = 3;
            if (data.length() < max) {
                max = data.length();
            }
            speechText += ". Your top  " + max + " " + attributeSpeech + " based on order count are: ";

            for (int i = 0; i < max; i++) {

                final int groupValue = data.getJSONObject(i).getJSONObject("current").getInt("count");
                final String groupName = data.getJSONObject(i).getJSONArray("group").getString(0);
                String groupNameSpeech = groupName;
                if (groupName.contains("to")) {
                    groupNameSpeech = "from " + groupName;
                }
                if (metric.contains("sales")) {
                    final NumberFormat numberFormat = new DecimalFormat("#,###,###.##");

                    String centPartStr = numberFormat.format(groupValue);
                    final String number[] = centPartStr.split("\\.");
                    if (number.length > 1) {
                        try {
                            centPartStr = number[1];
                            if (centPartStr.length() == 1) {
                                centPartStr += "0";
                            }
                            // centPart = bigDecimal.precision();
                        } catch (Exception e) {
                            centPartStr += " " + e.toString();
                        }
                    }

                    speechText += ". " + attributeSpeech + " " + groupNameSpeech + " has " + groupValue + " orders";

                } else {
                    speechText += ". " + attributeSpeech + " " + groupNameSpeech + " has " + groupValue + " orders";
                }

            }

            speechText += " . Please visit zoom data z d labs to visualize your real-time chart for more details.";
            //    speechText += ".  Goodbye!";

        } catch (Exception e) {
            speechText += " Sorry I am not able process your request. I got exception " + ExceptionUtils.getStackTrace(e) + "." + e.toString();
        }

        return getSpeechletResponse(speechText, repromptText, false);
    }


    private SpeechletResponse doRealTimeIntentTweetsOld(final Intent intent, final Session session) {
        // Get the slots from the intent.
        final Map<String, Slot> slots = intent.getSlots();

        // Get the color slot from the list of slots.
        Slot attributeSlot = slots.get(ATTRIBUTE_SLOT);
        Slot metricSlot = slots.get(METRIC_SLOT);
        Slot chartTypeSlot = slots.get(CHART_TYPE_SLOT);
        String speechText;
        String repromptText = null;
        final String filters = null; // "2016-06-04 00:00:00";
        final String timeRangeType = "on";
        final String filterField = "updated_ts";
        String attribute = null;
        String metric = metricSlot.getValue();
        String attributeSpeech = null;

        // Check for favorite color and create output to user.
        if (attributeSlot != null) {
            // Store the user's favorite color in the Session and create response.
            attribute = attributeSlot.getValue();
            if (StringUtils.isEmpty(attribute)) {
                attribute = "";
            } else if ((attribute.contains("trending")) || (attribute.contains("trend"))) {
                attribute = "hashtag_str";
            } else if ((attribute.contains("area")) || (attribute.contains("code")) || (attribute.contains("area code"))) {
                attribute = "country_area_code";
            } else if ((attribute.contains("phone")) || (attribute.contains("number")) || (attribute.contains("phone number"))) {
                attribute = "phone_num";
            }
            attributeSpeech = mapFieldToSpeech.get(attribute);
            if (StringUtils.isEmpty(attributeSpeech) == true) {
                attributeSpeech = attribute;
            }

            String chartType = chartTypeSlot.getValue();
            if (StringUtils.isEmpty(chartType)) {
                chartType = "bar";
            }

            //  final String sourceName ="edc-smart-w-cratedb";
            session.setAttribute(COLOR_KEY, attribute);

        }

        if (StringUtils.isEmpty(metric) == false) {
            speechText = "You are asking for the distribution of tweets related to zoomdata and spark summit. ";
        } else {
            speechText =
                    String.format("You are asking for the total %s of tweets related to zoomdata and spark summit. ",
                            attribute.replace("_", " "));
        }
        if (StringUtils.isEmpty(metric)) {
            metric = "*";
        } else {

        }

        String sourceId = sourceIdCommon;

        final String sourceIdEnv = System.getenv("SOURCE_ID");
        if (StringUtils.isEmpty(sourceIdEnv) == false) {
            sourceId = sourceIdEnv;
        }

        final ZoomdataEnv zoomdataEnv = getZoomdataEnv();

        try {
            final String function = "count";

            //   visPregen(bookmarkName, sourceId, chartType, metric, attribute);
            //  speechText += "Congratulations! Your chart is ready under the name  " + bookmarkName.replace("_", " ");

            final JSONObject jsonObject = zoomdataEnv.getDataTweets(metric, function, attribute, sourceId, filters, filterField, timeRangeType);
            if (jsonObject == null) {
                speechText += " Sorry I found no " + attribute; // + ". Goodbye!";
                return getSpeechletResponse(speechText, repromptText, false);
            }

            int size = 0;
            try {
                size = jsonObject.getInt("available");
            } catch (Exception e) {
                e.printStackTrace();
            }

            String unitSpeech = mapMetricToUnitSpeech.get(metric);
            if (StringUtils.isEmpty(unitSpeech)) {
                unitSpeech = metric;
            }


            JSONArray data = null;
            try {
                jsonObject.getJSONArray("data");
            } catch (Exception e) {
                e.printStackTrace();
                speechText += ". Sorry I found no fruit in the system at this hour. Please try again later.";
                return getSpeechletResponse(speechText, repromptText, false);

            }

            speechText += ". Congratulations! I found ";
            if (StringUtils.isEmpty(attributeSpeech)) {
                final int count = data.getJSONObject(0).getJSONObject("current").getInt("count");
                speechText += "a total of " + count;
                if (count > 1) {
                    speechText += " tweets";
                } else {
                    speechText += " tweets.!";
                }

                //  speechText += " Goodbye";
                return getSpeechletResponse(speechText, repromptText, false);

            } else {
                speechText += " " + size;
            }

            speechText += " " + attributeSpeech.trim() + "s";

            int max = 3;
            if (data.length() < max) {
                max = data.length();
            }
            speechText += ". Your top  " + max + " " + attributeSpeech + " based on order count are: ";

            for (int i = 0; i < max; i++) {

                final int groupValue = data.getJSONObject(i).getJSONObject("current").getInt("count");
                final String groupName = data.getJSONObject(i).getJSONArray("group").getString(0);
                String groupNameSpeech = groupName;
                if (groupName.contains("to")) {
                    groupNameSpeech = "from " + groupName;
                }
                if (metric.contains("sales")) {
                    final NumberFormat numberFormat = new DecimalFormat("#,###,###.##");
                    String centPartStr = numberFormat.format(groupValue);
                    final String number[] = centPartStr.split("\\.");
                    if (number.length > 1) {
                        try {
                            centPartStr = number[1];
                            if (centPartStr.length() == 1) {
                                centPartStr += "0";
                            }
                            // centPart = bigDecimal.precision();
                        } catch (Exception e) {
                            centPartStr += " " + e.toString();
                        }
                    }

                    speechText += ". " + attributeSpeech + " " + groupNameSpeech + " has " + groupValue + " tweets";

                } else {
                    speechText += ". " + attributeSpeech + " " + groupNameSpeech + " has " + groupValue + " tweets";
                }

            }

            speechText += " . Please visit zoom data preview to visualize your real-time chart for more details.";
            //    speechText += ".  Goodbye!";

        } catch (Exception e) {
            speechText += " Sorry I am not able process your request. I got exception " + ExceptionUtils.getStackTrace(e) + "." + e.toString();
        }

        return getSpeechletResponse(speechText, repromptText, false);
    }

    private SpeechletResponse doRealTimeIntentTweets(final Intent intent, final Session session) {
        // Get the slots from the intent.
        final Map<String, Slot> slots = intent.getSlots();

        // Get the color slot from the list of slots.
        Slot attributeSlot = slots.get(ATTRIBUTE_SLOT);
        Slot metricSlot = slots.get(METRIC_SLOT);
        Slot chartTypeSlot = slots.get(CHART_TYPE_SLOT);
        String speechText;
        String repromptText = null;
        final String filters = null; // "2016-06-04 00:00:00";
        final String timeRangeType = "on";
        final String filterField = "updated_ts";
        String attribute = null;
        String metric = metricSlot.getValue();
        String attributeSpeech;
        String orgAttribute = null;
        // Check for favorite color and create output to user.
        if (attributeSlot != null) {
            // Store the user's favorite color in the Session and create response.
            attribute = attributeSlot.getValue();
            orgAttribute = attribute;
            if (StringUtils.isEmpty(attribute)) {
                attribute = "start_of_hour";
            } else if ((attribute.contains("trending")) || (attribute.contains("trend"))) {
                attribute = "hashtag_str";
            } else if ((attribute.contains("hour")) || (attribute.contains("right now")) || (attribute.contains("now"))) {
                attribute = "start_of_hour";
            } else if ((attribute.contains("today")) || (attribute.contains("day")) || (attribute.contains("start of day"))) {
                attribute = "start_of_day";
            }
            attributeSpeech = mapFieldToSpeech.get(attribute);
            if (StringUtils.isEmpty(attributeSpeech) == true) {
                attributeSpeech = attribute;
            }

            String chartType = chartTypeSlot.getValue();
            if (StringUtils.isEmpty(chartType)) {
                chartType = "bar";
            }

            //  final String sourceName ="edc-smart-w-cratedb";
            session.setAttribute(COLOR_KEY, attribute);

            // final String bookmarkName = "Alexa_" + chartType + "_for_" + metric + " by " + attribute;
        } else {
            attribute = "start_of_hour";
            orgAttribute = "at this hour";
        }

        if (StringUtils.isEmpty(metric) == false) {
            speechText = "You are asking for the total number of tweets related to zoomdata and spark summit " + orgAttribute;
        } else {
            speechText =
                    String.format("You are asking for the distribution of tweets related to zoomdata and spark summit %s",
                            orgAttribute.replace("_", " "));
        }


        if (StringUtils.isEmpty(metric)) {
            metric = "";
        } else {

        }

        String sourceId = "57582c37e4b0628b8d8d4580";
        final String sourceIdEnv = System.getenv("SOURCE_ID");
        if (StringUtils.isEmpty(sourceIdEnv) == false) {
            sourceId = sourceIdEnv;
        }

        final ZoomdataEnv zoomdataEnv = getZoomdataEnv();

        // "5753258360b2b9cf90fd18e3";
        try {
            final String function = "count";

            //   visPregen(bookmarkName, sourceId, chartType, metric, attribute);
            //  speechText += "Congratulations! Your chart is ready under the name  " + bookmarkName.replace("_", " ");

            final JSONObject jsonObject = zoomdataEnv.getDataTweets(metric, function, attribute, sourceId, filters, filterField, timeRangeType);
            if (jsonObject == null) {
                speechText += " Sorry I found no " + attribute;// + ". Goodbye!";
                return getSpeechletResponse(speechText, repromptText, false);
            }

            int size = 0;
            try {
                size = jsonObject.getInt("available");
            } catch (Exception e) {
                e.printStackTrace();
            }

            String unitSpeech = mapMetricToUnitSpeech.get(metric);
            if (StringUtils.isEmpty(unitSpeech)) {
                unitSpeech = metric;
            }


            JSONArray data;
            try {
                data = jsonObject.getJSONArray("data");
            } catch (Exception e) {
                e.printStackTrace();
                speechText += ". Sorry I found no data in the system at this hour. Please try again later. ";
                return getSpeechletResponse(speechText, repromptText, false);

            }

            speechText += ". I found ";
            final int count = data.getJSONObject(0).getJSONObject("current").getInt("count");

            if (count == 0) {
                speechText += count + " tweets related to Zoom Data and Spark Summit. Please visit zoom data preview to visualize your real-time chart for more details.";
                //   speechText += ".  Goodbye!";
                return getSpeechletResponse(speechText, repromptText, false);

            }

            if (StringUtils.isEmpty(metric) != true) {
                speechText += "" + count;
                speechText += " tweets related to Zoom Data and Spark Summit. Please visit zoom data preview to visualize your real-time chart for more details.";
                //    speechText += ".  Goodbye!";
                return getSpeechletResponse(speechText, repromptText, false);
            }

            speechText += size + " groups ";

            //  speechText +=" Goodbye";
            //  return getSpeechletResponse(speechText, repromptText, false);

            //   speechText += " trends. ";

            int max = 5;
            if (data.length() < max) {
                max = data.length();
            }
            speechText += ". Your top  " + max + " trends based on number of tweets are: ";

            for (int i = 0; i < max; i++) {

                final int groupValue = data.getJSONObject(i).getJSONObject("current").getInt("count");
                final String groupName = data.getJSONObject(i).getJSONArray("group").getString(0);
                String groupNameSpeech = groupName;
                if (groupName.contains("to")) {
                    groupNameSpeech = "from " + groupName;
                }
                if (metric.contains("sales")) {
                    final NumberFormat numberFormat = new DecimalFormat("#,###,###.##");
                    String centPartStr = numberFormat.format(groupValue);
                    final String number[] = centPartStr.split("\\.");
                    if (number.length > 1) {
                        try {
                            centPartStr = number[1];
                            if (centPartStr.length() == 1) {
                                centPartStr += "0";
                            }
                            // centPart = bigDecimal.precision();
                        } catch (Exception e) {
                            centPartStr += " " + e.toString();
                        }
                    }

                    speechText += ". hash tag " + groupNameSpeech + " has " + groupValue + " tweets";

                } else {
                    speechText += ". hash tag " + groupNameSpeech + " has " + groupValue + " tweets";
                }

            }

            speechText += " . Please visit zoom data preview to visualize your real-time chart for more details.";
            //     speechText += ".  Goodbye!";

        } catch (Exception e) {
            speechText += " Sorry I am not able process your request. I got exception " + ExceptionUtils.getStackTrace(e) + "." + e.toString();
        }

        return getSpeechletResponse(speechText, repromptText, false);
    }

    private SpeechletResponse doRealTimeIntentTrends(final Intent intent, final Session session) {
        // Get the slots from the intent.
        final Map<String, Slot> slots = intent.getSlots();

        // Get the color slot from the list of slots.
        Slot attributeSlot = slots.get(ATTRIBUTE_SLOT);
        Slot metricSlot = slots.get(METRIC_SLOT);
        Slot chartTypeSlot = slots.get(CHART_TYPE_SLOT);
        String speechText;
        String repromptText = null;
        final String filters = null; // "2016-06-04 00:00:00";
        final String timeRangeType = "on";
        final String filterField = "updated_ts";
        String attribute = null;
        String metric = metricSlot.getValue();
        String attributeSpeech = null;
        String orgAttribute = null;
        // Check for favorite color and create output to user.
        if (attributeSlot != null) {
            // Store the user's favorite color in the Session and create response.
            attribute = attributeSlot.getValue();
            orgAttribute = attribute;
            if (StringUtils.isEmpty(attribute)) {
                attribute = "";
            } else if ((attribute.contains("trending")) || (attribute.contains("trend"))) {
                attribute = "hashtag_str";
            } else if ((attribute.contains("last"))) {
                attribute = "last hour";
            } else if ((attribute.contains("hour")) || (attribute.contains("right now")) || (attribute.contains("now"))) {
                attribute = "start_of_hour";
            } else if ((attribute.contains("today")) || (attribute.contains("day")) || (attribute.contains("start of day"))) {
                attribute = "start_of_day";
            }
            attributeSpeech = mapFieldToSpeech.get(attribute);
            if (StringUtils.isEmpty(attributeSpeech) == true) {
                attributeSpeech = attribute;
            }

            String chartType = chartTypeSlot.getValue();
            if (StringUtils.isEmpty(chartType)) {
                chartType = "bar";
            }

            //  final String sourceName ="edc-smart-w-cratedb";
            session.setAttribute(COLOR_KEY, attribute);

            // final String bookmarkName = "Alexa_" + chartType + "_for_" + metric + " by " + attribute;
        }

        speechText = "You are asking for what's trending at twitter " + orgAttribute + ". ";
        if (StringUtils.isEmpty(metric)) {
            metric = "*";
        } else {

        }

        String sourceId = "57582c37e4b0628b8d8d4580";

        final String sourceIdEnv = System.getenv("SOURCE_ID");
        if (StringUtils.isEmpty(sourceIdEnv) == false) {
            sourceId = sourceIdEnv;
        }

        //"5752eaa260b2b9cf90fcdd4c";
        final ZoomdataEnv zoomdataEnv = getZoomdataEnv();
        try {
            final String function = "count";

            //   visPregen(bookmarkName, sourceId, chartType, metric, attribute);
            //  speechText += "Congratulations! Your chart is ready under the name  " + bookmarkName.replace("_", " ");

            final JSONObject jsonObject = zoomdataEnv.getDataTrends(metric, function, attribute, sourceId, filters, filterField, timeRangeType);
            if (jsonObject == null) {
                speechText += " Sorry I found no " + attribute; // + ". Goodbye!";
                return getSpeechletResponse(speechText, repromptText, false);
            }

            int size = 0;
            try {
                size = jsonObject.getInt("available");
            } catch (Exception e) {
                e.printStackTrace();
            }

            /*
            String unitSpeech = mapMetricToUnitSpeech.get(metric);
            if (StringUtils.isEmpty(unitSpeech)) {
                unitSpeech = metric;
            }*/


            JSONArray data;
            try {
                data = jsonObject.getJSONArray("data");
            } catch (Exception e) {
                e.printStackTrace();
                speechText += ". Sorry I found no data in the system at this hour. Please try again later.";
                return getSpeechletResponse(speechText, repromptText, false);

            }

            int count = 0;
            try {
                count = data.getJSONObject(0).getJSONObject("current").getInt("count");
            } catch (Exception e) {
                e.printStackTrace();
                speechText += ". Sorry I found no data in the system at this hour. Please try again later. " +
                        "Goodbye! Details: " + ExceptionUtils.getStackTrace(e);
                return getSpeechletResponse(speechText, repromptText, false);
            }

            speechText += ". Congratulations! I found ";
            speechText += "" + size;
            if (count > 1) {
                speechText += " trends.";
            } else {
                speechText += " trend.";
            }

            int max = 5;
            if (data.length() < max) {
                max = data.length();
            }
            speechText += ". Your top  " + max + " trends based on number of tweets are: ";

            for (int i = 0; i < max; i++) {

                final int groupValue = data.getJSONObject(i).getJSONObject("current").getInt("count");
                final String groupName = data.getJSONObject(i).getJSONArray("group").getString(0);
                String groupNameSpeech = groupName;
                if (groupName.contains("to")) {
                    groupNameSpeech = "from " + groupName;
                }
                if (metric.contains("sales")) {
                    final NumberFormat numberFormat = new DecimalFormat("#,###,###.##");
                    String centPartStr = numberFormat.format(groupValue);
                    final String number[] = centPartStr.split("\\.");
                    if (number.length > 1) {
                        try {
                            centPartStr = number[1];
                            if (centPartStr.length() == 1) {
                                centPartStr += "0";
                            }
                            // centPart = bigDecimal.precision();
                        } catch (Exception e) {
                            centPartStr += " " + e.toString();
                        }
                    }

                    speechText += ". Trending hash tag " + groupNameSpeech + " has " + groupValue + " tweets";

                } else {
                    speechText += ".  Trending hash tag " + groupNameSpeech + " has " + groupValue + " tweets";
                }

            }

            speechText += " . Please visit zoom data preview to visualize your real-time chart for more details.";
            //    speechText += ".  Goodbye!";

        } catch (Exception e) {
            speechText += " Sorry I am not able process your request. I got exception " + ExceptionUtils.getStackTrace(e) + "." + e.toString();
        }

        return getSpeechletResponse(speechText, repromptText, false);
    }

    private SpeechletResponse doTopIntent(final Intent intent, final Session session) {
        final boolean topIntent = true;

        return distributionIntent(intent, session, topIntent);

    }

    private SpeechletResponse doDistributionIntent(final Intent intent, final Session session) {
        final boolean topIntent = false;
        return distributionIntent(intent, session, topIntent);
    }

    private ZoomdataEnv getZoomdataEnv() {
        final String hostName = System.getenv("HOST_NAME");
        final ZoomdataEnv zoomdataEnv = new ZoomdataEnv(hostName);
        return zoomdataEnv;
    }

    private SpeechletResponse doDebugIntent(final Intent intent, final Session session) {
        final ZoomdataEnv zoomdataEnv = getZoomdataEnv();
        final String repromptText = null;
        String speechText = "";


        final String debugValue = System.getenv("DEBUG");

        final String url = "https://s3.amazonaws.com/zoomdata-labs/hello.txt";
        try {
            final String s3Data = HttpUtil.httpGet(url);
            speechText += ",s3Data=" + s3Data;
        } catch (Exception e) {
            e.printStackTrace();
            speechText += ". Got exception " + e;
        }
        final Map<String, String> envs = System.getenv();
        final String data = MiscUtil.GetDataFromFileName("/Dashboard_Template_BAR.json");
        speechText += ", intent name=" + intent.getName() + ", zoomdataEnv=" + zoomdataEnv.toString();

        speechText += ", slots map=" + intent.getSlots();
        speechText += ", debug=" + debugValue + ", zoomdata details=" + zoomdataEnv.toString();

        speechText += ". envs=" + envs + ", Data length=" + data.length();
        return getSpeechletResponse(speechText, repromptText, false);
    }

    private SpeechletResponse distributionIntent(final Intent intent, final Session session,
                                                 final boolean topIntent) {

        // Get the slots from the intent.
        Map<String, Slot> slots = intent.getSlots();

        // Get the color slot from the list of slots.
        final Slot attributeSlot = slots.get(ATTRIBUTE_SLOT);
        final Slot metricSlot = slots.get(METRIC_SLOT);
        final Slot timeFilterSlot = slots.get(TIME_FILTER_SLOT);
        final Slot rangeTypeSlot = slots.get(RANGE_TYPE_SLOT);

        String speechText, repromptText;
        String attribute = null;
        String timeFilterValue = null;
        String timeRangeType;
        if (rangeTypeSlot != null) {
            timeRangeType = rangeTypeSlot.getValue();
        } else {
            timeRangeType = "since";
        }
        // Check for favorite color and create output to user.
        // If time is available use it. If not use attribute.

        if (attributeSlot != null) {
            attribute = attributeSlot.getValue();
        } else {
            // Ignore. Don't set attribute.
        }

        if (timeFilterSlot != null) {
            timeFilterValue = timeFilterSlot.getValue();

            if (StringUtils.isEmpty(timeFilterValue)) {
                // Ignore.
            } else {
                // override previously set attribute.
                attribute = "sale_date";
            }
        } else {
            // Ignore attribute population here.
        }

        if (StringUtils.isEmpty(attribute)) {
            // Render an error since we don't know what the users favorite color is.
            speechText = "I'm not sure what chart you want me to build, please try again " +
                    "with, for example, Alexa, tell zoomdata to build a bar chart for price based on gender";
            repromptText = null;
            return getSpeechletResponse(speechText, repromptText, false);
        }

        // Store the user's favorite color in the Session and create response.
        if (StringUtils.isEmpty(attribute)) {
            attribute = "usergender";
        } else if ((attribute.contains("city")) || (attribute.contains("cities")) ||
                (attribute.contains("citi")) || (attribute.contains("CD")) || (attribute.contains("ct"))) {
            attribute = "usercity";
        } else if (attribute.contains("state")) {
            attribute = "userstate";
        } else if ((attribute.contains("income")) || (attribute.contains("in")) || (attribute.contains("com"))) {
            attribute = "userincome";
        } else if (attribute.contains("group")) {
            attribute = "group_name";
        } else if ((attribute.contains("time")) || (attribute.contains("date")) || (attribute.contains("timestamp"))) {
            attribute = "sale_date";
        } else if (attribute.contains("gender")) {
            attribute = "usergender";
        } else if (attribute.contains("cat")) {
            attribute = "category";
        }
        String metric = metricSlot.getValue();
        if (StringUtils.isEmpty(metric)) {
            metric = "sales";
        }

        final String chartType = "BAR";

        session.setAttribute(COLOR_KEY, attribute);
        final String bookmarkName = "Alexa_" + chartType + "_" + metric + "_" + attribute;

        String speechIntent = "the distribution of";
        if (topIntent == true) {
            speechIntent = "the top three";
        }

        if (StringUtils.isEmpty(timeFilterValue) == false) {
            speechText =
                    String.format("You are asking for " + speechIntent + " %s %s %s. attribute=%s",
                            metric, timeRangeType, timeFilterValue, attribute);
        } else {
            if (topIntent == true) {
                speechText =
                        String.format("You are asking for the top three %s based on %s. ",
                                attribute.replace("_", " "), metric);
            } else {
                speechText =
                        String.format("You are asking for the distribution of %s based on %s. ",
                                metric, attribute.replace("_", " "));
            }
        }

        repromptText = "";
        // "You can ask me your favorite color by saying, what's my favorite color?";
        String sourceId = sourceIdCommon;

        final String sourceIdEnv = System.getenv("SOURCE_ID");
        if (StringUtils.isEmpty(sourceIdEnv) == false) {
            sourceId = sourceIdEnv;
        }

        String function = "sum";
        boolean debug = true;

        final String debugValue = System.getenv("DEBUG");
        if (StringUtils.isEmpty(debugValue) == false) {
            debug = Boolean.parseBoolean(debugValue);
        }


        final ZoomdataEnv zoomdataEnv = getZoomdataEnv();

        if (debug) {
            final String url = "https://s3.amazonaws.com/zoomdata-labs/hello.txt";
            try {
                final String s3Data = HttpUtil.httpGet(url);
                speechText += ",s3Data=" + s3Data;
            } catch (Exception e) {
                e.printStackTrace();
                speechText += ". Got exception " + e;
            }
            final Map<String, String> envs = System.getenv();
            final String data = MiscUtil.GetDataFromFileName("/Dashboard_Template_BAR.json");
            speechText += ", zoomdataEnv=" + zoomdataEnv.toString();
            speechText += ", debug=" + debug + ", zoomdata details=" + zoomdataEnv.toString();
            speechText += ". envs=" + envs + ", Data=" + data;
            return getSpeechletResponse(speechText, repromptText, false);

        }

        try {

            if (metric.toLowerCase().contains("sales")) {
                metric = "plannedsales";
            } else if ((metric.equals("volume")) ||
                    (metric.equals("count")) ||
                    (metric.contains("transaction"))) {
                metric = "*";
                function = "count";
            }

            zoomdataEnv.visPregen(bookmarkName, sourceId, chartType, metric, attribute);

            if (debug) {
                speechText += "Congratulations! Your chart is ready under the name  " + bookmarkName.replace("_", " ");
                speechText += ". Details=" + bookmarkName + ", sourceId=" + sourceId +
                        ", chartType=" + chartType + ", metric=" + metric + ", attribute=" + attribute;
                return getSpeechletResponse(speechText, repromptText, false);

            }

            final JSONObject jsonObject = zoomdataEnv.getData(metric, function, attribute, sourceId, timeFilterValue, timeRangeType);
            int size;
            try {
                size = jsonObject.getInt("available");
            } catch (Exception e) {
                // Ignore.
                speechText += ". Sorry I found no record based on what you are asking. ";
                speechText += " . For more details, please visit zoom data chart name " + bookmarkName.replace("_", " ");// + " at z d labs dot zoom data dot com.";
                //   speechText += ".  Goodbye!";
                return getSpeechletResponse(speechText, repromptText, false);

            }
            String attributeSpeech = mapFieldToSpeech.get(attribute);
            if (StringUtils.isEmpty(attributeSpeech) == true) {
                attributeSpeech = attribute;
            }
            String metricSpeech = mapFieldToSpeech.get(metric);
            if (StringUtils.isEmpty(metricSpeech)) {
                metricSpeech = metric;
            }

            String unitSpeech = mapMetricToUnitSpeech.get(metric);
            if (StringUtils.isEmpty(unitSpeech)) {
                unitSpeech = metricSpeech;
            }

            speechText += " I found " + size + " " + attributeSpeech.trim() + "s .";

            final JSONArray data = jsonObject.getJSONArray("data");
            int max = 3;
            if (data.length() < max) {
                max = data.length();
            }
            speechText += ". Your top  " + max + " " + attributeSpeech + "s based on " + metricSpeech + " are: ";

            if (function.equals("count")) {
                for (int i = 0; i < max; i++) {

                    final Long groupValue = data.getJSONObject(i).getJSONObject("current").getLong("count");
                    String groupName = data.getJSONObject(i).getJSONArray("group").getString(0);
                    try {
                        final Date date = GetDateFromSql(groupName);
                        // if this is a date value, then only extract out the day part and ignore the rest.
                        // Otherwise, it would have been caught in the exception.
                        groupName = groupName.split(" ")[0];
                    } catch (Exception e) {
                        // Ignore.
                    }
                    speechText += attributeSpeech + " " + groupName + " has " + groupValue.longValue() + " transactions. ";

                }

            } else {
                for (int i = 0; i < max; i++) {

                    final Double groupValue = data.getJSONObject(i).getJSONObject("current").getJSONObject("metrics").getJSONObject(metric).getDouble("sum");
                    final String groupName = data.getJSONObject(i).getJSONArray("group").getString(0);

                    String groupNameSpeech = groupName;
                    if (groupName.contains("to")) {
                        groupNameSpeech = "from " + groupName;
                    }
                    if ((metric.contains("sales")) || (metric.contains("price"))) {

                        final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
                        currencyFormat.setMaximumFractionDigits(0);

                        final String currencyValue = currencyFormat.format(groupValue);

                        speechText += ". " + attributeSpeech + " " + groupNameSpeech + " has " + currencyValue + " in sales.";

                        //  speechText += ". " + attributeSpeech + " " + groupNameSpeech + " has " + groupValue.longValue() + " dollars and " + centPartStr + " cents in sales.";

                    } else {
                        speechText += ". " + attributeSpeech + " " + groupNameSpeech + " has " + groupValue.longValue() + " " + unitSpeech;
                    }
                }
            }

            speechText += " . For more details, please visit zoom data chart name " + bookmarkName.replace("_", " ");// + " at z d labs dot zoom data dot com.";
            //    speechText += ".  Goodbye!";

        } catch (Exception e) {
            final String exceptionAsString = stackTraceToString(e);
            speechText += "Sorry I am not able to build your chart. I got exception " + exceptionAsString;
        }

        return getSpeechletResponse(speechText, repromptText, false);
    }


    private SpeechletResponse doWelcomeTechTalk(final Intent intent, final Session session) {

        // Hi everyone! I am very happy to be at the Tech Talk. Thank you for having me.
        final String techTalk = "Welcome all to the Zoom data Tech Talk . In this tech talk, Quan Luu will share the " +
                " Alexa Zoom data journey. Specifically, Quan Luu will share the followings: " +
                "1. The Who - Alexa? . Who is she? And that's me! " +
                "2. The Why - why Alexa Zoom data? For what purpose? " +
                "3. The What - what does the landscape of integration look like? " +
                "4. The How - how is it implemented? " +
                "5. Key Takeaways. " +
                "6. Demo and Q&A. " +
                "7. References with links that you can use later for implementing new Alexa skills. " +
                "I hope that you will enjoy the talk . have a quan tass tic time !";
        final String speechText = "Hi, I am Alexa with Zoom data Analytics skill. " +
                "Great to be here and thank you for having me . I was born on June 11, 2014 in Amazon Lab 126. " +
                " I am a hands-free speaker you control with your voice. I connect to the Alexa Voice Service to play " +
                " music, provide information, news, sports scores, weather, and more instantly. " +
                " You can ask me: Alexa, what’s the weather today? Alexa, tell me a joke. Alexa, play some music. " +
                " Alexa, what’s on the news today. Again, " + techTalk +
                ". Goodbye for now!";

        final String repromptText = null;


        return getSpeechletResponse(speechText, repromptText, false);
    }


    private SpeechletResponse doWelcome(final Intent intent, final Session session) {

        final Map<String, Slot> slots = intent.getSlots();
        if (slots != null) {
            final Slot attributeSlot = slots.get(ATTRIBUTE_SLOT);
            if (attributeSlot != null) {
                final String welcomeAttribute = attributeSlot.getValue();
                if (StringUtils.isEmpty(welcomeAttribute) == false) {
                    if ((welcomeAttribute.toLowerCase().contains("tech")) ||
                            (welcomeAttribute.toLowerCase().contains("talk")) ||
                            (welcomeAttribute.toLowerCase().contains("team"))) {
                        return doWelcomeTechTalk(intent, session);
                    }
                }
            } else {
                // default
            }
        }

        final String speechText = "Hi, I am Alexa with Zoom data Analytics skill. You can ask me questions like: " +
                "Alexa, ask zoom data to find me the top three cities in terms of sales or price or number of transactions " +
                ", or, Alexa, ask zoom data to build me a bar chart for sales based on user city, state or zip code " +
                ", or, Alexa, ask zoom data to give me a distribution of sales based on user income  " +
                ". Goodbye!";
        final String repromptText = null;


        return getSpeechletResponse(speechText, repromptText, false);
    }

    private SpeechletResponse doWelcomeRealTime(final Intent intent, final Session session) {

        final String speechText = "Hi, I am Alexa with Zoom data Real Time Analytics skill. You can ask me questions like: " +
                "Alexa, ask zoom data currently what is the order distribution of fruits " +
                ", or Alexa, ask zoom data currently what's the total order count for all fruits " +
                // ", or Alexa, ask zoom data right now what is the order distribution of fruits based on area code or phone number " +
                //  ", or, Alexa, ask zoom data currently what is the order distribution of fruits . "+
                ". Goodbye!";
        final String repromptText = null;


        return getSpeechletResponse(speechText, repromptText, false);
    }


    private SpeechletResponse doWelcomeRealTweets(final Intent intent, final Session session) {

        final String speechText = "Hi, I am Alexa with Zoom data Real Time Analytics skill. You can ask me questions like: " +
                "Alexa, ask zoom data what's trending at twitter at this hour " +
                ", or Alexa, ask zoom data what's trending at twitter today " +
                ", or Alexa, ask zoom data what's the distribution of tweets related to zoomdata and spark summit at this hour or today" +
                ", or Alexa, ask zoom data what's the total volume of tweets related to zoomdata and spark summit at this hour or today" +
                ". Goodbye!";
        final String repromptText = null;


        return getSpeechletResponse(speechText, repromptText, false);
    }

    private SpeechletResponse doWelcomeRealTrends(final Intent intent, final Session session) {

        final String speechText = "Hi, I am Alexa with Zoom data Real Time Analytics skill. You can ask me questions like: " +
                "Alexa, ask zoom data what's trending at twitter at this hour " +
                ", or Alexa, ask zoom data what's trending at twitter last hour " +
                ", or Alexa, ask zoom data what's trending at twitter today " +
                ", or Alexa, ask zoom data what's trending at twitter yesterday " +

                ". Goodbye!";
        final String repromptText = null;


        return getSpeechletResponse(speechText, repromptText, false);
    }


    /**
     * Creates a {@code SpeechletResponse} for the intent and stores the extracted color in the
     * Session.
     *
     * @param intent intent for the request
     * @return SpeechletResponse spoken and visual response the given intent
     */
    private SpeechletResponse buildVis(final Intent intent, final Session session) {
        // Get the slots from the intent.
        Map<String, Slot> slots = intent.getSlots();

        // Get the color slot from the list of slots.
        Slot favoriteColorSlot = slots.get(COLOR_SLOT);
        String speechText, repromptText;

        // Check for favorite color and create output to user.
        if (favoriteColorSlot != null) {
            // Store the user's favorite color in the Session and create response.
            String favoriteColor = favoriteColorSlot.getValue();
            session.setAttribute(COLOR_KEY, favoriteColor);
            speechText =
                    String.format("I now know that your favorite color is %s. You can ask me your "
                            + "favorite color by saying, what's my favorite color?", favoriteColor);
            repromptText = null;
            // "You can ask me your favorite color by saying, what's my favorite color?";

        } else {
            // Render an error since we don't know what the users favorite color is.
            speechText = "I'm not sure what your favorite color is, please try again";
            repromptText =
                    "I'm not sure what your favorite color is. You can tell me your favorite "
                            + "color by saying, my favorite color is red";
            repromptText = null;
        }

        return getSpeechletResponse(speechText, repromptText, false);
    }

    /**
     * Creates a {@code SpeechletResponse} for the intent and get the user's favorite color from the
     * Session.
     *
     * @param intent intent for the request
     * @return SpeechletResponse spoken and visual response for the intent
     */
    private SpeechletResponse getColorFromSession(final Intent intent, final Session session) {
        String speechText;
        boolean isAskResponse = false;

        // Get the user's favorite color from the session.
        String favoriteColor = (String) session.getAttribute(COLOR_KEY);

        // Check to make sure user's favorite color is set in the session.
        if (StringUtils.isNotEmpty(favoriteColor)) {
            speechText = String.format("Your favorite color is %s. Goodbye.", favoriteColor);
        } else {
            // Since the user's favorite color is not set render an error message.
            speechText =
                    "I'm not sure what your favorite color is. You can say, my favorite color is "
                            + "red";
            isAskResponse = true;
        }

        return getSpeechletResponse(speechText, speechText, isAskResponse);
    }

    /**
     * Returns a Speechlet response for a speech and reprompt text.
     */
    private SpeechletResponse getSpeechletResponse(String speechText, String repromptText,
                                                   boolean isAskResponse) {
        // Create the Simple card content.
        SimpleCard card = new SimpleCard();
        card.setTitle("Session");
        card.setContent(speechText);

        // Create the plain text output.
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText(speechText);

        if (isAskResponse) {
            // Create reprompt
            PlainTextOutputSpeech repromptSpeech = new PlainTextOutputSpeech();
            repromptSpeech.setText(repromptText);
            Reprompt reprompt = new Reprompt();
            reprompt.setOutputSpeech(repromptSpeech);

            return SpeechletResponse.newAskResponse(speech, reprompt, card);

        } else {
            return SpeechletResponse.newTellResponse(speech, card);
        }
    }

    private JSONObject buildRequest(final String metric,
                                    final String func,
                                    final String inGroupBy,
                                    final String sourceId,
                                    final String filterField,
                                    final String inFilterValues,
                                    final String inTimeRangeType) throws Exception {
        //  final String func = "sum";
        String groupBy = inGroupBy;
        if (groupBy.contains("date")) {
            groupBy = "$to_day(" + groupBy + ")";
        }
        final String message = "{\n" +
                "  \"type\": \"START_VIS\",\n" +
                "  \"cid\": \"09fe4f70532ea8efb51f109a865e39df\",\n" +
                "  \"request\": {\n" +
                "    \"streamSourceId\": \"56f5ab2760b2b5bf51a01d09\",\n" +
                "    \"cfg\": {\n" +
                "      \"filters\": [\n" +
                "        \n" +
                "      ],\n" +
                "      \"player\": null,\n" +
                "      \"group\": {\n" +
                "        \"fields\": [\n" +
                "          {\n" +
                "            \"name\": \"usercity\",\n" +
                "            \"sort\": {\n" +
                "              \"dir\": \"desc\",\n" +
                "              \"name\": \"count\"\n" +
                "            },\n" +
                "            \"limit\": 50\n" +
                "          }\n" +
                "        ],\n" +
                "        \"metrics\": [\n" +
                "          {\n" +
                "            \"name\": \"plannedsales\",\n" +
                "            \"func\": \"sum\"\n" +
                "          },\n" +
                "          {\n" +
                "            \"name\": \"*\",\n" +
                "            \"func\": \"count\"\n" +
                "          }\n" +
                "        ]\n" +
                "      }\n" +
                "    },\n" +
                "    \"time\": {\n" +
                "      \"timeField\": \"sale_date\",\n" +
                "    }\n" +
                "  }\n" +
                "}";

        final JSONObject jsonRequest = new JSONObject(message);

        jsonRequest.put("cid", Long.toString(System.nanoTime()));

        jsonRequest.getJSONObject("request").put("streamSourceId", sourceId);
        final JSONObject jsonConfig = jsonRequest.getJSONObject("request").getJSONObject("cfg");
        jsonConfig.getJSONObject("group").getJSONArray("fields").getJSONObject(0).put("name", groupBy);
        final JSONObject sort = jsonConfig.getJSONObject("group").getJSONArray("fields").getJSONObject(0).getJSONObject("sort");
        sort.put("name", metric);

        if (StringUtils.isEmpty(inFilterValues) == false) {
            String timeRangeType = inTimeRangeType;
            String filterValues = inFilterValues;
            // use filter.
            if (timeRangeType.equalsIgnoreCase("on")) {
                timeRangeType = "between";
                final Date date = GetDateFromSql(filterValues);
                // next day
                Calendar cal = Calendar.getInstance();
                cal.setTime(date);
                cal.add(Calendar.DAY_OF_YEAR, 1); // next day.
                cal.add(Calendar.MILLISECOND, -1);
                filterValues += "," + GetSqlZdDateFromDate(cal.getTime());
            } else {
                timeRangeType = "between";
                Calendar cal = Calendar.getInstance();
                filterValues += "," + GetSqlZdDateFromDate(cal.getTime());

            }
            final JSONObject filterItem = new JSONObject();
            filterItem.put("path", filterField);
            filterItem.put("operation", timeRangeType.toUpperCase());
            final JSONArray jsonFilterValues = new JSONArray();
            final String tmp[] = filterValues.split(",");
            for (final String item : tmp) {
                String itemValue = item;
                if (itemValue.split(" ").length <= 1) {
                    itemValue += " 00:00:00";
                }

                if (itemValue.split("\\.").length <= 1) {
                    itemValue += ".000";
                }

                jsonFilterValues.put(itemValue.trim());
            }
            filterItem.put("value", jsonFilterValues);
            final JSONArray filterArray = new JSONArray();
            filterArray.put(filterItem);
            jsonConfig.put("filters", filterArray);
        }


        return jsonRequest;

    }

    private static final String DATA_REQUEST_TEMPLATE =
            "{\n" +
                    "  \"type\": \"START_VIS\",\n" +
                    "  \"cid\": \"__CID__\",\n" +
                    "  \"sourceId\": \"__SOURCE_ID__\",\n" +
                    "  \"filters\": [\n" +
                    "    \n" +
                    "  ],\n" +
                    "  \"time\": {\n" +
                    "    \"from\": \"" + startTime + "\",\n" +
                    "    \"to\": \"" + endTime + "\",\n" +
                    "    \"timeField\": \"record_hour\"\n" +
                    "  },\n" +
                    "  \"player\": null,\n" +
                    "  \"aggregate\": true,\n" +
                    "  \"dimensions\": [\n" +
                    "    {\n" +
                    "      \"aggregations\": [\n" +
                    "        {\n" +
                    "          \"type\": \"TERMS\",\n" +
                    "          \"field\": {\n" +
                    "            \"name\": \"__ATTRIBUTE__\"\n" +
                    "          }\n" +
                    "        }\n" +
                    "      ],\n" +
                    "      \"window\": {\n" +
                    "        \"type\": \"COMPOSITE\",\n" +
                    "        \"aggregationWindows\": [\n" +
                    "          {\n" +
                    "            \"limit\": 50,\n" +
                    "            \"sort\": {\n" +
                    "              \"direction\": \"DESC\",\n" +
                    "              \"type\": \"METRIC\",\n" +
                    "              \"metric\": {\n" +
                    "                \"type\": \"COUNT\"\n" +
                    "              }\n" +
                    "            }\n" +
                    "          }\n" +
                    "        ]\n" +
                    "      }\n" +
                    "    }\n" +
                    "  ],\n" +
                    "  \"metrics\": [\n" +
                    "    {\n" +
                    "      \"type\": \"FIELD\",\n" +
                    "      \"field\": {\n" +
                    "        \"name\": \"__METRIC_NAME__\"\n" +
                    "      },\n" +
                    "      \"function\": \"__METRIC_FUNC__\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"type\": \"COUNT\"\n" +
                    "    }\n" +
                    "  ]\n" +
                    "}";

    private static final String DATA_REQUEST_TEMPLATE2 = "{\n" +
            "  \"type\": \"START_VIS\",\n" +
            "  \"cid\": \"__CID__\",\n" +
            "  \"sourceId\": \"__SOURCE_ID__\",\n" +
            "  \"filters\": [\n" +
            "  ],\n" +
            "  \"time\": {\n" +
            "    \"from\": \"+2013-11-11 01:00:00.999\",\n" +
            "    \"to\": \"+2013-11-11 02:00:00.999\",\n" +
            "    \"timeField\": \"_ts\"\n" +
            "  },\n" +
            "  \"player\": null,\n" +
            "  \"aggregate\": true,\n" +
            "  \"dimensions\": [\n" +
            "    {\n" +
            "      \"aggregations\": [\n" +
            "        {\n" +
            "          \"type\": \"TERMS\",\n" +
            "          \"field\": {\n" +
            "            \"name\": \"__ATTRIBUTE__\"\n" +
            "          }\n" +
            "        }\n" +
            "      ],\n" +
            "      \"window\": {\n" +
            "        \"type\": \"COMPOSITE\",\n" +
            "        \"aggregationWindows\": [\n" +
            "          {\n" +
            "            \"limit\": 50,\n" +
            "            \"sort\": {\n" +
            "              \"direction\": \"DESC\",\n" +
            "              \"type\": \"METRIC\",\n" +
            "              \"metric\": {\n" +
            "                \"type\": \"COUNT\"\n" +
            "              }\n" +
            "            }\n" +
            "          }\n" +
            "        ]\n" +
            "      }\n" +
            "    }\n" +
            "  ],\n" +
            "  \"metrics\": [\n" +
            "    {\n" +
            "      \"type\": \"FIELD\",\n" +
            "      \"field\": {\n" +
            "        \"name\": \"__METRIC_NAME__\"\n" +
            "      },\n" +
            "      \"function\": \"__METRIC_FUNC__\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"type\": \"COUNT\"\n" +
            "    }\n" +
            "  ]\n" +
            "}\n";

    public static Date GetDateFromSql(String sqlDate) throws Exception {
        if (StringUtils.isEmpty(sqlDate) == true) return null;
        sqlDate = sqlDate.trim();
        if (sqlDate.contains(" ") == false) {
            if (sqlDate.contains("-") == true) {
            } else {
                sqlDate += " 00:00:00";
            }
        }

        final String formats[] = {
                "dd/MMM/yyyy:HH:mm:ss",
                "yyyy-MM-dd HH:mm:ss.SSS",
                "EEE MMM dd HH:mm:ss z yyyy",
                "yyyy-MM-dd HH:mm:ss.SSS",
                "yyyy-MM-dd'T'HH:mm:ssXXX",
                "yyyy-MM-dd'T'HH:mm:ss",
                "yyyy-MM-dd",
                "yyyy-MM-dd HH:mm:ss", "MM/dd/yyyy",
        };

        Date dateString = null;
        for (int i = 0; i < formats.length; i++) {
            final String format = formats[i];
            try {
                // TimeZone UTC = TimeZone.getTimeZone("UTC");
                final SimpleDateFormat df = new SimpleDateFormat(format);
                // df.setTimeZone(UTC);

                dateString = df.parse(sqlDate);
            } catch (Exception e) {
                // swallow! Keep trying.
            }
        } // end for.

        if (dateString == null) {
            throw new Exception("*** Error ***: unable to parse sqlDate " + sqlDate);
        }

        return dateString;
    }

    public static String GetSqlZdDateFromDate(final Date date) throws Exception {
        if (date == null) return "";
        final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        return df.format(date);
    }

    public static String stackTraceToString(Exception e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        String exceptionAsString = sw.toString();
        return exceptionAsString;
    }
}
