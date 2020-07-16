package it.unifi.stud.my_career.repository.mongo;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.bson.Document;

import it.unifi.stud.my_career.model.Course;
import it.unifi.stud.my_career.model.Student;
import it.unifi.stud.my_career.repository.CourseRepository;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;

public class CourseRepositoryMongo implements CourseRepository {

	private static final String PARTICIPANTS = "participants";
	private static final String ID = "id";
	private static final String NAME = "name";
	private static final String CFU = "cfu";

	private MongoCollection<Document> collectionCourses;

	private Course fromDocumentToCourse(Document d) {
		return new Course("" + d.get(ID), "" + d.get(NAME), (int) d.get(CFU));
	}

	public CourseRepositoryMongo(MongoClient client, String databaseName, String coursesCollection) {
		collectionCourses = client.getDatabase(databaseName).getCollection(coursesCollection);
	}

	@Override
	public List<Course> findAll() {
		return StreamSupport.stream(collectionCourses.find().spliterator(), false).map(this::fromDocumentToCourse)
				.collect(Collectors.toList());
	}

	@Override
	public Course findById(String id) {
		Document d = collectionCourses.find(Filters.eq(ID, id)).first();
		if (d != null)
			return fromDocumentToCourse(d);
		return null;
	}

	@Override
	public void save(Course course) {
		collectionCourses.insertOne(
				new Document().append(ID, course.getId()).append(NAME, course.getName()).append(CFU, course.getCfu()));
	}

	@Override
	public void delete(String id) {
		collectionCourses.deleteOne(Filters.eq(ID, id));
	}

	@Override
	public List<String> getParticipantsStudentsIdByCourseId(String id) {
		Document d = collectionCourses.find(Filters.and(Filters.eq(ID, id))).first();
		if (d != null)
			return d.getList(PARTICIPANTS, String.class);
		return Collections.emptyList();
	}

	@Override
	public void deleteCourseParticipant(String courseId, Student student) {
		collectionCourses.updateOne(Filters.eq(ID, courseId), Updates.pull(PARTICIPANTS, student.getId()));
	}

	@Override
	public void addCourseParticipant(String courseId, Student student) {
		collectionCourses.updateOne(Filters.eq(ID, courseId), Updates.push(PARTICIPANTS, student.getId()));
	}

}
