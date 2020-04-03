package ez.forum.util;

import java.util.regex.Pattern;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class SHARED_OBJECTS {
	public static EntityManagerFactory emfactory = Persistence.createEntityManagerFactory("ezforum");
	public static final BBCodeConverter bbConverter = new BBCodeConverter();
	public static final Pattern emailPattern = Pattern.compile(".+@.+\\..+");
}
