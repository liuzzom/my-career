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

import it.unifi.stud.my_career.model.Course;
import it.unifi.stud.my_career.model.Student;

public class CourseRepositoryMongoIT {

	@SuppressWarnings("rawtypes")
	@ClassRule
	public static final GenericContainer mongo = new GenericContainer("krnbr/mongo:4.2.6").withExposedPorts(27017);

	private static final String CAREER_DB_NAME = "career";
	private static final String COURSES_COLLECTION_NAME = "courses";

	private static final String CFU = "cfu";
	private static final String NAME = "name";
	private static final String ID = "id";

	private static final String STUDENT_ID_1 = "123";
	private static final String STUDENT_NAME_1 = "Pippo";
	private static final String STUDENT_ID_2 = "1234";
	private static final String STUDENT_NAME_2 = "Peppo";

	private static final String COURSE_ID_1 = "777";
	private static final String COURSE_NAME_1 = "LabBello";
	private static final int COURSE_CFU_1 = 9;
	private static final String COURSE_ID_2 = "888";
	private static final String COURSE_NAME_2 = "LabBrutto";
	private static final int COURSE_CFU_2 = 6;

	private MongoClient client;
	private CourseRepositoryMongo courseRepository;
	private MongoCollection<Document> courseCollection;

	@Before
	public void setup() {
		client = new MongoClient(new ServerAddress(mongo.getContainerIpAddress(), mongo.getMappedPort(27017)));
		courseRepository = new CourseRepositoryMongo(client, CAREER_DB_NAME, COURSES_COLLECTION_NAME);
		MongoDatabase database = client.getDatabase(CAREER_DB_NAME);
		database.drop(); // starting with a clean database
		courseCollection = database.getCollection(COURSES_COLLECTION_NAME);
	}

	@After
	public void tearDown() {
		client.close();
	}

	private void addTestCourseToRepository(String courseId, String courseName, int cfu) {
		courseCollection.insertOne(new Document().append(ID, courseId).append(NAME, courseName).append(CFU, cfu));
	}

	private void addTestCourseToRepositoryWithParticipants(String courseId, String courseName, int cfu,
			List<String> participants) {
		courseCollection.insertOne(new Document().append(ID, courseId).append(NAME, courseName).append(CFU, cfu)
				.append("participants", participants));
	}

	private List<Course> retrieveAllCourses() {
		return StreamSupport.stream(courseCollection.find().spliterator(), false)
				.map(d -> new Course("" + d.get(ID), "" + d.get(NAME), (int) d.get(CFU))).collect(Collectors.toList());
	}

	private List<String> getCourseParticipants(String courseId) {
		Document d = courseCollection.find(Filters.eq(ID, courseId)).first();
		if (d != null)
			return d.getList("participants", String.class);
		return null;
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
		addTestCourseToRepository(COURSE_ID_2, COURSE_NAME_2, COURSE_CFU_2);
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
		assertThat(courseRepository.getParticipantsStudentsIdByCourseId(COURSE_ID_1)).isNull();
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
