package it.unifi.stud.my_career.app;

import static org.assertj.swing.launcher.ApplicationLauncher.*;
import static org.assertj.core.api.Assertions.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.ProcessBuilder.Redirect;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import org.assertj.swing.finder.WindowFinder;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.junit.runner.GUITestRunner;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.assertj.swing.launcher.ApplicationLauncher;
import org.assertj.swing.annotation.GUITest;
import org.assertj.swing.core.GenericTypeMatcher;
import org.assertj.swing.core.matcher.JButtonMatcher;

import org.bson.Document;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.testcontainers.containers.GenericContainer;

import javax.swing.JFrame;

import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import com.mongodb.client.model.Filters;

import it.unifi.stud.my_career.model.Course;
import it.unifi.stud.my_career.model.Student;

public class MyCareerAppCLIE2E {

	private static final String CAREER_DB_NAME = "career";
	private static final String STUDENTS_COLLECTION_NAME = "students";
	private static final String COURSES_COLLECTION_NAME = "courses";

	private static final String CFU = "cfu";
	private static final String NAME = "name";
	private static final String ID = "id";

	private static final int sleepTime = 10;

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

	// TODO add tests for cfu regex?

	@Before
	public void onSetUp() {

		try {
			// FIXME forse da fare con process builder

			mongoProcess = Runtime.getRuntime().exec("docker run -p 27017:27017 --detach --rm krnbr/mongo:4.2.6");
			InputStream is = mongoProcess.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));
			String line = null;
			while ((line = reader.readLine()) != null) {
				System.out.println("mongoID:" + line + "-");
				mongoTestContainerId = line;
			}

			ProcessBuilder builder = new ProcessBuilder("java", "-jar",
					"/home/davide/Downloads/my-career-app-FIXED.jar", "--ui=cli"); // FIXME get the right place

			builder.redirectErrorStream(true);

			Process process = builder.start();
			OutputStream stdin = process.getOutputStream();
			InputStream stdout = process.getInputStream();

			inp = new BufferedReader(new InputStreamReader(stdout));
			out = new BufferedWriter(new OutputStreamWriter(stdin));

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
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

	}

	@After
	public void onTearDown() {
		try {
			// FIXME forse da fare con process builder
			Runtime.getRuntime().exec("docker kill " + mongoTestContainerId);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mongoProcess.destroy();
	}

	@Test
	public void testInsertingNewStudentSuccess() throws IOException {
		String inputResult = sendInputAndGetOutput("1\n" + STUDENT_ID_1 + "\n" + STUDENT_NAME_1);
		System.out.println(inputResult);
		assertThat(inputResult).hasToString("Insert id: Insert name: Student account created : Student [id="
				+ STUDENT_ID_1 + ", name=" + STUDENT_NAME_1 + "]");
	}

	@Test
	public void testInsertingNewStudentFail() throws IOException, InterruptedException {
		TimeUnit.SECONDS.sleep(sleepTime); // FIXME magari possiamo aspettare non un tempo ma un evento...
		addTestStudents();
		String inputResult = sendInputAndGetOutput("1\n" + STUDENT_ID_1 + "\n" + STUDENT_NAME_1);
		assertThat(inputResult).hasToString("Insert id: Insert name: ERROR! Already exists a student with id "
				+ STUDENT_ID_1 + " : Student [id=" + STUDENT_ID_1 + ", name=" + STUDENT_NAME_1 + "]");
	}

	@Test
	public void testGetAllStudentsWithSomethingInDB() throws IOException, InterruptedException {
		TimeUnit.SECONDS.sleep(sleepTime);
		addTestStudents();
		String inputResult = sendInputAndGetMultipleOutput("2");
		assertThat(inputResult).hasToString("Student [id=" + STUDENT_ID_1 + ", name=" + STUDENT_NAME_1 + "]Student [id="
				+ STUDENT_ID_2 + ", name=" + STUDENT_NAME_2 + "]");
	}

	@Test
	public void testGetAllStudentsWithNothingInDB() throws IOException, InterruptedException {
		String inputResult = sendInputAndGetMultipleOutput("2");
		assertThat(inputResult).hasToString("");
	}

	@Test
	public void testDeleteStudentSuccess() throws IOException, InterruptedException {
		TimeUnit.SECONDS.sleep(sleepTime);
		addTestStudents();
		String inputResult = sendInputAndGetOutput("3\n" + STUDENT_ID_1 + "\n" + STUDENT_NAME_1);
		assertThat(inputResult).hasToString("Insert id: Insert name: Student account deleted : Student [id="
				+ STUDENT_ID_1 + ", name=" + STUDENT_NAME_1 + "]");
	}

	@Test
	public void testDeleteStudentError() throws IOException, InterruptedException {
		TimeUnit.SECONDS.sleep(sleepTime);
		String inputResult = sendInputAndGetOutput("3\n" + STUDENT_ID_1 + "\n" + STUDENT_NAME_1);
		assertThat(inputResult).hasToString("Insert id: Insert name: ERROR! Student does not exist : Student [id="
				+ STUDENT_ID_1 + ", name=" + STUDENT_NAME_1 + "]");
	}

	@Test
	public void testGetCoursesByStudentIdSuccess() throws IOException, InterruptedException {
		// FIXME questo non restituisce nulla
		TimeUnit.SECONDS.sleep(sleepTime);
		addTestStudents();
		addTestCourses();
		String inputResult = sendInputAndGetOutput("4\n" + STUDENT_ID_1 + "\n" + STUDENT_NAME_1);
		System.out.println("-" + inputResult + "-");
		assertThat(inputResult).hasToString("");
	}

	@Test
	public void testGetCoursesByStudentIdFail() throws IOException, InterruptedException {
		TimeUnit.SECONDS.sleep(sleepTime);
		String inputResult = sendInputAndGetMultipleOutput("4\n" + STUDENT_ID_1 + "\n" + STUDENT_NAME_1);
		assertThat(inputResult).hasToString("Insert id: Insert name: ERROR! Student does not exist : Student [id="
				+ STUDENT_ID_1 + ", name=" + STUDENT_NAME_1 + "]");
	}

	@Test
	public void testGetCoursesOfAStudentWithNoCourses() throws IOException, InterruptedException {
		// TODO
	}

	@Test
	public void testAddCourseSubscriptionSuccess() throws InterruptedException {
		TimeUnit.SECONDS.sleep(sleepTime);
		addTestStudents();
		addTestCourses();
		String inputResult = sendInputAndGetMultipleOutput("5\n" + STUDENT_ID_1 + "\n" + STUDENT_NAME_1 + "\n"
				+ COURSE_ID_2 + "\n" + COURSE_NAME_2 + "\n" + COURSE_CFU_2);
		assertThat(inputResult).hasToString(
				"Insert student id: Insert student name: Insert course id: Insert course name: Insert course CFU: Added a new course : Course [id="
						+ COURSE_ID_2 + ", name=" + COURSE_NAME_2 + ", cfu=" + COURSE_CFU_2 + "]");
	}

	@Test
	public void testAddCourseSubscriptionWrongStudent() throws InterruptedException {
		TimeUnit.SECONDS.sleep(sleepTime);
		addTestStudents();
		addTestCourses();
		String inputResult = sendInputAndGetMultipleOutput("5\n" + STUDENT_ID_3 + "\n" + STUDENT_NAME_3 + "\n"
				+ COURSE_ID_2 + "\n" + COURSE_NAME_2 + "\n" + COURSE_CFU_2);
		assertThat(inputResult).hasToString(
				"Insert student id: Insert student name: Insert course id: Insert course name: Insert course CFU: ERROR! The student does not exist : Course [id="
						+ COURSE_ID_2 + ", name=" + COURSE_NAME_2 + ", cfu=" + COURSE_CFU_2 + "]");

	}

	@Test
	public void testAddCourseSubscriptionStudentAlreadySubscribed() throws InterruptedException {
		// FIXME non è mai stato testato!?
		TimeUnit.SECONDS.sleep(sleepTime);
		addTestStudents();
		addTestCourses();
		String inputResult = sendInputAndGetMultipleOutput("5\n" + STUDENT_ID_1 + "\n" + STUDENT_NAME_1 + "\n"
				+ COURSE_ID_1 + "\n" + COURSE_NAME_1 + "\n" + COURSE_CFU_1);
		System.out.println("-" + inputResult + "-");
		assertThat(inputResult).hasToString(
				"");

	}

	@Test
	public void testRemoveCourseSubscriptionSuccess() throws InterruptedException {
		// FIXME
		TimeUnit.SECONDS.sleep(sleepTime);
		addTestStudents();
		addTestCourses();
		String inputResult = sendInputAndGetMultipleOutput("6\n" + STUDENT_ID_1 + "\n" + STUDENT_NAME_1 + "\n"
				+ COURSE_ID_1 + "\n" + COURSE_NAME_1 + "\n" + COURSE_CFU_1);
		System.out.println("-" + inputResult + "-");
		assertThat(inputResult).hasToString("");

	}

	@Test
	public void testRemoveCourseSubscriptionFailNoStudent() throws InterruptedException {
		TimeUnit.SECONDS.sleep(sleepTime);
		addTestStudents();
		addTestCourses();
		String inputResult = sendInputAndGetMultipleOutput("6\n" + STUDENT_ID_3 + "\n" + STUDENT_NAME_3 + "\n"
				+ COURSE_ID_1 + "\n" + COURSE_NAME_1 + "\n" + COURSE_CFU_1);
		assertThat(inputResult).hasToString(
				"Insert student id: Insert student name: Insert course id: Insert course name: Insert course CFU: ERROR! The student does not exist : Course [id="
						+ COURSE_ID_1 + ", name=" + COURSE_NAME_1 + ", cfu=" + COURSE_CFU_1 + "]");
	}

	@Test
	public void testRemoveCourseSubscriptionFailNoCourse() throws InterruptedException {
		TimeUnit.SECONDS.sleep(sleepTime);
		addTestStudents();
		addTestCourses();
		String inputResult = sendInputAndGetMultipleOutput("6\n" + STUDENT_ID_1 + "\n" + STUDENT_NAME_1 + "\n"
				+ COURSE_ID_3 + "\n" + COURSE_NAME_3 + "\n" + COURSE_CFU_3);
		assertThat(inputResult).hasToString(
				"Insert student id: Insert student name: Insert course id: Insert course name: Insert course CFU: ERROR! The course does not exist : Course [id="
						+ COURSE_ID_3 + ", name=" + COURSE_NAME_3 + ", cfu=" + COURSE_CFU_3 + "]");
	}

	@Test
	public void testGetStudentsByCourseWithAllInDB() throws InterruptedException {
		// FIXME
		TimeUnit.SECONDS.sleep(sleepTime);
		addTestStudents();
		addTestCourses();
		String inputResult = sendInputAndGetMultipleOutput(
				"7\n" + COURSE_ID_1 + "\n" + COURSE_NAME_1 + "\n" + COURSE_CFU_1);
		System.out.println("-" + inputResult + "-");
		assertThat(inputResult).hasToString(
				"");

	}

	@Test
	public void testGetStudentsByCourseWithoutStudentsInDB() throws InterruptedException {
		// FIXME
		TimeUnit.SECONDS.sleep(sleepTime);
		addTestCourses();
		String inputResult = sendInputAndGetMultipleOutput(
				"7\n" + COURSE_ID_1 + "\n" + COURSE_NAME_1 + "\n" + COURSE_CFU_1);
		System.out.println("-" + inputResult + "-");
		assertThat(inputResult).hasToString(
				"");

	}

	@Test
	public void testGetStudentsByCourseWithoutCourseInDB() throws InterruptedException {
		// FIXME
		TimeUnit.SECONDS.sleep(sleepTime);
		addTestStudents();
		String inputResult = sendInputAndGetMultipleOutput(
				"7\n" + COURSE_ID_1 + "\n" + COURSE_NAME_1 + "\n" + COURSE_CFU_1);
		System.out.println("-" + inputResult + "-");
		assertThat(inputResult).hasToString(
				"");
	}

	@Test
	public void testExit() throws InterruptedException {
		// FIXME
		TimeUnit.SECONDS.sleep(sleepTime);
		String inputResult = sendInputAndGetMultipleOutput("8\n");
		System.out.println("-" + inputResult + "-");
		assertThat(inputResult).hasToString("");

	}

	private void addTestStudents() {
		try {

			// This method ads student1 and student2 to db
			Process r = Runtime.getRuntime().exec("mongoimport -d " + CAREER_DB_NAME + " -c " + STUDENTS_COLLECTION_NAME
					+ " --file /home/davide/importStudents.json");

			InputStream is = r.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));
			String line = null;
			while ((line = reader.readLine()) != null) {
				System.out.println("insertion:" + line + "-");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void addTestCourses() {
		try {

			// This method ads course1 and course2 to db
			Process r = Runtime.getRuntime().exec("mongoimport -d " + CAREER_DB_NAME + " -c " + COURSES_COLLECTION_NAME
					+ " --file /home/davide/importCourses.json");
			InputStream is = r.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));
			String line = null;
			while ((line = reader.readLine()) != null) {
				System.out.println("insertion:" + line + "-");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static String sendInputAndGetOutput(String msg) {
		String result;
		try {
			out.write(msg + "\n");
			out.flush();
			out.close();
			result = inp.readLine();
			return result;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}

	public static String sendInputAndGetMultipleOutput(String msg) {
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}

}
