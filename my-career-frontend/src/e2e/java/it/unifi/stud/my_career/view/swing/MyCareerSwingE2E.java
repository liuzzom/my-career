package it.unifi.stud.my_career.view.swing;

import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.bson.Document;
import org.junit.ClassRule;
import org.testcontainers.containers.GenericContainer;

import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;

public class MyCareerSwingE2E extends AssertJSwingJUnitTestCase{
	
	@SuppressWarnings("rawtypes")
	@ClassRule
	public static final GenericContainer mongoContainer = new GenericContainer("krnbr/mongo:4.2.6").withExposedPorts(27017);
	
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
	
	@Override
	protected void onSetUp() {
		client = new MongoClient(new ServerAddress(mongoContainer.getContainerIpAddress(), mongoContainer.getMappedPort(27017)));
		client.getDatabase(CAREER_DB_NAME).drop();
	}
	
	
	
	private void addTestStudentToDatabase(String studentId, String studentName) {
		client.getDatabase(CAREER_DB_NAME).getCollection(STUDENTS_COLLECTION_NAME).insertOne(new Document().append(NAME, studentName).append(ID, studentId));
	}


}
