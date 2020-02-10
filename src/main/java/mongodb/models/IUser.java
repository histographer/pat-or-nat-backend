package mongodb.models;

import com.mongodb.DBObject;
import org.bson.Document;

public interface IUser{
    public Document toDBDocument();
    public IUser toJavaObject(Document object);
    public void setId(String id);
    public String getId();
    public void setUsername(String username);
    public String getUsername();
}
