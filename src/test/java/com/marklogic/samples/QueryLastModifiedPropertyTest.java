package com.marklogic.samples;

import com.marklogic.client.DatabaseClient;
import com.marklogic.client.document.XMLDocumentManager;
import com.marklogic.client.io.SearchHandle;
import com.marklogic.client.io.StringHandle;
import com.marklogic.client.query.QueryManager;
import com.marklogic.client.query.RawStructuredQueryDefinition;
import com.marklogic.client.query.StringQueryDefinition;
import com.marklogic.junit.spring.AbstractSpringTest;
import org.junit.After;
import org.junit.Test;
import org.junit.Before;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = {com.marklogic.junit.spring.BasicTestConfig.class })
public class QueryLastModifiedPropertyTest extends AbstractSpringTest {

    DatabaseClient client;
    XMLDocumentManager docMgr;

    String uri = "hello.xml";

    @Before
    public void loadSampleDocument() {
        client = getClientProvider().getDatabaseClient();
        docMgr = client.newXMLDocumentManager();
        StringHandle handle = new StringHandle("<hello />");
        docMgr.write(uri, handle);
    }

    @Test
    public void documentLastModifiedTest() {
        assertNotNull(docMgr.read(uri));

        QueryManager queryMgr = client.newQueryManager();
        String rawXMLQuery =
                "<query xmlns=\"http://marklogic.com/appservices/search\">" +
                     "<properties-fragment-query>" +
                     "  <range-constraint-query>" +
                     "    <constraint-name>modified</constraint-name>" +
                     "      <value>2012-12-31T00:00:00.0Z</value>" +
                     "      <range-operator>%s</range-operator>" +
                     "  </range-constraint-query>" +
                     "</properties-fragment-query>" +
                 "</query>";
        StringHandle rawHandle = new StringHandle(String.format(rawXMLQuery, "GT"));
        // Use the persistent options previously saved as "default"
        RawStructuredQueryDefinition querydef = queryMgr.newRawStructuredQueryDefinition(rawHandle, "default");
        SearchHandle resultsHandle = queryMgr.search(querydef, new SearchHandle());
        assertEquals(1, resultsHandle.getTotalResults());

        rawHandle = new StringHandle(String.format(rawXMLQuery, "LT"));
        // Use the persistent options previously saved as "default"
        querydef = queryMgr.newRawStructuredQueryDefinition(rawHandle, "default");
        resultsHandle = queryMgr.search(querydef, new SearchHandle());
        assertEquals(0, resultsHandle.getTotalResults());
    }

    @After
    public void destroy() {
        client.release();
    }
}
