package it.unifi.stud.my_career.app;

import static org.assertj.swing.launcher.ApplicationLauncher.*;
import static org.assertj.core.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.assertj.swing.finder.WindowFinder;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.junit.runner.GUITestRunner;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.assertj.swing.annotation.GUITest;
import org.assertj.swing.core.GenericTypeMatcher;
import org.assertj.swing.core.matcher.JButtonMatcher;

import org.bson.Document;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.testcontainers.containers.GenericContainer;

import javax.swing.JFrame;

import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import com.mongodb.client.model.Filters;

import it.unifi.stud.my_career.model.Course;
import it.unifi.stud.my_career.model.Student;

@RunWith(GUITestRunner.class)
public class MyCareerAppGUIE2E extends AssertJSwingJUnitTestCase {

	@SuppressWarnings("rawtypes")
	@ClassRule
	public static final GenericContainer mongoContainer = new GenericContainer("krnbr/mongo:4.2.6")
			.withExposedPorts(27017);

	private static final String CAREER_DB_NAME = "career";
	private static final String STUDENTS_COLLECTION_NAME = "students";
	private static final String COURSES_COLLECTION_NAME = "courses";

	private static final String CFU = "cfu";
	private static final String NAME = "name";
	private static final String ID = "id";

	private static final String STUDENT_ID_1 = "1423";
	private static final String STUDENT_NAME_1 = "Pippo";
	private static final String STUDENT_ID_2 = "1234";
	private static final String STUDENT_NAME_2 = "Peppo";
	private static final String STUDENT_ID_3 = "12345";
	private static final String STUDENT_NAME_3 = "Giorgino";

	private static final String COURSE_ID_1 = "777";
	private static final String COURSE_NAME_1 = "LabBello";
	private static final int COURSE_CFU_1 = 9;
	private static final String COURSE_ID_2 = "888";
	private static final String COURSE_NAME_2 = "LabBrutto";
	private static final int COURSE_CFU_2 = 6;
	private static final String COURSE_ID_3 = "883";
	private static final String COURSE_NAME_3 = "LabBlu";
	private static final int COURSE_CFU_3 = 12;

	private MongoClient client;

	private FrameFixture window;

	@Override
	protected void onSetUp() {
		String mongoContainerIpAddress = mongoContainer.getContainerIpAddress();
		Integer mongoContainerMappedPort = mongoContainer.getMappedPort(27017);
		client = new MongoClient(new ServerAddress(mongoContainerIpAddress, mongoContainerMappedPort));
		client.getDatabase(CAREER_DB_NAME).drop();
		// adding students to db
		List<String> student1Participations = new ArrayList<String>();
		student1Participations.add(COURSE_ID_1);
		addTestStudentToDatabaseWithParticipations(STUDENT_ID_1, STUDENT_NAME_1, student1Participations);
		List<String> student2Participations = new ArrayList<String>();
		student2Participations.add(COURSE_ID_1);
		student2Participations.add(COURSE_ID_2);
		addTestStudentToDatabaseWithParticipations(STUDENT_ID_2, STUDENT_NAME_2, student2Participations);
		// adding courses to db
		List<String> course1Participants = new ArrayList<String>();
		course1Participants.add(STUDENT_ID_1);
		course1Participants.add(STUDENT_ID_2);
		addTestCourseToDatabaseWithParticipants(COURSE_ID_1, COURSE_NAME_1, COURSE_CFU_1, course1Participants);
		List<String> course2Participants = new ArrayList<String>();
		course2Participants.add(STUDENT_ID_2);
		addTestCourseToDatabaseWithParticipants(COURSE_ID_2, COURSE_NAME_2, COURSE_CFU_2, course2Participants);
		// start swing view
		application("it.unifi.stud.my_career.app.MyCareerApp").withArgs("--user-interface="+"gui","--mongo-host=" + mongoContainerIpAddress,
				"--mongo-port=" + mongoContainerMappedPort.toString(), "--db-name=" + CAREER_DB_NAME,
				"--db-student-collection=" + STUDENTS_COLLECTION_NAME,
				"--db-course-collection=" + COURSES_COLLECTION_NAME).start();
		// get window reference
		window = WindowFinder.findFrame(new GenericTypeMatcher<JFrame>(JFrame.class) {
			@Override
			protected boolean isMatching(JFrame frame) {
				return "My Career".equals(frame.getTitle()) && frame.isShowing();
			}
		}).using(robot());

	}

	@Override
	protected void onTearDown() {
		client.close();
	}

	// show all students
	@Test
	@GUITest
	public void testOnStartAllStudentsAreShown() {
		Student student1 = new Student(STUDENT_ID_1, STUDENT_NAME_1);
		Student student2 = new Student(STUDENT_ID_2, STUDENT_NAME_2);
		assertThat(window.list("studentsList").contents()).anySatisfy(e -> assertThat(e).contains(student1.toString()))
				.anySatisfy(e -> assertThat(e).contains(student2.toString()));
	}

	// add student Button
	@Test
	@GUITest
	public void testAddStudentButtonSuccess() {
		Student student3 = new Student(STUDENT_ID_3, STUDENT_NAME_3);
		window.textBox("studentIDTextField").enterText(STUDENT_ID_3);
		window.textBox("studentNameTextField").enterText(STUDENT_NAME_3);
		window.button(JButtonMatcher.withText("Add Student")).click();
		assertThat(window.list("studentsList").contents()).anySatisfy(e -> assertThat(e).contains(student3.toString()));
	}

	@Test
	@GUITest
	public void testAddStudentButtonError() {
		window.textBox("studentIDTextField").enterText(STUDENT_ID_1);
		window.textBox("studentNameTextField").enterText("Piergiorgio");
		window.button(JButtonMatcher.withText("Add Student")).click();
		assertThat(window.label("studentInfoErrorMessageLabel").text()).contains(STUDENT_ID_1, STUDENT_NAME_1);
	}

	// add course Button
	@Test
	@GUITest
	public void testAddCourseButtonSuccess() {
		window.list("studentsList").selectItem(Pattern.compile(".*" + STUDENT_ID_1 + ".*"));
		Course course3 = new Course(COURSE_ID_3, COURSE_NAME_3, COURSE_CFU_3);
		window.textBox("courseIDTextField").enterText(COURSE_ID_3);
		window.textBox("courseNameTextField").enterText(COURSE_NAME_3);
		window.textBox("courseCFUsTextField").enterText(String.valueOf(COURSE_CFU_3));
		window.button(JButtonMatcher.withText("Add Course")).click();
		assertThat(window.list("coursesList").contents()).anySatisfy(e -> assertThat(e).contains(course3.toString()));
	}

	@Test
	@GUITest
	public void testAddCourseButtonError() {
		window.list("studentsList").selectItem(Pattern.compile(".*" + STUDENT_ID_1 + ".*"));
		window.textBox("courseIDTextField").enterText(COURSE_ID_1);
		window.textBox("courseNameTextField").enterText("LabFinto");
		window.textBox("courseCFUsTextField").enterText(String.valueOf(COURSE_CFU_1));
		window.button(JButtonMatcher.withText("Add Course")).click();
		assertThat(window.label("courseInfoErrorMessageLabel").text()).contains(COURSE_ID_1, "LabFinto",
				String.valueOf(COURSE_CFU_1));
	}
	
	// delete student button
	@Test
	@GUITest
	public void testDeleteStudentButtonSuccess() {
		window.list("studentsList").selectItem(Pattern.compile(".*" + STUDENT_ID_1 + ".*"));
		window.button(JButtonMatcher.withText("Delete Student")).click();
		assertThat(window.list("studentsList").contents()).noneMatch(e -> e.contains(STUDENT_ID_1));
	}

	@Test
	@GUITest
	public void testDeleteStudentButtonError() {
		window.list("studentsList").selectItem(Pattern.compile(".*" + STUDENT_ID_1 + ".*"));
		removeTestStudentFromDatabase(STUDENT_ID_1);
		window.button(JButtonMatcher.withText("Delete Student")).click();
		assertThat(window.label("studentInfoErrorMessageLabel").text()).contains(STUDENT_ID_1, STUDENT_NAME_1);
	}

	// delete course button
	@Test
	@GUITest
	public void testDeleteCourseButtonSuccess() {
		window.list("studentsList").selectItem(Pattern.compile(".*" + STUDENT_ID_1 + ".*"));
		window.list("coursesList").selectItem(Pattern.compile(".*" + COURSE_ID_1 + ".*"));
		window.button(JButtonMatcher.withText("Delete Course")).click();
		assertThat(window.list("coursesList").contents()).noneMatch(e -> e.contains(COURSE_ID_1));
	}

	@Test
	@GUITest
	public void testDeleteCourseButtonError() {
		window.list("studentsList").selectItem(Pattern.compile(".*" + STUDENT_ID_1 + ".*"));
		window.list("coursesList").selectItem(Pattern.compile(".*" + COURSE_ID_1 + ".*"));
		removeTestCourseFromDatabase(COURSE_ID_1);
		window.button(JButtonMatcher.withText("Delete Course")).click();
		assertThat(window.label("courseInfoErrorMessageLabel").text()).contains(COURSE_ID_1, COURSE_NAME_1,
				String.valueOf(COURSE_CFU_1));
	}

	// selecting a student show curses
	@Test
	@GUITest
	public void testSelectStudentShowParticipatedCourses() {
		Course course2 = new Course(COURSE_ID_2, COURSE_NAME_2, COURSE_CFU_2);
		window.list("studentsList").selectItem(Pattern.compile(".*" + STUDENT_ID_2 + ".*"));
		assertThat(window.list("coursesList").contents()).anySatisfy(e -> assertThat(e).contains(course2.toString()));
	}

	private void addTestStudentToDatabaseWithParticipations(String studentId, String studentName,
			List<String> participations) {
		client.getDatabase(CAREER_DB_NAME).getCollection(STUDENTS_COLLECTION_NAME).insertOne(new Document()
				.append(NAME, studentName).append(ID, studentId).append("participations", participations));
	}

	private void addTestCourseToDatabaseWithParticipants(String courseId, String courseName, int cfu,
			List<String> participants) {
		client.getDatabase(CAREER_DB_NAME).getCollection(COURSES_COLLECTION_NAME).insertOne(new Document()
				.append(ID, courseId).append(NAME, courseName).append(CFU, cfu).append("participants", participants));
	}

	private void removeTestStudentFromDatabase(String id) {
		client.getDatabase(CAREER_DB_NAME).getCollection(STUDENTS_COLLECTION_NAME).deleteOne(Filters.eq("id", id));
	}

	private void removeTestCourseFromDatabase(String id) {
		client.getDatabase(CAREER_DB_NAME).getCollection(COURSES_COLLECTION_NAME).deleteOne(Filters.eq("id", id));
	}

}
