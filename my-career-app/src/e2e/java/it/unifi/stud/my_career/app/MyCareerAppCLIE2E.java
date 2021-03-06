package it.unifi.stud.my_career.app;

import static org.assertj.core.api.Assertions.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.mongodb.MongoClient;
import com.mongodb.client.model.Filters;

public class MyCareerAppCLIE2E {

	private static final String CAREER_DB_NAME = "career";
	private static final String STUDENTS_COLLECTION_NAME = "students";
	private static final String COURSES_COLLECTION_NAME = "courses";

	private static final String CFU = "cfu";
	private static final String NAME = "name";
	private static final String ID = "id";

	private static final String STUDENT_ID_1 = "1423";
	private static final String STUDENT_NAME_1 = "Pippo";
	private static final String STUDENT_ID_2 = "1234";
	private static final String STUDENT_NAME_2 = "Maria";
	private static final String STUDENT_ID_3 = "12345";
	private static final String STUDENT_NAME_3 = "Giorgino";

	private static final String COURSE_ID_1 = "101";
	private static final String COURSE_NAME_1 = "APT";
	private static final int COURSE_CFU_1 = 6;
	private static final String COURSE_ID_2 = "202";
	private static final String COURSE_NAME_2 = "BDA";
	private static final int COURSE_CFU_2 = 9;
	private static final String COURSE_ID_3 = "883";
	private static final String COURSE_NAME_3 = "LabBlu";
	private static final int COURSE_CFU_3 = 12;

	public static BufferedReader inp;
	public static BufferedWriter out;
	public static Process mongoProcess;
	public static String mongoTestContainerId;

	private MongoClient mongoClient;

	@BeforeClass
	public static void dbStart() {
		try {
			mongoProcess = Runtime.getRuntime().exec("docker run -p 27017:27017 --detach --rm krnbr/mongo:4.2.6");
			InputStream is = mongoProcess.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));
			String line = null;
			while ((line = reader.readLine()) != null) {
				System.out.println("mongoID:" + line + "-");
				mongoTestContainerId = line;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	@AfterClass
	public static void dbShutDown() {
		try {
			Runtime.getRuntime().exec("docker kill " + mongoTestContainerId);
		} catch (IOException e) {
			e.printStackTrace();
		}
		mongoProcess.destroy();
	}

	@Before
	public void onSetUp() {

		try {

			mongoClient = new MongoClient("localhost");
			mongoClient.getDatabase(CAREER_DB_NAME).drop();
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

			//Start application
			ProcessBuilder builder = new ProcessBuilder("java", "-jar",
					"./target/my-career-app-0.0.1-SNAPSHOT-jar-with-dependencies.jar", "--ui=cli");
			builder.redirectErrorStream(true);
			Process process = builder.start();

			//redirect streams
			OutputStream stdin = process.getOutputStream();
			InputStream stdout = process.getInputStream();
			inp = new BufferedReader(new InputStreamReader(stdout));
			out = new BufferedWriter(new OutputStreamWriter(stdin));

			//waiting for startup
			String line = null;
			line = null;
			boolean initFinished = false;
			while (((line = inp.readLine()) != null) & !initFinished) {
				System.out.println("ProcessOut: " + line);
				if (line.contains("Enter a valid digit")) {
					initFinished = true;
					return;
				}
			}

		} catch (IOException e1) {
			e1.printStackTrace();
		}

	}

	@After
	public void onTearDown() {
		mongoClient.close();
	}
	
	//add student

	@Test
	public void testInsertingNewStudentSuccess() throws IOException {
		//Exercise
		String inputResult = sendInputAndGetMultipleOutput("1\n" + STUDENT_ID_3 + "\n" + STUDENT_NAME_3);
		//Verify
		assertThat(inputResult).hasToString("Insert id: Insert name: Student account created : Student [id="
				+ STUDENT_ID_3 + ", name=" + STUDENT_NAME_3 + "]");
	}

	@Test
	public void testInsertingNewStudentFail() throws IOException, InterruptedException {
		//Exercise
		String inputResult = sendInputAndGetMultipleOutput("1\n" + STUDENT_ID_1 + "\n" + STUDENT_NAME_1);
		//Verify
		assertThat(inputResult).hasToString("Insert id: Insert name: ERROR! Already exists a student with id "
				+ STUDENT_ID_1 + " : Student [id=" + STUDENT_ID_1 + ", name=" + STUDENT_NAME_1 + "]");
	}
	
	//get students

	@Test
	public void testGetAllStudentsWithSomethingInDB() throws IOException, InterruptedException {
		//Exercise
		String inputResult = sendInputAndGetMultipleOutput("2");
		//Verify
		assertThat(inputResult).hasToString("Student [id=" + STUDENT_ID_1 + ", name=" + STUDENT_NAME_1 + "]Student [id="
				+ STUDENT_ID_2 + ", name=" + STUDENT_NAME_2 + "]");
	}

	@Test
	public void testGetAllStudentsWithNothingInDB() throws IOException, InterruptedException {
		//Setup
		removeTestStudentFromDatabase(STUDENT_ID_1);
		removeTestStudentFromDatabase(STUDENT_ID_2);
		//Exercise
		String inputResult = sendInputAndGetMultipleOutput("2");
		//Verify
		assertThat(inputResult).hasToString("");
	}
	
	//delete student

	@Test
	public void testDeleteStudentSuccess() throws IOException, InterruptedException {
		//Exercise
		String inputResult = sendInputAndGetMultipleOutput("3\n" + STUDENT_ID_1 + "\n" + STUDENT_NAME_1);
		//Verify
		assertThat(inputResult).hasToString("Insert id: Insert name: Student account deleted : Student [id="
				+ STUDENT_ID_1 + ", name=" + STUDENT_NAME_1 + "]");
	}

	@Test
	public void testDeleteStudentError() throws IOException, InterruptedException {
		//Exercise
		String inputResult = sendInputAndGetMultipleOutput("3\n" + STUDENT_ID_3 + "\n" + STUDENT_NAME_3);
		//Verify
		assertThat(inputResult).hasToString("Insert id: Insert name: ERROR! Student does not exist : Student [id="
				+ STUDENT_ID_3 + ", name=" + STUDENT_NAME_3 + "]");
	}
	
	//get course by student id

	@Test
	public void testGetCoursesByStudentIdSuccess() throws IOException, InterruptedException {
		//Exercise
		String inputResult = sendInputAndGetMultipleOutput("4\n" + STUDENT_ID_1 + "\n" + STUDENT_NAME_1);
		//Verify
		assertThat(inputResult).hasToString("Insert id: Insert name: Course [id=" + COURSE_ID_1 + ", name="
				+ COURSE_NAME_1 + ", cfu=" + COURSE_CFU_1 + "]");
	}

	@Test
	public void testGetCoursesByStudentIdFail() throws IOException, InterruptedException {
		//Exercise
		String inputResult = sendInputAndGetMultipleOutput("4\n" + STUDENT_ID_3 + "\n" + STUDENT_NAME_3);
		//Verify
		assertThat(inputResult).hasToString("Insert id: Insert name: ERROR! Student does not exist : Student [id="
				+ STUDENT_ID_3 + ", name=" + STUDENT_NAME_3 + "]");
	}
	
	// add subscription

	@Test
	public void testAddCourseSubscriptionSuccess() throws InterruptedException {
		//Exercise 
		String inputResult = sendInputAndGetMultipleOutput("5\n" + STUDENT_ID_1 + "\n" + STUDENT_NAME_1 + "\n"
				+ COURSE_ID_2 + "\n" + COURSE_NAME_2 + "\n" + COURSE_CFU_2);
		//Verify
		assertThat(inputResult).hasToString(
				"Insert student id: Insert student name: Insert course id: Insert course name: Insert course CFU: Added a new course : Course [id="
						+ COURSE_ID_2 + ", name=" + COURSE_NAME_2 + ", cfu=" + COURSE_CFU_2 + "]");
	}

	@Test
	public void testAddCourseSubscriptionWrongStudent() throws InterruptedException {
		//Exercise
		String inputResult = sendInputAndGetMultipleOutput("5\n" + STUDENT_ID_3 + "\n" + STUDENT_NAME_3 + "\n"
				+ COURSE_ID_2 + "\n" + COURSE_NAME_2 + "\n" + COURSE_CFU_2);
		//Verify
		assertThat(inputResult).hasToString(
				"Insert student id: Insert student name: Insert course id: Insert course name: Insert course CFU: ERROR! The student does not exist : Course [id="
						+ COURSE_ID_2 + ", name=" + COURSE_NAME_2 + ", cfu=" + COURSE_CFU_2 + "]");
	}
	
	//remove course subscription

	@Test
	public void testRemoveCourseSubscriptionSuccess() throws InterruptedException {
		//Exercise
		String inputResult = sendInputAndGetMultipleOutput("6\n" + STUDENT_ID_1 + "\n" + STUDENT_NAME_1 + "\n"
				+ COURSE_ID_1 + "\n" + COURSE_NAME_1 + "\n" + COURSE_CFU_1);
		//Verify
		assertThat(inputResult).hasToString(
				"Insert student id: Insert student name: Insert course id: Insert course name: Insert course CFU: The course has been removed : Course [id="
						+ COURSE_ID_1 + ", name=" + COURSE_NAME_1 + ", cfu=" + COURSE_CFU_1 + "]");
	}

	@Test
	public void testRemoveCourseSubscriptionFailNoStudent() throws InterruptedException {
		//Exercise
		String inputResult = sendInputAndGetMultipleOutput("6\n" + STUDENT_ID_3 + "\n" + STUDENT_NAME_3 + "\n"
				+ COURSE_ID_1 + "\n" + COURSE_NAME_1 + "\n" + COURSE_CFU_1);
		//Verify
		assertThat(inputResult).hasToString(
				"Insert student id: Insert student name: Insert course id: Insert course name: Insert course CFU: ERROR! The student does not exist : Course [id="
						+ COURSE_ID_1 + ", name=" + COURSE_NAME_1 + ", cfu=" + COURSE_CFU_1 + "]");
	}
	
	//get students by course participation

	@Test
	public void testGetStudentsByCourseWithAllInDB() throws InterruptedException {
		//Exercise
		String inputResult = sendInputAndGetMultipleOutput(
				"7\n" + COURSE_ID_1 + "\n" + COURSE_NAME_1 + "\n" + COURSE_CFU_1);
		//Verify
		assertThat(inputResult).hasToString(
				"Insert course id: Insert course name: Insert course CFU: Student [id=" + STUDENT_ID_1 + ", name="
						+ STUDENT_NAME_1 + "]Student [id=" + STUDENT_ID_2 + ", name=" + STUDENT_NAME_2 + "]");

	}

	@Test
	public void testGetStudentsByWrongCourse() throws InterruptedException {
		//Exercise
		String inputResult = sendInputAndGetMultipleOutput(
				"7\n" + COURSE_ID_3 + "\n" + COURSE_NAME_3 + "\n" + COURSE_CFU_3);
		//Verify
		assertThat(inputResult).hasToString(
				"Insert course id: Insert course name: Insert course CFU: ERROR! The course does not exist : Course [id="
						+ COURSE_ID_3 + ", name=" + COURSE_NAME_3 + ", cfu=" + COURSE_CFU_3 + "]");
	}
	
	//exit

	@Test
	public void testExit() throws InterruptedException, IOException {
		//Exercise
		out.write("8\n");
		out.flush();
		out.close();
		String inputResult = inp.readLine();
		//Verify
		assertThat(inputResult).hasToString("Goodbye");
	}

	private static String sendInputAndGetMultipleOutput(String msg) {
		String result = "";
		try {
			out.write(msg + "\n");
			out.flush();
			out.close();
			String line = null;
			while (((line = inp.readLine()) != null) & !line.equalsIgnoreCase("Student Section")) {
				result = result + line;
			}
			return result;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "";
	}

	private void addTestStudentToDatabaseWithParticipations(String studentId, String studentName,
			List<String> participations) {
		mongoClient.getDatabase(CAREER_DB_NAME).getCollection(STUDENTS_COLLECTION_NAME).insertOne(new Document()
				.append(NAME, studentName).append(ID, studentId).append("participations", participations));
	}

	private void addTestCourseToDatabaseWithParticipants(String courseId, String courseName, int cfu,
			List<String> participants) {
		mongoClient.getDatabase(CAREER_DB_NAME).getCollection(COURSES_COLLECTION_NAME).insertOne(new Document()
				.append(ID, courseId).append(NAME, courseName).append(CFU, cfu).append("participants", participants));
	}

	private void removeTestStudentFromDatabase(String id) {
		mongoClient.getDatabase(CAREER_DB_NAME).getCollection(STUDENTS_COLLECTION_NAME).deleteOne(Filters.eq("id", id));
	}

}