package it.unifi.stud.my_career.repository.mongo;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.bson.Document;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

import de.bwaldvogel.mongo.MongoServer;
import de.bwaldvogel.mongo.backend.memory.MemoryBackend;
import it.unifi.stud.my_career.model.Course;
import it.unifi.stud.my_career.model.Student;

public class StudentRepositoryMongoTest {

	private static MongoServer server;
	private static InetSocketAddress serverAddress;
	private MongoClient client;
	private StudentRepositoryMongo studentRepository;
	private MongoCollection<Document> studentCollection;

	private static final String CAREER_DB_NAME = "career";
	private static final String STUDENTS_COLLECTION_NAME = "students";

	private static final String STUDENT_NAME_1 = "Pippo";
	private static final String STUDENT_ID_1 = "123";
	private static final String STUDENT_NAME_2 = "Peppo";
	private static final String STUDENT_ID_2 = "1234";
	private static final String COURSE_ID_1 = "223";
	private static final String COURSE_NAME_1 = "LABbello";
	private static final int COURSE_CFU_1 = 6;
	private static final String COURSE_ID_2 = "323";
	private static final String COURSE_NAME_2 = "LABbellissimo";
	private static final int COURSE_CFU_2 = 9;

	@BeforeClass
	public static void setupServer() {
		server = new MongoServer(new MemoryBackend());
		// bind on a random local port
		serverAddress = server.bind();
	}

	@AfterClass
	public static void shutdownServer() {
		server.shutdown();
	}

	@Before
	public void setup() {
		client = new MongoClient(new ServerAddress(serverAddress));
		studentRepository = new StudentRepositoryMongo(client, CAREER_DB_NAME, STUDENTS_COLLECTION_NAME);
		MongoDatabase database = client.getDatabase(CAREER_DB_NAME);
		// make sure we always start with a clean database
		database.drop();
		studentCollection = database.getCollection(STUDENTS_COLLECTION_NAME);
	}

	@After
	public void tearDown() {
		client.close();
	}

	private void addTestStudentToRepository(String studentId, String studentName) {
		studentCollection.insertOne(new Document().append("name", studentName).append("id", studentId));
	}

	private void addTestStudentToRepositoryWithParticipations(String studentId, String studentName,
			List<String> participations) {
		studentCollection.insertOne(new Document().append("name", studentName).append("id", studentId)
				.append("participations", participations));
	}

	private List<Student> retrieveAllStudents() {
		return StreamSupport.stream(studentCollection.find().spliterator(), false)
				.map(d -> new Student("" + d.get("id"), "" + d.get("name"))).collect(Collectors.toList());
	}

	private List<String> getStudentParticipations(String studentId) {
		Document d = studentCollection.find(Filters.eq("id", studentId)).first();
		if (d != null)
			return d.getList("participations", String.class);
		return null;
	}

	// findAll

	@Test
	public void testFindAllWhenDBFull() {
		addTestStudentToRepository(STUDENT_ID_1, STUDENT_NAME_1);
		addTestStudentToRepository(STUDENT_ID_2, STUDENT_NAME_2);
		assertThat(studentRepository.findAll()).containsExactly(new Student(STUDENT_ID_1, STUDENT_NAME_1),
				new Student(STUDENT_ID_2, STUDENT_NAME_2));
	}

	@Test
	public void testFindAllWhenDBEmpty() {
		assertThat(studentRepository.findAll()).isEmpty();
	}

	// findById

	@Test
	public void testFindByIdWhenInDBIsPresent() {
		addTestStudentToRepository(STUDENT_ID_1, STUDENT_NAME_1);
		assertThat(studentRepository.findById(STUDENT_ID_1)).isEqualTo(new Student(STUDENT_ID_1, STUDENT_NAME_1));
	}

	@Test
	public void testFindByIdWhenInDBIsNotPresent() {
		assertThat(studentRepository.findById(STUDENT_ID_1)).isNull();
	}

	// save

	@Test
	public void testSave() {
		Student studentToSave = new Student(STUDENT_ID_1, STUDENT_NAME_1);
		studentRepository.save(studentToSave);
		assertThat(retrieveAllStudents()).containsExactly(studentToSave);
	}

	// delete

	@Test
	public void testDelete() {
		addTestStudentToRepository(STUDENT_ID_1, STUDENT_NAME_1);
		studentRepository.delete(STUDENT_ID_1);
		assertThat(retrieveAllStudents()).isEmpty();
	}

	// GetParticipatedCoursesIdByStudentId

	@Test
	public void testGetParticipatedCoursesIdByStudentId() {
		List<String> participations = new ArrayList<String>();
		participations.add(COURSE_ID_1);
		participations.add(COURSE_ID_2);
		addTestStudentToRepositoryWithParticipations(STUDENT_ID_1, STUDENT_NAME_1, participations);
		assertThat(studentRepository.getParticipatedCoursesIdByStudentId(STUDENT_ID_1)).containsExactly(COURSE_ID_1,
				COURSE_ID_2);
	}

	@Test
	public void testNotGettingParticipantsStudentsIdByCourseId() {
		assertThat(studentRepository.getParticipatedCoursesIdByStudentId(STUDENT_ID_1)).isNull();
	}

	// DeleteStudentParticipation

	@Test
	public void testDeleteStudentParticipation() {
		List<String> participations = new ArrayList<String>();
		participations.add(COURSE_ID_1);
		addTestStudentToRepositoryWithParticipations(STUDENT_ID_1, STUDENT_NAME_1, participations);
		studentRepository.deleteStudentParticipation(STUDENT_ID_1,
				new Course(COURSE_ID_1, COURSE_NAME_1, COURSE_CFU_1));
		assertThat(getStudentParticipations(STUDENT_ID_1)).isEmpty();
	}

	// AddStudentParticipation

	@Test
	public void testAddStudentParticipation() {
		List<String> participations = new ArrayList<String>();
		participations.add(COURSE_ID_1);
		addTestStudentToRepositoryWithParticipations(STUDENT_ID_1, STUDENT_NAME_1, participations);
		studentRepository.addStudentParticipation(STUDENT_ID_1, new Course(COURSE_ID_2, COURSE_NAME_2, COURSE_CFU_2));
		assertThat(getStudentParticipations(STUDENT_ID_1)).containsExactly(COURSE_ID_1, COURSE_ID_2);
	}

}
