package ez.forum.util;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.TypedQuery;

import ez.forum.entities.PostReviewRequest;
import ez.forum.entities.TopicReviewRequest;
import ez.forum.entities.User;

/**
 * Dispatcher of a (Topic|Post)ReviewRequest.
 * Can get and remove requests sent by a specific user.
 * Does all the validation and stuff.
 * @author matafokka
 *
 */
public class ReviewRequestDispatcher {
	
	/**
	 * Gets query stuff for given class
	 * @return
	 * String[] where:
	 * <ul>
	 * <li>Element 0 - Entity class for given cls</li>
	 * <li>Element 1 - (topic|post)Bean of given class</li>
	 * </ul>
	 * Or null if class is not (Topic|Post)ReviewRequest
	 */
	private static String[] getQueryStuff(Class<?> cls) {
		String[] s = new String[2];
		if (cls == TopicReviewRequest.class) {
			s[0] = "TopicReviewRequest";
			s[1] = "topicBean";
		} else if (cls == PostReviewRequest.class) {
			s[0] = "PostReviewRequest";
			s[1] = "postBean";
		} else {
			return null;
		}
		return s;
	}
	
	/**
	 * Finds (Topic|Post)ReviewRequest with (topic|post)Bean.id = requestId sent by User u.
	 * @param <T> - exact class of a review request
	 * @param requestId - id of a Topic or Post
	 * @param u - instance of user
	 * @param cls - exact class of a review request
	 * @return
	 * (Topic|Post)ReviewRequest or null if none has been found or cls is not a (Topic|Post)ReviewRequest.
	 */
	public static <T> T getReviewRequestByEntityId(Long entityId, User u, Class<T> cls) {
		String[] stuff = getQueryStuff(cls);
		if (u == null || entityId == null || stuff == null) { return null; }

		EntityManager em = SHARED_OBJECTS.emfactory.createEntityManager();
		TypedQuery<T> query = em.createQuery(
				"SELECT e FROM " + stuff[0] + " e WHERE "
				+ "e.user.id = ?1 AND e." + stuff[1] + ".id = ?2",
				cls
				);
		query.setParameter(1, u.getId());
		query.setParameter(2, entityId);
		
		try {
			T toReturn = query.getResultList().get(0);
			em.close();
			return toReturn;
		}
		catch (IndexOutOfBoundsException e) {}
		em.close();
		return null;
	}
	
	/**
	 * Finds (Topic|Post)ReviewRequest with id = requestId sent by User u.
	 * @param <T>
	 * @param requestId - id of a review request
	 * @param u - instance of user
	 * @param cls - exact class of a review request
	 * @return
	 * (Topic|Post)ReviewRequest or null if none has been found or cls is not a (Topic|Post)ReviewRequest.
	 */
	public static <T> T getReviewRequest(Long requestId, User u, Class<T> cls) {
		String[] stuff = getQueryStuff(cls);
		if (u == null || requestId == null || stuff == null) { return null; }

		EntityManager em = SHARED_OBJECTS.emfactory.createEntityManager();
		TypedQuery<T> query = em.createQuery(
				"SELECT e FROM " + stuff[0] + " e WHERE "
				+ "e.id = ?1 AND e.user.id = ?2",
				cls
				);
		query.setParameter(1, requestId);
		query.setParameter(2, u.getId());
		
		try {
			T toReturn = query.getResultList().get(0);
			em.close();
			return toReturn;
		}
		catch (IndexOutOfBoundsException e) {}
		em.close();
		return null;
	}
	
	/**
	 * Validates and removes (Topic|Post)ReviewRequest with id=requestId sent by a User u.
	 * @param <T> - exact class of a review request
	 * @param requestId - id of a review request
	 * @param u - instance of user
	 * @param cls - exact class of a review request
	 * @param request
	 */
	public static <T> void removeReviewRequest(Long requestId, User u, Class<T> cls) {
		T request = getReviewRequest(requestId, u, cls);
		if (request == null) { return; }

		EntityManager em = SHARED_OBJECTS.emfactory.createEntityManager();
		EntityTransaction trans = em.getTransaction();
		trans.begin();
		em.remove(em.find(cls, requestId));
		trans.commit();
		em.close();
	}
}
