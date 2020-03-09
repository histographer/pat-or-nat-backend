package no.digipat.patornat.mongodb.dao;

import static org.junit.Assert.*;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import com.mongodb.MongoClient;

import no.digipat.patornat.mongodb.DatabaseUnitTests;
import no.digipat.patornat.models.image.ImageComparison;
import no.digipat.patornat.models.image.ImageChoice;

public class MongoImageComparisonDAOTest {
    
    private static MongoClient client;
    private static String databaseName;
    
    @BeforeClass
    public static void setUpClass() {
        client = DatabaseUnitTests.getMongoClient();
        databaseName = DatabaseUnitTests.getDatabaseName();
    }
    
    @Test
    public void testGetAllImageComparisons() {
        MongoImageComparisonDAO dao = new MongoImageComparisonDAO(client, databaseName);
        // Test with no data in database
        assertEquals(0, dao.getAllImageComparisons().size());
        // Test with some data
        ImageComparison comparison1 = new ImageComparison("user1", new ImageChoice(1, "comment1"), new ImageChoice(2, "comment2"));
        ImageComparison comparison2 = new ImageComparison("user2", new ImageChoice(3, "comment3"),  new ImageChoice(1, "comment4"));
        dao.createImageComparison(comparison1);
        dao.createImageComparison(comparison2);
        List<ImageComparison> allComparisons = dao.getAllImageComparisons();
        assertEquals(2, allComparisons.size());
        Collections.sort(allComparisons, new Comparator<ImageComparison>() {
            // Sort by ID of chosen image so we can more easily test the contents of the list 
            @Override
            public int compare(ImageComparison arg0, ImageComparison arg1) {
                return (int) (arg0.getChosen().getId() - arg1.getChosen().getId());
            }
        });
        ImageComparison retrievedComparison1 = allComparisons.get(0);
        ImageComparison retrievedComparison2 = allComparisons.get(1);
        assertEquals(comparison1.getUser(), retrievedComparison1.getUser());
        assertEquals(comparison1.getChosen().getId(), retrievedComparison1.getChosen().getId());
        assertEquals(comparison1.getChosen().getComment(), retrievedComparison1.getChosen().getComment());
        assertEquals(comparison1.getOther().getId(), retrievedComparison1.getOther().getId());
        assertEquals(comparison1.getOther().getComment(), retrievedComparison1.getOther().getComment());
        
        assertEquals(comparison2.getUser(), retrievedComparison2.getUser());
        assertEquals(comparison2.getChosen().getId(), retrievedComparison2.getChosen().getId());
        assertEquals(comparison2.getChosen().getComment(), retrievedComparison2.getChosen().getComment());
        assertEquals(comparison2.getOther().getId(), retrievedComparison2.getOther().getId());
        assertEquals(comparison2.getOther().getComment(), retrievedComparison2.getOther().getComment());
    }
    
    @After
    public void tearDown() {
        client.getDatabase(databaseName).drop();
    }
    
}
