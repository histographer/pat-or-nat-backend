package no.digipat.patornat.mongodb.dao;

import no.digipat.patornat.mongodb.models.image.BestImageChoice;
import no.digipat.patornat.mongodb.models.user.IUser;
import no.digipat.patornat.mongodb.models.image.ImageChoice;
import no.digipat.patornat.mongodb.models.user.User;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.simple.JSONObject;

public class Converter {
    /**
     *  Creates a database document that can be inserted directly to the db
     * @return
     */
    public static Document userToDBDocument(IUser user) {
        return new Document("username", user.getUsername());
    }

    /**
     * Convert from database document to java object
     * @param object
     * @return
     */
    public static IUser userDocumentToJavaObject(Document object) {
        String username = (String) object.get("username");
        ObjectId id = (ObjectId) object.get("_id");
        User user = new User(id.toString(), username);
        return user;
    }


    /** Takes in json object and returns image. If the optionals are not provided, empty string or 0
     * {
     *     "id": 1,
     *     "comment": "testcomment",
     *  }
     * @param json in the form of Image
     * @return Image
     */
    public static ImageChoice jsonToImage(JSONObject json) {
        try {
            int id = ((Long) json.get("id")).intValue();
            String comment = (String) json.getOrDefault("comment", "");
            return new ImageChoice(id, comment);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("JSON is not valid, id is missing");
        }
    }


    /**
     * Takes bestImage object and creates mongodb document
     * @param bestImageChoice
     * @return
     */
    public static Document bestImageToDBDocument(BestImageChoice bestImageChoice) {
        return new Document().
                append("chosen", imageToDBDocument(bestImageChoice.getChosen()))
                .append("other", imageToDBDocument(bestImageChoice.getOther()))
                .append("user", bestImageChoice.getUser());
    }
    
    /**
     * Converts a MongoDB document to an instance of {@code BestImageChoice}.
     * 
     * @param document the MongoDB document
     * @return an instance of {@code BestImageChoice} representing the given document
     * @throws IllegalArgumentException if {@code document} is not valid. Specifically,
     * if it is {@code null}, its {@code chosen} or {@code other} attribute is not a
     * non-{@code null} document, or if any attribute cannot be cast to the appropriate type.
     */
    public static BestImageChoice dbDocumentToBestImageChoice(Document document) {
        // TODO
        
        return null;
    }
    
    /**
     * Takes image object and creates mongodb document
     * @param imageChoice
     * @return
     */
    public static Document imageToDBDocument(ImageChoice imageChoice) {
       return new Document()
               .append("id", imageChoice.getId())
               .append("comment", imageChoice.getComment());
    }

    /**
     * Takes in
     * {
     *   "user": "string"
     *   "chosen": {
     *     "id": 1,
     *     "comment": "testcomment",
     *   },
     *   "other": {
     *     "id": 2,
     *     "comment": "testcomment2",
     *   }
     * }
     * @param json
     * @return
     */
    public static BestImageChoice jsonToBestImageChoice(JSONObject json) {
        ImageChoice chosen = Converter.jsonToImage((JSONObject) json.get("chosen"));
        ImageChoice other = Converter.jsonToImage((JSONObject) json.get("other"));
        String user = (String) json.get("user");
        return new BestImageChoice(user, chosen, other);
    }
}
