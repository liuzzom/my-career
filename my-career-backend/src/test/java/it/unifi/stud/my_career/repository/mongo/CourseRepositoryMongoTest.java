package it.unifi.stud.my_career.repository.mongo;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.InetSocketAddress;

import org.junit.Test;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;

import de.bwaldvogel.mongo.MongoServer;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.bson.Document;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import com.mongodb.ServerAddress;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

import de.bwaldvogel.mongo.backend.memory.MemoryBackend;
import it.unifi.stud.my_career.model.Course;
import it.unifi.stud.my_career.model.Student;

public class CourseRepositoryMongoTest {

	private static MongoServer server;
	private static InetSocketAddress serverAddress;
	private MongoClient client;
	private CourseRepositoryMongo courseRepository;
	private MongoCollection<Document> courseCollection;

	private static final String CAREER_DB_NAME = "career";
	private static final String COURSES_COLLECTION_NAME = "courses";

	private static final String STUDENT_NAME_1 = "Pippo";
	private static final String STUDENT_ID_1 = "123";
	private static final String STUDENT_NAME_2 = "Peppo";
	private static final String STUDENT_ID_2 = "1234";

	private static final String COURSE_NAME_1 = "LabBello";
	private static final String COURSE_ID_1 = "777";
	private static final int COURSE_CFU_1 = 9;
	private static final String COURSE_NAME_2 = "LabBrutto";
	private static final String COURSE_ID_2 = "888";
	private static final int COURSE_CFU_2 = 6;

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
		courseRepository = new CourseRepositoryMongo(client, CAREER_DB_NAME, COURSES_COLLECTION_NAME);
		MongoDatabase database = client.getDatabase(CAREER_DB_NAME);
		// make sure we always start with a clean database
		database.drop();
		courseCollection = database.getCollection(COURSES_COLLECTION_NAME);
	}

	@After
	public void tearDown() {
		client.close();
	}

	private void addTestCourseToRepository(String courseId, String courseName, int cfu) {
		courseCollection.insertOne(new Document().append("id", courseId).append("name", courseName).append("cfu", cfu));
	}

	private void addTestCourseToRepositoryWithParticipants(String courseId, String courseName, int cfu,
			List<String> participants) {
		courseCollection.insertOne(new Document().append("id", courseId).append("name", courseName).append("cfu", cfu)
				.append("participants", participants));
	}

	private List<Course> retrieveAllCourses() {
		return StreamSupport.stream(courseCollection.find().spliterator(), false)
				.map(d -> new Course("" + d.get("id"), "" + d.get("name"), (int) d.get("cfu")))
				.collect(Collectors.toList());
	}

	private List<String> getCourseParticipants(String courseId) {
		Document d = courseCollection.find(Filters.eq("id", courseId)).first();
		if (d != null)
			return d.getList("participants", String.class);
		return null;
	}

	@Test
	public void testFindAll() {
		addTestCourseToRepository(COURSE_ID_1, COURSE_NAME_1, COURSE_CFU_1);
		assertThat(courseRepository.findById(COURSE_ID_1))
				.isEqualTo(new Course(COURSE_ID_1, COURSE_NAME_1, COURSE_CFU_1));
	}

	// findAll

	@Test
	public void testFindAllWhenDBFull() {
		addTestCourseToRepository(COURSE_ID_1, COURSE_NAME_1, COURSE_CFU_1);
		addTestCourseToRepository(COURSE_ID_2, COURSE_NAME_2, COURSE_CFU_2);
		assertThat(courseRepository.findAll()).containsExactly(new Course(COURSE_ID_1, COURSE_NAME_1, COURSE_CFU_1),
				new Course(COURSE_ID_2, COURSE_NAME_2, COURSE_CFU_2));
	}

	@Test
	public void testFindAllWhenDBEmpty() {
		assertThat(courseRepository.findAll()).isEmpty();
	}

	// FindById

	@Test
	public void testFindByIdWhenInDBIsPresent() {
		addTestCourseToRepository(COURSE_ID_1, COURSE_NAME_1, COURSE_CFU_1);
		assertThat(courseRepository.findById(COURSE_ID_1))
				.isEqualTo(new Course(COURSE_ID_1, COURSE_NAME_1, COURSE_CFU_1));
	}

	@Test
	public void testFindByIdWhenInDBIsNotPresent() {
		assertThat(courseRepository.findById(COURSE_ID_1)).isNull();
	}

	// save

	@Test
	public void testSave() {
		courseRepository.save(new Course(COURSE_ID_1, COURSE_NAME_1, COURSE_CFU_1));
		assertThat(retrieveAllCourses()).containsExactly(new Course(COURSE_ID_1, COURSE_NAME_1, COURSE_CFU_1));
	}

	// delete

	@Test
	public void testDelete() {
		addTestCourseToRepository(COURSE_ID_1, COURSE_NAME_1, COURSE_CFU_1);
		courseRepository.delete(COURSE_ID_1);
		assertThat(retrieveAllCourses()).isEmpty();
	}

	// testGetParticipantsStudentsIdByCourseId

	@Test
	public void testGetParticipantsStudentsIdByCourseId() {
		List<String> participants = new ArrayList<String>();
		participants.add(STUDENT_ID_1);
		participants.add(STUDENT_ID_2);
		addTestCourseToRepositoryWithParticipants(COURSE_ID_1, COURSE_NAME_1, COURSE_CFU_1, participants);
		assertThat(courseRepository.getParticipantsStudentsIdByCourseId(COURSE_ID_1)).containsExactly(STUDENT_ID_1,
				STUDENT_ID_2);
	}

	@Test
	public void testNotGettingParticipantsStudentsIdByCourseId() {
		assertThat(courseRepository.getParticipantsStudentsIdByCourseId(COURSE_ID_1)).isEmpty();
	}

	// DeleteCourseParticipant

	@Test
	public void testDeleteCourseParticipant() {
		List<String> participants = new ArrayList<String>();
		participants.add(STUDENT_ID_1);
		addTestCourseToRepositoryWithParticipants(COURSE_ID_1, COURSE_NAME_1, COURSE_CFU_1, participants);
		courseRepository.deleteCourseParticipant(COURSE_ID_1, new Student(STUDENT_ID_1, STUDENT_NAME_1));
		assertThat(getCourseParticipants(COURSE_ID_1)).isEmpty();
	}

	// AddCourseParticipant

	@Test
	public void testAddCourseParticipant() {
		List<String> participants = new ArrayList<String>();
		participants.add(STUDENT_ID_1);
		addTestCourseToRepositoryWithParticipants(COURSE_ID_1, COURSE_NAME_1, COURSE_CFU_1, participants);
		courseRepository.addCourseParticipant(COURSE_ID_1, new Student(STUDENT_ID_2, STUDENT_NAME_2));
		assertThat(getCourseParticipants(COURSE_ID_1)).containsExactly(STUDENT_ID_1, STUDENT_ID_2);
	}

}
