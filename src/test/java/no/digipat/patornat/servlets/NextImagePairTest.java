package no.digipat.patornat.servlets;

import static org.junit.Assert.*;

import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.mongodb.MongoClient;

import no.digipat.patornat.mongodb.dao.MongoBestImageDAO;
import no.digipat.patornat.mongodb.dao.MongoImageDAO;
import no.digipat.patornat.mongodb.models.image.BestImageChoice;
import no.digipat.patornat.mongodb.models.image.Image;
import no.digipat.patornat.mongodb.models.image.ImageChoice;

public class NextImagePairTest {
    
    private static URL baseUrl;
    private static MongoClient client;
    private static String databaseName;
    private MongoImageDAO imageDao;
    private MongoBestImageDAO choiceDao;
    
    @BeforeClass
    public static void setUpClass() {
        baseUrl = IntegrationTests.getBaseUrl();
        client = IntegrationTests.getMongoClient();
        databaseName = IntegrationTests.getDatabaseName();
    }
    
    @Before
    public void setUp() {
        imageDao = new MongoImageDAO(client, databaseName);
        choiceDao = new MongoBestImageDAO(client, databaseName);
    }
    
    @Test
    public void testNotEnoughImages() throws Exception {
        WebConversation conversation = new WebConversation();
        conversation.setExceptionsThrownOnErrorStatus(false);
        WebRequest request = new GetMethodWebRequest(baseUrl, "imagePair");
        // Zero images in database
        WebResponse response1 = conversation.getResponse(request);
        assertEquals(500, response1.getResponseCode());
        // One image in database
        imageDao.createImage(new Image(1));
        WebResponse response2 = conversation.getResponse(request);
        assertEquals(500, response2.getResponseCode());
    }
    
    @Test
    public void testWithValidServerState() throws Exception {
        choiceDao.getAllBestImageChoices();
        imageDao.createImage(new Image(1337));
        imageDao.createImage(new Image(42));
        choiceDao.createBestImage(new BestImageChoice("some_user", new ImageChoice(1, "good"), new ImageChoice(2, "really bad")));
        WebConversation conversation = new WebConversation();
        WebRequest request = new GetMethodWebRequest(baseUrl, "imagePair");
        WebResponse response = conversation.getResponse(request);
        // Test status code
        assertEquals(200, response.getResponseCode());
        // Test content type
        assertEquals("application/json", response.getContentType());
        // Test response body
        String body = response.getText();
        JSONParser parser = new JSONParser();
        JSONObject json = (JSONObject) parser.parse(body);
        Set<String> expectedKeys = new HashSet<>();
        expectedKeys.add("pair");
        assertEquals(expectedKeys, json.keySet());
        JSONArray pair = (JSONArray) json.get("pair");
        Set<Integer> expectedIds = new HashSet<>();
        expectedIds.add(42);
        expectedIds.add(1337);
        Set<Integer> actualIds = new HashSet<>();
        for (Object object : pair) {
            int id = (int) (Integer) object;
            actualIds.add(id);
        }
        assertEquals(expectedIds, actualIds);
    }
    
    @After
    public void tearDown() {
        client.getDatabase(databaseName).drop();
    }
    
}