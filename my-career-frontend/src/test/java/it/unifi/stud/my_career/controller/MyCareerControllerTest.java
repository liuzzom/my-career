package it.unifi.stud.my_career.controller;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import it.unifi.stud.my_career.model.Course;
import it.unifi.stud.my_career.model.Student;
import it.unifi.stud.my_career.service.MyCareerService;
import it.unifi.stud.my_career.view.MyCareerView;

public class MyCareerControllerTest {

	private static final String COURSE_NAME_1 = "APT";
	private static final String COURSE_ID_1 = "123";

	private static final String STUDENT_NAME_1 = "test";
	private static final String STUDENT_ID_1 = "1";

	// Mocking Dependencies
	@Mock
	private MyCareerService myCareerService;

	@Mock
	private MyCareerView myCareerView;

	// SUT
	@InjectMocks
	private MyCareerController myCareerController;

	// Setup
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}

	// getAllStudent Tests

	@Test
	public void testGetAllStudents() {
		// Setup
		List<Student> students = asList(new Student(STUDENT_ID_1, STUDENT_NAME_1));
		when(myCareerService.getAllStudents()).thenReturn(students);
		// Exercise
		myCareerController.getAllStudents();
		// Verify
		verify(myCareerView).showAllStudents(students);
	}

	// addStudent Tests

	@Test
	public void testAddStudentWhenStudentDoesNotExistShouldAddAndNotify() {
		// Setup
		Student student = new Student(STUDENT_ID_1, STUDENT_NAME_1);
		when(myCareerService.findStudent(new Student(STUDENT_ID_1, STUDENT_NAME_1))).thenReturn(null);
		// Exercise
		myCareerController.addStudent(student);
		// Verify
		InOrder inOrder = inOrder(myCareerService, myCareerView);
		inOrder.verify(myCareerService).saveStudent(student);
		inOrder.verify(myCareerView).studentAdded("Student account created", student);
	}

	@Test
	public void testAddStudentWhenStudentDoesExistShouldShowError() {
		// Setup
		Student studentToAdd = new Student(STUDENT_ID_1, STUDENT_NAME_1);
		Student existingStudent = new Student(STUDENT_ID_1, "name");
		when(myCareerService.findStudent(new Student(STUDENT_ID_1, STUDENT_NAME_1))).thenReturn(existingStudent);
		// Exercise
		myCareerController.addStudent(studentToAdd);
		// Verify
		verify(myCareerView).showStudentError("Already exists a student with id 1", existingStudent);
		verifyNoMoreInteractions(ignoreStubs(myCareerService));

	}
	// deleteStudent Tests

	@Test
	public void testDeleteStudentWhenStudentDoesExistShouldDeleteAndShowInfo() {
		// Setup
		Student student = new Student(STUDENT_ID_1, STUDENT_NAME_1);
		when(myCareerService.findStudent(new Student(STUDENT_ID_1, STUDENT_NAME_1))).thenReturn(student);
		// Exercise
		myCareerController.deleteStudent(student);
		// Verify
		InOrder inOrder = inOrder(myCareerService, myCareerView);
		inOrder.verify(myCareerService).deleteStudent(student);
		inOrder.verify(myCareerView).studentRemoved("Student account deleted", student);
	}

	@Test
	public void testDeletStudentWhenStudentDoesNotExistShouldShowError() {
		// Setup
		Student student = new Student(STUDENT_ID_1, STUDENT_NAME_1);
		when(myCareerService.findStudent(new Student(STUDENT_ID_1, STUDENT_NAME_1))).thenReturn(null);
		// Exercise
		myCareerController.deleteStudent(student);
		// Verify
		verify(myCareerView).showStudentError("Student does not exist", student);
		verifyNoMoreInteractions(ignoreStubs(myCareerService));
	}

	// getCoursesByStudent Tests
	@Test
	public void testGetCoursesByStudentWhenStudentDoesExistShouldGetCourses() {
		// Setup
		Student student = new Student(STUDENT_ID_1, STUDENT_NAME_1);
		List<Course> courses = asList(new Course(COURSE_ID_1, COURSE_NAME_1, 6));
		when(myCareerService.findStudent(new Student(STUDENT_ID_1, STUDENT_NAME_1))).thenReturn(student);
		when(myCareerService.getCoursesByStudent(new Student(STUDENT_ID_1, STUDENT_NAME_1))).thenReturn(courses);
		// Exercise
		myCareerController.getCoursesByStudent(student);
		// Verify
		verify(myCareerView).showAllCourses(courses);
	}

	@Test
	public void testGetCoursesByStudentWhenStudentDoesNotExistShouldShowError() {
		// Setup
		Student student = new Student(STUDENT_ID_1, STUDENT_NAME_1);
		when(myCareerService.findStudent(new Student(STUDENT_ID_1, STUDENT_NAME_1))).thenReturn(null);
		// Exercise
		myCareerController.getCoursesByStudent(student);
		// Verify
		verify(myCareerView).showStudentError("Student does not exist", student);
		verifyNoMoreInteractions(ignoreStubs(myCareerService));

	}

	// addCourse Tests

	@Test
	public void testAddCourseWhenCourseDoesNotExistAndStudentExistsShouldAddCourseAndShowInfo() {
		// Setup
		Student student = new Student(STUDENT_ID_1, STUDENT_NAME_1);
		Course course = new Course(COURSE_ID_1, COURSE_NAME_1, 6);
		when(myCareerService.findStudent(new Student(STUDENT_ID_1, STUDENT_NAME_1))).thenReturn(student);
		when(myCareerService.findSingleCourseByStudent(new Student(STUDENT_ID_1, STUDENT_NAME_1),
				new Course(COURSE_ID_1, COURSE_NAME_1, 6))).thenReturn(null);
		// Exercise
		myCareerController.addCourse(student, course);
		// Verify
		InOrder inOrder = inOrder(myCareerService, myCareerView);
		inOrder.verify(myCareerService).saveCourse(student, course);
		inOrder.verify(myCareerView).courseAdded("Added a new course", course);
	}

	@Test
	public void testAddCourseWhenCourseExistsAndStudentExistsShouldShowError() {
		// Setup
		Student student = new Student(STUDENT_ID_1, STUDENT_NAME_1);
		Course course = new Course(COURSE_ID_1, COURSE_NAME_1, 6);
		when(myCareerService.findStudent(new Student(STUDENT_ID_1, STUDENT_NAME_1))).thenReturn(student);
		when(myCareerService.findSingleCourseByStudent(new Student(STUDENT_ID_1, STUDENT_NAME_1),
				new Course(COURSE_ID_1, COURSE_NAME_1, 6))).thenReturn(course);
		// Exercise
		myCareerController.addCourse(student, course);
		// Verify
		verify(myCareerView).showCourseError("The course already exists", course);
		verifyNoMoreInteractions(ignoreStubs(myCareerService));
	}

	@Test
	public void testAddCourseWhenStudentDoesNotExistShouldShowError() {
		// Setup
		Student student = new Student(STUDENT_ID_1, STUDENT_NAME_1);
		Course course = new Course(COURSE_ID_1, COURSE_NAME_1, 6);
		when(myCareerService.findStudent(new Student(STUDENT_ID_1, STUDENT_NAME_1))).thenReturn(null);
		// Exercise
		myCareerController.addCourse(student, course);
		// Verify
		verify(myCareerView).showCourseError("The student does not exist", course);
		verifyNoMoreInteractions(ignoreStubs(myCareerService));
	}

	// removeCourse Tests

	@Test
	public void testRemoveCourseWhenStudentExistsAndCourseExistsShouldRemoveAndShowInfo() {
		// Setup
		Student student = new Student(STUDENT_ID_1, STUDENT_NAME_1);
		Course course = new Course(COURSE_ID_1, COURSE_NAME_1, 6);
		when(myCareerService.findStudent(new Student(STUDENT_ID_1, STUDENT_NAME_1))).thenReturn(student);
		when(myCareerService.findSingleCourseByStudent(new Student(STUDENT_ID_1, STUDENT_NAME_1),
				new Course(COURSE_ID_1, COURSE_NAME_1, 6))).thenReturn(course);
		// Exercise
		myCareerController.removeCourse(student, course);
		// Verify
		InOrder inOrder = inOrder(myCareerService, myCareerView);
		inOrder.verify(myCareerService).removeCourse(student, course);
		inOrder.verify(myCareerView).courseRemoved("The course has been removed", course);
	}

	@Test
	public void testRemoveCourseWhenStudentExitsAndCourseDoesNotExitsShouldShowError() {
		// Setup
		Student student = new Student(STUDENT_ID_1, STUDENT_NAME_1);
		Course course = new Course(COURSE_ID_1, COURSE_NAME_1, 6);
		when(myCareerService.findStudent(new Student(STUDENT_ID_1, STUDENT_NAME_1))).thenReturn(student);
		when(myCareerService.findSingleCourseByStudent(new Student(STUDENT_ID_1, STUDENT_NAME_1),
				new Course(COURSE_ID_1, COURSE_NAME_1, 6))).thenReturn(null);
		// Exercise
		myCareerController.removeCourse(student, course);
		// Verify
		verify(myCareerView).showCourseError("The course does not exist", course);
		verifyNoMoreInteractions(ignoreStubs(myCareerService));
	}

	@Test
	public void testRemoveCourseWhenStudentDoesNotExistShouldShowError() {
		// Setup
		Student student = new Student(STUDENT_ID_1, "test1");
		Course course = new Course(COURSE_ID_1, COURSE_NAME_1, 6);
		when(myCareerService.findStudent(new Student(STUDENT_ID_1, "test1"))).thenReturn(null);
		// Exercise
		myCareerController.removeCourse(student, course);
		// Verify
		verify(myCareerView).showCourseError("The student does not exist", course);
		verifyNoMoreInteractions(ignoreStubs(myCareerService));
	}

	// getStudentsByCourse Tests
	@Test
	public void testGetStudentsByCourseWhenCourseDoesExistShoulGetStudents() {
		// Setup
		Course course = new Course(COURSE_ID_1, COURSE_NAME_1, 6);
		List<Student> students = asList(new Student(STUDENT_ID_1, STUDENT_NAME_1));
		when(myCareerService.findCourse(new Course(COURSE_ID_1, COURSE_NAME_1, 6))).thenReturn(course);
		when(myCareerService.getStudentsByCourse(new Course(COURSE_ID_1, COURSE_NAME_1, 6))).thenReturn(students);
		// Exercise
		myCareerController.getStudentsByCourse(course);
		// Verify
		verify(myCareerView).showAllStudents(students);
	}

	@Test
	public void testGetStudentsByCourseWhenCourseDoesNotExistShouldGetError() {
		// Setup
		Course course = new Course(COURSE_ID_1, COURSE_NAME_1, 6);
		when(myCareerService.findCourse(new Course(COURSE_ID_1, COURSE_NAME_1, 6))).thenReturn(null);
		// Exercise
		myCareerController.getStudentsByCourse(course);
		// Verify
		verify(myCareerView).showCourseError("The course does not exist", course);
		verifyNoMoreInteractions(ignoreStubs(myCareerService));
	}
}
