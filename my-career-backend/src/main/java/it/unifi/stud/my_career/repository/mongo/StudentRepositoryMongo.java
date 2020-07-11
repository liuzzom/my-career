package it.unifi.stud.my_career.repository.mongo;

import com.mongodb.MongoClient;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.bson.Document;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;

import it.unifi.stud.my_career.model.Course;
import it.unifi.stud.my_career.model.Student;
import it.unifi.stud.my_career.repository.StudentRepository;

public class StudentRepositoryMongo implements StudentRepository {

	private static final String ID = "id";
	private static final String NAME = "name";

	private MongoCollection<Document> collectionStudents;

	private Student fromDocumentToStudent(Document d) {
		return new Student("" + d.get(ID), "" + d.get(NAME));
	}

	public StudentRepositoryMongo(MongoClient client, String databaseName, String studentsCollection) {
		collectionStudents = client.getDatabase(databaseName).getCollection(studentsCollection);
	}

	@Override
	public List<Student> findAll() {
		return StreamSupport.stream(collectionStudents.find().spliterator(), false).map(this::fromDocumentToStudent)
				.collect(Collectors.toList());
	}

	@Override
	public Student findById(String id) {
		Document d = collectionStudents.find(Filters.eq(ID, id)).first();
		if (d != null)
			return fromDocumentToStudent(d);
		return null;
	}

	@Override
	public void save(Student student) {
		collectionStudents.insertOne(new Document().append(ID, student.getId()).append(NAME, student.getName()));
	}

	@Override
	public void delete(String id) {
		collectionStudents.deleteOne(Filters.eq(ID, id));
	}

	@Override
	public List<String> getParticipatedCoursesIdByStudentId(String id) {
		Document d = collectionStudents.find(Filters.and(Filters.eq(ID, id))).first();
		if (d != null)
			return d.getList("participations", String.class);
		return null;
	}

	@Override
	public void deleteStudentParticipation(String studentId, Course course) {
	    collectionStudents.updateOne(Filters.eq(ID, studentId), Updates.pull("participations", course.getId()));
	}

	@Override
	public void addStudentParticipation(String studentId, Course course) {
	    collectionStudents.updateOne(Filters.eq(ID, studentId), Updates.push("participations", course.getId()));
	}

}
