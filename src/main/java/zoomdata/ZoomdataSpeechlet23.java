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
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zoomdata.util.HttpUtil;
import zoomdata.util.ZoomdataWebsocketSecuredClient;

import javax.xml.bind.DatatypeConverter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * This sample shows how to create a simple speechlet for handling intent requests and managing
 * session interactions.
 */
public class ZoomdataSpeechlet23 implements Speechlet {
    private static final Logger logger = LoggerFactory.getLogger(ZoomdataSpeechlet23.class);

    private static final String COLOR_KEY = "COLOR";
    private static final String COLOR_SLOT = "Color";
    private static final String ATTRIBUTE_SLOT = "Attribute";
    private static final String METRIC_SLOT = "Metric";
    private static final String TIME_FILTER_SLOT = "TimeFilter";
    private static final String RANGE_TYPE_SLOT = "RangeType";

    private static final String bookmarkUrl = "https://zdlabs.zoomdata.com/zoomdata/service/bookmarks";

    private static final String CHART_TYPE_SLOT = "ChartType";

//    private static final String zoomdataUrl = "https://zdlabs.zoomdata.com";
            // "https://preview.zoomdata.com";
 //   private static final ApiClient client = new ApiClient(zoomdataUrl, "admin", "Z00mda1a");
 //   public static final ZoomdataApi zoomdataApi = new ZoomdataApi(client);

    private static final Map<String, String> HEADERS = new HashMap<>();
    static {
        final String credential = "admin:Z00mda1a";
        final String encoding = DatatypeConverter.printBase64Binary(credential.getBytes());
        HEADERS.put("Authorization", "Basic " + encoding);

    }

    static public void visPregen2(final String bookmarkName,
                                  final String sourceId,
                                  final String chartType,
                                  final String metricField,
                                  final String groupByField) {

        try {

            final String tmp[] = metricField.split(",");
            for (final String metricItem : tmp) {
                String metricFunc = "sum";
                String metricName = "*";
                if (metricField.equals("*")) {
                    metricFunc = "count";
                } else {

                    final String tmpItem[] = metricItem.split(":");
                    metricName = tmpItem[0];
                    metricFunc = metricItem;
                }

                try {
                   // zoomdataApi.createBookmark(bookmarkName, sourceId, chartType, metric, metricItem, groupByField);
                    createBookmark(bookmarkName, sourceId, chartType, metricFunc, metricName, groupByField);

                    // Use the source id to stream result.
                } catch (Exception e) {
                    e.printStackTrace();
                }


                logger.info("Done creating bookmark for " + bookmarkName);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static public void deleteBookmarkByName(final String name) {

        try {
            final String bookmarkData = HttpUtil.sendGetWithHeader(bookmarkUrl, HEADERS);

            logger.info("bookmarkData=" + bookmarkData);
            final JSONArray jsonArray = new JSONArray(bookmarkData);
            System.out.println("# items=" + jsonArray.length());
            final int length = jsonArray.length();

            for (int i = 0; i < length; i++){
                final JSONObject item = jsonArray.getJSONObject(i);
                final String bookmarkName = item.getString("name");
                if (name.equals(bookmarkName)) {
                    // Execute delete name here
                    final String id = item.getString("id");
                    System.out.println("matched name " + name + ", id=" + id);
                    final String status = HttpUtil.sendDeleteWithHeader(bookmarkUrl+"/"+id,HEADERS);
                    System.out.println("delete status=" + status);
                    return;

                } else {
                    // Ignore.
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static private void createBookmark(final String name, final String sourceId, final String type,
                                final String metricFunc, final String metricName, final String attribute) throws Exception{

        // 2.5 version
        final String bookmarkUrl25 = "https://2-5-latest.zoomdata.com/zoomdata/service/bookmarks";

        try {
            // Delete if exists
            deleteBookmarkByName(name);
            if (type.toLowerCase().contains("bar")) {
                final String request = REQUEST_TEMPLATE_BAR_25.replace("__VIS_NAME__", name)
                        .replace("__METRIC_FUNC__", metricFunc)
                        .replace("__METRIC_NAME__", metricName)
                        .replace("__ATTRIBUTE__", attribute)
                        .replace("__SOURCE_ID__", sourceId);


                final Map<String, Object> results = HttpUtil.httpsPost(bookmarkUrl25, request, HEADERS);
                logger.info("results=" + results);
            } else {

                final String request = REQUEST_TEMPLATE_PIE.replace("__VIS_NAME__", name)
                        .replace("__METRIC_FUNC__", metricFunc)
                        .replace("__METRIC_NAME__", metricName)
                        .replace("__ATTRIBUTE__", attribute)
                        .replace("__SOURCE_ID__", sourceId);

                final Map<String, Object> results = HttpUtil.httpsPost(bookmarkUrl25, request, HEADERS);
                logger.info("results=" + results);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    static private void createBookmark23(final String name, final String sourceId, final String type,
                                       final String metricFunc, final String metricName, final String attribute) throws Exception{

        try {
            // Delete if exists
            deleteBookmarkByName(name);
            if (type.toLowerCase().contains("bar")) {
                final String request = REQUEST_TEMPLATE_BAR.replace("__VIS_NAME__", name)
                        .replace("__METRIC_FUNC__", metricFunc)
                        .replace("__METRIC_NAME__", metricName)
                        .replace("__ATTRIBUTE__", attribute)
                        .replace("__SOURCE_ID__", sourceId);


                final Map<String, Object> results = HttpUtil.httpsPost(bookmarkUrl, request, HEADERS);
                logger.info("results=" + results);
            } else {

                final String request = REQUEST_TEMPLATE_PIE.replace("__VIS_NAME__", name)
                        .replace("__METRIC_FUNC__", metricFunc)
                        .replace("__METRIC_NAME__", metricName)
                        .replace("__ATTRIBUTE__", attribute)
                        .replace("__SOURCE_ID__", sourceId);

                final Map<String, Object> results = HttpUtil.httpsPost(bookmarkUrl, request, HEADERS);
                logger.info("results=" + results);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    static private final String REQUEST_TEMPLATE_BAR_25="{\n" +
            "  \"name\": \"test dashboard w filter5\",\n" +
            "  \"description\": \"\",\n" +
            "  \"layout\": \"unset\",\n" +
            "  \"showDescription\": false,\n" +
            "  \"visualizations\": [\n" +
            "    {\n" +
            "      \"id\": \"58ed1d96e4b03f8d866ef1a0\",\n" +
            "      \"visId\": \"5898841fe4b0c9dbdb60134b\",\n" +
            "      \"name\": \"Bars\",\n" +
            "      \"type\": \"UBER_BARS\",\n" +
            "      \"widgetId\": \"d2e07cddb3b7e34f9ec6ce713c92bb24\",\n" +
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
            "              \"name\": \"usergender\",\n" +
            "              \"limit\": 50,\n" +
            "              \"sort\": {\n" +
            "                \"dir\": \"desc\",\n" +
            "                \"name\": \"count\"\n" +
            "              },\n" +
            "              \"label\": \"Usergender\",\n" +
            "              \"type\": \"ATTRIBUTE\",\n" +
            "              \"groupColorSet\": \"ZoomPalette\",\n" +
            "              \"autoShowColorLegend\": true,\n" +
            "              \"colorNumb\": 2,\n" +
            "              \"autoColor\": true,\n" +
            "              \"groupColors\": {\n" +
            "                \n" +
            "              }\n" +
            "            }\n" +
            "          ],\n" +
            "          \"Chart Name\": \"1 Billion Rows\",\n" +
            "          \"Metric\": [\n" +
            "            {\n" +
            "              \"name\": \"count\"\n" +
            "            }\n" +
            "          ],\n" +
            "          \"Bar Color\": [\n" +
            "            {\n" +
            "              \"name\": \"count\",\n" +
            "              \"colorConfig\": {\n" +
            "                \"colorNumb\": 3,\n" +
            "                \"legendType\": \"palette\",\n" +
            "                \"colors\": [\n" +
            "                  \n" +
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
            "                \"metricsName\": \"Volume\",\n" +
            "                \"from\": 0,\n" +
            "                \"to\": 600000000,\n" +
            "                \"step\": 100000000,\n" +
            "                \"fromAuto\": true,\n" +
            "                \"toAuto\": true,\n" +
            "                \"stepAuto\": true,\n" +
            "                \"logScaleEnabled\": false,\n" +
            "                \"type\": \"yAxis\"\n" +
            "              }\n" +
            "            ],\n" +
            "            \"reflines\": [\n" +
            "              \n" +
            "            ]\n" +
            "          },\n" +
            "          \"_SubheadFiltersList\": true\n" +
            "        },\n" +
            "        \"filters\": [\n" +
            "          {\n" +
            "            \"path\": \"record_min\",\n" +
            "            \"value\": [\n" +
            "              \"2013-11-10 22:00:00.000\",\n" +
            "              \"2013-11-11 21:59:59.999\"\n" +
            "            ],\n" +
            "            \"operation\": \"BETWEEN\"\n" +
            "          }\n" +
            "        ],\n" +
            "        \"sourceId\": \"589b6174e4b013e5f420137a\",\n" +
            "        \"sourceName\": \"1 Billion Rows\",\n" +
            "        \"sourceType\": \"IMPALA\",\n" +
            "        \"sparkIt\": false,\n" +
            "        \"playbackMode\": false,\n" +
            "        \"textSearchEnabled\": false,\n" +
            "        \"live\": false\n" +
            "      },\n" +
            "      \"dashboardLink\": {\n" +
            "        \"inheritFilterCfg\": true\n" +
            "      },\n" +
            "      \"controlsCfg\": {\n" +
            "        \"id\": \"58ed5065e4b06dfd61dd8665\",\n" +
            "        \"visualizationDefId\": \"58ed1d96e4b03f8d866ef1a0\",\n" +
            "        \"timeControlCfg\": {\n" +
            "          \n" +
            "        },\n" +
            "        \"playerControlCfg\": {\n" +
            "          \n" +
            "        }\n" +
            "      }\n" +
            "    }\n" +
            "  ],\n" +
            "  \"shareState\": \"NOT_SHARED\",\n" +
            "  \"rememberTime\": true,\n" +
            "  \"unifiedBarCfgs\": [\n" +
            "    {\n" +
            "      \"id\": \"58ed5065e4b06dfd61dd8665\",\n" +
            "      \"dashboardId\": \"58ed1d96e4b03f8d866ef19e\",\n" +
            "      \"timeControlCfg\": {\n" +
            "        \n" +
            "      },\n" +
            "      \"playerControlCfg\": {\n" +
            "        \n" +
            "      },\n" +
            "      \"widgetIds\": [\n" +
            "        \"d2e07cddb3b7e34f9ec6ce713c92bb24\"\n" +
            "      ],\n" +
            "      \"visualizationDefId\": \"58ed1d96e4b03f8d866ef1a0\"\n" +
            "    }\n" +
            "  ],\n" +
            "  \"type\": \"UBER_BARS\",\n" +
            "  \"createdByUserID\": \"5898841fe4b0c9dbdb601338\",\n" +
            "  \"lastModifiedByUserID\": \"5898841fe4b0c9dbdb601338\",\n" +
            "  \"createdDate\": \"2017-04-11 18:16:54.960\",\n" +
            "  \"lastModifiedDate\": \"2017-04-11 21:53:41.028\",\n" +
            "  \"accountId\": \"5898841fe4b0c9dbdb601337\",\n" +
            "  \"userId\": \"5898841fe4b0c9dbdb601338\",\n" +
            "  \"viewsCount\": 1,\n" +
            "  \"thumbnailDate\": \"2017-04-11 19:39:34.644\",\n" +
            "  \"selectedWidgetId\": \"d2e07cddb3b7e34f9ec6ce713c92bb24\",\n" +
            "  \"ownerName\": \"admin\"\n" +
            "}";



    static private final String REQUEST_TEMPLATE_PIE = "{\n" +
            "  \n" +
            "  \"name\": \"__VIS_NAME__\",\n" +
            "  \"accountId\": \"57582988e4b0628b8d8d4452\",\n" +
            "  \"userId\": \"57582988e4b0628b8d8d4453\",\n" +
            "  \"description\": \"\",\n" +
            "  \"layout\": \"unset\",\n" +
            "  \"icon\": \"pie\",\n" +
            "  \"shareState\": \"NOT_SHARED\",\n" +
            "  \"rememberTime\": true,\n" +
            "  \"viewsCount\": 0,\n" +
            "  \"showDescription\": false,\n" +
            "  \"unifiedBarCfgs\": [\n" +
            "    {\n" +
            "      \"id\": \"58ed10d6e4b00f194a294412\",\n" +
            "      \"dashboardId\": \"58ed10d6e4b00f194a294411\",\n" +
            "      \"timeControlCfg\": {\n" +
            "        \"timeField\": \"sale_date\"\n" +
            "      },\n" +
            "      \"widgetIds\": [\n" +
            "        \"040fc86c5767008c93084996e6fc3aa0\"\n" +
            "      ]\n" +
            "    }\n" +
            "  ],\n" +
            "  \"visualizations\": [\n" +
            "    {\n" +
            "      \"id\": \"58ed10d6e4b00f194a294413\",\n" +
            "      \"visId\": \"57582997e4b0628b8d8d447a\",\n" +
            "      \"ownerDashboardId\": \"58ed10d6e4b00f194a294411\",\n" +
            "      \"name\": \"Pie\",\n" +
            "      \"type\": \"PIE\",\n" +
            "      \"enabled\": false,\n" +
            "      \"widgetId\": \"040fc86c5767008c93084996e6fc3aa0\",\n" +
            "      \"layout\": {\n" +
            "        \"col\": 1,\n" +
            "        \"row\": 1,\n" +
            "        \"rowSpan\": 4,\n" +
            "        \"colSpan\": 4\n" +
            "      },\n" +
            "      \"source\": {\n" +
            "        \"variables\": {\n" +
            "          \"Chart Name\": \"\",\n" +
            "          \"Size\": \"__METRIC_FUNC__\",\n" +
            "          \"Chart Description\": \"\",\n" +
            "          \"Group By\": \"{\\\"sort\\\":{\\\"dir\\\":\\\"desc\\\",\\\"name\\\":\\\"count\\\"},\\\"limit\\\":20,\\\"name\\\":\\\"__ATTRIBUTE__\\\",\\\"label\\\":\\\"__ATTRIBUTE__\\\",\\\"type\\\":\\\"ATTRIBUTE\\\",\\\"groupColorSet\\\":\\\"ZoomPalette\\\",\\\"autoShowColorLegend\\\":true,\\\"colorNumb\\\":20,\\\"autoColor\\\":true,\\\"groupColors\\\":{\\\"Los Angeles\\\":\\\"#0095b7\\\",\\\"Arlington Heights\\\":\\\"#a0b774\\\",\\\"Houston\\\":\\\"#f4c658\\\",\\\"Phoenix\\\":\\\"#fe8b3e\\\",\\\"Addison\\\":\\\"#cf2f23\\\",\\\"Buena Park\\\":\\\"#756c56\\\",\\\"Auburn\\\":\\\"#007896\\\",\\\"Brooklyn\\\":\\\"#47a694\\\",\\\"Hialeah\\\":\\\"#f9a94b\\\",\\\"Glen Oaks\\\":\\\"#ff6b30\\\",\\\"Corona\\\":\\\"#e94d29\\\",\\\"Arlington\\\":\\\"#005b76\\\",\\\"Alta Loma\\\":\\\"#0095b7\\\",\\\"The Lakes\\\":\\\"#a0b774\\\",\\\"Los Altos\\\":\\\"#f4c658\\\",\\\"Atascosa\\\":\\\"#fe8b3e\\\",\\\"Allen Park\\\":\\\"#cf2f23\\\",\\\"Dania\\\":\\\"#756c56\\\",\\\"Saint Paul\\\":\\\"#007896\\\",\\\"Aurora\\\":\\\"#47a694\\\"}}\",\n" +
            "          \"_custom_variables\": \"[{\\\"name\\\":\\\"UberBarsSettings\\\",\\\"value\\\":{\\\"showAbsoluteValues\\\":true,\\\"showRelativeValues\\\":true,\\\"showGroupLabels\\\":true}},{\\\"name\\\":\\\"font\\\",\\\"value\\\":{\\\"title\\\":{\\\"size\\\":\\\"normal\\\",\\\"typeface\\\":\\\"sans\\\",\\\"align\\\":\\\"left\\\",\\\"style\\\":\\\"\\\"}}},{\\\"name\\\":\\\"rulers\\\",\\\"value\\\":{\\\"gridlines\\\":{\\\"X1grid\\\":false,\\\"Y1grid\\\":false,\\\"X2grid\\\":false,\\\"Y2grid\\\":false},\\\"axis\\\":[],\\\"reflines\\\":[]}}]\"\n" +
            "        },\n" +
            "        \"filters\": [],\n" +
            "        \"sourceId\": \"__SOURCE_ID__\",\n" +
            "        \"sparkIt\": false,\n" +
            "        \"sourceName\": \"rts_1m\",\n" +
            "        \"playbackMode\": false,\n" +
            "        \"sourceType\": \"EDC2\",\n" +
            "        \"textSearchEnabled\": false,\n" +
            "        \"live\": false\n" +
            "      },\n" +
            "      \"dashboardLink\": {\n" +
            "        \"bookmarkId\": null,\n" +
            "        \"inheritTimeCfg\": true,\n" +
            "        \"bookmarkName\": null\n" +
            "      },\n" +
            "      \"controlsCfg\": {\n" +
            "        \"id\": \"58ed10d6e4b00f194a294414\",\n" +
            "        \"visualizationDefId\": \"58ed10d6e4b00f194a294413\",\n" +
            "        \"timeControlCfg\": {\n" +
            "          \"timeField\": \"sale_date\"\n" +
            "        }\n" +
            "      },\n" +
            "      \"lastModified\": 0\n" +
            "    }\n" +
            "  ]\n" +
            "}";



    static private final String REQUEST_TEMPLATE_BAR = "{\n" +
            "  \"name\": \"__VIS_NAME__\",\n" +
            "  \"accountId\": \"57582988e4b0628b8d8d4452\",\n" +
            "  \"description\": \"\",\n" +
            "  \"layout\": \"unset\",\n" +
            "  \"icon\": \"uber_bars\",\n" +
            "  \"shareState\": \"NOT_SHARED\",\n" +
            "  \"rememberTime\": true,\n" +
            "  \"viewsCount\": 0,\n" +
            "  \"showDescription\": false,\n" +
            "  \"unifiedBarCfgs\": [\n" +
            "    {\n" +
            "      \"id\": \"58ecf8c8e4b00f194a294386\",\n" +
            "      \"dashboardId\": \"58ecf8c8e4b00f194a294385\",\n" +
            "      \"timeControlCfg\": {\n" +
            "        \"timeField\": \"sale_date\"\n" +
            "      },\n" +
            "      \"widgetIds\": [\n" +
            "        \"278481987a45fea296d84902d82fe05e\"\n" +
            "      ]\n" +
            "    }\n" +
            "  ],\n" +
            "  \"visualizations\": [\n" +
            "    {\n" +
            "      \"id\": \"58ecf8c8e4b00f194a294387\",\n" +
            "      \"visId\": \"57582997e4b0628b8d8d4464\",\n" +
            "      \"ownerDashboardId\": \"58ecf8c8e4b00f194a294385\",\n" +
            "      \"name\": \"Bars\",\n" +
            "      \"type\": \"UBER_BARS\",\n" +
            "      \"enabled\": false,\n" +
            "      \"widgetId\": \"278481987a45fea296d84902d82fe05e\",\n" +
            "      \"layout\": {\n" +
            "        \"col\": 1,\n" +
            "        \"row\": 1,\n" +
            "        \"rowSpan\": 4,\n" +
            "        \"colSpan\": 4\n" +
            "      },\n" +
            "      \"source\": {\n" +
            "        \"variables\": {\n" +
            "          \"Multi Group By\": \"[{\\\"sort\\\":{\\\"dir\\\":\\\"desc\\\",\\\"name\\\":\\\"count\\\"},\\\"limit\\\":50,\\\"name\\\":\\\"__ATTRIBUTE__\\\",\\\"label\\\":\\\"Gender\\\",\\\"type\\\":\\\"ATTRIBUTE\\\",\\\"groupColorSet\\\":\\\"ZoomSequential\\\",\\\"autoShowColorLegend\\\":false,\\\"colorNumb\\\":2,\\\"autoColor\\\":true,\\\"groupColors\\\":{}}]\",\n" +
            "          \"Chart Name\": \"\",\n" +
            "          \"Metric\": \"__METRIC_FUNC__\",\n" +
            "          \"Bar Color\": \"count::{\\\"colorNumb\\\":3,\\\"legendType\\\":\\\"palette\\\",\\\"colors\\\":[{\\\"name\\\":\\\"Color 1\\\",\\\"color\\\":\\\"#ffc65f\\\"},{\\\"name\\\":\\\"Color 2\\\",\\\"color\\\":\\\"#9eb778\\\"},{\\\"name\\\":\\\"Color 3\\\",\\\"color\\\":\\\"#0096b6\\\"}],\\\"colorSet\\\":\\\"ZoomSequential\\\",\\\"autoShowColorLegend\\\":true,\\\"autoColor\\\":true,\\\"colorPositions\\\":null,\\\"colorScaleType\\\":\\\"gradient\\\"} \",\n" +
            "          \"Chart Description\": \"\",\n" +
            "          \"_custom_variables\": \"[{\\\"name\\\":\\\"UberBarsSettings\\\",\\\"value\\\":{\\\"showXGrid\\\":true,\\\"showYGrid\\\":true,\\\"chartType\\\":\\\"plain\\\",\\\"chartOrientation\\\":\\\"vertical\\\",\\\"thickness\\\":\\\"100\\\",\\\"showAbsoluteValues\\\":false,\\\"showRelativeValues\\\":false,\\\"showGroupLabels\\\":true,\\\"enableLogScale\\\":false}},{\\\"name\\\":\\\"font\\\",\\\"value\\\":{\\\"barLabels\\\":{\\\"size\\\":\\\"normal\\\",\\\"typeface\\\":\\\"sans\\\",\\\"align\\\":\\\"left\\\",\\\"style\\\":\\\"\\\"},\\\"title\\\":{\\\"size\\\":\\\"normal\\\",\\\"typeface\\\":\\\"sans\\\",\\\"align\\\":\\\"left\\\",\\\"style\\\":\\\"\\\"}}},{\\\"name\\\":\\\"rulers\\\",\\\"value\\\":{\\\"gridlines\\\":{\\\"X1grid\\\":false,\\\"Y1grid\\\":true,\\\"X2grid\\\":false,\\\"Y2grid\\\":false},\\\"axis\\\":[{\\\"axis\\\":\\\"Metric\\\",\\\"name\\\":\\\"Metric\\\",\\\"metricsName\\\":\\\"__METRIC_NAME__\\\",\\\"from\\\":0,\\\"to\\\":1000000,\\\"step\\\":100000,\\\"fromAuto\\\":true,\\\"toAuto\\\":true,\\\"stepAuto\\\":true,\\\"logScaleEnabled\\\":false,\\\"type\\\":\\\"yAxis\\\"}],\\\"reflines\\\":[]}}]\"\n" +
            "        },\n" +
            "        \"filters\": [],\n" +
            "        \"sourceId\": \"__SOURCE_ID__\",\n" +
            "        \"sparkIt\": false,\n" +
            "        \"sourceName\": \"rts_1m\",\n" +
            "        \"playbackMode\": false,\n" +
            "        \"sourceType\": \"EDC2\",\n" +
            "        \"textSearchEnabled\": false,\n" +
            "        \"live\": false\n" +
            "      },\n" +
            "      \"dashboardLink\": {\n" +
            "        \"bookmarkId\": null,\n" +
            "        \"inheritTimeCfg\": true,\n" +
            "        \"bookmarkName\": null\n" +
            "      },\n" +
            "      \"controlsCfg\": {\n" +
            "        \"id\": \"58ecf8c8e4b00f194a294388\",\n" +
            "        \"visualizationDefId\": \"58ecf8c8e4b00f194a294387\",\n" +
            "        \"timeControlCfg\": {\n" +
            "          \"timeField\": \"sale_date\"\n" +
            "        }\n" +
            "      },\n" +
            "      \"lastModified\": 0\n" +
            "    }\n" +
            "  ]\n" +
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

        } else if ("DistributionIntent".equals(intentName)) {
            return doDistributionIntent(intent, session);
            // throw new SpeechletException("Invalid Intent");

        } else if ("TopIntent".equals(intentName)) {
            return doTopIntent(intent, session);
            // throw new SpeechletException("Invalid Intent");

        } else if ("RealTimeIntent".equals(intentName)) {
            return doRealTimeIntent(intent, session);
            // throw new SpeechletException("Invalid Intent");

        } else if ("RealTimeIntentTrends".equals(intentName)) {
            return doRealTimeIntentTrends(intent, session);
            // throw new SpeechletException("Invalid Intent");

        }
        else if ("RealTimeIntentTweets".equals(intentName)) {
            return doRealTimeIntentTweets(intent, session);
            // throw new SpeechletException("Invalid Intent");

        }else if ("WelcomeIntent".equals(intentName)) {
            //return getColorFromSession(intent, session);
            return doWelcome(intent,session);
            //  return doWelcomeRealTime(intent, session);
            // return doWelcomeRealTweets(intent, session);
            // return doWelcomeRealTrends(intent,session);

        }else if ("WelcomeIntentRealtime".equals(intentName)) {
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


        mapMetricToUnitSpeech.put("sales", "dollars");
        mapMetricToUnitSpeech.put("plannedsales", "dollars");
        mapMetricToUnitSpeech.put("price", "dollars");

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
                attribute = "gender";
            } else if (attribute.contains("group")) {
                attribute = "group_name";
            } else if ((attribute.contains("city")) || (attribute.contains("citi")) || (attribute.contains("CD")) || (attribute.contains("ct"))) {
                attribute = "usercity";
            } else if ((attribute.contains("state")) || (attribute.contains("states"))) {
                attribute = "userstate";
            } else if (attribute.contains("income")) {
                attribute = "userincome";
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
            final String bookmarkName = "Alexa_" + chartType + "_" + metric + "_" + attribute;

            speechText =
                    String.format("You are asking to build %s chart for %s based on %s. ",
                            chartType, metric, attribute.replace("_", " "));
            final String sourceId =
                    "576826d7e4b0628b8d8d4cec";
            try {

                if (metric.contains("sales")) {
                    metric = "plannedsales";
                }

                if (chartType.toLowerCase().equals("bar")) {
                    chartType = "UBER_BARS";
                } else {
                    chartType = "PIE";
                }
                final String function = "sum";

                visPregen2(bookmarkName, sourceId, chartType, metric, attribute);
                speechText += "Congratulations! Your chart is ready under the name  " + bookmarkName.replace("_", " ");

                final JSONObject jsonObject = getData(metric, function, attribute, sourceId, filters, timeRangeType);
                final int size = jsonObject.getInt("available");

                String unitSpeech = mapMetricToUnitSpeech.get(metric);
                if (StringUtils.isEmpty(unitSpeech)) {
                    unitSpeech = metric;
                }

                speechText += " I found " + size + " " + attributeSpeech.trim() + "s .";

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

        final String sourceId = "57582ab1e4b0628b8d8d44de";
                // "5752f18560b2b9cf90fce508";
        try {
            final String function = "count";

            //   visPregen(bookmarkName, sourceId, chartType, metric, attribute);
            //  speechText += "Congratulations! Your chart is ready under the name  " + bookmarkName.replace("_", " ");

            final JSONObject jsonObject = getDataFruits(metric, function, attribute, sourceId, filters, filterField, timeRangeType);
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

        final String sourceId = "5752f18560b2b9cf90fce508";
        try {
            final String function = "count";

            //   visPregen(bookmarkName, sourceId, chartType, metric, attribute);
            //  speechText += "Congratulations! Your chart is ready under the name  " + bookmarkName.replace("_", " ");

            final JSONObject jsonObject = getDataTweets(metric, function, attribute, sourceId, filters, filterField, timeRangeType);
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

        final String sourceId = "57582c37e4b0628b8d8d4580";
                // "5753258360b2b9cf90fd18e3";
        try {
            final String function = "count";

            //   visPregen(bookmarkName, sourceId, chartType, metric, attribute);
            //  speechText += "Congratulations! Your chart is ready under the name  " + bookmarkName.replace("_", " ");

            final JSONObject jsonObject = getDataTweets(metric, function, attribute, sourceId, filters, filterField, timeRangeType);
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
                speechText += count +" tweets related to Zoom Data and Spark Summit. Please visit zoom data preview to visualize your real-time chart for more details.";
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

        final String sourceId = "57582c37e4b0628b8d8d4580";
                //"5752eaa260b2b9cf90fcdd4c";
        try {
            final String function = "count";

            //   visPregen(bookmarkName, sourceId, chartType, metric, attribute);
            //  speechText += "Congratulations! Your chart is ready under the name  " + bookmarkName.replace("_", " ");

            final JSONObject jsonObject = getDataTrends(metric, function, attribute, sourceId, filters, filterField, timeRangeType);
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
                        "Goodbye! Details: " + ExceptionUtils.getStackTrace(e) ;
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
            attribute = "gender";
        } else if ((attribute.contains("city")) || (attribute.contains("citi")) || (attribute.contains("CD")) || (attribute.contains("ct"))) {
            attribute = "usercity";
        } else if (attribute.contains("state")) {
            attribute = "userstate";
        } else if ((attribute.contains("income")) || (attribute.contains("in"))) {
            attribute = "userincome";
        }else if (attribute.contains("group")) {
            attribute = "group_name";
        } else if ((attribute.contains("time")) || (attribute.contains("date")) || (attribute.contains("timestamp"))) {
            attribute = "sale_date";
        }
        String metric = metricSlot.getValue();
        if (StringUtils.isEmpty(metric)) {
            metric = "sales";
        }

        final String chartType = "UBER_BARS";

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
        final String sourceId =
                "576826d7e4b0628b8d8d4cec";
               // "57652ad160b2b9cf910e5227";
               //  "56f5ab2760b2b5bf51a01d09";
        String function = "sum";

        try {

            if (metric.toLowerCase().contains("sales")) {
                metric = "plannedsales";
            } else if ((metric.equals("volume")) ||
                    (metric.equals("count")) ||
                    (metric.contains("transaction"))) {
                metric = "*";
                function = "count";
            }

            visPregen2(bookmarkName, sourceId, chartType, metric, attribute);
            // speechText +="Congratulations! Your chart is ready under the name  " + bookmarkName.replace("_", " ");

            final JSONObject jsonObject = getData(metric, function, attribute, sourceId, timeFilterValue, timeRangeType);
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
                        final String currencyValue = currencyFormat.format(groupValue);

                        speechText += ". " + attributeSpeech + " " + groupNameSpeech + " has " + currencyValue +" in sales.";

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
        final String techTalk  ="Welcome all to the Zoom data Tech Talk . In this tech talk, Quan Luu will share the " +
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
                " I am a hands-free speaker you control with your voice. I connect to the Alexa Voice Service to play "+
                " music, provide information, news, sports scores, weather, and more instantly. " +
                " You can ask me: Alexa, what’s the weather today? Alexa, tell me a joke. Alexa, play some music. " +
                " Alexa, what’s on the news today. Again, "+techTalk +
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
                            (welcomeAttribute.toLowerCase().contains("team"))){
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

    private static final String DATA_REQUEST_TEMPLATE="{\n" +
            "  \"type\": \"START_VIS\",\n" +
            "  \"cid\": \"5dc56813f3c8798a8786858a3748ec3c\",\n" +
            "  \"sourceId\": \"589b6174e4b013e5f420137a\",\n" +
            "  \"filters\": [\n" + "{\n" +
            "      \"operation\": \"BETWEEN\",\n" +
            "      \"value\": [\n" +
            "        \"2013-11-10 22:00:00.000\",\n" +
            "        \"$now\"\n" +
            "      ],\n" +
            "      \"path\": {\n" +
            "        \"name\": \"record_min\"\n" +
            "      }\n" +
            "    }" +
            "\n" +
            "  ],\n" +
            "  \"time\": null,\n" +
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

    public JSONObject getData25(final String metric,
                              final String function,
                              final String groupBy,
                              final String sourceId,
                              final String filters,
                              final String timeRangeType) throws Exception {


        final String filterField = "sale_date"; // TODO: Hard code for now.

     //   final String sortBy = metric; // "count";

        final Map<String, String> headers = new HashMap<>();
        final String credential = "admin:Z00mda1a";
        final String encoding = DatatypeConverter.printBase64Binary(credential.getBytes());
        headers.put("Authorization", "Basic " + encoding);
        headers.put("Cookie",
                "JSESSIONID=0DF6B3538CF76D387B3FCEA297EB6F55; _ga=GA1.2.1509295338.1415671928; __utma=41381033.1509295338.1415671928.1436360392.1436794843.51; __utmc=41381033; __utmz=41381033.1435595058.49.11.utmcsr=google|utmccn=(organic)|utmcmd=organic|utmctr=(not%20provided); JSESSIONID=6B977DF17C10D7D7FC3BF05D4ED416C9; __utmt=1; __utma=108567959.515813076.1429193063.1436825769.1436885790.135; __utmb=108567959.5.10.1436885790; __utmc=108567959; __utmz=108567959.1429193063.1.1.utmcsr=(direct)|utmccn=(direct)|utmcmd=(none)");
        final String websocket =
            "wss://2-5-latest.zoomdata.com:8443/zoomdata/websocket";

        final ZoomdataWebsocketSecuredClient websocketClient = new ZoomdataWebsocketSecuredClient(websocket, headers);

        final String request = DATA_REQUEST_TEMPLATE.replace("__METRIC_NAME__", metric)
                .replace("__METRIC_FUNC__", function.toUpperCase())
                .replace("__ATTRIBUTE__", groupBy);

        final JSONObject jsonRequest = new JSONObject(request);

        if (metric.equals("*")) {
            final String starMetric="[\n" +
                    "    {\n" +
                    "      \"type\": \"COUNT\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"type\": \"COUNT\"\n" +
                    "    }\n" +
                    "  ]";
            jsonRequest.put("metrics", new JSONArray(starMetric));
        }

        if (filters != null) {
            // TODO: modify filter here.
        }
        jsonRequest.put("sourceId", sourceId);
        jsonRequest.put("cid", UUID.randomUUID().toString());

        final String message = jsonRequest.toString();
        logger.info(message);
        websocketClient.connectAndSend(message);

        logger.info(Thread.currentThread() + ": isDone = " + websocketClient.isDone());

        while (websocketClient.isDone() == false) {
            int size = 0;
            JSONObject data = websocketClient.getJsonResponseData();
            if (data != null) {
                size = data.toString().length();
            }
            logger.info(Thread.currentThread() + ": Checking async response ... isDone = " +
                    websocketClient.isDone() + ", response data size=" + size);
            Thread.sleep(1000);
        }

        logger.info(Thread.currentThread() + ": isDone = " + websocketClient.isDone());

        final JSONObject jsonResponseData = websocketClient.getJsonResponseData();

        websocketClient.close();

        logger.info("Got response: " + jsonResponseData);
        if (jsonResponseData == null) {
            throw new Exception("Got response data from request=" + message);
        }
        return jsonResponseData;
    }

    public JSONObject getData(final String metric,
                              final String function,
                              final String groupBy,
                              final String sourceId,
                              final String filters,
                              final String timeRangeType) throws Exception {

        final String filterField = "sale_date"; // TODO: Hard code for now.

       // final String sortBy = metric; // "count";

        final Map<String, String> headers = new HashMap<>();
        final String credential = "admin:Z00mda1a";
        final String encoding = DatatypeConverter.printBase64Binary(credential.getBytes());
        headers.put("Authorization", "Basic " + encoding);
        headers.put("Cookie",
                "JSESSIONID=0DF6B3538CF76D387B3FCEA297EB6F55; _ga=GA1.2.1509295338.1415671928; __utma=41381033.1509295338.1415671928.1436360392.1436794843.51; __utmc=41381033; __utmz=41381033.1435595058.49.11.utmcsr=google|utmccn=(organic)|utmcmd=organic|utmctr=(not%20provided); JSESSIONID=6B977DF17C10D7D7FC3BF05D4ED416C9; __utmt=1; __utma=108567959.515813076.1429193063.1436825769.1436885790.135; __utmb=108567959.5.10.1436885790; __utmc=108567959; __utmz=108567959.1429193063.1.1.utmcsr=(direct)|utmccn=(direct)|utmcmd=(none)");
        final String websocket =
                    "wss://zdlabs.zoomdata.com:8443/zoomdata/websocket";
                //"wss://2-5-latest.zoomdata.com:8443/zoomdata/websocket";



        //  "wss://preview.zoomdata.com:8443/zoomdata/websocket";
        final ZoomdataWebsocketSecuredClient websocketClient = new ZoomdataWebsocketSecuredClient(websocket, headers);

        final JSONObject jsonRequest = buildRequest(metric, function, groupBy, sourceId, filterField, filters, timeRangeType);

        final String message =
                 jsonRequest.toString();
                // "{\"type\":\"START_VIS\",\"cid\":\"e1c7ae1512281621b0d2df98f1c99dc3\",\"request\":{\"streamSourceId\":\"576826d7e4b0628b8d8d4cec\",\"cfg\":{\"filters\":[],\"player\":null,\"group\":{\"fields\":[{\"name\":\"record_hour\",\"sort\":{\"dir\":\"desc\",\"name\":\"count\"},\"limit\":50}],\"metrics\":[{\"name\":\"*\",\"func\":\"count\"}]}},\"time\":{\"timeField\":\"sale_date\"}}}";

        logger.info(message);
        websocketClient.connectAndSend(message);

        logger.info(Thread.currentThread() + ": isDone = " + websocketClient.isDone());

        while (websocketClient.isDone() == false) {
            int size = 0;
            JSONObject data = websocketClient.getJsonResponseData();
            if (data != null) {
                size = data.toString().length();
            }
            logger.info(Thread.currentThread() + ": Checking async response ... isDone = " +
                    websocketClient.isDone() + ", response data size=" + size);
            Thread.sleep(1000);
        }

        logger.info(Thread.currentThread() + ": isDone = " + websocketClient.isDone());

        final JSONObject jsonResponseData = websocketClient.getJsonResponseData();

        websocketClient.close();

        logger.info("Got response: " + jsonResponseData);
        if (jsonResponseData == null) {
            throw new Exception("Got response data from request=" + message);
        }
        return jsonResponseData;
    }

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


    public JSONObject getDataFruits(final String metric,
                                    final String function,
                                    final String groupBy,
                                    final String sourceId,
                                    final String filters,
                                    final String filterField,
                                    final String timeRangeType) throws Exception {

        /*
        final String groupBy = "usercity"; // "gender";
        final String metric = "plannedsales";
        final String sourceId = "56f5ab2760b2b5bf51a01d09";
        */
        //   final String filterField = "sale_date"; // TODO: Hard code for now.

        final String sortBy = metric; // "count";
        //       final String function = "sum";

        final Map<String, String> headers = new HashMap<>();
        final String credential = "admin:Z00mda1a";
        final String encoding = DatatypeConverter.printBase64Binary(credential.getBytes());
        headers.put("Authorization", "Basic " + encoding);
        headers.put("Cookie",
                "JSESSIONID=0DF6B3538CF76D387B3FCEA297EB6F55; _ga=GA1.2.1509295338.1415671928; __utma=41381033.1509295338.1415671928.1436360392.1436794843.51; __utmc=41381033; __utmz=41381033.1435595058.49.11.utmcsr=google|utmccn=(organic)|utmcmd=organic|utmctr=(not%20provided); JSESSIONID=6B977DF17C10D7D7FC3BF05D4ED416C9; __utmt=1; __utma=108567959.515813076.1429193063.1436825769.1436885790.135; __utmb=108567959.5.10.1436885790; __utmc=108567959; __utmz=108567959.1429193063.1.1.utmcsr=(direct)|utmccn=(direct)|utmcmd=(none)");
        final String websocket =
             "wss://zdlabs.zoomdata.com:8443/zoomdata/websocket";

        // "wss://preview.zoomdata.com:8443/zoomdata/websocket";
        final ZoomdataWebsocketSecuredClient websocketClient = new ZoomdataWebsocketSecuredClient(websocket, headers);

        final JSONObject jsonRequest = buildRequestFruits(metric, function, groupBy, sourceId, filterField, filters, timeRangeType);

        final String message =
                jsonRequest.toString();
        //"{\"type\":\"START_VIS\",\"cid\":\"" + System.nanoTime() + "\",\"request\":{\"streamSourceId\":\"" + sourceId + "\",\"cfg\":{\"filters\":[],\"player\":null,\"group\":{\"fields\":[{\"name\":\"" + groupBy + "\",\"sort\":{\"dir\":\"desc\",\"name\":\"" + sortBy + "\",\"metricFunc\":\"" + function + "\"},\"limit\":50}],\"metrics\":[{\"name\":\"" + metric + "\",\"func\":\"" + function + "\"},{\"name\":\"*\",\"func\":\"count\"}]}},\"time\":{\"timeField\":\"sale_date\"}}}";

        logger.info(message);
        websocketClient.connectAndSend(message);

        logger.info(Thread.currentThread() + ": isDone = " + websocketClient.isDone());

        while (websocketClient.isDone() == false) {
            int size = 0;
            JSONObject data = websocketClient.getJsonResponseData();
            if (data != null) {
                size = data.toString().length();
            }
            logger.info(Thread.currentThread() + ": Checking async response ... isDone = " +
                    websocketClient.isDone() + ", response data size=" + size);
            Thread.sleep(1000);
        }

        logger.info(Thread.currentThread() + ": isDone = " + websocketClient.isDone());

        final JSONObject jsonResponseData = websocketClient.getJsonResponseData();

        websocketClient.close();

        logger.info("Got response: " + jsonResponseData);
        if (jsonResponseData == null) {
            // return null;
            throw new Exception("Got null response data from request=" + message);
        }
        return jsonResponseData;
    }


    private JSONObject buildRequestFruits(final String metric,
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
                "  \"cid\": \"f640d8559e2362499845b07f47b8e63f\",\n" +
                "  \"request\": {\n" +
                "    \"streamSourceId\": \"5752f18560b2b9cf90fce508\",\n" +
                "    \"cfg\": {\n" +
                "      \"filters\": [\n" +
                "        \n" +
                "      ],\n" +
                "      \"player\": {\n" +
                "        \"timeWindowScale\": \"PINNED\",\n" +
                "        \"pauseAfterRead\": false\n" +
                "      },\n" +
                "      \"group\": {\n" +
                "        \"fields\": [\n" +
                "          {\n" +
                "            \"name\": \"fruit\",\n" +
                "            \"sort\": {\n" +
                "              \"dir\": \"desc\",\n" +
                "              \"name\": \"count\"\n" +
                "            },\n" +
                "            \"limit\": 50\n" +
                "          }\n" +
                "        ],\n" +
                "        \"metrics\": [\n" +
                "          {\n" +
                "            \"name\": \"*\",\n" +
                "            \"func\": \"count\"\n" +
                "          }\n" +
                "        ]\n" +
                "      }\n" +
                "    },\n" +
                "    \"time\": {\n" +
                "      \"from\": \"+$start_of_hour\",\n" + // or start_of_hour
                "      \"to\": \"+$now\",\n" +
                "      \"timeField\": \"updated_ts\"\n" +
                "    }\n" +
                "  }\n" +
                "}";

        final String timeField = "updated_ts";
        final JSONObject jsonRequest = new JSONObject(message);

        jsonRequest.put("cid", Long.toString(System.nanoTime()));
        jsonRequest.getJSONObject("request").put("streamSourceId", sourceId);
        final JSONObject jsonTimeField = jsonRequest.getJSONObject("request").getJSONObject("time");
        jsonTimeField.put("timeField", timeField);

        final JSONObject jsonConfig = jsonRequest.getJSONObject("request").getJSONObject("cfg");
        jsonConfig.getJSONObject("group").getJSONArray("fields").getJSONObject(0).put("name", groupBy);
        if (StringUtils.isEmpty(groupBy)) {
            jsonConfig.getJSONObject("group").put("fields", new JSONArray());
        } else {
            final JSONObject sort = jsonConfig.getJSONObject("group").getJSONArray("fields").getJSONObject(0).getJSONObject("sort");
            sort.put("name", metric);
            sort.put("metricFunc", func);
        }

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

    private JSONObject buildRequestTweets(final String metric,
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
        final String messageVolume = "{\n" +
                "  \"type\": \"START_VIS\",\n" +
                "  \"cid\": \"4fdca132f6be5978a8b4c17c030ecc04\",\n" +
                "  \"request\": {\n" +
                "    \"streamSourceId\": \"5753258360b2b9cf90fd18e3\",\n" +
                "    \"cfg\": {\n" +
                "      \"filters\": [\n" +
                "        {\n" +
                "          \"path\": \"hashtag_str\",\n" +
                "          \"operation\": \"IN\",\n" +
                "          \"value\": [\n" +
                "            \"zoomdata\",\n" +
                "            \"zoomdatalabs\",\n" +
                "            \"SparkSummit\",\n" +
                "            \"sparksummit\"\n" +
                "          ],\n" +
                "          \"form\": null\n" +
                "        }\n" +
                "      ],\n" +
                "      \"player\": {\n" +
                "        \"timeWindowScale\": \"PINNED\",\n" +
                "        \"pauseAfterRead\": false\n" +
                "      },\n" +
                "      \"group\": {\n" +
                "        \"fields\": [\n" +
                "          \n" +
                "        ],\n" +
                "        \"metrics\": [\n" +
                "          {\n" +
                "            \"name\": \"*\",\n" +
                "            \"func\": \"count\"\n" +
                "          }\n" +
                "        ]\n" +
                "      }\n" +
                "    },\n" +
                "    \"time\": {\n" +
                "      \"from\": \"+$start_of_hour\",\n" + // or start_of_hour
                "      \"to\": \"+$now\",\n" +
                "      \"timeField\": \"updated_ts\"\n" +
                "    }\n" +
                "  }\n" +
                "}";

        if (StringUtils.isEmpty(metric) == false) {
            if (groupBy.contains("hour")) {
                return new JSONObject(messageVolume);
            }
            return new JSONObject(messageVolume.replace("start_of_hour",groupBy));
        }

        final String message = "{\n" +
                "  \"type\": \"START_VIS\",\n" +
                "  \"cid\": \"b2c30305432d407ca4c333e766bcad95\",\n" +
                "  \"request\": {\n" +
                "    \"streamSourceId\": \"5753258360b2b9cf90fd18e3\",\n" +
                "    \"cfg\": {\n" +
                "      \"filters\": [\n" +
                "        {\n" +
                "          \"path\": \"hashtag_str\",\n" +
                "          \"operation\": \"IN\",\n" +
                "          \"value\": [\n" +
                "            \"zoomdata\",\n" +
                "            \"zoomdatalabs\",\n" +
                "            \"SparkSummit\",\n" +
                "            \"sparksummit\"\n" +
                "          ],\n" +
                "          \"form\": null\n" +
                "        }\n" +
                "      ],\n" +
                "      \"player\": {\n" +
                "        \"timeWindowScale\": \"PINNED\",\n" +
                "        \"pauseAfterRead\": false\n" +
                "      },\n" +
                "      \"group\": {\n" +
                "        \"fields\": [\n" +
                "          {\n" +
                "            \"name\": \"hashtag_str\",\n" +
                "            \"sort\": {\n" +
                "              \"dir\": \"desc\",\n" +
                "              \"name\": \"count\"\n" +
                "            },\n" +
                "            \"limit\": 50\n" +
                "          }\n" +
                "        ],\n" +
                "        \"metrics\": [\n" +
                "          {\n" +
                "            \"name\": \"*\",\n" +
                "            \"func\": \"count\"\n" +
                "          }\n" +
                "        ]\n" +
                "      }\n" +
                "    },\n" +
                "    \"time\": {\n" +
                "      \"from\": \"+$start_of_hour\",\n" +
                "      \"to\": \"+$now\",\n" +
                "      \"timeField\": \"updated_ts\"\n" +
                "    }\n" +
                "  }\n" +
                "}";

        final String timeField = "updated_ts";
        final JSONObject jsonRequest = new JSONObject(message);
        if (jsonRequest != null) {
            if (groupBy.contains("hour")) {
                return new JSONObject(message);
            }
            return new JSONObject(message.replace("start_of_hour",groupBy));
        }

        jsonRequest.put("cid", Long.toString(System.nanoTime()));
        jsonRequest.getJSONObject("request").put("streamSourceId", sourceId);
        final JSONObject jsonTimeField = jsonRequest.getJSONObject("request").getJSONObject("time");
        jsonTimeField.put("timeField", timeField);

        final JSONObject jsonConfig = jsonRequest.getJSONObject("request").getJSONObject("cfg");
        jsonConfig.getJSONObject("group").getJSONArray("fields").getJSONObject(0).put("name", groupBy);
        if (StringUtils.isEmpty(groupBy)) {
            jsonConfig.getJSONObject("group").put("fields", new JSONArray());
        } else {
            final JSONObject sort = jsonConfig.getJSONObject("group").getJSONArray("fields").getJSONObject(0).getJSONObject("sort");
            sort.put("name", metric);
            sort.put("metricFunc", func);
        }

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
            //jsonConfig.put("filters", filterArray);
        }


        return jsonRequest;

    }


    public JSONObject getDataTweets(final String metric,
                                    final String function,
                                    final String groupBy,
                                    final String sourceId,
                                    final String filters,
                                    final String filterField,
                                    final String timeRangeType) throws Exception {

        /*
        final String groupBy = "usercity"; // "gender";
        final String metric = "plannedsales";
        final String sourceId = "56f5ab2760b2b5bf51a01d09";
        */
        //   final String filterField = "sale_date"; // TODO: Hard code for now.

        //  final String sortBy = metric; // "count";
        //       final String function = "sum";

        final Map<String, String> headers = new HashMap<>();
        final String credential = "admin:Z00mda1a";
        final String encoding = DatatypeConverter.printBase64Binary(credential.getBytes());
        headers.put("Authorization", "Basic " + encoding);
        headers.put("Cookie",
                "JSESSIONID=0DF6B3538CF76D387B3FCEA297EB6F55; _ga=GA1.2.1509295338.1415671928; __utma=41381033.1509295338.1415671928.1436360392.1436794843.51; __utmc=41381033; __utmz=41381033.1435595058.49.11.utmcsr=google|utmccn=(organic)|utmcmd=organic|utmctr=(not%20provided); JSESSIONID=6B977DF17C10D7D7FC3BF05D4ED416C9; __utmt=1; __utma=108567959.515813076.1429193063.1436825769.1436885790.135; __utmb=108567959.5.10.1436885790; __utmc=108567959; __utmz=108567959.1429193063.1.1.utmcsr=(direct)|utmccn=(direct)|utmcmd=(none)");
        final String websocket =
             "wss://zdlabs.zoomdata.com:8443/zoomdata/websocket";

        // "wss://preview.zoomdata.com:8443/zoomdata/websocket";
        final ZoomdataWebsocketSecuredClient websocketClient = new ZoomdataWebsocketSecuredClient(websocket, headers);

        final JSONObject jsonRequest = buildRequestTweets(metric, function, groupBy, sourceId, filterField, filters, timeRangeType);

        final String message =
                jsonRequest.toString();
        //"{\"type\":\"START_VIS\",\"cid\":\"" + System.nanoTime() + "\",\"request\":{\"streamSourceId\":\"" + sourceId + "\",\"cfg\":{\"filters\":[],\"player\":null,\"group\":{\"fields\":[{\"name\":\"" + groupBy + "\",\"sort\":{\"dir\":\"desc\",\"name\":\"" + sortBy + "\",\"metricFunc\":\"" + function + "\"},\"limit\":50}],\"metrics\":[{\"name\":\"" + metric + "\",\"func\":\"" + function + "\"},{\"name\":\"*\",\"func\":\"count\"}]}},\"time\":{\"timeField\":\"sale_date\"}}}";

        logger.info(message);
        websocketClient.connectAndSend(message);

        logger.info(Thread.currentThread() + ": isDone = " + websocketClient.isDone());

        while (websocketClient.isDone() == false) {
            int size = 0;
            JSONObject data = websocketClient.getJsonResponseData();
            if (data != null) {
                size = data.toString().length();
            }
            logger.info(Thread.currentThread() + ": Checking async response ... isDone = " +
                    websocketClient.isDone() + ", response data size=" + size);
            Thread.sleep(1000);
        }

        logger.info(Thread.currentThread() + ": isDone = " + websocketClient.isDone());

        final JSONObject jsonResponseData = websocketClient.getJsonResponseData();

        websocketClient.close();

        logger.info("Got response: " + jsonResponseData);
        if (jsonResponseData == null) {
            throw new Exception("Got response data from request=" + message);
        }
        return jsonResponseData;
    }


    public JSONObject getDataTrends(final String metric,
                                    final String function,
                                    final String groupBy,
                                    final String sourceId,
                                    final String filters,
                                    final String filterField,
                                    final String timeRangeType) throws Exception {

        /*
        final String groupBy = "usercity"; // "gender";
        final String metric = "plannedsales";
        final String sourceId = "56f5ab2760b2b5bf51a01d09";
        */
        //   final String filterField = "sale_date"; // TODO: Hard code for now.

        //  final String sortBy = metric; // "count";
        //       final String function = "sum";

        final Map<String, String> headers = new HashMap<>();
        final String credential = "admin:Z00mda1a";
        final String encoding = DatatypeConverter.printBase64Binary(credential.getBytes());
        headers.put("Authorization", "Basic " + encoding);
        headers.put("Cookie",
                "JSESSIONID=0DF6B3538CF76D387B3FCEA297EB6F55; _ga=GA1.2.1509295338.1415671928; __utma=41381033.1509295338.1415671928.1436360392.1436794843.51; __utmc=41381033; __utmz=41381033.1435595058.49.11.utmcsr=google|utmccn=(organic)|utmcmd=organic|utmctr=(not%20provided); JSESSIONID=6B977DF17C10D7D7FC3BF05D4ED416C9; __utmt=1; __utma=108567959.515813076.1429193063.1436825769.1436885790.135; __utmb=108567959.5.10.1436885790; __utmc=108567959; __utmz=108567959.1429193063.1.1.utmcsr=(direct)|utmccn=(direct)|utmcmd=(none)");
        final String websocket =
             "wss://zdlabs.zoomdata.com:8443/zoomdata/websocket";

        // "wss://preview.zoomdata.com:8443/zoomdata/websocket";
        final ZoomdataWebsocketSecuredClient websocketClient = new ZoomdataWebsocketSecuredClient(websocket, headers);

        final JSONObject jsonRequest = buildRequestTrends(metric, function, groupBy, sourceId, filterField, filters, timeRangeType);

        final String message =
                jsonRequest.toString();
        //"{\"type\":\"START_VIS\",\"cid\":\"" + System.nanoTime() + "\",\"request\":{\"streamSourceId\":\"" + sourceId + "\",\"cfg\":{\"filters\":[],\"player\":null,\"group\":{\"fields\":[{\"name\":\"" + groupBy + "\",\"sort\":{\"dir\":\"desc\",\"name\":\"" + sortBy + "\",\"metricFunc\":\"" + function + "\"},\"limit\":50}],\"metrics\":[{\"name\":\"" + metric + "\",\"func\":\"" + function + "\"},{\"name\":\"*\",\"func\":\"count\"}]}},\"time\":{\"timeField\":\"sale_date\"}}}";

        logger.info(message);
        websocketClient.connectAndSend(message);

        logger.info(Thread.currentThread() + ": isDone = " + websocketClient.isDone());

        while (websocketClient.isDone() == false) {
            int size = 0;
            JSONObject data = websocketClient.getJsonResponseData();
            if (data != null) {
                size = data.toString().length();
            }
            logger.info(Thread.currentThread() + ": Checking async response ... isDone = " +
                    websocketClient.isDone() + ", response data size=" + size);
            Thread.sleep(1000);
        }

        logger.info(Thread.currentThread() + ": isDone = " + websocketClient.isDone());

        final JSONObject jsonResponseData = websocketClient.getJsonResponseData();

        websocketClient.close();

        logger.info("Got response: " + jsonResponseData);
        if (jsonResponseData == null) {
            throw new Exception("Got response data from request=" + message);
        }
        return jsonResponseData;
    }

    private JSONObject buildRequestTrends(final String metric,
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
        /*
    "time": {
      "from": "+$start_of_hour_-1_hour",
      "to": "+$end_of_hour_-1_hour",
      "timeField": "updated_ts"
    }

         */

        final String message = "{\n" +
                "  \"type\": \"START_VIS\",\n" +
                "  \"cid\": \"f9d9e6c95f73ca6bb7b94c21db1208b6\",\n" +
                "  \"request\": {\n" +
                "    \"streamSourceId\": \"5752eaa260b2b9cf90fcdd4c\",\n" +
                "    \"cfg\": {\n" +
                "      \"filters\": [\n" +
                "        {\n" +
                "          \"path\": \"hashtag_str\",\n" +
                "          \"operation\": \"NOTIN\",\n" +
                "          \"value\": [\n" +
                "            \"\"\n" +
                "          ],\n" +
                "          \"form\": null\n" +
                "        }\n" +
                "      ],\n" +
                "      \"player\": {\n" +
                "        \"timeWindowScale\": \"ROLLING\",\n" +
                "        \"pauseAfterRead\": false\n" +
                "      },\n" +
                "      \"group\": {\n" +
                "        \"fields\": [\n" +
                "          {\n" +
                "            \"name\": \"hashtag_str\",\n" +
                "            \"sort\": {\n" +
                "              \"dir\": \"desc\",\n" +
                "              \"name\": \"count\"\n" +
                "            },\n" +
                "            \"limit\": 50\n" +
                "          }\n" +
                "        ],\n" +
                "        \"metrics\": [\n" +
                "          {\n" +
                "            \"name\": \"*\",\n" +
                "            \"func\": \"count\"\n" +
                "          }\n" +
                "        ]\n" +
                "      }\n" +
                "    },\n" +
                "    \"time\": {\n" +
                "      \"from\": \"+$start_of_hour\",\n" + // or start_of_hour
                "      \"to\": \"+$now\",\n" +
                "      \"timeField\": \"updated_ts\"\n" +
                "    }\n" +
                "  }\n" +
                "}";
        final String timeField = "updated_ts";

        if (groupBy.contains("yesterday")) {
            final JSONObject yesterday = new JSONObject("\"time\": {\n" +
                    "      \"from\": \"+$start_of_day_-1_day\",\n" +
                    "      \"to\": \"+$end_of_day_-1_day\",\n" +
                    "      \"timeField\": \"updated_ts\"\n" +
                    "    }");

            final JSONObject jsonRequest = new JSONObject(message);
            jsonRequest.getJSONObject("request").put("time", yesterday);
            return jsonRequest;
        }
        if (groupBy.contains("last")) {

            final JSONObject lastHour = new JSONObject("{\n" +
                    "      \"from\": \"+$start_of_hour_-1_hour\",\n" +
                    "      \"to\": \"+$end_of_hour_-1_hour\",\n" +
                    "      \"timeField\": \"updated_ts\"\n" +
                    "    }");
            final JSONObject jsonRequest = new JSONObject(message);
            jsonRequest.getJSONObject("request").put("time", lastHour);
            return jsonRequest;
        }

        final JSONObject jsonRequest = new JSONObject(message.replace("start_of_hour", groupBy));
        if (jsonRequest != null) {
            return jsonRequest;
        }

        jsonRequest.put("cid", Long.toString(System.nanoTime()));
        jsonRequest.getJSONObject("request").put("streamSourceId", sourceId);
        final JSONObject jsonTimeField = jsonRequest.getJSONObject("request").getJSONObject("time");
        jsonTimeField.put("timeField", timeField);

        final JSONObject jsonConfig = jsonRequest.getJSONObject("request").getJSONObject("cfg");
        jsonConfig.getJSONObject("group").getJSONArray("fields").getJSONObject(0).put("name", groupBy);
        if (StringUtils.isEmpty(groupBy)) {
            jsonConfig.getJSONObject("group").put("fields", new JSONArray());
        } else {
            final JSONObject sort = jsonConfig.getJSONObject("group").getJSONArray("fields").getJSONObject(0).getJSONObject("sort");
            sort.put("name", metric);
            sort.put("metricFunc", func);
        }

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
            //jsonConfig.put("filters", filterArray);
        }


        return jsonRequest;

    }


}
