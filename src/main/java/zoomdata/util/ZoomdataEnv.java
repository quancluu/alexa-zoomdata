package zoomdata.util;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.DatatypeConverter;
import java.util.*;

/**
 * Created by qcluu on 11/19/17.
 */
public class ZoomdataEnv {
    private static final Logger logger = LoggerFactory.getLogger(ZoomdataEnv.class);
    private static final String startTime = "+2013-11-11 01:00:00.000";
    private static final String endTime = "+2013-11-11 03:59:59.999";

    private String zoomdataHost = "54.89.168.2";

    final String sourceIdCommon = "5a0e1658e4b0e5f6a5084dd7";

    private String restUrl = "http://" + zoomdataHost + ":8080/zoomdata/service/";
    private String websocket = "ws://" + zoomdataHost + ":8443/zoomdata/websocket";
    private String credential = "admin:Z00mda1a";

    private final String bookmarkUrl;

    private final Map<String, String> HEADERS = new HashMap<>();

    public ZoomdataEnv(final String hostName) {
        this.zoomdataHost = hostName;
        restUrl = "http://" + zoomdataHost + ":8080/zoomdata/service/";
        websocket = "ws://" + zoomdataHost + ":8443/zoomdata/websocket";
        credential = "admin:Z00mda1a";
        bookmarkUrl = this.restUrl + "bookmarks";
        final String encoding = DatatypeConverter.printBase64Binary(credential.getBytes());
        HEADERS.put("Authorization", "Basic " + encoding);

        this.DATA_REQUEST_TEMPLATE =  MiscUtil.GetDataFromFileName("/Data_Request_Template.json");


    }

    public ZoomdataEnv(final String hostName, final String restUrl, final String websocket, final String credential) {
        this.zoomdataHost = hostName;
        this.restUrl = restUrl;
        this.websocket = websocket;
        this.credential = credential;
        bookmarkUrl = this.restUrl + "bookmarks";
        final String encoding = DatatypeConverter.printBase64Binary(credential.getBytes());
        HEADERS.put("Authorization", "Basic " + encoding);
        this.DATA_REQUEST_TEMPLATE =  MiscUtil.GetDataFromFileName("/Data_Request_Template.json");
    }

    private void setMetricInfoBar(final JSONObject orgRequest, final String metricName, final String metricFunc) throws Exception {

        final JSONObject jsonMetric = orgRequest.getJSONArray("visualizations").getJSONObject(0).getJSONObject("source")
                .getJSONObject("variables").getJSONArray("Metric").getJSONObject(0);
        System.out.println(jsonMetric);

        if ((StringUtils.isEmpty(metricName)) ||
                (metricName.equals("count")) ||
                (metricName.equals("*"))) {            // for count only
            jsonMetric.put("name", "count");
            jsonMetric.remove("func"); // if any.
        } else {
            // to set to specific metric
            jsonMetric.put("name", metricName);
            jsonMetric.put("func", metricFunc);
        }
    }
    private void setSortInfoBar(final JSONObject orgRequest, final String metricName, final String metricFunc) throws Exception {

        final JSONObject jsonSort = orgRequest.getJSONArray("visualizations").getJSONObject(0).getJSONObject("source")
                .getJSONObject("variables").getJSONArray("Multi Group By").getJSONObject(0).getJSONObject("sort");
        System.out.println(jsonSort);

        if ((StringUtils.isEmpty(metricName)) ||
                (metricName.equals("count")) ||
                (metricName.equals("*"))) {
            // for count:
            jsonSort.put("name", "count");
            jsonSort.put("dir", "desc");
        } else {

            // for metric
            jsonSort.put("name", metricName);
            jsonSort.put("metricFunc", metricFunc);
            jsonSort.put("dir", "desc");
        }

    }


    private void setMetricInfoPie(final JSONObject orgRequest, final String metricName, final String metricFunc) throws Exception {
        if ((StringUtils.isEmpty(metricName)) ||
                (metricName.equals("count")) ||
                (metricName.equals("*"))) {
            return;
        }

        final JSONObject jsonSize = orgRequest.getJSONArray("visualizations").getJSONObject(0).getJSONObject("source")
                .getJSONObject("variables").getJSONArray("Size").getJSONObject(0);

        // to set to specific metric
        jsonSize.put("name", metricName);
        jsonSize.put("func", metricFunc.toLowerCase());
    }


    private void setSortInfoPie(final JSONObject orgRequest, final String metricName, final String metricFunc) throws Exception {

        if ((StringUtils.isEmpty(metricName)) ||
                (metricName.equals("count")) ||
                (metricName.equals("*"))) {
            return;
        }

        final JSONObject jsonSort = orgRequest.getJSONArray("visualizations").getJSONObject(0).getJSONObject("source")
                .getJSONObject("variables").getJSONObject("Group By").getJSONObject("sort");

        // for metric
        jsonSort.put("name", metricName);
        jsonSort.put("metricFunc", metricFunc.toLowerCase());
        jsonSort.put("dir", "desc");
    }


    private void setSortDataRequest(final JSONObject orgRequest, final String metricName, final String metricFunc) throws Exception {

        if ((StringUtils.isEmpty(metricName)) ||
                (metricName.equals("count")) ||
                (metricName.equals("*"))) {
            return;
        }
        final JSONObject jsonSort = orgRequest.getJSONArray("dimensions").getJSONObject(0).getJSONObject("window")
                .getJSONArray("aggregationWindows").getJSONObject(0).getJSONObject("sort").getJSONObject("metric");

        // for metric
        jsonSort.put("type", "FIELD");
        jsonSort.put("field", new JSONObject().put("name", metricName));
        jsonSort.put("function", metricFunc.toUpperCase());

    }

    private void setMetricDataRequest(final JSONObject orgRequest, final String metricName, final String metricFunc) throws Exception {

        if ((StringUtils.isEmpty(metricName)) ||
                (metricName.equals("count")) ||
                (metricName.equals("*"))) {
            return;
        }

        final JSONObject jsonMetric = orgRequest.getJSONArray("metrics").getJSONObject(0);
        System.out.println(jsonMetric);


        // for metric
        jsonMetric.put("type", "FIELD");

        jsonMetric.put("field", new JSONObject().put("name", metricName));
        jsonMetric.put("function", metricFunc.toUpperCase());

    }
    public JSONObject getData(final String metric,
                              final String function,
                              final String groupBy,
                              final String sourceId,
                              final String filters,
                              final String timeRangeType) throws Exception {


        final String filterField = "sale_date"; // TODO: Hard code for now.

        //   final String sortBy = metric; // "count";

        /*
        final Map<String, String> headers = new HashMap<>();
        final String credential = "admin:Z00mda1a";
        final String encoding = DatatypeConverter.printBase64Binary(credential.getBytes());
        headers.put("Authorization", "Basic " + encoding);
        headers.put("Cookie",
                "JSESSIONID=0DF6B3538CF76D387B3FCEA297EB6F55; _ga=GA1.2.1509295338.1415671928; __utma=41381033.1509295338.1415671928.1436360392.1436794843.51; __utmc=41381033; __utmz=41381033.1435595058.49.11.utmcsr=google|utmccn=(organic)|utmcmd=organic|utmctr=(not%20provided); JSESSIONID=6B977DF17C10D7D7FC3BF05D4ED416C9; __utmt=1; __utma=108567959.515813076.1429193063.1436825769.1436885790.135; __utmb=108567959.5.10.1436885790; __utmc=108567959; __utmz=108567959.1429193063.1.1.utmcsr=(direct)|utmccn=(direct)|utmcmd=(none)");
        */

        final ZoomdataWebsocketSecuredClient websocketClient = new ZoomdataWebsocketSecuredClient(websocket, HEADERS);

        final String request = DATA_REQUEST_TEMPLATE.replace("__METRIC_NAME__", metric)
                .replace("__METRIC_FUNC__", function.toUpperCase())
                .replace("__ATTRIBUTE__", groupBy)
                .replace("__START_TIME__", startTime)
                .replace("__END_TIME__", endTime);

        final JSONObject jsonRequest = new JSONObject(request);
        setSortDataRequest(jsonRequest, metric, function);
        setMetricDataRequest(jsonRequest, metric, function);

        /*
        if (metric.equals("*")) {
            final String starMetric = "[\n" +
                    "    {\n" +
                    "      \"type\": \"COUNT\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"type\": \"COUNT\"\n" +
                    "    }\n" +
                    "  ]";
            jsonRequest.put("metrics", new JSONArray(starMetric));
        }*/

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
            throw new Exception("Got null response data from request=" + message);
        }
        return jsonResponseData;
    }

    public JSONArray getSources() {

        final String params = "fields=name%2Cdescription%2Ctype%2Cenabled%2CsubStorageType%2CstorageConfiguration%2ClinkedSources%2Ccacheable%2CconnectionTypeId%2CfusedAttributes%2CcreatedByUserID";
        final String url = restUrl + "sources?" + params;

        try {
            final String data = HttpUtil.httpGetWithHeader(url, HEADERS);
            final JSONArray jsonArray = new JSONArray(data);
            return jsonArray;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public void deleteSourceById(final String sourceId) {
        final String url = restUrl + "sources/" + sourceId;

        try {
            final String status = HttpUtil.sendDeleteWithHeader(url, HEADERS);
            System.out.println("delete status=" + status + ", sourceId=" + sourceId);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getSourceById(final String sourceId) throws Exception {
        try {
            final String serviceUrl = restUrl + "sources/" + sourceId;
            final String data = HttpUtil.sendGetWithHeader(serviceUrl, HEADERS);
            return data;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
    public void visPregen(final String bookmarkName,
                          final String sourceId,

                          final String chartType,
                          final String metricField,
                          final String groupByField) throws Exception {

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
                    if (tmpItem.length > 1) {
                        metricFunc = tmpItem[1];
                    }
                }

                try {
                    // zoomdataApi.createBookmark(bookmarkName, sourceId, chartType, metric, metricItem, groupByField);
                    createBookmark(bookmarkName, sourceId, chartType, metricFunc, metricName, groupByField);

                    // Use the source id to stream result.
                } catch (Exception e) {
                    e.printStackTrace();
                    throw e;
                }


                logger.info("Done creating bookmark for " + bookmarkName);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /*
    public void deleteBookmarkByName(final String name) {

        try {
            final String bookmarkData = HttpUtil.sendGetWithHeader(bookmarkUrl, HEADERS);

            logger.info("bookmarkData=" + bookmarkData);
            final JSONObject jsonObject = new JSONObject(bookmarkData);

            final JSONArray jsonArray = jsonObject.getJSONArray("bookmarksMap");
            System.out.println("# existing dashboards: " + jsonArray.length());
            final int length = jsonArray.length();

            for (int i = 0; i < length; i++) {
                final JSONObject item = jsonArray.getJSONObject(i);
                final String bookmarkName = item.getString("name");
                if (name.equals(bookmarkName)) {
                    // Execute delete name here
                    final String id = item.getString("id");
                    //     System.out.println("Matched " + name + ", id=" + id);
                    final String status = HttpUtil.sendDeleteWithHeader(bookmarkUrl + "/" + id, HEADERS);
                    System.out.println("Deleted " + name + ", id=" + id);
                    return;

                } else {
                    // Ignore.
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/


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

        /*
        final Map<String, String> headers = new HashMap<>();
        final String credential = "admin:Z00mda1a";
        final String encoding = DatatypeConverter.printBase64Binary(credential.getBytes());
        headers.put("Authorization", "Basic " + encoding);
        headers.put("Cookie",
                "JSESSIONID=0DF6B3538CF76D387B3FCEA297EB6F55; _ga=GA1.2.1509295338.1415671928; __utma=41381033.1509295338.1415671928.1436360392.1436794843.51; __utmc=41381033; __utmz=41381033.1435595058.49.11.utmcsr=google|utmccn=(organic)|utmcmd=organic|utmctr=(not%20provided); JSESSIONID=6B977DF17C10D7D7FC3BF05D4ED416C9; __utmt=1; __utma=108567959.515813076.1429193063.1436825769.1436885790.135; __utmb=108567959.5.10.1436885790; __utmc=108567959; __utmz=108567959.1429193063.1.1.utmcsr=(direct)|utmccn=(direct)|utmcmd=(none)");
        final String websocket =
             "wss://zdlabs.zoomdata.com:8443/zoomdata/websocket";

        // "wss://preview.zoomdata.com:8443/zoomdata/websocket";
        */
        final ZoomdataWebsocketSecuredClient websocketClient = new ZoomdataWebsocketSecuredClient(websocket, HEADERS);

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
                final Date date = MiscUtil.GetDateFromSql(filterValues);
                // next day
                Calendar cal = Calendar.getInstance();
                cal.setTime(date);
                cal.add(Calendar.DAY_OF_YEAR, 1); // next day.
                cal.add(Calendar.MILLISECOND, -1);
                filterValues += "," + MiscUtil.GetSqlZdDateFromDate(cal.getTime());
            } else {
                timeRangeType = "between";
                Calendar cal = Calendar.getInstance();
                filterValues += "," + MiscUtil.GetSqlZdDateFromDate(cal.getTime());

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

        /*
        final Map<String, String> headers = new HashMap<>();
        final String credential = "admin:Z00mda1a";
        final String encoding = DatatypeConverter.printBase64Binary(credential.getBytes());
        headers.put("Authorization", "Basic " + encoding);
        headers.put("Cookie",
                "JSESSIONID=0DF6B3538CF76D387B3FCEA297EB6F55; _ga=GA1.2.1509295338.1415671928; __utma=41381033.1509295338.1415671928.1436360392.1436794843.51; __utmc=41381033; __utmz=41381033.1435595058.49.11.utmcsr=google|utmccn=(organic)|utmcmd=organic|utmctr=(not%20provided); JSESSIONID=6B977DF17C10D7D7FC3BF05D4ED416C9; __utmt=1; __utma=108567959.515813076.1429193063.1436825769.1436885790.135; __utmb=108567959.5.10.1436885790; __utmc=108567959; __utmz=108567959.1429193063.1.1.utmcsr=(direct)|utmccn=(direct)|utmcmd=(none)");
        final String websocket =
             "wss://zdlabs.zoomdata.com:8443/zoomdata/websocket";

        // "wss://preview.zoomdata.com:8443/zoomdata/websocket";
        */

        final ZoomdataWebsocketSecuredClient websocketClient = new ZoomdataWebsocketSecuredClient(websocket, HEADERS);

        final JSONObject jsonRequest = buildRequestTrends(metric, function, groupBy, sourceId, filterField, filters, timeRangeType);

        final String message = jsonRequest.toString();
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
                final Date date = MiscUtil.GetDateFromSql(filterValues);
                // next day
                Calendar cal = Calendar.getInstance();
                cal.setTime(date);
                cal.add(Calendar.DAY_OF_YEAR, 1); // next day.
                cal.add(Calendar.MILLISECOND, -1);
                filterValues += "," + MiscUtil.GetSqlZdDateFromDate(cal.getTime());
            } else {
                timeRangeType = "between";
                Calendar cal = Calendar.getInstance();
                filterValues += "," + MiscUtil.GetSqlZdDateFromDate(cal.getTime());

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

    private void createBookmark(final String name, final String sourceId, final String type,
                                       final String metricFunc, final String metricName,
                                       final String attribute) throws Exception {

        final String REQUEST_TEMPLATE_BAR = MiscUtil.GetDataFromFileName("/Dashboard_Template_BAR.json");
        final String REQUEST_TEMPLATE_PIE = MiscUtil.GetDataFromFileName("/Dashboard_Template_PIE.json");

        try {
            // Delete if exists
            deleteBookmarkByName(name);
            String metricString = "{\"name\": \"count\"}";
            if (metricName.equals("*") == false) {
                metricString = "{\n" +
                        "              \"name\": \"" + metricName + "\",\n" +
                        "              \"func\": \"" + metricFunc + "\"\n" +
                        "            }";
            }
            if (type.toLowerCase().contains("bar")) {
                // __METRIC_INFO__
                final String request = REQUEST_TEMPLATE_BAR.replace("__VIS_NAME__", name)
                        .replace("__METRIC_FUNC__", metricFunc)
                        .replace("__METRIC_NAME__", metricName)
                        .replace("__ATTRIBUTE__", attribute)
                        .replace("__START_TIME__", startTime)
                        .replace("__END_TIME__", endTime)
                        .replace("__SOURCE_ID__", sourceId);
                //        .replace("__METRIC_INFO__", metricString);
                final JSONObject jsonObject = new JSONObject(request);
                setMetricInfoBar(jsonObject, metricName, metricFunc);
                setSortInfoBar(jsonObject, metricName, metricFunc);

                final Map<String, Object> results = HttpUtil.httpsPost(bookmarkUrl, jsonObject.toString(), HEADERS);
                logger.info("createBookmark results=" + results);
            } else if (type.toLowerCase().contains("pie")) {

                final String request = REQUEST_TEMPLATE_PIE.replace("__VIS_NAME__", name)
                //        .replace("__METRIC_FUNC__", metricFunc)
                //        .replace("__METRIC_NAME__", metricName)
                //        .replace("__METRIC_INFO__", metricString)
                        .replace("__ATTRIBUTE__", attribute)
                        .replace("__START_TIME__", startTime)
                        .replace("__END_TIME__", endTime)
                        .replace("__SOURCE_ID__", sourceId);
                //        .replace("__SIZE__", metricString);

                final JSONObject jsonObject = new JSONObject(request);
                setMetricInfoPie(jsonObject, metricName, metricFunc);
                setSortInfoPie(jsonObject, metricName, metricFunc);

                final Map<String, Object> results = HttpUtil.httpsPost(bookmarkUrl, jsonObject.toString(), HEADERS);
                logger.info("createBookmark results=" + results);
            } else {
                throw new Exception("Chart type " + type + " NOT SUPPORTED");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    public void deleteBookmarkByName(final String name) {

        try {
            final String bookmarkData = HttpUtil.sendGetWithHeader(bookmarkUrl, HEADERS);

            logger.info("bookmarkData=" + bookmarkData);
            final JSONObject jsonObject = new JSONObject(bookmarkData);

            final JSONArray jsonArray = jsonObject.getJSONArray("bookmarksMap");
            System.out.println("# existing dashboards: " + jsonArray.length());
            final int length = jsonArray.length();

            for (int i = 0; i < length; i++) {
                final JSONObject item = jsonArray.getJSONObject(i);
                final String bookmarkName = item.getString("name");
                if (name.equals(bookmarkName)) {
                    // Execute delete name here
                    final String id = item.getString("id");
                    //     System.out.println("Matched " + name + ", id=" + id);
                    final String status = HttpUtil.sendDeleteWithHeader(bookmarkUrl + "/" + id, HEADERS);
                    System.out.println("Deleted " + name + ", id=" + id);
                    return;

                } else {
                    // Ignore.
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return "ZoomdataEnv{" +
                "zoomdataHost='" + zoomdataHost + '\'' +
                ", sourceIdCommon='" + sourceIdCommon + '\'' +
                ", restUrl='" + restUrl + '\'' +
                ", websocket='" + websocket + '\'' +
                ", credential='" + credential + '\'' +
                ", bookmarkUrl='" + bookmarkUrl + '\'' +
                ", HEADERS=" + HEADERS +
                '}';
    }

    private final String DATA_REQUEST_TEMPLATE;

    private static final String DATA_REQUEST_TEMPLATE_OLD =
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

        /*
        final Map<String, String> headers = new HashMap<>();
        final String credential = "admin:Z00mda1a";
        final String encoding = DatatypeConverter.printBase64Binary(credential.getBytes());
        headers.put("Authorization", "Basic " + encoding);
        headers.put("Cookie",
                "JSESSIONID=0DF6B3538CF76D387B3FCEA297EB6F55; _ga=GA1.2.1509295338.1415671928; __utma=41381033.1509295338.1415671928.1436360392.1436794843.51; __utmc=41381033; __utmz=41381033.1435595058.49.11.utmcsr=google|utmccn=(organic)|utmcmd=organic|utmctr=(not%20provided); JSESSIONID=6B977DF17C10D7D7FC3BF05D4ED416C9; __utmt=1; __utma=108567959.515813076.1429193063.1436825769.1436885790.135; __utmb=108567959.5.10.1436885790; __utmc=108567959; __utmz=108567959.1429193063.1.1.utmcsr=(direct)|utmccn=(direct)|utmcmd=(none)");
        final String websocket =
             "wss://zdlabs.zoomdata.com:8443/zoomdata/websocket";

        // "wss://preview.zoomdata.com:8443/zoomdata/websocket";
        */
        final ZoomdataWebsocketSecuredClient websocketClient = new ZoomdataWebsocketSecuredClient(websocket, HEADERS);

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
            return new JSONObject(messageVolume.replace("start_of_hour", groupBy));
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
            return new JSONObject(message.replace("start_of_hour", groupBy));
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
                final Date date = MiscUtil.GetDateFromSql(filterValues);
                // next day
                Calendar cal = Calendar.getInstance();
                cal.setTime(date);
                cal.add(Calendar.DAY_OF_YEAR, 1); // next day.
                cal.add(Calendar.MILLISECOND, -1);
                filterValues += "," + MiscUtil.GetSqlZdDateFromDate(cal.getTime());
            } else {
                timeRangeType = "between";
                Calendar cal = Calendar.getInstance();
                filterValues += "," + MiscUtil.GetSqlZdDateFromDate(cal.getTime());

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

