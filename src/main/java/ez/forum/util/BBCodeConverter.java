package ez.forum.util;

/**
 * Use it to convert BBCode to HTML.
 * It's a bit expensive to create this class, so share it between your stuff.
 */
public class BBCodeConverter {
	private static String[][] complexTags = new String[4][6];
	private static String[][] simpleTagsAndHtmlEntities = new String[14][2];
	private static String[][] urlEntities = new String[5][2];
	
	static {
		complexTags[0] = new String[] {
			"[URL=\"http://", "\"]", "[/URL]",
			"<a href=\"http://", "\">", "</a>"};
		complexTags[1] = new String[] {
			"[URL=\"https://", "\"]", "[/URL]",
			"<a href=\"https://", "\">", "</a>"};
		complexTags[2] = new String[] {
			"[IMG]", "[/IMG]", "",
			"<label><input type=\"checkbox\" class=\"post-img-checkbox\" /><div class=\"post-img-container\"><div class=\"post-img-label\">Press anywhere to close the image</div><img class=\"post-img\" src=\"", "\" /></div></label>", ""};
		complexTags[3] = new String[] {
			"[COLOR=\"", "\"]", "[/COLOR]",
			"<span style=\"color: ", ";\">", "</span>"};
		
		simpleTagsAndHtmlEntities[0] = new String[] {"[B]", "<b>"};
		simpleTagsAndHtmlEntities[1] = new String[] {"[/B]", "</b>"};
		simpleTagsAndHtmlEntities[2] = new String[] {"[I]", "<i>"};
		simpleTagsAndHtmlEntities[3] = new String[] {"[/I]", "</i>"};
		simpleTagsAndHtmlEntities[4] = new String[] {"[U]", "<u>"};
		simpleTagsAndHtmlEntities[5] = new String[] {"[/U]", "</u>"};
		simpleTagsAndHtmlEntities[6] = new String[] {"[S]", "<s>"};
		simpleTagsAndHtmlEntities[7] = new String[] {"[/S]", "</s>"};
		simpleTagsAndHtmlEntities[8] = new String[] {"[CODE]", "<div class=\"post-code\">"};
		simpleTagsAndHtmlEntities[9] = new String[] {"[/CODE]", "</div>"};
		simpleTagsAndHtmlEntities[10] = new String[]{"\"", "&quot;"};
		simpleTagsAndHtmlEntities[11] = new String[]{"'", "&apos;"};
		simpleTagsAndHtmlEntities[12] = new String[]{"<", "&lt;"};
		simpleTagsAndHtmlEntities[13] = new String[]{">", "&gt;"};
		
		urlEntities[0] = new String[]{"\"", "%22"};
		urlEntities[1] = new String[]{"'", "%27"};
		urlEntities[2] = new String[]{";", "%3B"};
		urlEntities[3] = new String[]{"<", "%3C"};
		urlEntities[4] = new String[]{">", "%3E"};
	}
	
	/**
	 * 
	 * Converts BBCode in a string to HTML tags. Output the result WITHOUT escaping!
	 * @param
	 * source - the source string containing HTML
	 * @return
	 * Escaped string containing HTML
	 */
	public static String toHtml(String source) {
		StringBuffer html = new StringBuffer(source);
		
		int indexOfCurrentTag = -1;
		Boolean part1Found = false;
		Boolean part2Found = false;
		
		for (int i = 0; i < html.length(); i++) {
			// Look for complex tag if none was found
			if (indexOfCurrentTag == -1) {
				for (int j = 0; j < complexTags.length; j++) {
					String[] tag = complexTags[j];
					String toReplace = tag[0];
					int l = i + toReplace.length();
					if (l < html.length() && html.subSequence(i, l).equals(toReplace)) {
						String replacement = tag[3];
						html = html.replace(i, l, replacement);
						i += replacement.length() - 1;
						part1Found = true;
						indexOfCurrentTag = j;
						break;
					}
				}
				// If tag has been found, restart loop
				if (part1Found) { continue; }
			}
			// Look for part 2 of complex tag
			else if(part1Found && !part2Found) {
				String[] tag = complexTags[indexOfCurrentTag];
				String toReplace = tag[1];
				int l = i + toReplace.length();
				
				// If part 2 found, replace it
				if (l - 1 < html.length() && html.subSequence(i, l).equals(toReplace)) {
					String replacement = tag[4];
					html = html.replace(i, l, replacement);
					i += replacement.length() - 1;
					// If current tag has only 2 parts, stop lookup
					if (tag[2].equals("")) {
						part1Found = false;
						indexOfCurrentTag = -1;
					}
					// Proceed lookup otherwise
					else {
						part2Found = true;
					}
					continue;
				}
			}
			// Look for part 3
			else if (part1Found && part2Found) {
				String[] tag = complexTags[indexOfCurrentTag];
				String toReplace = tag[2];
				int l = i + toReplace.length();
				
				// If part 3 found, replace it
				if (l - 1 < html.length() && html.subSequence(i, l).equals(toReplace)) {
					String replacement = tag[5];
					html = html.replace(i, l, replacement);
					i += replacement.length() - 1;
					part1Found = part2Found = false;
					indexOfCurrentTag = -1;
					continue;
				}
			}
			
			// Replace URL entities if parsing URL part of complex tag
			if (part1Found && !part2Found) {
				Boolean doContinue = false;
				for (String[] entity: urlEntities) {
					int l = i + 1;
					if (l < html.length() && entity[0].charAt(0) == html.charAt(i)) {
						html.replace(i, l, entity[1]);
						i += 2;
						doContinue = true;
						break;
					}
				}
				if (doContinue) { continue; }
			}
			// Replace simple tags otherwise
			for (String[] tag: simpleTagsAndHtmlEntities) {
				String toReplace = tag[0];
				int l = i + toReplace.length();
				if (l - 1 < html.length() && html.subSequence(i, l).equals(toReplace)) {
					String replacement = tag[1];
					html.replace(i, l, replacement);
					i += replacement.length() - 1;
					break;
				}
			}
			
		}
		
		// Close open complex tags
		if (part1Found) {
			String[] tag = complexTags[indexOfCurrentTag];
			if (!part2Found) {
				html.append(tag[4]);
			}
			html.append(tag[5]);
		}
		
		return html.toString();
	}
}
