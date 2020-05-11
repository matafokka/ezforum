package ez.forum.util;

import java.util.regex.Pattern;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import io.github.matafokka.bbcode_converter.BBCodeConverter;

public class SHARED_OBJECTS {
	public static EntityManagerFactory emfactory = Persistence.createEntityManagerFactory("ezforum");
	public static final Pattern emailPattern = Pattern.compile(".+@.+\\..+");
	public static BBCodeConverter conv = new BBCodeConverter();
	
	static {
		conv.addComplexTag(
				"[IMG]",
				"<label><input type=\"checkbox\" class=\"post-img-checkbox\" /><div class=\"post-img-container\"><div class=\"post-img-label\">Press anywhere to close the image</div><img class=\"post-img\" src=\"",
				"[/IMG]",
				"\" /></div></label>",
				"", ""
				);
		conv.addSimpleTag("[CODE]", "<div class=\"post-code\">");
		conv.addSimpleTag("[/CODE]", "</div>");
	}
}
