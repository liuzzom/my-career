package it.unifi.stud.my_career.repository.mongo;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.bson.Document;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.GenericContainer;

import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

import it.unifi.stud.my_career.model.Student;
import it.unifi.stud.my_career.model.Course;
import it.unifi.stud.my_career.repository.mongo.StudentRepositoryMongo;

public class StudentRepositoryMongoIT {

	@SuppressWarnings("rawtypes")
	@ClassRule
	public static final GenericContainer mongo = new GenericContainer("krnbr/mongo:4.2.6").withExposedPorts(27017);

	private static final String CAREER_DB_NAME = "career";
	private static final String STUDENTS_COLLECTION_NAME = "students";

	private static final String ID = "id";
	private static final String NAME = "name";

	private static final String STUDENT_ID_1 = "123";
	private static final String STUDENT_NAME_1 = "Pippo";
	private static final String STUDENT_ID_2 = "1234";
	private static final String STUDENT_NAME_2 = "Peppo";
	
	private static final String COURSE_ID_1 = "223";
	private static final String COURSE_NAME_1 = "LABbello";
	private static final int COURSE_CFU_1 = 6;
	private static final String COURSE_ID_2 = "323";
	private static final String COURSE_NAME_2 = "LABbellissimo";
	private static final int COURSE_CFU_2 = 9;

	private MongoClient client;
	private StudentRepositoryMongo studentRepository;
	private MongoCollection<Document> studentCollection;

	@Before
	public void setup() {
		client = new MongoClient(new ServerAddress(mongo.getContainerIpAddress(), mongo.getMappedPort(27017)));
		studentRepository = new StudentRepositoryMongo(client, CAREER_DB_NAME, STUDENTS_COLLECTION_NAME);
		MongoDatabase database = client.getDatabase(CAREER_DB_NAME);
		database.drop(); // starting with a clean database
		studentCollection = database.getCollection(STUDENTS_COLLECTION_NAME);
	}

	@After
	public void tearDown() {
		client.close();
	}

	private void addTestStudentToRepository(String studentId, String studentName) {
		studentCollection.insertOne(new Document().append(NAME, studentName).append(ID, studentId));
	}

	private void addTestStudentToRepositoryWithParticipations(String studentId, String studentName,
			List<String> participations) {
		studentCollection.insertOne(new Document().append(NAME, studentName).append(ID, studentId)
				.append("participations", participations));
	}

	private List<Student> retrieveAllStudents() {
		return StreamSupport.stream(studentCollection.find().spliterator(), false)
				.map(d -> new Student("" + d.get(ID), "" + d.get(NAME))).collect(Collectors.toList());
	}

	private List<String> getStudentParticipations(String studentId) {
		Document d = studentCollection.find(Filters.eq(ID, studentId)).first();
		if (d != null)
			return d.getList("participations", String.class);
		return null;
	}

	// findAll

	@Test
	public void testFindAllWhenDBFull() {
		//Setup
		addTestStudentToRepository(STUDENT_ID_1, STUDENT_NAME_1);
		addTestStudentToRepository(STUDENT_ID_2, STUDENT_NAME_2);
		//Exercise & Verify
		assertThat(studentRepository.findAll()).containsExactly(new Student(STUDENT_ID_1, STUDENT_NAME_1),
				new Student(STUDENT_ID_2, STUDENT_NAME_2));
	}

	@Test
	public void testFindAllWhenDBEmpty() {
		//Exercise & Verify
		assertThat(studentRepository.findAll()).isEmpty();
	}

	// findById

	@Test
	public void testFindByIdWhenInDBIsPresent() {
		//Setup
		addTestStudentToRepository(STUDENT_ID_1, STUDENT_NAME_1);
		addTestStudentToRepository(STUDENT_ID_2, STUDENT_NAME_2);
		//Exercise & Verify
		assertThat(studentRepository.findById(STUDENT_ID_1)).isEqualTo(new Student(STUDENT_ID_1, STUDENT_NAME_1));
	}

	@Test
	public void testFindByIdWhenInDBIsNotPresent() {
		//Exercise & Verify
		assertThat(studentRepository.findById(STUDENT_ID_1)).isNull();
	}

	// save

	@Test
	public void testSave() {
		//Setup
		Student studentToSave = new Student(STUDENT_ID_1, STUDENT_NAME_1);
		//Exercise
		studentRepository.save(studentToSave);
		//Verify
		assertThat(retrieveAllStudents()).containsExactly(studentToSave);
	}

	// delete

	@Test
	public void testDelete() {
		//Setup
		addTestStudentToRepository(STUDENT_ID_1, STUDENT_NAME_1);
		//Exercise
		studentRepository.delete(STUDENT_ID_1);
		//Verify
		assertThat(retrieveAllStudents()).isEmpty();
	}

	// GetParticipatedCoursesIdByStudentId

	@Test
	public void testGetParticipatedCoursesIdByStudentId() {
		//Setup
		List<String> participations = new ArrayList<String>();
		participations.add(COURSE_ID_1);
		participations.add(COURSE_ID_2);
		addTestStudentToRepositoryWithParticipations(STUDENT_ID_1, STUDENT_NAME_1, participations);
		//Exercise & Verify
		assertThat(studentRepository.getParticipatedCoursesIdByStudentId(STUDENT_ID_1)).containsExactly(COURSE_ID_1,
				COURSE_ID_2);
	}

	@Test
	public void testNotGettingParticipantsStudentsIdByCourseId() {
		//Exercise & Verify
		assertThat(studentRepository.getParticipatedCoursesIdByStudentId(STUDENT_ID_1)).isEmpty();
	}

	// DeleteStudentParticipation

	@Test
	public void testDeleteStudentParticipation() {
		//Setup
		List<String> participations = new ArrayList<String>();
		participations.add(COURSE_ID_1);
		addTestStudentToRepositoryWithParticipations(STUDENT_ID_1, STUDENT_NAME_1, participations);
		//Exercise
		studentRepository.deleteStudentParticipation(STUDENT_ID_1,
				new Course(COURSE_ID_1, COURSE_NAME_1, COURSE_CFU_1));
		//Verify
		assertThat(getStudentParticipations(STUDENT_ID_1)).isEmpty();
	}

	// AddStudentParticipation

	@Test
	public void testAddStudentParticipation() {
		//Setup
		List<String> participations = new ArrayList<String>();
		participations.add(COURSE_ID_1);
		addTestStudentToRepositoryWithParticipations(STUDENT_ID_1, STUDENT_NAME_1, participations);
		//Exercise
		studentRepository.addStudentParticipation(STUDENT_ID_1, new Course(COURSE_ID_2, COURSE_NAME_2, COURSE_CFU_2));
		//Verify
		assertThat(getStudentParticipations(STUDENT_ID_1)).containsExactly(COURSE_ID_1, COURSE_ID_2);
	}

}
