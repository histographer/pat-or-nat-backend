package no.digipat.compare.servlets;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.meterware.httpunit.PostMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.meterware.httpunit.protocol.MessageBody;
import com.meterware.httpunit.protocol.ParameterCollection;
import com.mongodb.MongoClient;

import junitparams.JUnitParamsRunner;
import no.digipat.compare.models.image.ImageChoice;
import no.digipat.compare.models.image.ImageComparison;
import no.digipat.compare.models.project.Project;
import no.digipat.compare.mongodb.dao.MongoImageComparisonDAO;
import no.digipat.compare.mongodb.dao.MongoProjectDAO;

@RunWith(JUnitParamsRunner.class)
public class CompareImageTest {
    
    private static URL baseUrl;
    private static MongoClient client;
    private static String databaseName;
    private static MongoImageComparisonDAO comparisonDao;
    private static MongoProjectDAO projectDao;
    private static WebConversation conversation;
    
    @BeforeClass
    public static void setUpClass() throws Exception {
        baseUrl = IntegrationTests.getBaseUrl();
        client = IntegrationTests.getMongoClient();
        databaseName = IntegrationTests.getDatabaseName();
        comparisonDao = new MongoImageComparisonDAO(client, databaseName);
        projectDao = new MongoProjectDAO(client, databaseName);
        
        Project project = new Project().setId(20l).setName("testname");
        projectDao.createProject(project);
        
        conversation = new WebConversation();
        login(conversation);
        conversation.setExceptionsThrownOnErrorStatus(false);
    }
    
    private static void login(WebConversation conversation) throws Exception {
        WebRequest loginRequest = createPostRequestWithMessageBody(
                "session",
                "{\"monitorType\": \"normal\", \"hospital\": \"St. Olavs\", \"projectId\": 20}",
                "application/json");
        conversation.sendRequest(loginRequest);
    }
    
    private static PostMethodWebRequest createPostRequestWithMessageBody(String path, String messageBody, String contentType) throws MalformedURLException {
        return new PostMethodWebRequest(new URL(baseUrl, path).toString()) {
            @Override
            protected MessageBody getMessageBody() {
                return new MessageBody("UTF8") {
                    @Override
                    public void writeTo(OutputStream outputStream, ParameterCollection parameters) throws IOException {
                        PrintWriter writer = new PrintWriter(outputStream);
                        writer.print(messageBody);
                        writer.flush();
                    }
                    @Override
                    public String getContentType() {
                        return contentType;
                    }
                };
            }
        };
    }
    
    @Test
    public void testCompareImages() throws Exception {
        PostMethodWebRequest request = createPostRequestWithMessageBody("scoring",
                "{\"projectId\": 20, \"chosen\": {\"id\": 1, \"comment\": \"a comment\"}, \"other\": {\"id\": 2}}",
                "application/json");
        WebResponse response = conversation.sendRequest(request);
        
        assertEquals(200, response.getResponseCode());
        List<ImageComparison> comparisons = comparisonDao.getAllImageComparisons(20L);
        assertEquals(1, comparisons.size());
        ImageComparison comparison = comparisons.get(0);
        assertEquals((Long) 20L, comparison.getProjectId());
        ImageChoice winner = comparison.getWinner();
        assertEquals(1L, winner.getImageId());
        assertEquals("a comment", winner.getComment());
        ImageChoice loser = comparison.getLoser();
        assertEquals(2L, loser.getImageId());
    }
    
    @AfterClass
    public static void tearDown() {
        client.getDatabase(databaseName).drop();
    }
    
}
