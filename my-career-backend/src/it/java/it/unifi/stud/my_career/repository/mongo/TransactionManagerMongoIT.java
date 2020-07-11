package it.unifi.stud.my_career.repository.mongo;

import static org.assertj.core.api.Assertions.assertThat;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.GenericContainer;

import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import it.unifi.stud.my_career.model.Course;
import it.unifi.stud.my_career.model.Student;

public class TransactionManagerMongoIT {

	@SuppressWarnings("rawtypes")
	@ClassRule
	public static final GenericContainer mongo = new GenericContainer("krnbr/mongo:4.2.6").withExposedPorts(27017);

	private static final String CAREER_DB_NAME = "career";
	private static final String COURSES_COLLECTION_NAME = "courses";
	private static final String STUDENTS_COLLECTION_NAME = "students";

	private static final String STUDENT_ID_1 = "123";
	private static final String STUDENT_NAME_1 = "Pippo";
	private static final String COURSE_ID_1 = "223";
	private static final String COURSE_NAME_1 = "LABbello";
	private static final int COURSE_CFU_1 = 9;

	private MongoClient client;
	private CourseRepositoryMongo courseRepository;
	private StudentRepositoryMongo studentRepository;
	private TransactionManagerMongo txManagerMongo;

	@Before
	public void setup() {
		client = new MongoClient(new ServerAddress(mongo.getContainerIpAddress(), mongo.getMappedPort(27017)));
		courseRepository = new CourseRepositoryMongo(client, CAREER_DB_NAME, COURSES_COLLECTION_NAME);
		studentRepository = new StudentRepositoryMongo(client, CAREER_DB_NAME, STUDENTS_COLLECTION_NAME);
		txManagerMongo = new TransactionManagerMongo(client, studentRepository, courseRepository);
	}

	@After
	public void tearDown() {
		client.close();
	}

	// careerTransaction

	@Test
	public void testCareerTransaction() {
		// Setup
		Student student = new Student(STUDENT_ID_1, STUDENT_NAME_1);
		Course course = new Course(COURSE_ID_1, COURSE_NAME_1, COURSE_CFU_1);
		studentRepository.save(student);
		courseRepository.save(course);
		studentRepository.addStudentParticipation(STUDENT_ID_1, course);
		courseRepository.addCourseParticipant(COURSE_ID_1, student);
		// Exercise
		Course found = txManagerMongo.careerTransaction((studentRepository, courseRepository) -> {
			List<String> coursesIds = studentRepository.getParticipatedCoursesIdByStudentId(student.getId());
			if (coursesIds.contains(course.getId())) {
				return courseRepository.findById(course.getId());
			}
			return null;
		});
		// Verify
		assertThat(found).isEqualTo(course);
	}

	// studentTransaction

	@Test
	public void testStudentTransaction() {
		// Setup
		Student student = new Student(STUDENT_ID_1, STUDENT_NAME_1);
		studentRepository.save(student);
		// Exercise
		Student found = txManagerMongo.studentTransaction((studentRepository) -> {
			return studentRepository.findById(STUDENT_ID_1);
		});
		// Verify
		assertThat(found).isEqualTo(student);
	}

	// courseTransaction

	@Test
	public void testCourseTransaction() {
		// Setup
		Course course = new Course(COURSE_ID_1, COURSE_NAME_1, COURSE_CFU_1);
		courseRepository.save(course);
		// Exercise
		Course found = txManagerMongo.courseTransaction((courseRepository) -> {
			return courseRepository.findById(COURSE_ID_1);
		});
		// Verify
		assertThat(found).isEqualTo(course);
	}

}
