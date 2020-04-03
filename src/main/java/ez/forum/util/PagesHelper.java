package ez.forum.util;

import java.util.List;

/**
 * Contains data for paging
 * @author matafokka
 *
 */
public class PagesHelper {
	
	/**
	 * Contains number of items per one page
	 */
	public static final int itemsPerPage = 20;
	
	/**
	 * Calculates total count of pages for given number of items.
	 * <br>
	 * I.e. how much pages is needed for given number of items.
	 * 
	 * @param numberOfItems - number of items that should be splitted into pages
	 * @return count of needed pages
	 */
	public static int calculatePagesCount(int numberOfItems) {
		return (int) Math.ceil(
				((float) numberOfItems) / itemsPerPage
				);
	}
	
	/**
	 * Determines, on which page should be item at given position (index) in a collection.
	 * @param itemPos - index of item in a collection
	 * @return Page number containing this item
	 */
	public static int getPageOfItem(int itemPos) {
		int page = (int) Math.ceil(
				((float) itemPos) / itemsPerPage
				);
		if (page == 0) { page = 1; } // For 0 indexes
		return page;
	}
	
	/**
	 * Creates sublist of all item for given page.
	 * @param <T> - type of items in a list
	 * @param list - list of items
	 * @param page - number of page
	 * @return Sublist where all elements should be placed in given page
	 */
	public static <T> List<T> sublistForPage(List<T> list, int page) {
		int start = (page - 1) * itemsPerPage;
		if (start < 0) { start = 0; }
		int end = page * itemsPerPage;
		if (end > list.size()) { end = list.size(); }
		try { return list.subList(start, end); }
		catch (IllegalArgumentException | IndexOutOfBoundsException e) { return null; }
	}
}
