package it.unifi.stud.my_career.view.cli;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import it.unifi.stud.my_career.controller.MyCareerController;
import it.unifi.stud.my_career.model.Course;
import it.unifi.stud.my_career.model.Student;

public class MyCareerCLIViewTest {

	private static final String COURSE_NAME_1 = "APT";
	private static final String COURSE_ID_1 = "123";
	private static final String COURSE_NAME_2 = "BDA";
	private static final String COURSE_ID_2 = "456";
	private static final String COURSE_NAME_3 = "ML";
	private static final String COURSE_ID_3 = "789";
	private static final String COURSE_NAME_4 = "HCI";
	private static final String COURSE_ID_4 = "101";
	
	private static final String STUDENT_NAME_1 = "test1";
	private static final String STUDENT_ID_1 = "1";
	private static final String STUDENT_NAME_2 = "test2";
	private static final String STUDENT_ID_2 = "2";
	private static final String STUDENT_NAME_3 = "test3";
	private static final String STUDENT_ID_3 = "3";
	private static final String STUDENT_NAME_4 = "test4";
	private static final String STUDENT_ID_4 = "4";

	@Mock
	private MyCareerController myCareerController;

	// SUT
	@InjectMocks
	private MyCareerCLIView myCareerCLIView;

	private ByteArrayOutputStream testOut;
	private ByteArrayInputStream testin;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		testOut = new ByteArrayOutputStream();
		myCareerCLIView = new MyCareerCLIView(System.in, new PrintStream(testOut));
		myCareerCLIView.setMyCareerController(myCareerController);
	}

	@Test
	public void testShowMenuShouldPrintMenu() {
		// Exercise
		myCareerCLIView.showMenu();
		// Verify
		assertThat(testOut.toString()).hasToString(
				"" + "Student Section\n" + "1) Add a student\n" + "2) Get all students\n" + "3) Delete a student\n"
						+ "4) Get courses (by student)\n" + "Course Section\n" + "5) Add a course subscription\n"
						+ "6) Remove a course subscription\n" + "7) Get students (by course)\n" + "8) Exit\n");
	}

	@Test
	public void testShowStudentErrorShouldShowAMessageInOutputStream() {
		// Setup
		Student student = new Student(STUDENT_ID_1, STUDENT_NAME_1);
		String errorMessage = "error message";
		// Exercise
		myCareerCLIView.showStudentError(errorMessage, student);
		// Verify
		assertThat(testOut.toString()).hasToString("ERROR! error message : " + student + "\n");
	}

	@Test
	public void testShowAllStudentWithEmptyInitialListShouldAddStudentDescriptionsToTheList() {
		// Setup
		Student student1 = new Student(STUDENT_ID_1, STUDENT_NAME_1);
		Student student2 = new Student(STUDENT_ID_2, STUDENT_NAME_2);
		// Exercise
		myCareerCLIView.showAllStudents(asList(student1, student2));
		// Verify
		assertThat(myCareerCLIView.getStudentsList()).containsExactly(student1, student2);
		assertThat(testOut.toString()).hasToString("Student [id=1, name=test1]\nStudent [id=2, name=test2]\n");
	}

	@Test
	public void testShowAllStudentWithNonEmptyListShouldRefreshTheList() {
		// Setup
		Student student1 = new Student(STUDENT_ID_1, STUDENT_NAME_1);
		Student student2 = new Student(STUDENT_ID_2, STUDENT_NAME_2);
		Student student3 = new Student(STUDENT_ID_3, STUDENT_NAME_3);
		Student student4 = new Student(STUDENT_ID_4, STUDENT_NAME_4);
		myCareerCLIView.getStudentsList().addAll(asList(student1, student2));
		// Exercise
		myCareerCLIView.showAllStudents(asList(student3, student4));
		// Verify
		assertThat(myCareerCLIView.getStudentsList()).containsExactly(student3, student4);
		assertThat(testOut.toString()).hasToString("Student [id=3, name=test3]\nStudent [id=4, name=test4]\n");
	}

	@Test
	public void testStudentAddedShouldAddTheStudentToTheListAndShowAMessage() {
		// Setup
		Student student1 = new Student(STUDENT_ID_1, STUDENT_NAME_1);
		Student student2 = new Student(STUDENT_ID_2, STUDENT_NAME_2);
		myCareerCLIView.getStudentsList().add(student1);
		// Exercise
		myCareerCLIView.studentAdded("Student added", student2);
		// Verify
		assertThat(myCareerCLIView.getStudentsList()).containsExactly(student1, student2);
		assertThat(testOut.toString()).hasToString("Student added : " + student2 + "\n");
	}

	@Test
	public void testStudentRemovedShouldRemoveTheStudentFromTheListAndShowAMessage() {
		// Setup
		Student student1 = new Student(STUDENT_ID_1, STUDENT_NAME_1);
		Student student2 = new Student(STUDENT_ID_2, STUDENT_NAME_2);
		myCareerCLIView.getStudentsList().addAll(asList(student1, student2));
		// Exercise
		myCareerCLIView.studentRemoved("Student removed", student2);
		// Verify
		assertThat(myCareerCLIView.getStudentsList()).containsExactly(student1);
		assertThat(testOut.toString()).hasToString("Student removed : " + student2 + "\n");
	}

	@Test
	public void testShowCourseErrorShouldShowTheMessageInTheOutputStream() {
		// Setup
		Course course = new Course(COURSE_ID_1, COURSE_NAME_1, 6);
		String errorMessage = "error message";
		// Exercise
		myCareerCLIView.showCourseError(errorMessage, course);
		// Verify
		assertThat(testOut.toString()).hasToString("ERROR! error message : " + course + "\n");
	}

	@Test
	public void testShowAllCoursesWithInitialEmptyListShouldAddCourseDescriptionsToTheList() {
		// Setup
		Course course1 = new Course(COURSE_ID_1, COURSE_NAME_1, 6);
		Course course2 = new Course(COURSE_ID_2, COURSE_NAME_2, 9);
		// Exercise
		myCareerCLIView.showAllCourses(asList(course1, course2));
		// Verify
		assertThat(myCareerCLIView.getCoursesList()).containsExactly(course1, course2);
		assertThat(testOut.toString())
				.hasToString("Course [id=123, name=APT, cfu=6]\nCourse [id=456, name=BDA, cfu=9]\n");
	}

	@Test
	public void testShowAllCoursesWithNonEmptyListShouldRefreshTheList() {
		// Setup
		Course course1 = new Course(COURSE_ID_1, COURSE_NAME_1, 6);
		Course course2 = new Course(COURSE_ID_2, COURSE_NAME_2, 9);
		Course course3 = new Course(COURSE_ID_3, COURSE_NAME_3, 9);
		Course course4 = new Course(COURSE_ID_4, COURSE_NAME_4, 6);
		myCareerCLIView.getCoursesList().addAll(asList(course1, course2));
		// Exercise
		myCareerCLIView.showAllCourses(asList(course3, course4));
		// Verify
		assertThat(myCareerCLIView.getCoursesList()).containsExactly(course3, course4);
		assertThat(testOut.toString())
				.hasToString("Course [id=789, name=ML, cfu=9]\nCourse [id=101, name=HCI, cfu=6]\n");
	}

	@Test
	public void testCourseAddedShouldAddTheCourseToTheListAndShowAMessage() {
		// Setup
		Course course1 = new Course(COURSE_ID_1, COURSE_NAME_1, 6);
		Course course2 = new Course(COURSE_ID_2, COURSE_NAME_2, 9);
		myCareerCLIView.getCoursesList().add(course1);
		// Exercise
		myCareerCLIView.courseAdded("Course added", course2);
		// Verify
		assertThat(myCareerCLIView.getCoursesList()).containsExactly(course1, course2);
		assertThat(testOut.toString()).hasToString("Course added : " + course2 + "\n");
	}

	@Test
	public void testCourseRemovedShouldRemoveTheCourseFromTheListAndShowAMessage() {
		// Setup
		Course course1 = new Course(COURSE_ID_1, COURSE_NAME_1, 6);
		Course course2 = new Course(COURSE_ID_2, COURSE_NAME_2, 9);
		myCareerCLIView.getCoursesList().addAll(asList(course1, course2));
		// Exercise
		myCareerCLIView.courseRemoved("Course removed", course2);
		// Verify
		assertThat(myCareerCLIView.getCoursesList()).containsExactly(course1);
		assertThat(testOut.toString()).hasToString("Course removed : " + course2 + "\n");
	}

	// Test user interaction

	@Test
	public void testAddStudentShouldTakeInputsAndDelegateToControllerNewStudent() {
		// Setup
		String userInput = "1\n1\ntest1";
		testin = new ByteArrayInputStream(userInput.getBytes());
		myCareerCLIView.setInputStream(testin);
		// Exercise
		int rValue = myCareerCLIView.exec();
		// Verify
		assertThat(testOut.toString()).contains("Insert id: Insert name: ");
		verify(myCareerController).addStudent(new Student(STUDENT_ID_1, STUDENT_NAME_1));
		assertThat(rValue).isEqualTo(1);

	}

	@Test
	public void testGetAllStudentsShouldDelegateToControllerGeAllStudents() {
		// Setup
		String userInput = "2";
		testin = new ByteArrayInputStream(userInput.getBytes());
		myCareerCLIView.setInputStream(testin);
		// Exercise
		int rValue = myCareerCLIView.exec();
		// Verify
		verify(myCareerController).getAllStudents();
		assertThat(rValue).isEqualTo(2);
	}

	@Test
	public void testDeleteStudentShouldTakeInpustAndDelegateToControllerDeleteStudent() {
		// Setup
		String userInput = "3\n1\ntest1";
		testin = new ByteArrayInputStream(userInput.getBytes());
		myCareerCLIView.setInputStream(testin);
		// Exercise
		int rValue = myCareerCLIView.exec();
		// Verify
		assertThat(testOut.toString()).contains("Insert id: Insert name: ");
		verify(myCareerController).deleteStudent(new Student(STUDENT_ID_1, STUDENT_NAME_1));
		assertThat(rValue).isEqualTo(3);
	}

	@Test
	public void testGetCoursesByStudentShouldTakeInputsAndDelegateToControllerGetCoursesByStudent() {
		// Setup
		String userInput = "4\n1\ntest1";
		testin = new ByteArrayInputStream(userInput.getBytes());
		myCareerCLIView.setInputStream(testin);
		// Exercise
		int rValue = myCareerCLIView.exec();
		// Verify
		assertThat(testOut.toString()).contains("Insert id: Insert name: ");
		verify(myCareerController).getCoursesByStudent(new Student(STUDENT_ID_1, STUDENT_NAME_1));
		assertThat(rValue).isEqualTo(4);
	}

	@Test
	public void testAddCourseShouldTakeInputsAndDelegateToControllerAddCourse() {
		// Setup
		String userInput = "5\n1\ntest1\n123\nAPT\n6";
		testin = new ByteArrayInputStream(userInput.getBytes());
		myCareerCLIView.setInputStream(testin);
		// Exercise
		int rValue = myCareerCLIView.exec();
		// Verify
		assertThat(testOut.toString()).contains(
				"Insert student id: Insert student name: Insert course id: Insert course name: Insert course CFU: ");
		verify(myCareerController).addCourse(new Student(STUDENT_ID_1, STUDENT_NAME_1),
				new Course(COURSE_ID_1, COURSE_NAME_1, 6));
		assertThat(rValue).isEqualTo(5);
	}

	@Test
	public void testAddCourseWhenCFUIsNotANumberShouldNotCallController() {
		// Setup
		String userInput = "5\n1\ntest1\n123\nAPT\nError";
		testin = new ByteArrayInputStream(userInput.getBytes());
		myCareerCLIView.setInputStream(testin);
		// Exercise
		int rValue = myCareerCLIView.exec();
		// Verify
		assertThat(testOut.toString()).contains(
				"Insert student id: Insert student name: Insert course id: Insert course name: Insert course CFU: CFU value must be a number\n");
		verify(myCareerController, never()).addCourse(new Student(STUDENT_ID_1, STUDENT_NAME_1),
				new Course(COURSE_ID_1, COURSE_NAME_1, 6));
		assertThat(rValue).isEqualTo(5);
	}

	@Test
	public void testRemoveCourseShouldDelegateToControllerRemoveController() {
		// Setup
		String userInput = "6\n1\ntest1\n123\nAPT\n6";
		testin = new ByteArrayInputStream(userInput.getBytes());
		myCareerCLIView.setInputStream(testin);
		// Exercise
		int rValue = myCareerCLIView.exec();
		// Verify
		assertThat(testOut.toString()).contains(
				"Insert student id: Insert student name: Insert course id: Insert course name: Insert course CFU: ");
		verify(myCareerController).removeCourse(new Student(STUDENT_ID_1, STUDENT_NAME_1),
				new Course(COURSE_ID_1, COURSE_NAME_1, 6));
		assertThat(rValue).isEqualTo(6);
	}

	@Test
	public void testRemoveCourseWhenCFUIsNotANumberShouldNotCallController() {
		// Setup
		String userInput = "6\n1\ntest1\n123\nAPT\nError";
		testin = new ByteArrayInputStream(userInput.getBytes());
		myCareerCLIView.setInputStream(testin);
		// Exercise
		int rValue = myCareerCLIView.exec();
		// Verify
		assertThat(testOut.toString()).contains(
				"Insert student id: Insert student name: Insert course id: Insert course name: Insert course CFU: CFU value must be a number\n");
		verify(myCareerController, never()).removeCourse(new Student(STUDENT_ID_1, STUDENT_NAME_1),
				new Course(COURSE_ID_1, COURSE_NAME_1, 6));
		assertThat(rValue).isEqualTo(6);
	}

	@Test
	public void testGetStudentsByCourseShouldTakeInputsAndDelegateToControllerGetStudentsByCourse() {
		// Setup
		String userInput = "7\n123\nAPT\n6";
		testin = new ByteArrayInputStream(userInput.getBytes());
		myCareerCLIView.setInputStream(testin);
		// Exercise
		int rValue = myCareerCLIView.exec();
		// Verify
		assertThat(testOut.toString()).contains("Insert course id: Insert course name: Insert course CFU: ");
		verify(myCareerController).getStudentsByCourse(new Course(COURSE_ID_1, COURSE_NAME_1, 6));
		assertThat(rValue).isEqualTo(7);
	}

	@Test
	public void testGetStudentsByCourseWhenCFUIsNotANumberShouldNotCallController() {
		// Setup
		String userInput = "7\n123\nAPT\nError";
		testin = new ByteArrayInputStream(userInput.getBytes());
		myCareerCLIView.setInputStream(testin);
		// Exercise
		int rValue = myCareerCLIView.exec();
		// Verify
		assertThat(testOut.toString())
				.contains("Insert course id: Insert course name: Insert course CFU: CFU value must be a number\n");
		verify(myCareerController, never()).getStudentsByCourse(new Course(COURSE_ID_1, COURSE_NAME_1, 6));
		assertThat(rValue).isEqualTo(7);
	}

	@Test
	public void testExit() {
		// Setup
		String userInput = "8";
		testin = new ByteArrayInputStream(userInput.getBytes());
		myCareerCLIView.setInputStream(testin);
		// Exercise
		int rValue = myCareerCLIView.exec();
		// Verify
		assertThat(testOut.toString()).contains("Goodbye");
		assertThat(rValue).isEqualTo(8);
	}

	@Test
	public void testWrongInput() {
		// Setup
		String userInput = "Error";
		testin = new ByteArrayInputStream(userInput.getBytes());
		myCareerCLIView.setInputStream(testin);
		// Exercise
		int rValue = myCareerCLIView.exec();
		// Verify
		assertThat(testOut.toString()).contains("Not a valid input");
		assertThat(rValue).isEqualTo(-1);
	}
}
