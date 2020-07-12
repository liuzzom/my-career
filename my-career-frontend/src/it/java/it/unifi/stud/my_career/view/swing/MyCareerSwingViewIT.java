package it.unifi.stud.my_career.view.swing;

import static org.assertj.core.api.Assertions.assertThat;
import org.assertj.swing.annotation.GUITest;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.junit.runner.GUITestRunner;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
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

@RunWith(GUITestRunner.class)
public class MyCareerSwingViewIT extends AssertJSwingJUnitTestCase {

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

	private MyCareerSwingView myCareerView;

	private MyCareerController myCareerController;
	private MyCareerService myCareerService;
	private TransactionManager transactionManager;
	private CourseRepository courseRepository;
	private StudentRepository studentRepository;
	private MongoClient mongoClient;

	private FrameFixture window;

	@Override
	protected void onSetUp() {
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

		GuiActionRunner.execute(() -> {
			myCareerView = new MyCareerSwingView();
			myCareerController = new MyCareerController(myCareerView, myCareerService);
			myCareerView.setMyCareerController(myCareerController);
			return myCareerView;
		});

		window = new FrameFixture(robot(), myCareerView);
		window.show();
	}

	@Override
	protected void onTearDown() {
		mongoClient.close();
	}

	@Test
	@GUITest
	public void testGetAllStudents() {
		// Setup
		Student student1 = new Student(STUDENT1_ID, STUDENT1_NAME);
		Student student2 = new Student(STUDENT2_ID, STUDENT2_NAME);
		myCareerService.saveStudent(student1);
		myCareerService.saveStudent(student2);
		// Exercise
		GuiActionRunner.execute(() -> myCareerController.getAllStudents());
		// Verify
		assertThat(window.list("studentsList").contents()).containsExactly(student1.toString(), student2.toString());
	}

	@Test
	@GUITest
	public void testAddStudentButtonSuccess() {
		// Setup
		window.textBox("studentIDTextField").enterText(STUDENT1_ID);
		window.textBox("studentNameTextField").enterText(STUDENT1_NAME);
		// Exercise
		window.button("addStudentButton").click();
		// Verify
		assertThat(window.list("studentsList").contents()).containsExactly(new Student(STUDENT1_ID, STUDENT1_NAME).toString());
		window.label("studentInfoErrorMessageLabel")
				.requireText("Student account created: " + new Student(STUDENT1_ID, STUDENT1_NAME).toString());
	}

	@Test
	@GUITest
	public void testAddStudentButtonError() {
		// Setup
		Student student = new Student(STUDENT1_ID, STUDENT1_NAME);
		studentRepository.save(student);
		window.textBox("studentIDTextField").enterText(STUDENT1_ID);
		window.textBox("studentNameTextField").enterText(STUDENT2_NAME);
		// Exercise
		window.button("addStudentButton").click();
		// Verify
		window.label("studentInfoErrorMessageLabel")
				.requireText("Already exists a student with id 1: " + new Student(STUDENT1_ID, STUDENT1_NAME).toString());
	}

	@Test
	@GUITest
	public void testDeleteStudentButtonSuccess() {
		// Setup
		GuiActionRunner.execute(() -> myCareerController.addStudent(new Student(STUDENT1_ID, STUDENT1_NAME)));
		window.list("studentsList").selectItem(0);
		// Exercise
		window.button("deleteStudentButton").click();
		// Verify
		assertThat(window.list("studentsList").contents()).isEmpty();
		window.label("studentInfoErrorMessageLabel")
				.requireText("Student account deleted: " + new Student(STUDENT1_ID, STUDENT1_NAME));
	}

	@Test
	@GUITest
	public void testDeleteStudentButtonError() {
		// Setup
		Student student = new Student(STUDENT1_ID, STUDENT1_NAME);
		GuiActionRunner.execute(() -> myCareerView.getStudentsListModel().addElement(student));
		window.list("studentsList").selectItem(0);
		// Exercise
		window.button("deleteStudentButton").click();
		// Verify
		assertThat(window.list("studentsList").contents()).containsExactly(student.toString());
		window.label("studentInfoErrorMessageLabel").requireText("Student does not exist: " + student);
	}

	@Test
	@GUITest
	public void testAddCourseButtonSuccess() {
		// Setup
		Student student = new Student(STUDENT1_ID, STUDENT1_NAME);
		Course course = new Course(COURSE1_ID, COURSE1_NAME, 6);
		GuiActionRunner.execute(() -> myCareerController.addStudent(student));
		window.list("studentsList").selectItem(0);
		window.textBox("courseIDTextField").enterText(COURSE1_ID);
		window.textBox("courseNameTextField").enterText(COURSE1_NAME);
		window.textBox("courseCFUsTextField").enterText("6");
		// Exercise
		window.button("addCourseButton").click();
		// Verify
		assertThat(window.list("studentsList").contents()).containsExactly(student.toString());
		assertThat(window.list("coursesList").contents()).containsExactly(course.toString());
	}

	@Test
	@GUITest
	public void testAddCourseButtonErrorCourseAlreadyExist() {
		// Setup
		Student student = new Student(STUDENT1_ID, STUDENT1_NAME);
		Course course = new Course(COURSE1_ID, COURSE1_NAME, 6);
		GuiActionRunner.execute(() -> {
			myCareerController.addStudent(student);
			myCareerController.addCourse(student, course);
		});
		window.list("studentsList").selectItem(0);
		window.textBox("courseIDTextField").enterText(COURSE1_ID);
		window.textBox("courseNameTextField").enterText(COURSE1_NAME);
		window.textBox("courseCFUsTextField").enterText("6");
		// Exercise
		window.button("addCourseButton").click();
		// Verify
		window.label("courseInfoErrorMessageLabel").requireText("The course already exists: " + course);
	}

	@Test
	@GUITest
	public void testAddButtonErrorStudentDoesNotExist() {
		// Setup
		Student student = new Student(STUDENT1_ID, STUDENT1_NAME);
		Course course = new Course(COURSE1_ID, COURSE1_NAME, 6);
		GuiActionRunner.execute(() -> myCareerView.getStudentsListModel().addElement(student));
		window.list("studentsList").selectItem(0);
		window.textBox("courseIDTextField").enterText(COURSE1_ID);
		window.textBox("courseNameTextField").enterText(COURSE1_NAME);
		window.textBox("courseCFUsTextField").enterText("6");
		// Exercise
		window.button("addCourseButton").click();
		// Verify
		window.label("courseInfoErrorMessageLabel").requireText("The student does not exist: " + course);
	}

	@Test
	@GUITest
	public void testGetCoursesByStudentSuccess() {
		// Setup
		Student student = new Student(STUDENT1_ID, STUDENT1_NAME);
		Course course = new Course(COURSE1_ID, COURSE1_NAME, 6);
		GuiActionRunner.execute(() -> {
			myCareerController.addStudent(student);
			myCareerService.saveCourse(student, course);
		});
		// Exercise
		window.list("studentsList").selectItem(0);
		// Verify
		assertThat(window.list("coursesList").contents()).containsExactly(course.toString());
	}

	@Test
	@GUITest
	public void testGetCoursesByStudentErrorStudentDoesNotExist() {
		// Setup
		Student student = new Student(STUDENT1_ID, STUDENT1_NAME);
		GuiActionRunner.execute(() -> {
			myCareerView.getStudentsListModel().addElement(student);
		});
		// Exercise
		window.list("studentsList").selectItem(0);
		// Verify
		window.label("studentInfoErrorMessageLabel").requireText("Student does not exist: " + student);
	}

	@Test
	@GUITest
	public void testDeleteCourseButtonSuccess() {
		// Setup
		Student student = new Student(STUDENT1_ID, STUDENT1_NAME);
		Course course = new Course(COURSE1_ID, COURSE1_NAME, 6);
		GuiActionRunner.execute(() -> {
			myCareerController.addStudent(student);
			myCareerService.saveCourse(student, course);
		});
		window.list("studentsList").selectItem(0);
		window.list("coursesList").selectItem(0);
		// Exercise
		window.button("deleteCourseButton").click();
		// Verify
		assertThat(window.list("coursesList").contents()).isEmpty();
		window.label("courseInfoErrorMessageLabel").requireText("The course has been removed: " + course);
	}

	@Test
	@GUITest
	public void testDeleteButtonErrorCourseDoesNotExist() {
		// Setup
		Student student = new Student(STUDENT1_ID, STUDENT1_NAME);
		Course course = new Course(COURSE1_ID, COURSE1_NAME, 6);
		GuiActionRunner.execute(() -> {
			myCareerController.addStudent(student);
			myCareerService.saveCourse(student, course);
			;
		});
		window.list("studentsList").selectItem(0);
		GuiActionRunner.execute(() -> myCareerService.removeCourse(student, course));
		window.list("coursesList").selectItem(0);
		// Exercise
		window.button("deleteCourseButton").click();
		// Verify
		window.label("courseInfoErrorMessageLabel").requireText("The course does not exist: " + course);

	}

	@Test
	@GUITest
	public void testDeleteButtonErrorStudentDoesNotExist() {
		// Setup
		Student student = new Student(STUDENT1_ID, STUDENT1_NAME);
		Course course = new Course(COURSE1_ID, COURSE1_NAME, 6);
		GuiActionRunner.execute(() -> {
			myCareerController.addStudent(student);
			myCareerService.saveCourse(student, course);
			;
		});
		window.list("studentsList").selectItem(0);
		GuiActionRunner.execute(() -> myCareerService.deleteStudent(student));
		window.list("coursesList").selectItem(0);
		// Exercise
		window.button("deleteCourseButton").click();
		// Verify
		window.label("courseInfoErrorMessageLabel").requireText("The student does not exist: " + course);
	}

}
