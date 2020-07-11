package it.unifi.stud.my_career.repository.controller;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testcontainers.containers.GenericContainer;

import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import it.unifi.stud.my_career.controller.MyCareerController;
import it.unifi.stud.my_career.model.Course;
import it.unifi.stud.my_career.model.Student;
import it.unifi.stud.my_career.repository.CourseRepository;
import it.unifi.stud.my_career.repository.StudentRepository;
import it.unifi.stud.my_career.repository.TransactionManager;
import it.unifi.stud.my_career.repository.mongo.CourseRepositoryMongo;
import it.unifi.stud.my_career.repository.mongo.StudentRepositoryMongo;
import it.unifi.stud.my_career.repository.mongo.TransactionManagerMongo;
import it.unifi.stud.my_career.service.MyCareerService;
import it.unifi.stud.my_career.view.MyCareerView;

public class MyCareerControllerIT {

	private static final String COURSE_NAME = "APT";
	private static final String COURSE_ID = "123";
	private static final String STUDENT_NAME = "test1";
	private static final String STUDENT_ID = "1";

	@SuppressWarnings("rawtypes")
	@ClassRule
	public static final GenericContainer mongo = new GenericContainer("krnbr/mongo:4.2.6").withExposedPorts(27017);

	private static final String CAREER_DB_NAME = "career";
	private static final String COURSES_COLLECTION_NAME = "courses";
	private static final String STUDENTS_COLLECTION_NAME = "students";

	@Mock
	private MyCareerView myCareerView;

	private MyCareerController myCareerController;

	private MyCareerService myCareerService;
	private TransactionManager transactionManager;
	private CourseRepository courseRepository;
	private StudentRepository studentRepository;
	private MongoClient mongoClient;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);

		mongoClient = new MongoClient(new ServerAddress(mongo.getContainerIpAddress(), mongo.getMappedPort(27017)));
		studentRepository = new StudentRepositoryMongo(mongoClient, CAREER_DB_NAME, STUDENTS_COLLECTION_NAME);
		courseRepository = new CourseRepositoryMongo(mongoClient, CAREER_DB_NAME, COURSES_COLLECTION_NAME);

		// explicitly empty the database through the repository
		for (Student student : studentRepository.findAll()) {
			studentRepository.delete(student.getId());
		}
		for (Course coursee : courseRepository.findAll()) {
			courseRepository.delete(coursee.getId());
		}

		transactionManager = new TransactionManagerMongo(mongoClient, studentRepository, courseRepository);
		myCareerService = new MyCareerService(transactionManager);

		myCareerController = new MyCareerController(myCareerView, myCareerService);
	}

	@Test
	public void testGetAllStudents() {
		// Setup
		Student student = new Student(STUDENT_ID, STUDENT_NAME);
		myCareerService.saveStudent(student);
		// Exercise
		myCareerController.getAllStudents();
		// Verify
		verify(myCareerView).showAllStudents(asList(student));
	}

	@Test
	public void testAddStudent() {
		// Setup
		Student student = new Student(STUDENT_ID, STUDENT_NAME);
		// Exercise
		myCareerController.addStudent(student);
		// Verify
		myCareerView.studentAdded("Student account created", student);
	}

	@Test
	public void testDeleteStudent() {
		// Setup
		Student studentToDelete = new Student(STUDENT_ID, STUDENT_NAME);
		studentRepository.save(studentToDelete);
		// Exercise
		myCareerController.deleteStudent(studentToDelete);
		// Verify
		myCareerView.studentRemoved("Student account deleted", studentToDelete);
	}

	@Test
	public void testGetCoursesByStudent() {
		// Setup
		Student student = new Student(STUDENT_ID, STUDENT_NAME);
		Course course = new Course(COURSE_ID, COURSE_NAME, 6);
		studentRepository.save(student);
		courseRepository.save(course);
		studentRepository.addStudentParticipation(STUDENT_ID, course);
		courseRepository.addCourseParticipant(COURSE_ID, student);
		// Exercise
		myCareerController.getCoursesByStudent(student);
		// Verify
		myCareerView.showAllCourses(asList(course));
	}

	@Test
	public void testAddCourse() {
		// Setup
		Student student = new Student(STUDENT_ID, STUDENT_NAME);
		Course course = new Course(COURSE_ID, COURSE_NAME, 6);
		// Exercise
		myCareerController.addCourse(student, course);
		// Verify
		myCareerView.courseAdded("Added a new course", course);
	}

	@Test
	public void testDeleteCourse() {
		// Setup
		Student student = new Student(STUDENT_ID, STUDENT_NAME);
		Course course = new Course(COURSE_ID, COURSE_NAME, 6);
		// Exercise
		myCareerController.removeCourse(student, course);
		// Verify
		myCareerView.courseRemoved("The course has been removed", course);
	}

	@Test
	public void testGetStudentsByCourse() {
		// Setup
		Student student = new Student(STUDENT_ID, STUDENT_NAME);
		Course course = new Course(COURSE_ID, COURSE_NAME, 6);
		studentRepository.save(student);
		courseRepository.save(course);
		studentRepository.addStudentParticipation(STUDENT_ID, course);
		courseRepository.addCourseParticipant(COURSE_ID, student);
		// Exercise
		myCareerController.getCoursesByStudent(student);
		// Verify
		myCareerView.showAllStudents(asList(student));
	}
}
