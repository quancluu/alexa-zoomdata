package zoomdata;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import zoomdata.util.HttpUtil;
import zoomdata.util.MiscUtil;
import zoomdata.util.ZoomdataEnv;

import javax.xml.bind.DatatypeConverter;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ZoomdataSpeechletTest {
    final String zoomdataHost = "54.173.52.54"; // "10.2.1.199"
    final ZoomdataEnv zoomdataEnv = new ZoomdataEnv(zoomdataHost);
    @Test
    public void testDataFromFile() throws Exception {
        final String data = MiscUtil.GetDataFromFileName("/Dashboard_Template_BAR.json");
        final JSONObject jsonObject = new JSONObject(data);

        System.out.println(jsonObject);
    }
    @Test
    public void testGetSourceById() throws  Exception {
        final org.json.JSONArray jsonArray = zoomdataEnv.getSources();

        int length = jsonArray.length();
        if (length > 5) {
            length = 5;
        }
        System.out.println("Total sources: " + length);

        for (int i = 0;  i < length; i++) {
            final long start = System.currentTimeMillis();
            final org.json.JSONObject sourceItem = jsonArray.getJSONObject(i);
            final String sourceId = sourceItem.getString("id");
            final String sourceName = sourceItem.getString("name");

            final String sourceData = zoomdataEnv.getSourceById(sourceId);

            final long timeTook = System.currentTimeMillis() - start;

            System.out.println("Source " + i + " of " + length + " (" + timeTook + "ms): " + sourceName + ": " + sourceData);
        }
    }
    @Test
    public void testS3Get() throws Exception {
        final String url = "https://s3.amazonaws.com/zoomdata-labs/hello.txt";
        final String data = HttpUtil.httpGet(url);

        System.out.println("data = " + data);
    }

    @Test
    public void testSetMetricInfoBar() throws Exception {
        final String data = MiscUtil.GetDataFromFileName("/Dashboard_Template_BAR.json");
        final JSONObject jsonObject = new JSONObject(data);

        final JSONObject jsonMetric = jsonObject.getJSONArray("visualizations").getJSONObject(0).getJSONObject("source")
                .getJSONObject("variables").getJSONArray("Metric").getJSONObject(0);
        System.out.println(jsonMetric);

        // for count only
        jsonMetric.put("name", "count");
        jsonMetric.remove("func"); // if any.

        // to set to specific metric
        jsonMetric.put("name", "plannedsales");
        jsonMetric.put("func", "sum");
    }
    @Test
    public void testSetSortInfoBar() throws Exception {
        final String data = MiscUtil.GetDataFromFileName("/Dashboard_Template_BAR.json");
        final JSONObject jsonObject = new JSONObject(data);

        final JSONObject jsonSort = jsonObject.getJSONArray("visualizations").getJSONObject(0).getJSONObject("source")
                .getJSONObject("variables").getJSONArray("Multi Group By").getJSONObject(0).getJSONObject("sort");
        System.out.println(jsonSort);

        // for count:
        jsonSort.put("name", "count");
        jsonSort.put("dir", "desc");

        // for metric
        jsonSort.put("name", "plannedsales");
        jsonSort.put("metricFunc", "sum");
        jsonSort.put("dir", "desc");


        System.out.println(jsonObject);
    }


    @Test
    public void testSetMetricInfoPie() throws Exception {
        final String data = MiscUtil.GetDataFromFileName("/Dashboard_Template_PIE.json");
        final JSONObject jsonObject = new JSONObject(data);

        final JSONObject jsonSize = jsonObject.getJSONArray("visualizations").getJSONObject(0).getJSONObject("source")
                .getJSONObject("variables").getJSONArray("Size").getJSONObject(0);
        System.out.println(jsonSize);


        // for count only
        jsonSize.put("name", "count");
        jsonSize.remove("func"); // if any.

        // to set to specific metric
        jsonSize.put("name", "plannedsales");
        jsonSize.put("func", "sum");
    }

    @Test
    public void testSetSortInfoPie() throws Exception {
        final String data = MiscUtil.GetDataFromFileName("/Dashboard_Template_PIE.json");
        final JSONObject jsonObject = new JSONObject(data);

        final JSONObject jsonSort = jsonObject.getJSONArray("visualizations").getJSONObject(0).getJSONObject("source")
                .getJSONObject("variables").getJSONObject("Group By").getJSONObject("sort");
        System.out.println(jsonSort);

        // for count:
        jsonSort.put("name", "count");
        jsonSort.put("dir", "desc");

        // for metric
        jsonSort.put("name", "plannedsales");
        jsonSort.put("metricFunc", "sum");
        jsonSort.put("dir", "desc");


        System.out.println(jsonObject);
    }

    @Test
    public void testVisgenBar() throws Exception{
        final String bookmarkName = "test_alexa_count_bar";
        final String chartType = "UBER_BARS";
        final String metricField = "*"; // or "price:sum" or "*"
        final String groupByField = "userstate";
        final String sourceId = "5a0e1658e4b0e5f6a5084dd7";
        final long start = System.currentTimeMillis();

        zoomdataEnv.visPregen(bookmarkName, sourceId, chartType, metricField, groupByField);
        final long timeTook = System.currentTimeMillis() - start;

        System.out.println("Done creating BAR chart " + bookmarkName + "! Time took(msecs): " + timeTook);
    }

    @Test
    public void testVisgenBarCount() throws Exception{
        final String bookmarkName = "test_alexa_price_bar_count";
        final String chartType = "UBER_BARS";
        final String metricField = "*"; // or "price:sum" or "*"
        final String groupByField = "userstate";
        final String sourceId = "5a0de69de4b0105b5562c89c";
        final long start = System.currentTimeMillis();

        zoomdataEnv.visPregen(bookmarkName, sourceId, chartType, metricField, groupByField);
        final long timeTook = System.currentTimeMillis() - start;

        System.out.println("Done creating BAR chart " + bookmarkName + "! Time took(msecs): " + timeTook);
    }

    @Test
    public void testVisgenPie() throws Exception{
        final String bookmarkName = "test_alexa_pie3";
        final String chartType = "pie";
        final String metricField = "price:sum"; // or "price:sum" or "*"
        final String groupByField = "userstate";
        final String sourceId = "5a0e1658e4b0e5f6a5084dd7";
        final long start = System.currentTimeMillis();

        zoomdataEnv.visPregen(bookmarkName, sourceId, chartType, metricField, groupByField);
        final long timeTook = System.currentTimeMillis() - start;

        System.out.println("Done creating PIE chart " + bookmarkName + "! Time took(msecs): " + timeTook);
    }

    @Test
    public void testCalendar() {
        Calendar calendar = Calendar.getInstance();

        calendar.set(Calendar.WEEK_OF_YEAR, 45);

        System.out.println(calendar.getTime());

    }

    @Test
    public void testGetStock() throws Exception {
        String api = "https://api.intrinio.com/prices?start_date=2017-11-17&end_date=2017-11-17&frequency=daily&identifier=AAPL";

        final String credential = "4dc21a631a4e0f039382c4018a04d8a9:8b9577f457c8e2c108c005d7244f607d";

        final Map<String, String> HEADERS = new HashMap<>();

            final String encoding = DatatypeConverter.printBase64Binary(credential.getBytes());
            HEADERS.put("Authorization", "Basic " + encoding);

    //    api = "https://api.intrinio.com/prices?start_date=2017-11-17&end_date=2017-11-17&frequency=daily&identifier=APPL";
        final String data = HttpUtil.httpGetWithHeader(api, HEADERS);

        System.out.println(data);
    }

    @Test
    public void testCurrency() throws Exception {
        Double groupValue = 10.99;
        final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
        currencyFormat.setMaximumFractionDigits(0);

        final String currencyValue = currencyFormat.format(groupValue);
        System.out.println("currencyValue="+ currencyValue);
    }

    @Test
    public void testVisgenPieCount() throws Exception{
        final String bookmarkName = "test_alexa_pie2";
        final String chartType = "pie";
        final String metricField = "*"; // or "price:sum" or "*"
        final String groupByField = "userstate";
        final String sourceId = "5a0e1658e4b0e5f6a5084dd7";
        final long start = System.currentTimeMillis();

        zoomdataEnv.visPregen(bookmarkName, sourceId, chartType, metricField, groupByField);
        final long timeTook = System.currentTimeMillis() - start;

        System.out.println("Done creating PIE chart " + bookmarkName + "! Time took(msecs): " + timeTook);
    }

    @Test
    public void testVisgenPieSales() throws Exception{
       // Details=Alexa_UBER_BARS_price_usercity, sourceId=5a0de69de4b0105b5562c89c, chartType=UBER_BARS, metric=price, attribute=usercity",

        final String chartType = "UBER_BARS";
        final String metricField = "plannedsales"; // or "price:sum" or "*"
        final String groupByField = "usercity";
        final String bookmarkName = "Alexa_BAR_" + groupByField + "_" + metricField;

        final String sourceId = "5a0de69de4b0105b5562c89c";
        final long start = System.currentTimeMillis();

        zoomdataEnv.visPregen(bookmarkName, sourceId, chartType, metricField, groupByField);
        final long timeTook = System.currentTimeMillis() - start;

        System.out.println("Done creating PIE chart " + bookmarkName + "! Time took(msecs): " + timeTook);
    }

    @Test
    public void testSortDataRequest() throws Exception {
        final String data = MiscUtil.GetDataFromFileName("/Data_Request_Template.json");
        final JSONObject jsonObject = new JSONObject(data);

        final JSONObject jsonSort = jsonObject.getJSONArray("dimensions").getJSONObject(0).getJSONObject("window")
                .getJSONArray("aggregationWindows").getJSONObject(0).getJSONObject("sort").getJSONObject("metric");
        System.out.println(jsonSort);

        // for count:
        jsonSort.put("name", "count");

        // for metric
        jsonSort.put("type", "FIELD");

        jsonSort.put("field", new JSONObject().put("name", "plannedsales"));
        jsonSort.put("function", "sum");

        System.out.println(jsonObject);
    }

    @Test
    public void testMetricDataRequest() throws Exception {
        final String data = MiscUtil.GetDataFromFileName("/Data_Request_Template.json");
        final JSONObject jsonObject = new JSONObject(data);

        final JSONObject jsonMetric = jsonObject.getJSONArray("metrics").getJSONObject(0);
        System.out.println(jsonMetric);


        // for metric
        jsonMetric.put("type", "FIELD");

        jsonMetric.put("field", new JSONObject().put("name", "plannedsales"));
        jsonMetric.put("function", "sum");

        System.out.println(jsonObject);
    }
    @Test
    public void testGetData() {
        try {
            final String metricFunc = "sum";
            final String metricField = "plannedsales";
            final String groupByField = "usergender";
            final String sourceId = "5a0e1658e4b0e5f6a5084dd7";
            final String filters = null;
            final String timeRange = null;
            final long start = System.currentTimeMillis();
            final org.json.JSONObject jsonObject = zoomdataEnv.getData(metricField,
                    metricFunc, groupByField, sourceId, filters, timeRange);
            final long timeTook = System.currentTimeMillis() - start;
            System.out.println("Done! Time took(msecs): " + timeTook + ", jsonObject=" + jsonObject);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}