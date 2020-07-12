package it.unifi.stud.my_career.app;

import static org.assertj.swing.launcher.ApplicationLauncher.*;

import java.util.ArrayList;
import java.util.List;

import org.assertj.swing.finder.WindowFinder;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.bson.Document;
import org.junit.ClassRule;
import org.omg.CORBA.portable.ApplicationException;
import org.testcontainers.containers.GenericContainer;

import static org.assertj.core.api.Assertions.*;

import javax.swing.JFrame;

import org.assertj.swing.annotation.GUITest;
import org.assertj.swing.core.GenericTypeMatcher;
import org.assertj.swing.core.matcher.JButtonMatcher;
import org.assertj.swing.finder.WindowFinder;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.junit.runner.GUITestRunner;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.bson.Document;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.testcontainers.containers.GenericContainer;

import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import com.mongodb.client.model.Filters;

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
		application("it.unifi.stud.my_career.app.MyCareerApp").withArgs("--mongo-host=" + mongoContainerIpAddress,
				"--mongo-port=" + mongoContainerMappedPort.toString(), "--db-name=" + CAREER_DB_NAME,
				"--db-student-collection=" + STUDENTS_COLLECTION_NAME,
				"--db-course-collection=" + COURSES_COLLECTION_NAME).start();
		// get window reference
		window = WindowFinder.findFrame(new GenericTypeMatcher<JFrame>(JFrame.class) {
			@Override
			protected boolean isMatching(JFrame frame) {
				System.out.println("---------------" + frame.getTitle());
				return "My Career".equals(frame.getTitle()) && frame.isShowing();
			}
		}).using(robot());

	}

	@Override
	protected void onTearDown() {
		client.close();
	}

	@Test
	@GUITest
	public void testOnStartAllStudentsAreShown() {

	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

	private void addTestStudentToDatabase(String studentId, String studentName) {
		client.getDatabase(CAREER_DB_NAME).getCollection(STUDENTS_COLLECTION_NAME)
				.insertOne(new Document().append(NAME, studentName).append(ID, studentId));
	}

	private void addTestStudentToDatabaseWithParticipations(String studentId, String studentName,
			List<String> participations) {
		client.getDatabase(CAREER_DB_NAME).getCollection(STUDENTS_COLLECTION_NAME).insertOne(new Document()
				.append(NAME, studentName).append(ID, studentId).append("participations", participations));
	}

	private void addTestCourseToDatabase(String courseId, String courseName, int cfu) {
		client.getDatabase(CAREER_DB_NAME).getCollection(COURSES_COLLECTION_NAME)
				.insertOne(new Document().append(ID, courseId).append(NAME, courseName).append(CFU, cfu));
	}

	private void addTestCourseToDatabaseWithParticipants(String courseId, String courseName, int cfu,
			List<String> participants) {
		client.getDatabase(CAREER_DB_NAME).getCollection(COURSES_COLLECTION_NAME).insertOne(new Document()
				.append(ID, courseId).append(NAME, courseName).append(CFU, cfu).append("participants", participants));
	}

	private void removeTestStudentFromDatabase(String id) {
		client.getDatabase(CAREER_DB_NAME).getCollection(STUDENTS_COLLECTION_NAME).deleteOne(Filters.eq("id", id));
	}

}
