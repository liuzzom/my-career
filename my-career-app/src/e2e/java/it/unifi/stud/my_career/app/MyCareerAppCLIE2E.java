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

	public static BufferedReader inp;
	public static BufferedWriter out;
	public static Process mongoProcess;
	public static String mongoTestContainerId;

	@Before
	public void onSetUp() {

		try {
			// FIXME forse da fare con process builder
			mongoProcess = Runtime.getRuntime().exec("docker run -p 27017:27017 --detach --rm krnbr/mongo:4.2.6");
			// TODO magari sarebbe carino inserire direttamente degli studenti da qui?
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

			Process process;

			process = builder.start();
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
		System.out.println("testingSuccess");
		String inputResult = sendInputAndGetOutput("1\n" + STUDENT_ID_1 + "\n" + STUDENT_NAME_1);
		System.out.println(inputResult);
		assertThat(inputResult).isEqualTo("Insert id: Insert name: Student account created : Student [id="
				+ STUDENT_ID_1 + ", name=" + STUDENT_NAME_1 + "]");
	}

	@Test
	public void testInsertingNewStudentFail() throws IOException {
		System.out.println("testingFail");
		// TODO insert student 1 here
		addTestStudentToDatabase(STUDENT_ID_1, STUDENT_NAME_1);
		String inputResult = sendInputAndGetOutput("1\n" + STUDENT_ID_1 + "\n" + STUDENT_NAME_1);
		assertThat(inputResult).isEqualTo("Insert id: Insert name: ERROR! Already exists a student with id "
				+ STUDENT_ID_1 + " : Student [id=" + STUDENT_ID_1 + ", name=" + STUDENT_NAME_1 + "]");
	}

	private void addTestStudentToDatabase(String studentId, String studentName) {
		try {

			Process r = Runtime.getRuntime().exec("sudo docker exec " + mongoTestContainerId+ " mongo 127.0.0.1/career --eval 'var document = { id : \"986\", name : \"newname\" }; db.students.insert(document);'\n");

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

}
