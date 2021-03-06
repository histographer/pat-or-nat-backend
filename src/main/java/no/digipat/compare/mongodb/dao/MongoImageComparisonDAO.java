package no.digipat.compare.mongodb.dao;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;

import no.digipat.compare.models.image.Image;
import no.digipat.compare.models.image.ImageChoice;
import no.digipat.compare.models.image.ImageComparison;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bson.Document;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.or;

/**
 * A Data Access Object (DAO) for image comparisons.
 * 
 * @author Kent Are Torvik
 * @author Jon Wallem Anundsen
 *
 */
public class MongoImageComparisonDAO {
    private MongoCollection<Document> collection;
    private String databaseName;
    private MongoClient client;

    /**
     * Creates a DAO.
     * 
     * @param client the client used to connect to the database
     * @param databaseName the name of the database
     */
    public MongoImageComparisonDAO(MongoClient client, String databaseName) {
        this.collection = client.getDatabase(databaseName).getCollection("ImageComparison");
        this.databaseName = databaseName;
        this.client = client;
    }

    /**
     * Inserts a new image comparison into the database.
     * 
     * @param imageComparison
     */
    public void createImageComparison(ImageComparison imageComparison) {
        Document document = imageComparisonToDBDocument(imageComparison);
        this.collection.insertOne(document);
    }
    
    /**
     * Retrieves every image comparison from a given project.
     * 
     * @param projectId the ID of the project
     * 
     * @return a list of every image comparison in the project
     */
    public List<ImageComparison> getAllImageComparisons(long projectId) {
        final List<ImageComparison> comparisons = new ArrayList<>();
        for (Document document : this.collection.find(Filters.eq("projectId", projectId))) {
            comparisons.add(dbDocumentToImageComparison(document));
        }
        return comparisons;
    }
    
    /**
     * Retrieves the number of times each image in a project has been compared.
     * 
     * @param projectId the ID of the project
     * 
     * @return a list of map entries, in which every entry's key is an image ID
     * and the entry's value is the number of times that image has been compared
     */
    public List<Map.Entry<Long, Long>> getNumberOfComparisonsForEachImage(long projectId) {
        final List<Map.Entry<Long, Long>> numbers = new ArrayList<>();
        MongoImageDAO imageDao = new MongoImageDAO(client, databaseName);
        for (Image image : imageDao.getAllImages(projectId)) {
            Long id = image.getImageId();
            long count = collection.countDocuments(or(eq("winner.id", id), eq("loser.id", id)));
            numbers.add(new Map.Entry<Long, Long>() {
                @Override
                public Long getKey() {
                    return id;
                }
                @Override
                public Long getValue() {
                    return count;
                }
                @Override
                public Long setValue(Long value) {
                    throw new UnsupportedOperationException();
                }
            });
        }
        return numbers;
    }
    
    private static Document imageChoiceToDBDocument(ImageChoice imageChoice) {
        return new Document()
                .append("id", imageChoice.getImageId())
                .append("comment", imageChoice.getComment());
    }
    
    private static Document imageComparisonToDBDocument(ImageComparison imageComparison) {
        return new Document().
                append("winner", imageChoiceToDBDocument(imageComparison.getWinner()))
                .append("loser", imageChoiceToDBDocument(imageComparison.getLoser()))
                .append("sessionId", imageComparison.getSessionId())
                .append("projectId", imageComparison.getProjectId());
    }
    
    private static ImageComparison dbDocumentToImageComparison(Document document) {
        try {
            String sessionId = document.getString("sessionId");
            long projectId = document.getLong("projectId");
            Document winnerDoc = (Document) document.get("winner");
            long winnerId = (Long) winnerDoc.get("id");
            String winnerComment = winnerDoc.getString("comment");
            ImageChoice winner = new ImageChoice(winnerId, winnerComment);
            Document loserDoc = (Document) document.get("loser");
            long loserId = (Long) loserDoc.get("id");
            String loserComment = loserDoc.getString("comment");
            ImageChoice loser = new ImageChoice(loserId, loserComment);
            ImageComparison imageComparison = new ImageComparison().setSessionId(sessionId)
                    .setWinner(winner).setLoser(loser).setProjectId(projectId);
            return imageComparison;
        } catch (NullPointerException | ClassCastException e) {
            throw new IllegalArgumentException("Invalid document", e);
        }
    }
    
}
