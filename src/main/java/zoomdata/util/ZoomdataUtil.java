package zoomdata.util;

import com.amazonaws.util.json.JSONArray;
import com.amazonaws.util.json.JSONObject;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.DatatypeConverter;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by qcluu on 4/12/17.
 */
public class ZoomdataUtil {
    private static final Logger logger = LoggerFactory.getLogger(ZoomdataUtil.class);


    private static final String REST_URL = "https://2-5-latest.zoomdata.com/zoomdata/service/bookmarks";

    private static final Map<String, String> HEADERS = new HashMap<>();

    static {
        final String credential = "admin:Z00mda1a";
        final String encoding = DatatypeConverter.printBase64Binary(credential.getBytes());
        HEADERS.put("Authorization", "Basic " + encoding);

    }

    public static void validateConnection() {
        final String url = "https://2-5-latest.zoomdata.com/zoomdata/service/connections/validate";
        final String payload = "{\n" +
                "  \"name\": \"test connection\",\n" +
                "  \"type\": \"EDC2\",\n" +
                "  \"subStorageType\": \"SOLR\",\n" +
                "  \"connectionTypeId\": \"SOLR_SOLR\",\n" +
                "  \"parameters\": {\n" +
                "    \"BASE_URL\": \"http://10.2.1.199:8983/solr/\",\n" +
                "    \"HOSTING_TYPE\": \"STANDALONE\"\n" +
                "  }\n" +
                "}";
    }

    public static void createConnection() {
        final String url = "https://2-5-latest.zoomdata.com/zoomdata/service/connections";
        final String payload = "{\n" +
                "  \"name\": \"test connection2\",\n" +
                "  \"type\": \"EDC2\",\n" +
                "  \"subStorageType\": \"SOLR\",\n" +
                "  \"connectionTypeId\": \"SOLR_SOLR\",\n" +
                "  \"parameters\": {\n" +
                "    \"BASE_URL\": \"http://10.2.1.199:8983/solr/\",\n" +
                "    \"HOSTING_TYPE\": \"STANDALONE\"\n" +
                "  }\n" +
                "}";

    }

    static public void deleteSourceByName(final String sourceName) {
        // https://2-5-latest.zoomdata.com/zoomdata/service/sources?fields=name
        final String url = "https://2-5-latest.zoomdata.com/zoomdata/service/sources?fields=name";
        try {
            final String data = HttpUtil.sendGetWithHeader(url, HEADERS);
            final JSONArray jsonArray = new JSONArray(data);
            final int length = jsonArray.length();
            for (int i = 0; i < length; i++) {
                final JSONObject item = jsonArray.getJSONObject(i);
                final String id = item.getString("id");
                final String name = item.getString("name").trim();
                if (name.equals(sourceName.trim())) {
                    deleteSourceById(id);
                    System.out.println("Delete source name '" + name + "' with id: " + id);
                    return;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
    static public final String GetDataFromFileName(final String fileName) throws Exception {
        final InputStream inputStream = ClassLoader.getSystemResourceAsStream(fileName);
        return IOUtils.toString(inputStream, StandardCharsets.UTF_8);
    }

    static public final String GetDataFromFileName2(final String fileName) throws Exception {
        final InputStream inputStream = ClassLoader.getSystemClassLoader().getResourceAsStream(fileName);
        return IOUtils.toString(inputStream, StandardCharsets.UTF_8);
    }*/


    public static void deleteSourceById(final String sourceId) {
        // https://2-5-latest.zoomdata.com/zoomdata/service/sources/58ee442de4b06dfd61d
        final String url = "https://2-5-latest.zoomdata.com/zoomdata/service/sources/" + sourceId;

        try {
            final String status = HttpUtil.sendDeleteWithHeader(url, HEADERS);
            System.out.println("delete status=" + status + ", sourceId=" + sourceId);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String GetSourceById(final String sourceId) {
        // https://2-5-latest.zoomdata.com/zoomdata/service/sources/58ee442de4b06dfd61d
        final String url = "https://2-5-latest.zoomdata.com/zoomdata/service/sources/" + sourceId;

        try {
            final String data = HttpUtil.sendGetWithHeader(url, HEADERS);
            return data;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static void createSource(final String sourceName, final String connectionId) {
        final String url = "https://2-5-latest.zoomdata.com/zoomdata/service/sources";
        // use SOURCE_CREATION_TEMPLATE.


        // Use SOURCE_UPDATE_TEMPLATE

        // delete any existing source
        deleteSourceByName(sourceName);
        try {
            final String data = SOURCE_CREATION_TEMPLATE
                    .replace("__SOURCE_NAME__", sourceName)
                    .replace("__CONNECTION_ID__", connectionId);

            final Map<String, Object> results = HttpUtil.httpsPost(url, data, HEADERS);
            System.out.println("results=" + results);
         //   final String id = getSourceByName(sourceName);
         //   System.out.println("Source '" + sourceName + "' created with id '" + id + "'");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getSourceByName(final String sourceName) {
        final String url = "https://2-5-latest.zoomdata.com/zoomdata/service/sources?fields=name";
        try {
            final String data = HttpUtil.sendGetWithHeader(url, HEADERS);
            final JSONArray jsonArray = new JSONArray(data);
            final int length = jsonArray.length();
            for (int i = 0; i < length; i++) {
                final JSONObject item = jsonArray.getJSONObject(i);
                final String id = item.getString("id");
                final String name = item.getString("name").trim();
                if (name.equals(sourceName.trim())) {
                    System.out.println("Returned source name '" + name + "' with id: " + id);
                    return id;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void updateSource(final String sourceId) {
        // Sample url: https://2-5-latest.zoomdata.com/zoomdata/service/sources/58ee442de4b06dfd61dd8aba
        final String url = "https://2-5-latest.zoomdata.com/zoomdata/service/sources/" + sourceId;

        // Use SOURCE_UPDATE_TEMPLATE

        try {
            final String method = "PATCH";
            final String data = SOURCE_UPDATE_TEMPLATE;

            final Map<String, Object> results = HttpUtil.httpsSend(url, data, HEADERS, method);
            logger.info("status=" + results);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    static final String SOURCE_CREATION_TEMPLATE = "{\n" +
            "  \"connectionTypeId\": \"IMPALA_IMPALA\",\n" +
            "  \"type\": \"EDC2\",\n" +
            "  \"subStorageType\": \"IMPALA\",\n" +
            "  \"name\": \"__SOURCE_NAME__\",\n" +
            "  \"description\": \"\",\n" +
            "  \"cacheable\": true,\n" +
            "  \"storageConfiguration\": {\n" +
            "    \"collectionParams\": {\n" +
            "      \n" +
            "    },\n" +
            "    \"parameters\": {\n" +
            "      \n" +
            "    },\n" +
            "    \"connectionId\": \"__CONNECTION_ID__\",\n" +
            "    \"schema\": \"support\",\n" +
            "    \"collection\": \"rts\",\n" +
            "    \"partitions\": {\n" +
            "      \n" +
            "    }\n" +
            "  },\n" +
            "  \"volumeMetric\": {\n" +
            "    \"label\": \"Volume\",\n" +
            "    \"visible\": true,\n" +
            "    \"name\": \"count\",\n" +
            "    \"type\": \"NUMBER\",\n" +
            "    \"storageConfig\": {\n" +
            "      \n" +
            "    }\n" +
            "  },\n" +
            "  \"objectFields\": [\n" +
            "    {\n" +
            "      \"label\": \"Category\",\n" +
            "      \"name\": \"category\",\n" +
            "      \"storageConfig\": {\n" +
            "        \"originalName\": \"category\",\n" +
            "        \"min\": 4,\n" +
            "        \"max\": 15,\n" +
            "        \"cardinality\": 46,\n" +
            "        \"rawFormat\": {\n" +
            "          \"RAW_TYPE\": \"STRING\",\n" +
            "          \"RAW_TYPE_CODE\": \"12\"\n" +
            "        },\n" +
            "        \"metaFlags\": [\n" +
            "          \n" +
            "        ],\n" +
            "        \"originalType\": \"ATTRIBUTE\"\n" +
            "      },\n" +
            "      \"visible\": true,\n" +
            "      \"checkedByDefault\": true,\n" +
            "      \"customListValuesOnly\": false,\n" +
            "      \"type\": \"ATTRIBUTE\",\n" +
            "      \"facet\": false,\n" +
            "      \"distinctCount\": false,\n" +
            "      \"rawFormatSupportTimeGroup\": false,\n" +
            "      \"refreshable\": false,\n" +
            "      \"timeZoneLabel\": \"\",\n" +
            "      \"parentField\": false,\n" +
            "      \"effectiveMin\": 4,\n" +
            "      \"effectiveMax\": 15\n" +
            "    },\n" +
            "    {\n" +
            "      \"label\": \"Categorygroup\",\n" +
            "      \"name\": \"categorygroup\",\n" +
            "      \"storageConfig\": {\n" +
            "        \"originalName\": \"categorygroup\",\n" +
            "        \"min\": 3,\n" +
            "        \"max\": 11,\n" +
            "        \"cardinality\": 5,\n" +
            "        \"rawFormat\": {\n" +
            "          \"RAW_TYPE\": \"STRING\",\n" +
            "          \"RAW_TYPE_CODE\": \"12\"\n" +
            "        },\n" +
            "        \"metaFlags\": [\n" +
            "          \n" +
            "        ],\n" +
            "        \"originalType\": \"ATTRIBUTE\"\n" +
            "      },\n" +
            "      \"visible\": true,\n" +
            "      \"checkedByDefault\": true,\n" +
            "      \"customListValuesOnly\": false,\n" +
            "      \"type\": \"ATTRIBUTE\",\n" +
            "      \"facet\": false,\n" +
            "      \"distinctCount\": false,\n" +
            "      \"rawFormatSupportTimeGroup\": false,\n" +
            "      \"refreshable\": false,\n" +
            "      \"timeZoneLabel\": \"\",\n" +
            "      \"parentField\": false,\n" +
            "      \"effectiveMin\": 3,\n" +
            "      \"effectiveMax\": 11\n" +
            "    },\n" +
            "    {\n" +
            "      \"label\": \"County\",\n" +
            "      \"name\": \"county\",\n" +
            "      \"storageConfig\": {\n" +
            "        \"originalName\": \"county\",\n" +
            "        \"min\": 3,\n" +
            "        \"max\": 22,\n" +
            "        \"cardinality\": 452,\n" +
            "        \"rawFormat\": {\n" +
            "          \"RAW_TYPE\": \"STRING\",\n" +
            "          \"RAW_TYPE_CODE\": \"12\"\n" +
            "        },\n" +
            "        \"metaFlags\": [\n" +
            "          \n" +
            "        ],\n" +
            "        \"originalType\": \"ATTRIBUTE\"\n" +
            "      },\n" +
            "      \"visible\": true,\n" +
            "      \"checkedByDefault\": true,\n" +
            "      \"customListValuesOnly\": false,\n" +
            "      \"type\": \"ATTRIBUTE\",\n" +
            "      \"facet\": false,\n" +
            "      \"distinctCount\": false,\n" +
            "      \"rawFormatSupportTimeGroup\": false,\n" +
            "      \"refreshable\": false,\n" +
            "      \"timeZoneLabel\": \"\",\n" +
            "      \"parentField\": false,\n" +
            "      \"effectiveMin\": 3,\n" +
            "      \"effectiveMax\": 22\n" +
            "    },\n" +
            "    {\n" +
            "      \"label\": \"Countycode\",\n" +
            "      \"name\": \"countycode\",\n" +
            "      \"storageConfig\": {\n" +
            "        \"originalName\": \"countycode\",\n" +
            "        \"min\": 5,\n" +
            "        \"max\": 5,\n" +
            "        \"cardinality\": 538,\n" +
            "        \"rawFormat\": {\n" +
            "          \"RAW_TYPE\": \"STRING\",\n" +
            "          \"RAW_TYPE_CODE\": \"12\"\n" +
            "        },\n" +
            "        \"metaFlags\": [\n" +
            "          \n" +
            "        ],\n" +
            "        \"originalType\": \"ATTRIBUTE\"\n" +
            "      },\n" +
            "      \"visible\": true,\n" +
            "      \"checkedByDefault\": true,\n" +
            "      \"customListValuesOnly\": false,\n" +
            "      \"type\": \"ATTRIBUTE\",\n" +
            "      \"facet\": false,\n" +
            "      \"distinctCount\": false,\n" +
            "      \"rawFormatSupportTimeGroup\": false,\n" +
            "      \"refreshable\": false,\n" +
            "      \"timeZoneLabel\": \"\",\n" +
            "      \"parentField\": false,\n" +
            "      \"effectiveMin\": 5,\n" +
            "      \"effectiveMax\": 5\n" +
            "    },\n" +
            "    {\n" +
            "      \"label\": \"Createdat\",\n" +
            "      \"name\": \"createdat\",\n" +
            "      \"storageConfig\": {\n" +
            "        \"originalName\": \"createdat\",\n" +
            "        \"min\": 1379975909000,\n" +
            "        \"max\": 1380012211000,\n" +
            "        \"cardinality\": 0,\n" +
            "        \"rawFormat\": {\n" +
            "          \"RAW_TYPE\": \"TIMESTAMP\",\n" +
            "          \"RAW_TYPE_CODE\": \"93\"\n" +
            "        },\n" +
            "        \"metaFlags\": [\n" +
            "          \n" +
            "        ],\n" +
            "        \"originalType\": \"TIME\"\n" +
            "      },\n" +
            "      \"visible\": true,\n" +
            "      \"checkedByDefault\": true,\n" +
            "      \"timestampGranularity\": \"SECOND\",\n" +
            "      \"customListValuesOnly\": false,\n" +
            "      \"type\": \"TIME\",\n" +
            "      \"facet\": false,\n" +
            "      \"distinctCount\": false,\n" +
            "      \"timestampFormat\": \"DEFAULT\",\n" +
            "      \"timeZoneId\": \"UTC\",\n" +
            "      \"rawFormatSupportTimeGroup\": false,\n" +
            "      \"refreshable\": true,\n" +
            "      \"timeZoneLabel\": \"\",\n" +
            "      \"parentField\": false,\n" +
            "      \"effectiveMin\": 1379975909000,\n" +
            "      \"effectiveMax\": 1380012211000\n" +
            "    },\n" +
            "    {\n" +
            "      \"label\": \"Price\",\n" +
            "      \"name\": \"price\",\n" +
            "      \"storageConfig\": {\n" +
            "        \"originalName\": \"price\",\n" +
            "        \"min\": 5,\n" +
            "        \"max\": 2849,\n" +
            "        \"cardinality\": 234,\n" +
            "        \"rawFormat\": {\n" +
            "          \"RAW_TYPE\": \"FLOAT\",\n" +
            "          \"RAW_TYPE_CODE\": \"6\"\n" +
            "        },\n" +
            "        \"metaFlags\": [\n" +
            "          \n" +
            "        ],\n" +
            "        \"originalType\": \"NUMBER\"\n" +
            "      },\n" +
            "      \"visible\": true,\n" +
            "      \"func\": \"SUM\",\n" +
            "      \"checkedByDefault\": true,\n" +
            "      \"customListValuesOnly\": false,\n" +
            "      \"type\": \"NUMBER\",\n" +
            "      \"facet\": false,\n" +
            "      \"distinctCount\": false,\n" +
            "      \"rawFormatSupportTimeGroup\": false,\n" +
            "      \"refreshable\": false,\n" +
            "      \"timeZoneLabel\": \"\",\n" +
            "      \"parentField\": false,\n" +
            "      \"effectiveMin\": 5,\n" +
            "      \"effectiveMax\": 2849\n" +
            "    },\n" +
            "    {\n" +
            "      \"label\": \"Sku\",\n" +
            "      \"name\": \"sku\",\n" +
            "      \"storageConfig\": {\n" +
            "        \"originalName\": \"sku\",\n" +
            "        \"min\": 8,\n" +
            "        \"max\": 8,\n" +
            "        \"cardinality\": 434,\n" +
            "        \"rawFormat\": {\n" +
            "          \"RAW_TYPE\": \"STRING\",\n" +
            "          \"RAW_TYPE_CODE\": \"12\"\n" +
            "        },\n" +
            "        \"metaFlags\": [\n" +
            "          \n" +
            "        ],\n" +
            "        \"originalType\": \"ATTRIBUTE\"\n" +
            "      },\n" +
            "      \"visible\": true,\n" +
            "      \"checkedByDefault\": true,\n" +
            "      \"customListValuesOnly\": false,\n" +
            "      \"type\": \"ATTRIBUTE\",\n" +
            "      \"facet\": false,\n" +
            "      \"distinctCount\": false,\n" +
            "      \"rawFormatSupportTimeGroup\": false,\n" +
            "      \"refreshable\": false,\n" +
            "      \"timeZoneLabel\": \"\",\n" +
            "      \"parentField\": false,\n" +
            "      \"effectiveMin\": 8,\n" +
            "      \"effectiveMax\": 8\n" +
            "    },\n" +
            "    {\n" +
            "      \"label\": \"Usercity\",\n" +
            "      \"name\": \"usercity\",\n" +
            "      \"storageConfig\": {\n" +
            "        \"originalName\": \"usercity\",\n" +
            "        \"min\": 3,\n" +
            "        \"max\": 19,\n" +
            "        \"cardinality\": 500,\n" +
            "        \"rawFormat\": {\n" +
            "          \"RAW_TYPE\": \"STRING\",\n" +
            "          \"RAW_TYPE_CODE\": \"12\"\n" +
            "        },\n" +
            "        \"metaFlags\": [\n" +
            "          \n" +
            "        ],\n" +
            "        \"originalType\": \"ATTRIBUTE\"\n" +
            "      },\n" +
            "      \"visible\": true,\n" +
            "      \"checkedByDefault\": true,\n" +
            "      \"customListValuesOnly\": false,\n" +
            "      \"type\": \"ATTRIBUTE\",\n" +
            "      \"facet\": false,\n" +
            "      \"distinctCount\": false,\n" +
            "      \"rawFormatSupportTimeGroup\": false,\n" +
            "      \"refreshable\": false,\n" +
            "      \"timeZoneLabel\": \"\",\n" +
            "      \"parentField\": false,\n" +
            "      \"effectiveMin\": 3,\n" +
            "      \"effectiveMax\": 19\n" +
            "    },\n" +
            "    {\n" +
            "      \"label\": \"Usergender\",\n" +
            "      \"name\": \"usergender\",\n" +
            "      \"storageConfig\": {\n" +
            "        \"originalName\": \"usergender\",\n" +
            "        \"min\": 4,\n" +
            "        \"max\": 6,\n" +
            "        \"cardinality\": 2,\n" +
            "        \"rawFormat\": {\n" +
            "          \"RAW_TYPE\": \"STRING\",\n" +
            "          \"RAW_TYPE_CODE\": \"12\"\n" +
            "        },\n" +
            "        \"metaFlags\": [\n" +
            "          \n" +
            "        ],\n" +
            "        \"originalType\": \"ATTRIBUTE\"\n" +
            "      },\n" +
            "      \"visible\": true,\n" +
            "      \"checkedByDefault\": true,\n" +
            "      \"customListValuesOnly\": false,\n" +
            "      \"type\": \"ATTRIBUTE\",\n" +
            "      \"facet\": false,\n" +
            "      \"distinctCount\": false,\n" +
            "      \"rawFormatSupportTimeGroup\": false,\n" +
            "      \"refreshable\": false,\n" +
            "      \"timeZoneLabel\": \"\",\n" +
            "      \"parentField\": false,\n" +
            "      \"effectiveMin\": 4,\n" +
            "      \"effectiveMax\": 6\n" +
            "    },\n" +
            "    {\n" +
            "      \"label\": \"Usersentiment\",\n" +
            "      \"name\": \"usersentiment\",\n" +
            "      \"storageConfig\": {\n" +
            "        \"originalName\": \"usersentiment\",\n" +
            "        \"min\": -1,\n" +
            "        \"max\": 1,\n" +
            "        \"cardinality\": 3,\n" +
            "        \"rawFormat\": {\n" +
            "          \"RAW_TYPE\": \"INT\",\n" +
            "          \"RAW_TYPE_CODE\": \"4\"\n" +
            "        },\n" +
            "        \"metaFlags\": [\n" +
            "          \n" +
            "        ],\n" +
            "        \"originalType\": \"INTEGER\"\n" +
            "      },\n" +
            "      \"visible\": true,\n" +
            "      \"func\": \"SUM\",\n" +
            "      \"checkedByDefault\": true,\n" +
            "      \"customListValuesOnly\": false,\n" +
            "      \"type\": \"INTEGER\",\n" +
            "      \"facet\": false,\n" +
            "      \"distinctCount\": false,\n" +
            "      \"rawFormatSupportTimeGroup\": false,\n" +
            "      \"refreshable\": false,\n" +
            "      \"timeZoneLabel\": \"\",\n" +
            "      \"parentField\": false,\n" +
            "      \"effectiveMin\": -1,\n" +
            "      \"effectiveMax\": 1\n" +
            "    },\n" +
            "    {\n" +
            "      \"label\": \"Userstate\",\n" +
            "      \"name\": \"userstate\",\n" +
            "      \"storageConfig\": {\n" +
            "        \"originalName\": \"userstate\",\n" +
            "        \"min\": 4,\n" +
            "        \"max\": 14,\n" +
            "        \"cardinality\": 46,\n" +
            "        \"rawFormat\": {\n" +
            "          \"RAW_TYPE\": \"STRING\",\n" +
            "          \"RAW_TYPE_CODE\": \"12\"\n" +
            "        },\n" +
            "        \"metaFlags\": [\n" +
            "          \n" +
            "        ],\n" +
            "        \"originalType\": \"ATTRIBUTE\"\n" +
            "      },\n" +
            "      \"visible\": true,\n" +
            "      \"checkedByDefault\": true,\n" +
            "      \"customListValuesOnly\": false,\n" +
            "      \"type\": \"ATTRIBUTE\",\n" +
            "      \"facet\": false,\n" +
            "      \"distinctCount\": false,\n" +
            "      \"rawFormatSupportTimeGroup\": false,\n" +
            "      \"refreshable\": false,\n" +
            "      \"timeZoneLabel\": \"\",\n" +
            "      \"parentField\": false,\n" +
            "      \"effectiveMin\": 4,\n" +
            "      \"effectiveMax\": 14\n" +
            "    },\n" +
            "    {\n" +
            "      \"label\": \"Zipcode\",\n" +
            "      \"name\": \"zipcode\",\n" +
            "      \"storageConfig\": {\n" +
            "        \"originalName\": \"zipcode\",\n" +
            "        \"min\": 5,\n" +
            "        \"max\": 5,\n" +
            "        \"cardinality\": 976,\n" +
            "        \"rawFormat\": {\n" +
            "          \"RAW_TYPE\": \"STRING\",\n" +
            "          \"RAW_TYPE_CODE\": \"12\"\n" +
            "        },\n" +
            "        \"metaFlags\": [\n" +
            "          \n" +
            "        ],\n" +
            "        \"originalType\": \"ATTRIBUTE\"\n" +
            "      },\n" +
            "      \"visible\": true,\n" +
            "      \"checkedByDefault\": true,\n" +
            "      \"customListValuesOnly\": false,\n" +
            "      \"type\": \"ATTRIBUTE\",\n" +
            "      \"facet\": false,\n" +
            "      \"distinctCount\": false,\n" +
            "      \"rawFormatSupportTimeGroup\": false,\n" +
            "      \"refreshable\": false,\n" +
            "      \"timeZoneLabel\": \"\",\n" +
            "      \"parentField\": false,\n" +
            "      \"effectiveMin\": 5,\n" +
            "      \"effectiveMax\": 5\n" +
            "    }\n" +
            "  ],\n" +
            "  \"isConnectionValid\": true,\n" +
            "  \"textSearchEnabled\": false,\n" +
            "  \"formulas\": [\n" +
            "    \n" +
            "  ],\n" +
            "  \"controlsCfg\": null,\n" +
            "  \"playbackMode\": false,\n" +
            "  \"visualizations\": [\n" +
            "    \n" +
            "  ]\n" +
            "}";


    static final String SOURCE_CREATION_TEMPLATE_OLD = "{\n" +
            "  \"connectionTypeId\": \"IMPALA_IMPALA\",\n" +
            "  \"type\": \"EDC2\",\n" +
            "  \"subStorageType\": \"IMPALA\",\n" +
            "  \"name\": \"__SOURCE_NAME__\",\n" +
            "  \"description\": \"\",\n" +
            "  \"cacheable\": true,\n" +
            "  \"storageConfiguration\": {\n" +
            "    \"collectionParams\": {\n" +
            "      \n" +
            "    },\n" +
            "    \"parameters\": {\n" +
            "      \n" +
            "    },\n" +
            "    \"connectionId\": \"589b6173e4b013e5f4201379\",\n" +
            "    \"schema\": \"support\",\n" +
            "    \"collection\": \"rts\",\n" +
            "    \"partitions\": {\n" +
            "      \n" +
            "    }\n" +
            "  },\n" +
            "  \"volumeMetric\": {\n" +
            "    \"label\": \"Volume\",\n" +
            "    \"visible\": true,\n" +
            "    \"name\": \"count\",\n" +
            "    \"type\": \"NUMBER\",\n" +
            "    \"storageConfig\": {\n" +
            "      \n" +
            "    }\n" +
            "  },\n" +
            "  \"objectFields\": [\n" +
            "    {\n" +
            "      \"label\": \"Category\",\n" +
            "      \"name\": \"category\",\n" +
            "      \"storageConfig\": {\n" +
            "        \"originalName\": \"category\",\n" +
            "        \"min\": 4,\n" +
            "        \"max\": 15,\n" +
            "        \"cardinality\": 46,\n" +
            "        \"rawFormat\": {\n" +
            "          \"RAW_TYPE\": \"STRING\",\n" +
            "          \"RAW_TYPE_CODE\": \"12\"\n" +
            "        },\n" +
            "        \"metaFlags\": [\n" +
            "          \n" +
            "        ],\n" +
            "        \"originalType\": \"ATTRIBUTE\"\n" +
            "      },\n" +
            "      \"visible\": true,\n" +
            "      \"checkedByDefault\": true,\n" +
            "      \"customListValuesOnly\": false,\n" +
            "      \"type\": \"ATTRIBUTE\",\n" +
            "      \"facet\": false,\n" +
            "      \"distinctCount\": false,\n" +
            "      \"rawFormatSupportTimeGroup\": false,\n" +
            "      \"refreshable\": false,\n" +
            "      \"timeZoneLabel\": \"\",\n" +
            "      \"parentField\": false,\n" +
            "      \"effectiveMin\": 4,\n" +
            "      \"effectiveMax\": 15\n" +
            "    },\n" +
            "    {\n" +
            "      \"label\": \"Categorygroup\",\n" +
            "      \"name\": \"categorygroup\",\n" +
            "      \"storageConfig\": {\n" +
            "        \"originalName\": \"categorygroup\",\n" +
            "        \"min\": 3,\n" +
            "        \"max\": 11,\n" +
            "        \"cardinality\": 5,\n" +
            "        \"rawFormat\": {\n" +
            "          \"RAW_TYPE\": \"STRING\",\n" +
            "          \"RAW_TYPE_CODE\": \"12\"\n" +
            "        },\n" +
            "        \"metaFlags\": [\n" +
            "          \n" +
            "        ],\n" +
            "        \"originalType\": \"ATTRIBUTE\"\n" +
            "      },\n" +
            "      \"visible\": true,\n" +
            "      \"checkedByDefault\": true,\n" +
            "      \"customListValuesOnly\": false,\n" +
            "      \"type\": \"ATTRIBUTE\",\n" +
            "      \"facet\": false,\n" +
            "      \"distinctCount\": false,\n" +
            "      \"rawFormatSupportTimeGroup\": false,\n" +
            "      \"refreshable\": false,\n" +
            "      \"timeZoneLabel\": \"\",\n" +
            "      \"parentField\": false,\n" +
            "      \"effectiveMin\": 3,\n" +
            "      \"effectiveMax\": 11\n" +
            "    },\n" +
            "    {\n" +
            "      \"label\": \"County\",\n" +
            "      \"name\": \"county\",\n" +
            "      \"storageConfig\": {\n" +
            "        \"originalName\": \"county\",\n" +
            "        \"min\": 3,\n" +
            "        \"max\": 22,\n" +
            "        \"cardinality\": 452,\n" +
            "        \"rawFormat\": {\n" +
            "          \"RAW_TYPE\": \"STRING\",\n" +
            "          \"RAW_TYPE_CODE\": \"12\"\n" +
            "        },\n" +
            "        \"metaFlags\": [\n" +
            "          \n" +
            "        ],\n" +
            "        \"originalType\": \"ATTRIBUTE\"\n" +
            "      },\n" +
            "      \"visible\": true,\n" +
            "      \"checkedByDefault\": true,\n" +
            "      \"customListValuesOnly\": false,\n" +
            "      \"type\": \"ATTRIBUTE\",\n" +
            "      \"facet\": false,\n" +
            "      \"distinctCount\": false,\n" +
            "      \"rawFormatSupportTimeGroup\": false,\n" +
            "      \"refreshable\": false,\n" +
            "      \"timeZoneLabel\": \"\",\n" +
            "      \"parentField\": false,\n" +
            "      \"effectiveMin\": 3,\n" +
            "      \"effectiveMax\": 22\n" +
            "    },\n" +
            "    {\n" +
            "      \"label\": \"Countycode\",\n" +
            "      \"name\": \"countycode\",\n" +
            "      \"storageConfig\": {\n" +
            "        \"originalName\": \"countycode\",\n" +
            "        \"min\": 5,\n" +
            "        \"max\": 5,\n" +
            "        \"cardinality\": 538,\n" +
            "        \"rawFormat\": {\n" +
            "          \"RAW_TYPE\": \"STRING\",\n" +
            "          \"RAW_TYPE_CODE\": \"12\"\n" +
            "        },\n" +
            "        \"metaFlags\": [\n" +
            "          \n" +
            "        ],\n" +
            "        \"originalType\": \"ATTRIBUTE\"\n" +
            "      },\n" +
            "      \"visible\": true,\n" +
            "      \"checkedByDefault\": true,\n" +
            "      \"customListValuesOnly\": false,\n" +
            "      \"type\": \"ATTRIBUTE\",\n" +
            "      \"facet\": false,\n" +
            "      \"distinctCount\": false,\n" +
            "      \"rawFormatSupportTimeGroup\": false,\n" +
            "      \"refreshable\": false,\n" +
            "      \"timeZoneLabel\": \"\",\n" +
            "      \"parentField\": false,\n" +
            "      \"effectiveMin\": 5,\n" +
            "      \"effectiveMax\": 5\n" +
            "    },\n" +
            "    {\n" +
            "      \"label\": \"Createdat\",\n" +
            "      \"name\": \"createdat\",\n" +
            "      \"storageConfig\": {\n" +
            "        \"originalName\": \"createdat\",\n" +
            "        \"min\": 1379975909000,\n" +
            "        \"max\": 1380012211000,\n" +
            "        \"cardinality\": 0,\n" +
            "        \"rawFormat\": {\n" +
            "          \"RAW_TYPE\": \"TIMESTAMP\",\n" +
            "          \"RAW_TYPE_CODE\": \"93\"\n" +
            "        },\n" +
            "        \"metaFlags\": [\n" +
            "          \n" +
            "        ],\n" +
            "        \"originalType\": \"TIME\"\n" +
            "      },\n" +
            "      \"visible\": true,\n" +
            "      \"checkedByDefault\": true,\n" +
            "      \"timestampGranularity\": \"SECOND\",\n" +
            "      \"customListValuesOnly\": false,\n" +
            "      \"type\": \"TIME\",\n" +
            "      \"facet\": false,\n" +
            "      \"distinctCount\": false,\n" +
            "      \"timestampFormat\": \"DEFAULT\",\n" +
            "      \"timeZoneId\": \"UTC\",\n" +
            "      \"rawFormatSupportTimeGroup\": false,\n" +
            "      \"refreshable\": true,\n" +
            "      \"timeZoneLabel\": \"\",\n" +
            "      \"parentField\": false,\n" +
            "      \"effectiveMin\": 1379975909000,\n" +
            "      \"effectiveMax\": 1380012211000\n" +
            "    },\n" +
            "    {\n" +
            "      \"label\": \"Price\",\n" +
            "      \"name\": \"price\",\n" +
            "      \"storageConfig\": {\n" +
            "        \"originalName\": \"price\",\n" +
            "        \"min\": 5,\n" +
            "        \"max\": 2849,\n" +
            "        \"cardinality\": 234,\n" +
            "        \"rawFormat\": {\n" +
            "          \"RAW_TYPE\": \"FLOAT\",\n" +
            "          \"RAW_TYPE_CODE\": \"6\"\n" +
            "        },\n" +
            "        \"metaFlags\": [\n" +
            "          \n" +
            "        ],\n" +
            "        \"originalType\": \"NUMBER\"\n" +
            "      },\n" +
            "      \"visible\": true,\n" +
            "      \"func\": \"SUM\",\n" +
            "      \"checkedByDefault\": true,\n" +
            "      \"customListValuesOnly\": false,\n" +
            "      \"type\": \"NUMBER\",\n" +
            "      \"facet\": false,\n" +
            "      \"distinctCount\": false,\n" +
            "      \"rawFormatSupportTimeGroup\": false,\n" +
            "      \"refreshable\": false,\n" +
            "      \"timeZoneLabel\": \"\",\n" +
            "      \"parentField\": false,\n" +
            "      \"effectiveMin\": 5,\n" +
            "      \"effectiveMax\": 2849\n" +
            "    },\n" +
            "    {\n" +
            "      \"label\": \"Sku\",\n" +
            "      \"name\": \"sku\",\n" +
            "      \"storageConfig\": {\n" +
            "        \"originalName\": \"sku\",\n" +
            "        \"min\": 8,\n" +
            "        \"max\": 8,\n" +
            "        \"cardinality\": 434,\n" +
            "        \"rawFormat\": {\n" +
            "          \"RAW_TYPE\": \"STRING\",\n" +
            "          \"RAW_TYPE_CODE\": \"12\"\n" +
            "        },\n" +
            "        \"metaFlags\": [\n" +
            "          \n" +
            "        ],\n" +
            "        \"originalType\": \"ATTRIBUTE\"\n" +
            "      },\n" +
            "      \"visible\": true,\n" +
            "      \"checkedByDefault\": true,\n" +
            "      \"customListValuesOnly\": false,\n" +
            "      \"type\": \"ATTRIBUTE\",\n" +
            "      \"facet\": false,\n" +
            "      \"distinctCount\": false,\n" +
            "      \"rawFormatSupportTimeGroup\": false,\n" +
            "      \"refreshable\": false,\n" +
            "      \"timeZoneLabel\": \"\",\n" +
            "      \"parentField\": false,\n" +
            "      \"effectiveMin\": 8,\n" +
            "      \"effectiveMax\": 8\n" +
            "    },\n" +
            "    {\n" +
            "      \"label\": \"Usercity\",\n" +
            "      \"name\": \"usercity\",\n" +
            "      \"storageConfig\": {\n" +
            "        \"originalName\": \"usercity\",\n" +
            "        \"min\": 3,\n" +
            "        \"max\": 19,\n" +
            "        \"cardinality\": 500,\n" +
            "        \"rawFormat\": {\n" +
            "          \"RAW_TYPE\": \"STRING\",\n" +
            "          \"RAW_TYPE_CODE\": \"12\"\n" +
            "        },\n" +
            "        \"metaFlags\": [\n" +
            "          \n" +
            "        ],\n" +
            "        \"originalType\": \"ATTRIBUTE\"\n" +
            "      },\n" +
            "      \"visible\": true,\n" +
            "      \"checkedByDefault\": true,\n" +
            "      \"customListValuesOnly\": false,\n" +
            "      \"type\": \"ATTRIBUTE\",\n" +
            "      \"facet\": false,\n" +
            "      \"distinctCount\": false,\n" +
            "      \"rawFormatSupportTimeGroup\": false,\n" +
            "      \"refreshable\": false,\n" +
            "      \"timeZoneLabel\": \"\",\n" +
            "      \"parentField\": false,\n" +
            "      \"effectiveMin\": 3,\n" +
            "      \"effectiveMax\": 19\n" +
            "    },\n" +
            "    {\n" +
            "      \"label\": \"Usergender\",\n" +
            "      \"name\": \"usergender\",\n" +
            "      \"storageConfig\": {\n" +
            "        \"originalName\": \"usergender\",\n" +
            "        \"min\": 4,\n" +
            "        \"max\": 6,\n" +
            "        \"cardinality\": 2,\n" +
            "        \"rawFormat\": {\n" +
            "          \"RAW_TYPE\": \"STRING\",\n" +
            "          \"RAW_TYPE_CODE\": \"12\"\n" +
            "        },\n" +
            "        \"metaFlags\": [\n" +
            "          \n" +
            "        ],\n" +
            "        \"originalType\": \"ATTRIBUTE\"\n" +
            "      },\n" +
            "      \"visible\": true,\n" +
            "      \"checkedByDefault\": true,\n" +
            "      \"customListValuesOnly\": false,\n" +
            "      \"type\": \"ATTRIBUTE\",\n" +
            "      \"facet\": false,\n" +
            "      \"distinctCount\": false,\n" +
            "      \"rawFormatSupportTimeGroup\": false,\n" +
            "      \"refreshable\": false,\n" +
            "      \"timeZoneLabel\": \"\",\n" +
            "      \"parentField\": false,\n" +
            "      \"effectiveMin\": 4,\n" +
            "      \"effectiveMax\": 6\n" +
            "    },\n" +
            "    {\n" +
            "      \"label\": \"Usersentiment\",\n" +
            "      \"name\": \"usersentiment\",\n" +
            "      \"storageConfig\": {\n" +
            "        \"originalName\": \"usersentiment\",\n" +
            "        \"min\": -1,\n" +
            "        \"max\": 1,\n" +
            "        \"cardinality\": 3,\n" +
            "        \"rawFormat\": {\n" +
            "          \"RAW_TYPE\": \"INT\",\n" +
            "          \"RAW_TYPE_CODE\": \"4\"\n" +
            "        },\n" +
            "        \"metaFlags\": [\n" +
            "          \n" +
            "        ],\n" +
            "        \"originalType\": \"INTEGER\"\n" +
            "      },\n" +
            "      \"visible\": true,\n" +
            "      \"func\": \"SUM\",\n" +
            "      \"checkedByDefault\": true,\n" +
            "      \"customListValuesOnly\": false,\n" +
            "      \"type\": \"INTEGER\",\n" +
            "      \"facet\": false,\n" +
            "      \"distinctCount\": false,\n" +
            "      \"rawFormatSupportTimeGroup\": false,\n" +
            "      \"refreshable\": false,\n" +
            "      \"timeZoneLabel\": \"\",\n" +
            "      \"parentField\": false,\n" +
            "      \"effectiveMin\": -1,\n" +
            "      \"effectiveMax\": 1\n" +
            "    },\n" +
            "    {\n" +
            "      \"label\": \"Userstate\",\n" +
            "      \"name\": \"userstate\",\n" +
            "      \"storageConfig\": {\n" +
            "        \"originalName\": \"userstate\",\n" +
            "        \"min\": 4,\n" +
            "        \"max\": 14,\n" +
            "        \"cardinality\": 46,\n" +
            "        \"rawFormat\": {\n" +
            "          \"RAW_TYPE\": \"STRING\",\n" +
            "          \"RAW_TYPE_CODE\": \"12\"\n" +
            "        },\n" +
            "        \"metaFlags\": [\n" +
            "          \n" +
            "        ],\n" +
            "        \"originalType\": \"ATTRIBUTE\"\n" +
            "      },\n" +
            "      \"visible\": true,\n" +
            "      \"checkedByDefault\": true,\n" +
            "      \"customListValuesOnly\": false,\n" +
            "      \"type\": \"ATTRIBUTE\",\n" +
            "      \"facet\": false,\n" +
            "      \"distinctCount\": false,\n" +
            "      \"rawFormatSupportTimeGroup\": false,\n" +
            "      \"refreshable\": false,\n" +
            "      \"timeZoneLabel\": \"\",\n" +
            "      \"parentField\": false,\n" +
            "      \"effectiveMin\": 4,\n" +
            "      \"effectiveMax\": 14\n" +
            "    },\n" +
            "    {\n" +
            "      \"label\": \"Zipcode\",\n" +
            "      \"name\": \"zipcode\",\n" +
            "      \"storageConfig\": {\n" +
            "        \"originalName\": \"zipcode\",\n" +
            "        \"min\": 5,\n" +
            "        \"max\": 5,\n" +
            "        \"cardinality\": 976,\n" +
            "        \"rawFormat\": {\n" +
            "          \"RAW_TYPE\": \"STRING\",\n" +
            "          \"RAW_TYPE_CODE\": \"12\"\n" +
            "        },\n" +
            "        \"metaFlags\": [\n" +
            "          \n" +
            "        ],\n" +
            "        \"originalType\": \"ATTRIBUTE\"\n" +
            "      },\n" +
            "      \"visible\": true,\n" +
            "      \"checkedByDefault\": true,\n" +
            "      \"customListValuesOnly\": false,\n" +
            "      \"type\": \"ATTRIBUTE\",\n" +
            "      \"facet\": false,\n" +
            "      \"distinctCount\": false,\n" +
            "      \"rawFormatSupportTimeGroup\": false,\n" +
            "      \"refreshable\": false,\n" +
            "      \"timeZoneLabel\": \"\",\n" +
            "      \"parentField\": false,\n" +
            "      \"effectiveMin\": 5,\n" +
            "      \"effectiveMax\": 5\n" +
            "    }\n" +
            "  ],\n" +
            "  \"isConnectionValid\": true,\n" +
            "  \"textSearchEnabled\": false,\n" +
            "  \"formulas\": [\n" +
            "    \n" +
            "  ],\n" +
            "  \"controlsCfg\": null,\n" +
            "  \"playbackMode\": false,\n" +
            "  \"visualizations\": [\n" +
            "    \n" +
            "  ]\n" +
            "}";


    static private final String SOURCE_UPDATE_TEMPLATE = "{\n" +
            "  \"connectionTypeId\": \"IMPALA_IMPALA\",\n" +
            "  \"type\": \"EDC2\",\n" +
            "  \"subStorageType\": \"IMPALA\",\n" +
            "  \"name\": \"qcluu rts test\",\n" +
            "  \"description\": \"\",\n" +
            "  \"cacheable\": true,\n" +
            "  \"storageConfiguration\": {\n" +
            "    \"connectionId\": \"589b6173e4b013e5f4201379\",\n" +
            "    \"collection\": \"rts\",\n" +
            "    \"schema\": \"support\",\n" +
            "    \"collectionParams\": {\n" +
            "      \n" +
            "    },\n" +
            "    \"parameters\": {\n" +
            "      \n" +
            "    },\n" +
            "    \"partitions\": {\n" +
            "      \n" +
            "    }\n" +
            "  },\n" +
            "  \"volumeMetric\": {\n" +
            "    \"label\": \"Volume\",\n" +
            "    \"visible\": true,\n" +
            "    \"name\": \"count\",\n" +
            "    \"type\": \"NUMBER\",\n" +
            "    \"storageConfig\": {\n" +
            "      \n" +
            "    }\n" +
            "  },\n" +
            "  \"objectFields\": [\n" +
            "    {\n" +
            "      \"sourceId\": \"58ee442de4b06dfd61dd8aba\",\n" +
            "      \"label\": \"Category\",\n" +
            "      \"name\": \"category\",\n" +
            "      \"storageConfig\": {\n" +
            "        \"originalName\": \"category\",\n" +
            "        \"min\": 4,\n" +
            "        \"max\": 15,\n" +
            "        \"cardinality\": 46,\n" +
            "        \"rawFormat\": {\n" +
            "          \"RAW_TYPE\": \"STRING\",\n" +
            "          \"RAW_TYPE_CODE\": \"12\"\n" +
            "        },\n" +
            "        \"metaFlags\": [\n" +
            "          \n" +
            "        ],\n" +
            "        \"originalType\": \"ATTRIBUTE\"\n" +
            "      },\n" +
            "      \"visible\": true,\n" +
            "      \"checkedByDefault\": true,\n" +
            "      \"customListValuesOnly\": false,\n" +
            "      \"type\": \"ATTRIBUTE\",\n" +
            "      \"facet\": false,\n" +
            "      \"distinctCount\": false,\n" +
            "      \"rawFormatSupportTimeGroup\": false,\n" +
            "      \"refreshable\": false,\n" +
            "      \"timeZoneLabel\": \"\",\n" +
            "      \"parentField\": false,\n" +
            "      \"effectiveMin\": 4,\n" +
            "      \"effectiveMax\": 15,\n" +
            "      \"fieldId\": \"58ee442de4b06dfd61dd8abc\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"sourceId\": \"58ee442de4b06dfd61dd8aba\",\n" +
            "      \"label\": \"Categorygroup\",\n" +
            "      \"name\": \"categorygroup\",\n" +
            "      \"storageConfig\": {\n" +
            "        \"originalName\": \"categorygroup\",\n" +
            "        \"min\": 3,\n" +
            "        \"max\": 11,\n" +
            "        \"cardinality\": 5,\n" +
            "        \"rawFormat\": {\n" +
            "          \"RAW_TYPE\": \"STRING\",\n" +
            "          \"RAW_TYPE_CODE\": \"12\"\n" +
            "        },\n" +
            "        \"metaFlags\": [\n" +
            "          \n" +
            "        ],\n" +
            "        \"originalType\": \"ATTRIBUTE\"\n" +
            "      },\n" +
            "      \"visible\": true,\n" +
            "      \"checkedByDefault\": true,\n" +
            "      \"customListValuesOnly\": false,\n" +
            "      \"type\": \"ATTRIBUTE\",\n" +
            "      \"facet\": false,\n" +
            "      \"distinctCount\": false,\n" +
            "      \"rawFormatSupportTimeGroup\": false,\n" +
            "      \"refreshable\": false,\n" +
            "      \"timeZoneLabel\": \"\",\n" +
            "      \"parentField\": false,\n" +
            "      \"effectiveMin\": 3,\n" +
            "      \"effectiveMax\": 11,\n" +
            "      \"fieldId\": \"58ee442de4b06dfd61dd8abd\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"sourceId\": \"58ee442de4b06dfd61dd8aba\",\n" +
            "      \"label\": \"County\",\n" +
            "      \"name\": \"county\",\n" +
            "      \"storageConfig\": {\n" +
            "        \"originalName\": \"county\",\n" +
            "        \"min\": 3,\n" +
            "        \"max\": 22,\n" +
            "        \"cardinality\": 452,\n" +
            "        \"rawFormat\": {\n" +
            "          \"RAW_TYPE\": \"STRING\",\n" +
            "          \"RAW_TYPE_CODE\": \"12\"\n" +
            "        },\n" +
            "        \"metaFlags\": [\n" +
            "          \n" +
            "        ],\n" +
            "        \"originalType\": \"ATTRIBUTE\"\n" +
            "      },\n" +
            "      \"visible\": true,\n" +
            "      \"checkedByDefault\": true,\n" +
            "      \"customListValuesOnly\": false,\n" +
            "      \"type\": \"ATTRIBUTE\",\n" +
            "      \"facet\": false,\n" +
            "      \"distinctCount\": false,\n" +
            "      \"rawFormatSupportTimeGroup\": false,\n" +
            "      \"refreshable\": false,\n" +
            "      \"timeZoneLabel\": \"\",\n" +
            "      \"parentField\": false,\n" +
            "      \"effectiveMin\": 3,\n" +
            "      \"effectiveMax\": 22,\n" +
            "      \"fieldId\": \"58ee442de4b06dfd61dd8abe\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"sourceId\": \"58ee442de4b06dfd61dd8aba\",\n" +
            "      \"label\": \"Countycode\",\n" +
            "      \"name\": \"countycode\",\n" +
            "      \"storageConfig\": {\n" +
            "        \"originalName\": \"countycode\",\n" +
            "        \"min\": 5,\n" +
            "        \"max\": 5,\n" +
            "        \"cardinality\": 538,\n" +
            "        \"rawFormat\": {\n" +
            "          \"RAW_TYPE\": \"STRING\",\n" +
            "          \"RAW_TYPE_CODE\": \"12\"\n" +
            "        },\n" +
            "        \"metaFlags\": [\n" +
            "          \n" +
            "        ],\n" +
            "        \"originalType\": \"ATTRIBUTE\"\n" +
            "      },\n" +
            "      \"visible\": true,\n" +
            "      \"checkedByDefault\": true,\n" +
            "      \"customListValuesOnly\": false,\n" +
            "      \"type\": \"ATTRIBUTE\",\n" +
            "      \"facet\": false,\n" +
            "      \"distinctCount\": false,\n" +
            "      \"rawFormatSupportTimeGroup\": false,\n" +
            "      \"refreshable\": false,\n" +
            "      \"timeZoneLabel\": \"\",\n" +
            "      \"parentField\": false,\n" +
            "      \"effectiveMin\": 5,\n" +
            "      \"effectiveMax\": 5,\n" +
            "      \"fieldId\": \"58ee442de4b06dfd61dd8abf\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"sourceId\": \"58ee442de4b06dfd61dd8aba\",\n" +
            "      \"label\": \"Createdat\",\n" +
            "      \"name\": \"createdat\",\n" +
            "      \"storageConfig\": {\n" +
            "        \"originalName\": \"createdat\",\n" +
            "        \"min\": 1379975909000,\n" +
            "        \"max\": 1380012211000,\n" +
            "        \"cardinality\": 0,\n" +
            "        \"rawFormat\": {\n" +
            "          \"RAW_TYPE\": \"TIMESTAMP\",\n" +
            "          \"RAW_TYPE_CODE\": \"93\"\n" +
            "        },\n" +
            "        \"metaFlags\": [\n" +
            "          \n" +
            "        ],\n" +
            "        \"originalType\": \"TIME\"\n" +
            "      },\n" +
            "      \"visible\": true,\n" +
            "      \"checkedByDefault\": true,\n" +
            "      \"timestampGranularity\": \"SECOND\",\n" +
            "      \"customListValuesOnly\": false,\n" +
            "      \"type\": \"TIME\",\n" +
            "      \"facet\": false,\n" +
            "      \"distinctCount\": false,\n" +
            "      \"timestampFormat\": \"DEFAULT\",\n" +
            "      \"timeZoneId\": \"UTC\",\n" +
            "      \"rawFormatSupportTimeGroup\": true,\n" +
            "      \"refreshable\": true,\n" +
            "      \"timeZoneLabel\": \"UTC\",\n" +
            "      \"parentField\": false,\n" +
            "      \"effectiveMin\": 1379975909000,\n" +
            "      \"effectiveMax\": 1380012211000,\n" +
            "      \"fieldId\": \"58ee442de4b06dfd61dd8ac0\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"sourceId\": \"58ee442de4b06dfd61dd8aba\",\n" +
            "      \"label\": \"Price\",\n" +
            "      \"name\": \"price\",\n" +
            "      \"storageConfig\": {\n" +
            "        \"originalName\": \"price\",\n" +
            "        \"min\": 5,\n" +
            "        \"max\": 2849,\n" +
            "        \"cardinality\": 234,\n" +
            "        \"rawFormat\": {\n" +
            "          \"RAW_TYPE\": \"FLOAT\",\n" +
            "          \"RAW_TYPE_CODE\": \"6\"\n" +
            "        },\n" +
            "        \"metaFlags\": [\n" +
            "          \n" +
            "        ],\n" +
            "        \"originalType\": \"NUMBER\"\n" +
            "      },\n" +
            "      \"visible\": true,\n" +
            "      \"func\": \"SUM\",\n" +
            "      \"checkedByDefault\": true,\n" +
            "      \"customListValuesOnly\": false,\n" +
            "      \"type\": \"NUMBER\",\n" +
            "      \"facet\": false,\n" +
            "      \"distinctCount\": false,\n" +
            "      \"rawFormatSupportTimeGroup\": false,\n" +
            "      \"refreshable\": false,\n" +
            "      \"timeZoneLabel\": \"\",\n" +
            "      \"parentField\": false,\n" +
            "      \"effectiveMin\": 5,\n" +
            "      \"effectiveMax\": 2849,\n" +
            "      \"fieldId\": \"58ee442de4b06dfd61dd8ac1\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"sourceId\": \"58ee442de4b06dfd61dd8aba\",\n" +
            "      \"label\": \"Sku\",\n" +
            "      \"name\": \"sku\",\n" +
            "      \"storageConfig\": {\n" +
            "        \"originalName\": \"sku\",\n" +
            "        \"min\": 8,\n" +
            "        \"max\": 8,\n" +
            "        \"cardinality\": 434,\n" +
            "        \"rawFormat\": {\n" +
            "          \"RAW_TYPE\": \"STRING\",\n" +
            "          \"RAW_TYPE_CODE\": \"12\"\n" +
            "        },\n" +
            "        \"metaFlags\": [\n" +
            "          \n" +
            "        ],\n" +
            "        \"originalType\": \"ATTRIBUTE\"\n" +
            "      },\n" +
            "      \"visible\": true,\n" +
            "      \"checkedByDefault\": true,\n" +
            "      \"customListValuesOnly\": false,\n" +
            "      \"type\": \"ATTRIBUTE\",\n" +
            "      \"facet\": false,\n" +
            "      \"distinctCount\": false,\n" +
            "      \"rawFormatSupportTimeGroup\": false,\n" +
            "      \"refreshable\": false,\n" +
            "      \"timeZoneLabel\": \"\",\n" +
            "      \"parentField\": false,\n" +
            "      \"effectiveMin\": 8,\n" +
            "      \"effectiveMax\": 8,\n" +
            "      \"fieldId\": \"58ee442de4b06dfd61dd8ac2\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"sourceId\": \"58ee442de4b06dfd61dd8aba\",\n" +
            "      \"label\": \"Usercity\",\n" +
            "      \"name\": \"usercity\",\n" +
            "      \"storageConfig\": {\n" +
            "        \"originalName\": \"usercity\",\n" +
            "        \"min\": 3,\n" +
            "        \"max\": 19,\n" +
            "        \"cardinality\": 500,\n" +
            "        \"rawFormat\": {\n" +
            "          \"RAW_TYPE\": \"STRING\",\n" +
            "          \"RAW_TYPE_CODE\": \"12\"\n" +
            "        },\n" +
            "        \"metaFlags\": [\n" +
            "          \n" +
            "        ],\n" +
            "        \"originalType\": \"ATTRIBUTE\"\n" +
            "      },\n" +
            "      \"visible\": true,\n" +
            "      \"checkedByDefault\": true,\n" +
            "      \"customListValuesOnly\": false,\n" +
            "      \"type\": \"ATTRIBUTE\",\n" +
            "      \"facet\": false,\n" +
            "      \"distinctCount\": false,\n" +
            "      \"rawFormatSupportTimeGroup\": false,\n" +
            "      \"refreshable\": false,\n" +
            "      \"timeZoneLabel\": \"\",\n" +
            "      \"parentField\": false,\n" +
            "      \"effectiveMin\": 3,\n" +
            "      \"effectiveMax\": 19,\n" +
            "      \"fieldId\": \"58ee442de4b06dfd61dd8ac3\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"sourceId\": \"58ee442de4b06dfd61dd8aba\",\n" +
            "      \"label\": \"Usergender\",\n" +
            "      \"name\": \"usergender\",\n" +
            "      \"storageConfig\": {\n" +
            "        \"originalName\": \"usergender\",\n" +
            "        \"min\": 4,\n" +
            "        \"max\": 6,\n" +
            "        \"cardinality\": 2,\n" +
            "        \"rawFormat\": {\n" +
            "          \"RAW_TYPE\": \"STRING\",\n" +
            "          \"RAW_TYPE_CODE\": \"12\"\n" +
            "        },\n" +
            "        \"metaFlags\": [\n" +
            "          \n" +
            "        ],\n" +
            "        \"originalType\": \"ATTRIBUTE\"\n" +
            "      },\n" +
            "      \"visible\": true,\n" +
            "      \"checkedByDefault\": true,\n" +
            "      \"customListValuesOnly\": false,\n" +
            "      \"type\": \"ATTRIBUTE\",\n" +
            "      \"facet\": false,\n" +
            "      \"distinctCount\": false,\n" +
            "      \"rawFormatSupportTimeGroup\": false,\n" +
            "      \"refreshable\": false,\n" +
            "      \"timeZoneLabel\": \"\",\n" +
            "      \"parentField\": false,\n" +
            "      \"effectiveMin\": 4,\n" +
            "      \"effectiveMax\": 6,\n" +
            "      \"fieldId\": \"58ee442de4b06dfd61dd8ac4\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"sourceId\": \"58ee442de4b06dfd61dd8aba\",\n" +
            "      \"label\": \"Usersentiment\",\n" +
            "      \"name\": \"usersentiment\",\n" +
            "      \"storageConfig\": {\n" +
            "        \"originalName\": \"usersentiment\",\n" +
            "        \"min\": -1,\n" +
            "        \"max\": 1,\n" +
            "        \"cardinality\": 3,\n" +
            "        \"rawFormat\": {\n" +
            "          \"RAW_TYPE\": \"INT\",\n" +
            "          \"RAW_TYPE_CODE\": \"4\"\n" +
            "        },\n" +
            "        \"metaFlags\": [\n" +
            "          \n" +
            "        ],\n" +
            "        \"originalType\": \"INTEGER\"\n" +
            "      },\n" +
            "      \"visible\": true,\n" +
            "      \"func\": \"SUM\",\n" +
            "      \"checkedByDefault\": true,\n" +
            "      \"customListValuesOnly\": false,\n" +
            "      \"type\": \"INTEGER\",\n" +
            "      \"facet\": false,\n" +
            "      \"distinctCount\": false,\n" +
            "      \"rawFormatSupportTimeGroup\": false,\n" +
            "      \"refreshable\": false,\n" +
            "      \"timeZoneLabel\": \"\",\n" +
            "      \"parentField\": false,\n" +
            "      \"effectiveMin\": -1,\n" +
            "      \"effectiveMax\": 1,\n" +
            "      \"fieldId\": \"58ee442de4b06dfd61dd8ac5\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"sourceId\": \"58ee442de4b06dfd61dd8aba\",\n" +
            "      \"label\": \"Userstate\",\n" +
            "      \"name\": \"userstate\",\n" +
            "      \"storageConfig\": {\n" +
            "        \"originalName\": \"userstate\",\n" +
            "        \"min\": 4,\n" +
            "        \"max\": 14,\n" +
            "        \"cardinality\": 46,\n" +
            "        \"rawFormat\": {\n" +
            "          \"RAW_TYPE\": \"STRING\",\n" +
            "          \"RAW_TYPE_CODE\": \"12\"\n" +
            "        },\n" +
            "        \"metaFlags\": [\n" +
            "          \n" +
            "        ],\n" +
            "        \"originalType\": \"ATTRIBUTE\"\n" +
            "      },\n" +
            "      \"visible\": true,\n" +
            "      \"checkedByDefault\": true,\n" +
            "      \"customListValuesOnly\": false,\n" +
            "      \"type\": \"ATTRIBUTE\",\n" +
            "      \"facet\": false,\n" +
            "      \"distinctCount\": false,\n" +
            "      \"rawFormatSupportTimeGroup\": false,\n" +
            "      \"refreshable\": false,\n" +
            "      \"timeZoneLabel\": \"\",\n" +
            "      \"parentField\": false,\n" +
            "      \"effectiveMin\": 4,\n" +
            "      \"effectiveMax\": 14,\n" +
            "      \"fieldId\": \"58ee442de4b06dfd61dd8ac6\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"sourceId\": \"58ee442de4b06dfd61dd8aba\",\n" +
            "      \"label\": \"Zipcode\",\n" +
            "      \"name\": \"zipcode\",\n" +
            "      \"storageConfig\": {\n" +
            "        \"originalName\": \"zipcode\",\n" +
            "        \"min\": 5,\n" +
            "        \"max\": 5,\n" +
            "        \"cardinality\": 976,\n" +
            "        \"rawFormat\": {\n" +
            "          \"RAW_TYPE\": \"STRING\",\n" +
            "          \"RAW_TYPE_CODE\": \"12\"\n" +
            "        },\n" +
            "        \"metaFlags\": [\n" +
            "          \n" +
            "        ],\n" +
            "        \"originalType\": \"ATTRIBUTE\"\n" +
            "      },\n" +
            "      \"visible\": true,\n" +
            "      \"checkedByDefault\": true,\n" +
            "      \"customListValuesOnly\": false,\n" +
            "      \"type\": \"ATTRIBUTE\",\n" +
            "      \"facet\": false,\n" +
            "      \"distinctCount\": false,\n" +
            "      \"rawFormatSupportTimeGroup\": false,\n" +
            "      \"refreshable\": false,\n" +
            "      \"timeZoneLabel\": \"\",\n" +
            "      \"parentField\": false,\n" +
            "      \"effectiveMin\": 5,\n" +
            "      \"effectiveMax\": 5,\n" +
            "      \"fieldId\": \"58ee442de4b06dfd61dd8ac7\"\n" +
            "    }\n" +
            "  ],\n" +
            "  \"isConnectionValid\": true,\n" +
            "  \"textSearchEnabled\": false,\n" +
            "  \"formulas\": [\n" +
            "    \n" +
            "  ],\n" +
            "  \"controlsCfg\": {\n" +
            "    \"id\": \"58ee442de4b06dfd61dd8abb\",\n" +
            "    \"timeControlCfg\": {\n" +
            "      \"from\": \"+$end_of_data_-1_hour\",\n" +
            "      \"to\": \"+$end_of_data\",\n" +
            "      \"timeField\": \"createdat\"\n" +
            "    },\n" +
            "    \"playerControlCfg\": {\n" +
            "      \"timeWindowScale\": \"ROLLING\"\n" +
            "    }\n" +
            "  },\n" +
            "  \"playbackMode\": false,\n" +
            "  \"visualizations\": [\n" +
            "    {\n" +
            "      \"visId\": \"5898841fe4b0c9dbdb60134b\",\n" +
            "      \"name\": \"Bars\",\n" +
            "      \"controlsCfg\": {\n" +
            "        \"timeControlCfg\": {\n" +
            "          \n" +
            "        },\n" +
            "        \"playerControlCfg\": {\n" +
            "          \n" +
            "        }\n" +
            "      },\n" +
            "      \"type\": \"UBER_BARS\",\n" +
            "      \"enabled\": true,\n" +
            "      \"source\": {\n" +
            "        \"variables\": {\n" +
            "          \"Multi Group By\": [\n" +
            "            {\n" +
            "              \"name\": \"usergender\",\n" +
            "              \"limit\": 50,\n" +
            "              \"sort\": {\n" +
            "                \"dir\": \"desc\",\n" +
            "                \"name\": \"count\"\n" +
            "              }\n" +
            "            },\n" +
            "            {\n" +
            "              \"name\": \"none\",\n" +
            "              \"limit\": 20,\n" +
            "              \"sort\": {\n" +
            "                \"dir\": \"desc\",\n" +
            "                \"name\": \"count\"\n" +
            "              }\n" +
            "            }\n" +
            "          ],\n" +
            "          \"Chart Name\": \"\",\n" +
            "          \"Bar Color\": [\n" +
            "            {\n" +
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
            "                \"autoShowColorLegend\": true\n" +
            "              },\n" +
            "              \"name\": \"count\"\n" +
            "            }\n" +
            "          ],\n" +
            "          \"Metric\": [\n" +
            "            {\n" +
            "              \"name\": \"count\"\n" +
            "            }\n" +
            "          ],\n" +
            "          \"Chart Description\": \"\"\n" +
            "        },\n" +
            "        \"sourceId\": \"58ee442de4b06dfd61dd8aba\",\n" +
            "        \"sourceName\": \"qcluu rts test\",\n" +
            "        \"sourceType\": \"EDC2\",\n" +
            "        \"sparkIt\": false,\n" +
            "        \"playbackMode\": false,\n" +
            "        \"textSearchEnabled\": false,\n" +
            "        \"live\": false\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"visId\": \"5898841fe4b0c9dbdb6013c6\",\n" +
            "      \"name\": \"Bars: Histogram\",\n" +
            "      \"controlsCfg\": {\n" +
            "        \"timeControlCfg\": {\n" +
            "          \n" +
            "        },\n" +
            "        \"playerControlCfg\": {\n" +
            "          \n" +
            "        }\n" +
            "      },\n" +
            "      \"type\": \"HISTOGRAM\",\n" +
            "      \"enabled\": true,\n" +
            "      \"source\": {\n" +
            "        \"variables\": {\n" +
            "          \"Bins Color\": \"#1f78b4\",\n" +
            "          \"Cumulative Line Color\": \"#ff7f00\",\n" +
            "          \"Chart Name\": \"\",\n" +
            "          \"Chart Description\": \"\",\n" +
            "          \"Group By\": {\n" +
            "            \"binsType\": \"auto\",\n" +
            "            \"binsCount\": 10,\n" +
            "            \"binsWidth\": 100,\n" +
            "            \"values\": \"absolute\",\n" +
            "            \"cumulative\": false,\n" +
            "            \"name\": \"usersentiment\"\n" +
            "          }\n" +
            "        },\n" +
            "        \"sourceId\": \"58ee442de4b06dfd61dd8aba\",\n" +
            "        \"sourceName\": \"qcluu rts test\",\n" +
            "        \"sourceType\": \"EDC2\",\n" +
            "        \"sparkIt\": false,\n" +
            "        \"playbackMode\": false,\n" +
            "        \"textSearchEnabled\": false,\n" +
            "        \"live\": false\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"visId\": \"5898841fe4b0c9dbdb601394\",\n" +
            "      \"name\": \"Bars: Multiple Metrics\",\n" +
            "      \"controlsCfg\": {\n" +
            "        \"timeControlCfg\": {\n" +
            "          \n" +
            "        },\n" +
            "        \"playerControlCfg\": {\n" +
            "          \n" +
            "        }\n" +
            "      },\n" +
            "      \"type\": \"MULTI_METRIC_BARS\",\n" +
            "      \"enabled\": true,\n" +
            "      \"source\": {\n" +
            "        \"variables\": {\n" +
            "          \"Chart Name\": \"\",\n" +
            "          \"Chart Description\": \"\",\n" +
            "          \"Bar Height\": [\n" +
            "            {\n" +
            "              \"colorConfig\": {\n" +
            "                \"autoShowColorLegend\": true\n" +
            "              },\n" +
            "              \"name\": \"count\"\n" +
            "            }\n" +
            "          ],\n" +
            "          \"Group By\": {\n" +
            "            \"name\": \"usergender\",\n" +
            "            \"limit\": 20,\n" +
            "            \"sort\": {\n" +
            "              \"dir\": \"desc\",\n" +
            "              \"name\": \"count\"\n" +
            "            }\n" +
            "          }\n" +
            "        },\n" +
            "        \"sourceId\": \"58ee442de4b06dfd61dd8aba\",\n" +
            "        \"sourceName\": \"qcluu rts test\",\n" +
            "        \"sourceType\": \"EDC2\",\n" +
            "        \"sparkIt\": false,\n" +
            "        \"playbackMode\": false,\n" +
            "        \"textSearchEnabled\": false,\n" +
            "        \"live\": false\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"visId\": \"5898841fe4b0c9dbdb601351\",\n" +
            "      \"name\": \"Box Plot\",\n" +
            "      \"controlsCfg\": {\n" +
            "        \"timeControlCfg\": {\n" +
            "          \n" +
            "        },\n" +
            "        \"playerControlCfg\": {\n" +
            "          \n" +
            "        }\n" +
            "      },\n" +
            "      \"type\": \"BOX_PLOT\",\n" +
            "      \"enabled\": true,\n" +
            "      \"source\": {\n" +
            "        \"variables\": {\n" +
            "          \"Chart Name\": \"\",\n" +
            "          \"Metric\": {\n" +
            "            \"name\": \"price\",\n" +
            "            \"func\": \"percentiles\",\n" +
            "            \"args\": [\n" +
            "              0,\n" +
            "              25,\n" +
            "              50,\n" +
            "              75,\n" +
            "              100\n" +
            "            ]\n" +
            "          },\n" +
            "          \"Chart Description\": \"\",\n" +
            "          \"Group By\": {\n" +
            "            \"name\": \"usergender\",\n" +
            "            \"limit\": 10,\n" +
            "            \"sort\": {\n" +
            "              \"dir\": \"desc\",\n" +
            "              \"name\": \"count\"\n" +
            "            }\n" +
            "          }\n" +
            "        },\n" +
            "        \"sourceId\": \"58ee442de4b06dfd61dd8aba\",\n" +
            "        \"sourceName\": \"qcluu rts test\",\n" +
            "        \"sourceType\": \"EDC2\",\n" +
            "        \"sparkIt\": false,\n" +
            "        \"playbackMode\": false,\n" +
            "        \"textSearchEnabled\": false,\n" +
            "        \"live\": false\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"visId\": \"5898841fe4b0c9dbdb6013a4\",\n" +
            "      \"name\": \"Donut\",\n" +
            "      \"controlsCfg\": {\n" +
            "        \"timeControlCfg\": {\n" +
            "          \n" +
            "        },\n" +
            "        \"playerControlCfg\": {\n" +
            "          \n" +
            "        }\n" +
            "      },\n" +
            "      \"type\": \"DONUT\",\n" +
            "      \"enabled\": true,\n" +
            "      \"source\": {\n" +
            "        \"variables\": {\n" +
            "          \"Chart Name\": \"\",\n" +
            "          \"Size\": [\n" +
            "            {\n" +
            "              \"name\": \"count\"\n" +
            "            }\n" +
            "          ],\n" +
            "          \"Chart Description\": \"\",\n" +
            "          \"Group By\": {\n" +
            "            \"name\": \"usergender\",\n" +
            "            \"limit\": 10,\n" +
            "            \"sort\": {\n" +
            "              \"dir\": \"desc\",\n" +
            "              \"name\": \"count\"\n" +
            "            }\n" +
            "          }\n" +
            "        },\n" +
            "        \"sourceId\": \"58ee442de4b06dfd61dd8aba\",\n" +
            "        \"sourceName\": \"qcluu rts test\",\n" +
            "        \"sourceType\": \"EDC2\",\n" +
            "        \"sparkIt\": false,\n" +
            "        \"playbackMode\": false,\n" +
            "        \"textSearchEnabled\": false,\n" +
            "        \"live\": false\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"visId\": \"5898841fe4b0c9dbdb6013c0\",\n" +
            "      \"name\": \"Floating Bubbles\",\n" +
            "      \"controlsCfg\": {\n" +
            "        \"timeControlCfg\": {\n" +
            "          \n" +
            "        },\n" +
            "        \"playerControlCfg\": {\n" +
            "          \n" +
            "        }\n" +
            "      },\n" +
            "      \"type\": \"FLOATING_BUBBLES\",\n" +
            "      \"enabled\": true,\n" +
            "      \"source\": {\n" +
            "        \"variables\": {\n" +
            "          \"Multi Group By\": [\n" +
            "            {\n" +
            "              \"name\": \"usergender\",\n" +
            "              \"limit\": 50,\n" +
            "              \"sort\": {\n" +
            "                \"dir\": \"desc\",\n" +
            "                \"name\": \"count\"\n" +
            "              }\n" +
            "            },\n" +
            "            {\n" +
            "              \"name\": \"categorygroup\",\n" +
            "              \"limit\": 20,\n" +
            "              \"sort\": {\n" +
            "                \"dir\": \"desc\",\n" +
            "                \"name\": \"count\"\n" +
            "              }\n" +
            "            }\n" +
            "          ],\n" +
            "          \"Chart Name\": \"\",\n" +
            "          \"Size\": [\n" +
            "            {\n" +
            "              \"name\": \"count\"\n" +
            "            }\n" +
            "          ],\n" +
            "          \"Y Axis\": [\n" +
            "            {\n" +
            "              \"name\": \"count\"\n" +
            "            }\n" +
            "          ],\n" +
            "          \"Chart Description\": \"\"\n" +
            "        },\n" +
            "        \"sourceId\": \"58ee442de4b06dfd61dd8aba\",\n" +
            "        \"sourceName\": \"qcluu rts test\",\n" +
            "        \"sourceType\": \"EDC2\",\n" +
            "        \"sparkIt\": false,\n" +
            "        \"playbackMode\": false,\n" +
            "        \"textSearchEnabled\": false,\n" +
            "        \"live\": false\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"visId\": \"5898841fe4b0c9dbdb60138f\",\n" +
            "      \"name\": \"Heat Map\",\n" +
            "      \"controlsCfg\": {\n" +
            "        \"timeControlCfg\": {\n" +
            "          \n" +
            "        },\n" +
            "        \"playerControlCfg\": {\n" +
            "          \n" +
            "        }\n" +
            "      },\n" +
            "      \"type\": \"HEAT_MAP\",\n" +
            "      \"enabled\": true,\n" +
            "      \"source\": {\n" +
            "        \"variables\": {\n" +
            "          \"Multi Group By\": [\n" +
            "            {\n" +
            "              \"name\": \"usergender\",\n" +
            "              \"limit\": 50,\n" +
            "              \"sort\": {\n" +
            "                \"dir\": \"desc\",\n" +
            "                \"name\": \"count\"\n" +
            "              }\n" +
            "            },\n" +
            "            {\n" +
            "              \"name\": \"categorygroup\",\n" +
            "              \"limit\": 20,\n" +
            "              \"sort\": {\n" +
            "                \"dir\": \"desc\",\n" +
            "                \"name\": \"count\"\n" +
            "              }\n" +
            "            }\n" +
            "          ],\n" +
            "          \"Chart Name\": \"\",\n" +
            "          \"Color Metric\": [\n" +
            "            {\n" +
            "              \"colorConfig\": {\n" +
            "                \"autoShowColorLegend\": true\n" +
            "              },\n" +
            "              \"name\": \"count\"\n" +
            "            }\n" +
            "          ],\n" +
            "          \"Chart Description\": \"\"\n" +
            "        },\n" +
            "        \"sourceId\": \"58ee442de4b06dfd61dd8aba\",\n" +
            "        \"sourceName\": \"qcluu rts test\",\n" +
            "        \"sourceType\": \"EDC2\",\n" +
            "        \"sparkIt\": false,\n" +
            "        \"playbackMode\": false,\n" +
            "        \"textSearchEnabled\": false,\n" +
            "        \"live\": false\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"visId\": \"5898841fe4b0c9dbdb6013b1\",\n" +
            "      \"name\": \"KPI\",\n" +
            "      \"controlsCfg\": {\n" +
            "        \"timeControlCfg\": {\n" +
            "          \n" +
            "        },\n" +
            "        \"playerControlCfg\": {\n" +
            "          \n" +
            "        }\n" +
            "      },\n" +
            "      \"type\": \"KPI\",\n" +
            "      \"enabled\": true,\n" +
            "      \"source\": {\n" +
            "        \"variables\": {\n" +
            "          \"Comparison Data Color\": \"#ffffff\",\n" +
            "          \"Comparison Metric\": [\n" +
            "            {\n" +
            "              \"name\": \"none\"\n" +
            "            }\n" +
            "          ],\n" +
            "          \"Chart Name\": \"\",\n" +
            "          \"Label Color\": \"#ffffff\",\n" +
            "          \"Up Arrow Color\": \"#68ad44\",\n" +
            "          \"Background Color\": \"#585858\",\n" +
            "          \"Data Color\": \"#d6df23\",\n" +
            "          \"Metric\": [\n" +
            "            {\n" +
            "              \"name\": \"count\"\n" +
            "            }\n" +
            "          ],\n" +
            "          \"Chart Description\": \"\",\n" +
            "          \"Down Arrow Color\": \"#e46839\"\n" +
            "        },\n" +
            "        \"sourceId\": \"58ee442de4b06dfd61dd8aba\",\n" +
            "        \"sourceName\": \"qcluu rts test\",\n" +
            "        \"sourceType\": \"EDC2\",\n" +
            "        \"sparkIt\": false,\n" +
            "        \"playbackMode\": false,\n" +
            "        \"textSearchEnabled\": false,\n" +
            "        \"live\": false\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"visId\": \"5898841fe4b0c9dbdb601399\",\n" +
            "      \"name\": \"Line & Bars Trend\",\n" +
            "      \"controlsCfg\": {\n" +
            "        \"timeControlCfg\": {\n" +
            "          \n" +
            "        },\n" +
            "        \"playerControlCfg\": {\n" +
            "          \n" +
            "        }\n" +
            "      },\n" +
            "      \"type\": \"LINE_AND_BARS\",\n" +
            "      \"enabled\": true,\n" +
            "      \"source\": {\n" +
            "        \"variables\": {\n" +
            "          \"Y1 Color\": \"#43a2ca\",\n" +
            "          \"Y1 Axis\": [\n" +
            "            {\n" +
            "              \"name\": \"count\"\n" +
            "            }\n" +
            "          ],\n" +
            "          \"Y2 Color\": \"#FF7F0E\",\n" +
            "          \"Chart Name\": \"\",\n" +
            "          \"Y2 Axis\": [\n" +
            "            {\n" +
            "              \"name\": \"count\"\n" +
            "            }\n" +
            "          ],\n" +
            "          \"Chart Description\": \"\",\n" +
            "          \"Trend Attribute\": {\n" +
            "            \"name\": \"createdat\",\n" +
            "            \"limit\": 1000,\n" +
            "            \"sort\": {\n" +
            "              \"dir\": \"asc\",\n" +
            "              \"name\": \"createdat\"\n" +
            "            }\n" +
            "          }\n" +
            "        },\n" +
            "        \"sourceId\": \"58ee442de4b06dfd61dd8aba\",\n" +
            "        \"sourceName\": \"qcluu rts test\",\n" +
            "        \"sourceType\": \"EDC2\",\n" +
            "        \"sparkIt\": false,\n" +
            "        \"playbackMode\": false,\n" +
            "        \"textSearchEnabled\": false,\n" +
            "        \"live\": false\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"visId\": \"5898841fe4b0c9dbdb601372\",\n" +
            "      \"name\": \"Line Trend: Attribute Values\",\n" +
            "      \"controlsCfg\": {\n" +
            "        \"timeControlCfg\": {\n" +
            "          \n" +
            "        },\n" +
            "        \"playerControlCfg\": {\n" +
            "          \n" +
            "        }\n" +
            "      },\n" +
            "      \"type\": \"LINE_CHART_CONTINUOUS\",\n" +
            "      \"enabled\": true,\n" +
            "      \"source\": {\n" +
            "        \"variables\": {\n" +
            "          \"Multi Group By\": [\n" +
            "            {\n" +
            "              \"name\": \"usergender\",\n" +
            "              \"limit\": 20,\n" +
            "              \"sort\": {\n" +
            "                \"dir\": \"desc\",\n" +
            "                \"name\": \"count\"\n" +
            "              }\n" +
            "            },\n" +
            "            {\n" +
            "              \"name\": \"createdat\",\n" +
            "              \"limit\": 1000,\n" +
            "              \"sort\": {\n" +
            "                \"dir\": \"asc\",\n" +
            "                \"name\": \"createdat\"\n" +
            "              }\n" +
            "            }\n" +
            "          ],\n" +
            "          \"Chart Name\": \"\",\n" +
            "          \"Y Axis\": [\n" +
            "            {\n" +
            "              \"name\": \"count\"\n" +
            "            }\n" +
            "          ],\n" +
            "          \"Chart Description\": \"\"\n" +
            "        },\n" +
            "        \"sourceId\": \"58ee442de4b06dfd61dd8aba\",\n" +
            "        \"sourceName\": \"qcluu rts test\",\n" +
            "        \"sourceType\": \"EDC2\",\n" +
            "        \"sparkIt\": false,\n" +
            "        \"playbackMode\": false,\n" +
            "        \"textSearchEnabled\": false,\n" +
            "        \"live\": false\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"visId\": \"5898841fe4b0c9dbdb601356\",\n" +
            "      \"name\": \"Line Trend: Multiple Metrics\",\n" +
            "      \"controlsCfg\": {\n" +
            "        \"timeControlCfg\": {\n" +
            "          \n" +
            "        },\n" +
            "        \"playerControlCfg\": {\n" +
            "          \n" +
            "        }\n" +
            "      },\n" +
            "      \"type\": \"LINE_CHART\",\n" +
            "      \"enabled\": true,\n" +
            "      \"source\": {\n" +
            "        \"variables\": {\n" +
            "          \"Chart Name\": \"\",\n" +
            "          \"Y Axis\": [\n" +
            "            {\n" +
            "              \"colorConfig\": {\n" +
            "                \"autoShowColorLegend\": true\n" +
            "              },\n" +
            "              \"name\": \"count\"\n" +
            "            }\n" +
            "          ],\n" +
            "          \"Chart Description\": \"\",\n" +
            "          \"Trend Attribute\": {\n" +
            "            \"name\": \"createdat\",\n" +
            "            \"limit\": 1000,\n" +
            "            \"sort\": {\n" +
            "              \"dir\": \"asc\",\n" +
            "              \"name\": \"createdat\"\n" +
            "            }\n" +
            "          }\n" +
            "        },\n" +
            "        \"sourceId\": \"58ee442de4b06dfd61dd8aba\",\n" +
            "        \"sourceName\": \"qcluu rts test\",\n" +
            "        \"sourceType\": \"EDC2\",\n" +
            "        \"sparkIt\": false,\n" +
            "        \"playbackMode\": false,\n" +
            "        \"textSearchEnabled\": false,\n" +
            "        \"live\": false\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"visId\": \"5898841fe4b0c9dbdb60133e\",\n" +
            "      \"name\": \"Packed Bubbles\",\n" +
            "      \"controlsCfg\": {\n" +
            "        \"timeControlCfg\": {\n" +
            "          \n" +
            "        },\n" +
            "        \"playerControlCfg\": {\n" +
            "          \n" +
            "        }\n" +
            "      },\n" +
            "      \"type\": \"BUBBLES\",\n" +
            "      \"enabled\": true,\n" +
            "      \"source\": {\n" +
            "        \"variables\": {\n" +
            "          \"Bubble Size\": [\n" +
            "            {\n" +
            "              \"name\": \"count\"\n" +
            "            }\n" +
            "          ],\n" +
            "          \"Chart Name\": \"\",\n" +
            "          \"Bubble Color\": [\n" +
            "            {\n" +
            "              \"colorConfig\": {\n" +
            "                \"autoShowColorLegend\": true\n" +
            "              },\n" +
            "              \"name\": \"count\"\n" +
            "            }\n" +
            "          ],\n" +
            "          \"Chart Description\": \"\",\n" +
            "          \"Group By\": {\n" +
            "            \"name\": \"usergender\",\n" +
            "            \"limit\": 20,\n" +
            "            \"sort\": {\n" +
            "              \"dir\": \"desc\",\n" +
            "              \"name\": \"count\"\n" +
            "            }\n" +
            "          }\n" +
            "        },\n" +
            "        \"sourceId\": \"58ee442de4b06dfd61dd8aba\",\n" +
            "        \"sourceName\": \"qcluu rts test\",\n" +
            "        \"sourceType\": \"EDC2\",\n" +
            "        \"sparkIt\": false,\n" +
            "        \"playbackMode\": false,\n" +
            "        \"textSearchEnabled\": false,\n" +
            "        \"live\": false\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"visId\": \"5898841fe4b0c9dbdb601361\",\n" +
            "      \"name\": \"Pie\",\n" +
            "      \"controlsCfg\": {\n" +
            "        \"timeControlCfg\": {\n" +
            "          \n" +
            "        },\n" +
            "        \"playerControlCfg\": {\n" +
            "          \n" +
            "        }\n" +
            "      },\n" +
            "      \"type\": \"PIE\",\n" +
            "      \"enabled\": true,\n" +
            "      \"source\": {\n" +
            "        \"variables\": {\n" +
            "          \"Chart Name\": \"\",\n" +
            "          \"Size\": [\n" +
            "            {\n" +
            "              \"name\": \"count\"\n" +
            "            }\n" +
            "          ],\n" +
            "          \"Chart Description\": \"\",\n" +
            "          \"Group By\": {\n" +
            "            \"name\": \"usergender\",\n" +
            "            \"limit\": 20,\n" +
            "            \"sort\": {\n" +
            "              \"dir\": \"desc\",\n" +
            "              \"name\": \"count\"\n" +
            "            }\n" +
            "          }\n" +
            "        },\n" +
            "        \"sourceId\": \"58ee442de4b06dfd61dd8aba\",\n" +
            "        \"sourceName\": \"qcluu rts test\",\n" +
            "        \"sourceType\": \"EDC2\",\n" +
            "        \"sparkIt\": false,\n" +
            "        \"playbackMode\": false,\n" +
            "        \"textSearchEnabled\": false,\n" +
            "        \"live\": false\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"visId\": \"5898841fe4b0c9dbdb601366\",\n" +
            "      \"name\": \"Pivot Table\",\n" +
            "      \"controlsCfg\": {\n" +
            "        \"timeControlCfg\": {\n" +
            "          \n" +
            "        },\n" +
            "        \"playerControlCfg\": {\n" +
            "          \n" +
            "        }\n" +
            "      },\n" +
            "      \"type\": \"PIVOT_TABLE\",\n" +
            "      \"enabled\": true,\n" +
            "      \"source\": {\n" +
            "        \"variables\": {\n" +
            "          \"Metrics\": [\n" +
            "            {\n" +
            "              \"name\": \"count\"\n" +
            "            }\n" +
            "          ],\n" +
            "          \"Chart Name\": \"\",\n" +
            "          \"Column Attributes\": [\n" +
            "            \n" +
            "          ],\n" +
            "          \"Metric Direction\": \"Columns\",\n" +
            "          \"Row Attributes\": [\n" +
            "            {\n" +
            "              \"name\": \"usergender\"\n" +
            "            }\n" +
            "          ],\n" +
            "          \"Rows per Page\": \"200\",\n" +
            "          \"Chart Description\": \"\"\n" +
            "        },\n" +
            "        \"sourceId\": \"58ee442de4b06dfd61dd8aba\",\n" +
            "        \"sourceName\": \"qcluu rts test\",\n" +
            "        \"sourceType\": \"EDC2\",\n" +
            "        \"sparkIt\": false,\n" +
            "        \"playbackMode\": false,\n" +
            "        \"textSearchEnabled\": false,\n" +
            "        \"live\": false\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"visId\": \"5898841fe4b0c9dbdb60136e\",\n" +
            "      \"name\": \"Raw Data Table\",\n" +
            "      \"controlsCfg\": {\n" +
            "        \"timeControlCfg\": {\n" +
            "          \n" +
            "        },\n" +
            "        \"playerControlCfg\": {\n" +
            "          \n" +
            "        }\n" +
            "      },\n" +
            "      \"type\": \"RAW_DATA_TABLE\",\n" +
            "      \"enabled\": true,\n" +
            "      \"source\": {\n" +
            "        \"variables\": {\n" +
            "          \"Chart Name\": \"\",\n" +
            "          \"Columns\": [\n" +
            "            {\n" +
            "              \"name\": \"category\",\n" +
            "              \"limit\": 250\n" +
            "            },\n" +
            "            {\n" +
            "              \"name\": \"categorygroup\",\n" +
            "              \"limit\": 250\n" +
            "            },\n" +
            "            {\n" +
            "              \"name\": \"county\",\n" +
            "              \"limit\": 250\n" +
            "            },\n" +
            "            {\n" +
            "              \"name\": \"countycode\",\n" +
            "              \"limit\": 250\n" +
            "            },\n" +
            "            {\n" +
            "              \"name\": \"createdat\",\n" +
            "              \"limit\": 250\n" +
            "            },\n" +
            "            {\n" +
            "              \"name\": \"price\",\n" +
            "              \"limit\": 250\n" +
            "            },\n" +
            "            {\n" +
            "              \"name\": \"sku\",\n" +
            "              \"limit\": 250\n" +
            "            },\n" +
            "            {\n" +
            "              \"name\": \"usercity\",\n" +
            "              \"limit\": 250\n" +
            "            },\n" +
            "            {\n" +
            "              \"name\": \"usergender\",\n" +
            "              \"limit\": 250\n" +
            "            },\n" +
            "            {\n" +
            "              \"name\": \"usersentiment\",\n" +
            "              \"limit\": 250\n" +
            "            },\n" +
            "            {\n" +
            "              \"name\": \"userstate\",\n" +
            "              \"limit\": 250\n" +
            "            },\n" +
            "            {\n" +
            "              \"name\": \"zipcode\",\n" +
            "              \"limit\": 250\n" +
            "            }\n" +
            "          ],\n" +
            "          \"Chart Description\": \"\"\n" +
            "        },\n" +
            "        \"sourceId\": \"58ee442de4b06dfd61dd8aba\",\n" +
            "        \"sourceName\": \"qcluu rts test\",\n" +
            "        \"sourceType\": \"EDC2\",\n" +
            "        \"sparkIt\": false,\n" +
            "        \"playbackMode\": false,\n" +
            "        \"textSearchEnabled\": false,\n" +
            "        \"live\": false\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"visId\": \"5898841fe4b0c9dbdb601344\",\n" +
            "      \"name\": \"Scatter Plot\",\n" +
            "      \"controlsCfg\": {\n" +
            "        \"timeControlCfg\": {\n" +
            "          \n" +
            "        },\n" +
            "        \"playerControlCfg\": {\n" +
            "          \n" +
            "        }\n" +
            "      },\n" +
            "      \"type\": \"SCATTERPLOT\",\n" +
            "      \"enabled\": true,\n" +
            "      \"source\": {\n" +
            "        \"variables\": {\n" +
            "          \"X Axis\": [\n" +
            "            {\n" +
            "              \"name\": \"count\"\n" +
            "            }\n" +
            "          ],\n" +
            "          \"Chart Name\": \"\",\n" +
            "          \"Size\": [\n" +
            "            {\n" +
            "              \"name\": \"count\"\n" +
            "            }\n" +
            "          ],\n" +
            "          \"Y Axis\": [\n" +
            "            {\n" +
            "              \"name\": \"count\"\n" +
            "            }\n" +
            "          ],\n" +
            "          \"Chart Description\": \"\",\n" +
            "          \"Group By\": {\n" +
            "            \"name\": \"usergender\",\n" +
            "            \"limit\": 20,\n" +
            "            \"sort\": {\n" +
            "              \"dir\": \"desc\",\n" +
            "              \"name\": \"count\"\n" +
            "            }\n" +
            "          }\n" +
            "        },\n" +
            "        \"sourceId\": \"58ee442de4b06dfd61dd8aba\",\n" +
            "        \"sourceName\": \"qcluu rts test\",\n" +
            "        \"sourceType\": \"EDC2\",\n" +
            "        \"sparkIt\": false,\n" +
            "        \"playbackMode\": false,\n" +
            "        \"textSearchEnabled\": false,\n" +
            "        \"live\": false\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"visId\": \"5898841fe4b0c9dbdb601389\",\n" +
            "      \"name\": \"Tree Map\",\n" +
            "      \"controlsCfg\": {\n" +
            "        \"timeControlCfg\": {\n" +
            "          \n" +
            "        },\n" +
            "        \"playerControlCfg\": {\n" +
            "          \n" +
            "        }\n" +
            "      },\n" +
            "      \"type\": \"TREE_MAP\",\n" +
            "      \"enabled\": true,\n" +
            "      \"source\": {\n" +
            "        \"variables\": {\n" +
            "          \"Chart Name\": \"\",\n" +
            "          \"Size\": [\n" +
            "            {\n" +
            "              \"name\": \"count\"\n" +
            "            }\n" +
            "          ],\n" +
            "          \"Color\": [\n" +
            "            {\n" +
            "              \"colorConfig\": {\n" +
            "                \"autoShowColorLegend\": true\n" +
            "              },\n" +
            "              \"name\": \"count\"\n" +
            "            }\n" +
            "          ],\n" +
            "          \"Chart Description\": \"\",\n" +
            "          \"Group By\": {\n" +
            "            \"name\": \"usergender\",\n" +
            "            \"limit\": 100,\n" +
            "            \"sort\": {\n" +
            "              \"dir\": \"desc\",\n" +
            "              \"name\": \"count\"\n" +
            "            }\n" +
            "          }\n" +
            "        },\n" +
            "        \"sourceId\": \"58ee442de4b06dfd61dd8aba\",\n" +
            "        \"sourceName\": \"qcluu rts test\",\n" +
            "        \"sourceType\": \"EDC2\",\n" +
            "        \"sparkIt\": false,\n" +
            "        \"playbackMode\": false,\n" +
            "        \"textSearchEnabled\": false,\n" +
            "        \"live\": false\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"visId\": \"5898841fe4b0c9dbdb60135b\",\n" +
            "      \"name\": \"Word Cloud\",\n" +
            "      \"controlsCfg\": {\n" +
            "        \"timeControlCfg\": {\n" +
            "          \n" +
            "        },\n" +
            "        \"playerControlCfg\": {\n" +
            "          \n" +
            "        }\n" +
            "      },\n" +
            "      \"type\": \"WORD_CLOUD\",\n" +
            "      \"enabled\": true,\n" +
            "      \"source\": {\n" +
            "        \"variables\": {\n" +
            "          \"Chart Name\": \"\",\n" +
            "          \"Size\": [\n" +
            "            {\n" +
            "              \"name\": \"count\"\n" +
            "            }\n" +
            "          ],\n" +
            "          \"Color\": [\n" +
            "            {\n" +
            "              \"colorConfig\": {\n" +
            "                \"autoShowColorLegend\": true\n" +
            "              },\n" +
            "              \"name\": \"count\"\n" +
            "            }\n" +
            "          ],\n" +
            "          \"Chart Description\": \"\",\n" +
            "          \"Group By\": {\n" +
            "            \"name\": \"usergender\",\n" +
            "            \"limit\": 20,\n" +
            "            \"sort\": {\n" +
            "              \"dir\": \"desc\",\n" +
            "              \"name\": \"count\"\n" +
            "            }\n" +
            "          }\n" +
            "        },\n" +
            "        \"sourceId\": \"58ee442de4b06dfd61dd8aba\",\n" +
            "        \"sourceName\": \"qcluu rts test\",\n" +
            "        \"sourceType\": \"EDC2\",\n" +
            "        \"sparkIt\": false,\n" +
            "        \"playbackMode\": false,\n" +
            "        \"textSearchEnabled\": false,\n" +
            "        \"live\": false\n" +
            "      }\n" +
            "    }\n" +
            "  ],\n" +
            "  \"features\": [\n" +
            "    {\n" +
            "      \"name\": \"PERCENTILES\",\n" +
            "      \"params\": {\n" +
            "        \n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"name\": \"MULTI_GROUP_SUPPORT\",\n" +
            "      \"params\": {\n" +
            "        \n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"name\": \"SUPPORTED_BY_SPARKIT\",\n" +
            "      \"params\": {\n" +
            "        \n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"name\": \"LIVE_SOURCE\",\n" +
            "      \"params\": {\n" +
            "        \n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"name\": \"GROUP_BY_TIME\",\n" +
            "      \"params\": {\n" +
            "        \"GROUP_BY_UNIX_TIME\": \"true\"\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"name\": \"HISTOGRAM\",\n" +
            "      \"params\": {\n" +
            "        \"HISTOGRAM_FOR_FLOAT_POINT_VALUES\": \"true\"\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"name\": \"LV_METRIC\",\n" +
            "      \"params\": {\n" +
            "        \n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"name\": \"PAGING_AND_SORTING\",\n" +
            "      \"params\": {\n" +
            "        \"AGGREGATED\": \"true\"\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"name\": \"PARTITION\",\n" +
            "      \"params\": {\n" +
            "        \n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"name\": \"DISTINCT_COUNT\",\n" +
            "      \"params\": {\n" +
            "        \"DISTINCT_COUNT_ONLY_ONE\": \"true\"\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"name\": \"SUPPORTS_SCHEMA\",\n" +
            "      \"params\": {\n" +
            "        \n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"name\": \"REFRESHABLE\",\n" +
            "      \"params\": {\n" +
            "        \n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"name\": \"CUSTOM_QUERY\",\n" +
            "      \"params\": {\n" +
            "        \n" +
            "      }\n" +
            "    }\n" +
            "  ],\n" +
            "  \"createdByUserID\": \"5898841fe4b0c9dbdb601338\",\n" +
            "  \"lastModifiedByUserID\": \"5898841fe4b0c9dbdb601338\",\n" +
            "  \"createdDate\": \"2017-04-12 15:13:49.156\",\n" +
            "  \"lastModifiedDate\": \"2017-04-12 15:13:49.156\",\n" +
            "  \"id\": \"58ee442de4b06dfd61dd8aba\",\n" +
            "  \"accountId\": \"5898841fe4b0c9dbdb601337\",\n" +
            "  \"live\": false,\n" +
            "  \"liveRefreshRate\": 1,\n" +
            "  \"delay\": 1,\n" +
            "  \"delayUnit\": \"SECONDS\",\n" +
            "  \"enabled\": true,\n" +
            "  \"viewsCount\": 0,\n" +
            "  \"cacheAttributeValues\": true,\n" +
            "  \"queryStrategy\": \"LOAD_ALL\",\n" +
            "  \"hardLimit\": 100000,\n" +
            "  \"version\": 1,\n" +
            "  \"delayMillis\": 1000\n" +
            "}";


}