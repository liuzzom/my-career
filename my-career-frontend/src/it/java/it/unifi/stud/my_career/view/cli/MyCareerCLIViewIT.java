package it.unifi.stud.my_career.view.cli;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
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

public class MyCareerCLIViewIT {
	private static final String COURSE1_NAME = "APT";
	private static final String COURSE1_ID = "123";

	private static final String STUDENT2_NAME = "test2";
	private static final String STUDENT2_ID = "2";
	private static final String STUDENT1_NAME = "test1";
	private static final String STUDENT1_ID = "1";

	@SuppressWarnings("rawtypes")
	@ClassRule
	public static final GenericContainer mongo = new GenericContainer("krnbr/mongo:4.2.6").withExposedPorts(27017);

	private static final String CAREER_DB_NAME = "career";
	private static final String COURSES_COLLECTION_NAME = "courses";
	private static final String STUDENTS_COLLECTION_NAME = "students";

	private MyCareerCLIView myCareerCLIView;

	private MyCareerController myCareerController;
	private MyCareerService myCareerService;
	private TransactionManager transactionManager;
	private CourseRepository courseRepository;
	private StudentRepository studentRepository;
	private MongoClient mongoClient;

	private ByteArrayOutputStream testOut;
	private ByteArrayInputStream testin;

	@Before
	public void setup() {
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

		testOut = new ByteArrayOutputStream();
		myCareerCLIView = new MyCareerCLIView(System.in, new PrintStream(testOut));
		myCareerController = new MyCareerController(myCareerCLIView, myCareerService);
		myCareerCLIView.setMyCareerController(myCareerController);

	}
	
	@After
	public void onTearDown() {
		mongoClient.close();
	}	

	@Test
	public void testAllStudents() {
		// Setup
		Student student1 = new Student(STUDENT1_ID, STUDENT1_NAME);
		Student student2 = new Student(STUDENT2_ID, STUDENT2_NAME);
		myCareerService.saveStudent(student1);
		myCareerService.saveStudent(student2);
		// Execute
		myCareerController.getAllStudents();
		// Verify
		assertThat(testOut.toString()).isEqualTo("Student [id=1, name=test1]\nStudent [id=2, name=test2]\n");
		assertThat(myCareerCLIView.getStudentsList()).containsExactly(student1, student2);
	}

	@Test
	public void testAddStudentSuccess() {
		// Setup
		String userInput = "1\n1\ntest1";
		testin = new ByteArrayInputStream(userInput.getBytes());
		myCareerCLIView.setInputStream(testin);
		// Exercise
		myCareerCLIView.exec();
		// Verify
		assertThat(testOut.toString())
				.contains("Insert id: Insert name: Student account created : Student [id=1, name=test1]\n");
		assertThat(myCareerCLIView.getStudentsList()).containsExactly(new Student(STUDENT1_ID, STUDENT1_NAME));
	}

	@Test
	public void testAddStudentErrorStudentDoesAlreadyExist() {
		// Setup
		Student student = new Student(STUDENT1_ID, STUDENT1_NAME);
		myCareerService.saveStudent(student);
		String userInput = "1\n1\ntest1";
		testin = new ByteArrayInputStream(userInput.getBytes());
		myCareerCLIView.setInputStream(testin);
		// Exercise
		myCareerCLIView.exec();
		// Verify
		assertThat(testOut.toString()).contains(
				"Insert id: Insert name: ERROR! Already exists a student with id 1 : Student [id=1, name=test1]\n");
	}

	@Test
	public void testDeleteStudentSuccess() {
		// Setup
		Student student = new Student(STUDENT1_ID, STUDENT1_NAME);
		myCareerService.saveStudent(student);
		String userInput = "3\n1\ntest1";
		testin = new ByteArrayInputStream(userInput.getBytes());
		myCareerCLIView.setInputStream(testin);
		// Exercise
		myCareerCLIView.exec();
		// Verify
		assertThat(testOut.toString())
				.contains("Insert id: Insert name: Student account deleted : Student [id=1, name=test1]\n");
		assertThat(myCareerCLIView.getStudentsList()).isEmpty();
	}

	@Test
	public void testDeletStudentErrorStudentDoesNotExist() {
		// Setup
		String userInput = "3\n1\ntest1";
		testin = new ByteArrayInputStream(userInput.getBytes());
		myCareerCLIView.setInputStream(testin);
		// Exercise
		myCareerCLIView.exec();
		// Verify
		assertThat(testOut.toString())
				.contains("Insert id: Insert name: ERROR! Student does not exist : Student [id=1, name=test1]\n");
	}

	@Test
	public void testAddCourseSuccess() {
		// Setup
		Student student = new Student(STUDENT1_ID, STUDENT1_NAME);
		myCareerController.addStudent(student);
		String userInput = "5\n1\ntest1\n123\nAPT\n6";
		testin = new ByteArrayInputStream(userInput.getBytes());
		myCareerCLIView.setInputStream(testin);
		// Exercise
		myCareerCLIView.exec();
		// Verify
		assertThat(myCareerCLIView.getStudentsList()).containsExactly(new Student(STUDENT1_ID, STUDENT1_NAME));
		assertThat(myCareerCLIView.getCoursesList()).containsExactly(new Course(COURSE1_ID, COURSE1_NAME, 6));
	}

	@Test
	public void testAddCourseErrorrCourseAlreadyExist() {
		// Setup
		Student student = new Student(STUDENT1_ID, STUDENT1_NAME);
		myCareerService.saveStudent(student);
		Course course = new Course(COURSE1_ID, COURSE1_NAME, 6);
		myCareerService.saveCourse(student, course);
		String userInput = "5\n1\ntest1\n123\nAPT\n6";
		testin = new ByteArrayInputStream(userInput.getBytes());
		myCareerCLIView.setInputStream(testin);
		// Exercise
		myCareerCLIView.exec();
		// Verify
		assertThat(testOut.toString()).contains(
				"Insert student id: Insert student name: Insert course id: Insert course name: Insert course CFU:"
						+ " ERROR! The course already exists : Course [id=123, name=APT, cfu=6]\n");
	}

	@Test
	public void testAddCourseErrorStudentDoesNotExist() {
		// Setup
		String userInput = "5\n1\ntest1\n123\nAPT\n6";
		testin = new ByteArrayInputStream(userInput.getBytes());
		myCareerCLIView.setInputStream(testin);
		// Exercise
		myCareerCLIView.exec();
		// Verify
		assertThat(testOut.toString()).contains(
				"Insert student id: Insert student name: Insert course id: Insert course name: Insert course CFU:"
						+ " ERROR! The student does not exist : Course [id=123, name=APT, cfu=6]\n");
	}

	@Test
	public void testRemoveCourseSuccess() {
		// Setup
		Student student = new Student(STUDENT1_ID, STUDENT1_NAME);
		myCareerController.addStudent(student);
		Course course = new Course(COURSE1_ID, COURSE1_NAME, 6);
		myCareerController.addCourse(student, course);
		String userInput = "6\n1\ntest1\n123\nAPT\n6";
		testin = new ByteArrayInputStream(userInput.getBytes());
		myCareerCLIView.setInputStream(testin);
		// Exercise
		myCareerCLIView.exec();
		// Verify
		assertThat(myCareerCLIView.getCoursesList()).isEmpty();
	}

	@Test
	public void testRemoveCourseErrorCourseDoesNotExist() {
		// Setup
		Student student = new Student(STUDENT1_ID, STUDENT1_NAME);
		myCareerService.saveStudent(student);
		String userInput = "6\n1\ntest1\n123\nAPT\n6";
		testin = new ByteArrayInputStream(userInput.getBytes());
		myCareerCLIView.setInputStream(testin);
		// Exercise
		myCareerCLIView.exec();
		// Verify
		assertThat(testOut.toString()).contains(
				"Insert student id: Insert student name: Insert course id: Insert course name: Insert course CFU: "
						+ "ERROR! The course does not exist : Course [id=123, name=APT, cfu=6]\n");
	}

	@Test
	public void testRemoveCourseErrorStudentDoesNotExist() {
		// Setup
		String userInput = "6\n1\ntest1\n123\nAPT\n6";
		testin = new ByteArrayInputStream(userInput.getBytes());
		myCareerCLIView.setInputStream(testin);
		// Exercise
		myCareerCLIView.exec();
		// Verify
		assertThat(testOut.toString()).contains(
				"Insert student id: Insert student name: Insert course id: Insert course name: Insert course CFU: "
						+ "ERROR! The student does not exist : Course [id=123, name=APT, cfu=6]\n");

	}

	@Test
	public void testGetCoursesByStudentSuccess() {
		// Setup
		Student student = new Student(STUDENT1_ID, STUDENT1_NAME);
		myCareerService.saveStudent(student);
		Course course = new Course(COURSE1_ID, COURSE1_NAME, 6);
		myCareerService.saveCourse(student, course);
		String userInput = "4\n1\ntest1";
		testin = new ByteArrayInputStream(userInput.getBytes());
		myCareerCLIView.setInputStream(testin);
		// Exercise
		myCareerCLIView.exec();
		// Verify
		assertThat(testOut.toString()).contains("Insert id: Insert name: Course [id=123, name=APT, cfu=6]\n");
		assertThat(myCareerCLIView.getCoursesList()).containsExactly(course);
	}

	@Test
	public void testGetCoursesByStudentErrorStudentDoesNotExist() {
		String userInput = "4\n1\ntest1";
		testin = new ByteArrayInputStream(userInput.getBytes());
		myCareerCLIView.setInputStream(testin);
		// Exercise
		myCareerCLIView.exec();
		// Verify
		assertThat(testOut.toString())
				.contains("Insert id: Insert name: ERROR! Student does not exist : Student [id=1, name=test1]\n");
	}

	@Test
	public void testGetStudentsByCourseSuccess() {
		// Setup
		Student student = new Student(STUDENT1_ID, STUDENT1_NAME);
		myCareerService.saveStudent(student);
		Course course = new Course(COURSE1_ID, COURSE1_NAME, 6);
		myCareerService.saveCourse(student, course);
		String userInput = "7\n123\nAPT\n6";
		testin = new ByteArrayInputStream(userInput.getBytes());
		myCareerCLIView.setInputStream(testin);
		// Exercise
		myCareerCLIView.exec();
		// Verify
		assertThat(testOut.toString())
				.contains("Insert course id: Insert course name: Insert course CFU: Student [id=1, name=test1]\n");
		assertThat(myCareerCLIView.getStudentsList()).containsExactly(student);
	}
}
