package zoomdata.util;

import com.amazonaws.util.json.JSONObject;
import org.junit.Test;

import javax.xml.bind.DatatypeConverter;

import static org.junit.Assert.*;

public class ZoomdataUtilTest {

    @Test
    public void testValidateConnection() throws Exception {

    }

    @Test
    public void testCreateConnection() throws Exception {

    }


    @Test
    public void testCreateSource() throws Exception {
        final String connectionId = "589b6173e4b013e5f4201379";
        final String sourceName = "qcluu_test_source3";
        ZoomdataUtil.createSource(sourceName, connectionId);
    }

    @Test

    public void testGetSourceIdByName() throws Exception {
        final String sourceName = "qcluu_test_source3";
        ZoomdataUtil.getSourceByName(sourceName);
    }

    @Test
    public void testUpdateSource() throws Exception {
        final String sourceId = "58ee442de4b06dfd61dd8aba";
        ZoomdataUtil.updateSource(sourceId);
    }

    @Test
    public void testDeleteSource() throws Exception {
        final String sourceId = "5909d919e4b0f816d6e501f1";
        ZoomdataUtil.deleteSourceById(sourceId);
    }
}