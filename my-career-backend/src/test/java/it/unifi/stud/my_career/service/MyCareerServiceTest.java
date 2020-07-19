package it.unifi.stud.my_career.service;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.AdditionalAnswers.answer;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import it.unifi.stud.my_career.model.Course;
import it.unifi.stud.my_career.model.Student;
import it.unifi.stud.my_career.repository.CourseRepository;
import it.unifi.stud.my_career.repository.CourseTransactionCode;
import it.unifi.stud.my_career.repository.StudentRepository;
import it.unifi.stud.my_career.repository.StudentTransactionCode;
import it.unifi.stud.my_career.repository.TransactionCode;
import it.unifi.stud.my_career.repository.TransactionManager;

public class MyCareerServiceTest {

	private static final String STUDENT_NAME_1 = "test1";
	private static final String STUDENT_ID_1 = "1";
	private static final String STUDENT_NAME_2 = "test2";
	private static final String STUDENT_ID_2 = "2";
	
	private static final String COURSE_NAME_1 = "APT";
	private static final String COURSE_ID_1 = "123";
	private static final String COURSE_NAME_2 = "HCI";
	private static final String COURSE_ID_2 = "456";
	private static final int COURSE_CFU = 6;
	
	// SUT
	private MyCareerService myCareerService;

	// Mocks
	@Mock
	private TransactionManager transactionManager;

	@Mock
	private StudentRepository studentRepository;

	@Mock
	private CourseRepository courseRepository;

	// Setup
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);

		when(transactionManager.careerTransaction(any()))
				.thenAnswer(answer((TransactionCode<?> code) -> code.apply(studentRepository, courseRepository)));

		when(transactionManager.studentTransaction(any()))
				.thenAnswer(answer((StudentTransactionCode<?> code) -> code.apply(studentRepository)));

		when(transactionManager.courseTransaction(any()))
				.thenAnswer(answer((CourseTransactionCode<?> code) -> code.apply(courseRepository)));

		myCareerService = new MyCareerService(transactionManager);
	}

	// getAllStudents tests

	@Test
	public void testGetAllStudentsWithSomeStudent() {
		// Setup
		Student student1 = new Student(STUDENT_ID_1, STUDENT_NAME_1);
		Student student2 = new Student(STUDENT_ID_2, STUDENT_NAME_2);
		when(studentRepository.findAll()).thenReturn(asList(student1, student2));
		// Exercise
		List<Student> students = myCareerService.getAllStudents();
		// Verify
		assertThat(students).containsExactly(student1, student2);
		verify(transactionManager).studentTransaction(any());
	}

	@Test
	public void testGetAllStudentsWithAnEmptyList() {
		// Setup
		when(studentRepository.findAll()).thenReturn(null);
		// Exercise
		assertThat(myCareerService.getAllStudents()).isNull();
		// Verify
		verify(transactionManager).studentTransaction(any());
	}

	// findStudent tests

	@Test
	public void testFindStudentWhenStudentDoesExist() {
		// Setup
		Student existingStudent = new Student(STUDENT_ID_1, STUDENT_NAME_1);
		when(studentRepository.findById(STUDENT_ID_1)).thenReturn(existingStudent);
		// Exercise
		Student student = myCareerService.findStudent(existingStudent);
		// Verify
		assertThat(student).isEqualTo(existingStudent);
		verify(transactionManager).studentTransaction(any());
	}

	@Test
	public void testFindStudentWhenStudentDoesNotExist() {
		// Setup
		Student student = new Student(STUDENT_ID_1, STUDENT_NAME_1);
		when(studentRepository.findById(STUDENT_ID_1)).thenReturn(null);
		// Exercise and Verify
		assertThat(myCareerService.findStudent(student)).isNull();
		verify(transactionManager).studentTransaction(any());
	}

	// saveStudent tests

	@Test
	public void testSaveStudent() {
		// Setup
		Student student = new Student(STUDENT_ID_1, STUDENT_NAME_1);
		// Exercise
		myCareerService.saveStudent(student);
		// Verify
		verify(studentRepository).save(student);
		verify(transactionManager).studentTransaction(any());
	}

	// deleteStudent test

	@Test
	public void testDeleteStudent() {
		// Setup
		Student student = new Student(STUDENT_ID_1, STUDENT_NAME_1);
		// Exercise
		myCareerService.deleteStudent(student);
		// Verify
		verify(studentRepository).delete(STUDENT_ID_1);
		verify(transactionManager).studentTransaction(any());
	}

	// getCoursesByStudent test

	@Test
	public void testGetCoursesByStudentWithNonEmptyCoursesList() {
		// Setup
		Student student = new Student(STUDENT_ID_1, STUDENT_NAME_1);
		Course course1 = new Course(COURSE_ID_1, COURSE_NAME_1, COURSE_CFU);
		Course course2 = new Course(COURSE_ID_2, COURSE_NAME_2, COURSE_CFU);
		when(studentRepository.getParticipatedCoursesIdByStudentId(STUDENT_ID_1))
				.thenReturn(asList(COURSE_ID_1, COURSE_ID_2));
		when(courseRepository.findById(COURSE_ID_1)).thenReturn(course1);
		when(courseRepository.findById(COURSE_ID_2)).thenReturn(course2);
		// Exercise
		List<Course> courses = myCareerService.getCoursesByStudent(student);
		// Verify
		assertThat(courses).containsExactly(course1, course2);
		verify(transactionManager).careerTransaction(any());
	}

	@Test
	public void testGetCoursesByStudentWithEmptyCoursesList() {
		// Setup
		Student student = new Student(STUDENT_ID_1, STUDENT_NAME_1);
		when(studentRepository.getParticipatedCoursesIdByStudentId(STUDENT_ID_1)).thenReturn(Collections.emptyList());
		// Exercise
		List<Course> courses = myCareerService.getCoursesByStudent(student);
		// Verify
		assertThat(courses).isEmpty();
		verify(transactionManager).careerTransaction(any());
	}

	// findCourse test

	@Test
	public void testFindCourseWhenCourseDoesExist() {
		// Setup
		Course existingCourse = new Course(COURSE_ID_1, COURSE_NAME_1, 6);
		when(courseRepository.findById(COURSE_ID_1)).thenReturn(existingCourse);
		// Exercise
		Course course = myCareerService.findCourse(existingCourse);
		// Verify
		assertThat(course).isEqualTo(existingCourse);
		verify(transactionManager).courseTransaction(any());
	}

	@Test
	public void testFindCourseWhenCourseDoesNotExist() {
		// Setup
		Course course = new Course(COURSE_ID_1, COURSE_NAME_1, 6);
		when(courseRepository.findById(COURSE_ID_1)).thenReturn(null);
		// Exercise and Verify
		assertThat(myCareerService.findCourse(course)).isNull();
		verify(transactionManager).courseTransaction(any());
	}

	// findSingleCourseByStudent test

	@Test
	public void testFindSingleCourseByStudentWhenCourseDoesExist() {
		// Setup
		Student student = new Student(STUDENT_ID_1, STUDENT_NAME_1);
		Course courseNotToFind = new Course(COURSE_ID_1, COURSE_NAME_1, COURSE_CFU);
		Course courseToFind = new Course(COURSE_ID_2, COURSE_NAME_2, COURSE_CFU);
		when(studentRepository.getParticipatedCoursesIdByStudentId(STUDENT_ID_1))
				.thenReturn(asList(COURSE_ID_1, COURSE_ID_2));
		when(courseRepository.findById(COURSE_ID_1)).thenReturn(courseNotToFind);
		when(courseRepository.findById(COURSE_ID_2)).thenReturn(courseToFind);
		// Exercise
		Course courseFound = myCareerService.findSingleCourseByStudent(student, courseToFind);
		// Verify
		assertThat(courseFound).isEqualTo(courseToFind);
		verify(transactionManager).careerTransaction(any());
	}

	@Test
	public void testFindSingleCourseByStudentWhenCourseDoesNotExist() {
		// Setup
		Student student = new Student(STUDENT_ID_1, STUDENT_NAME_1);
		Course courseNotToFind = new Course(COURSE_ID_1, COURSE_NAME_1, COURSE_CFU);
		Course courseToFind = new Course(COURSE_ID_2, COURSE_NAME_2, COURSE_CFU);
		when(studentRepository.getParticipatedCoursesIdByStudentId(STUDENT_ID_1)).thenReturn(asList(COURSE_ID_2));
		when(courseRepository.findById(COURSE_ID_1)).thenReturn(null);
		when(courseRepository.findById(COURSE_ID_2)).thenReturn(courseToFind);
		// Exercise
		Course courseFound = myCareerService.findSingleCourseByStudent(student, courseNotToFind);
		// Verify
		assertThat(courseFound).isNull();
		verify(transactionManager).careerTransaction(any());
	}

	// saveCourse tests

	@Test
	public void testSaveCourseWhenCourseDoesNotExist() {
		// Setup
		Student student = new Student(STUDENT_ID_1, STUDENT_NAME_1);
		Course course = new Course(COURSE_ID_1, COURSE_NAME_1, COURSE_CFU);
		when(courseRepository.findById(COURSE_ID_1)).thenReturn(null);
		// Exercise
		myCareerService.saveCourse(student, course);
		// Verify
		InOrder inOrder = inOrder(courseRepository, studentRepository);
		inOrder.verify(courseRepository).save(course);
		inOrder.verify(courseRepository).addCourseParticipant(COURSE_ID_1, student);
		inOrder.verify(studentRepository).addStudentParticipation(STUDENT_ID_1, course);
		verify(transactionManager).careerTransaction(any());
	}

	@Test
	public void testSaveCourseWhenCourseDoesExist() {
		// Setup
		Student student = new Student(STUDENT_ID_1, STUDENT_NAME_1);
		Course course = new Course(COURSE_ID_1, COURSE_NAME_1, COURSE_CFU);
		when(courseRepository.findById(COURSE_ID_1)).thenReturn(course);
		// Exercise
		myCareerService.saveCourse(student, course);
		// Verify
		InOrder inOrder = inOrder(courseRepository, studentRepository);
		verify(courseRepository, never()).save(course);
		inOrder.verify(courseRepository).addCourseParticipant(COURSE_ID_1, student);
		inOrder.verify(studentRepository).addStudentParticipation(STUDENT_ID_1, course);
		verify(transactionManager).careerTransaction(any());
	}

	// removeCourse tests

	@Test
	public void testRemoveCourseWhenCourseIsFollowedOnlyByStudent() {
		// Setup
		Student student = new Student(STUDENT_ID_1, STUDENT_NAME_1);
		Course course = new Course(COURSE_ID_1, COURSE_NAME_1, COURSE_CFU);
		when(courseRepository.getParticipantsStudentsIdByCourseId(COURSE_ID_1)).thenReturn(Collections.emptyList());
		// Exercise
		myCareerService.removeCourse(student, course);
		// Verify
		InOrder inOrder = inOrder(courseRepository, studentRepository);
		inOrder.verify(courseRepository).deleteCourseParticipant(COURSE_ID_1, student);
		inOrder.verify(studentRepository).deleteStudentParticipation(STUDENT_ID_1, course);
		inOrder.verify(courseRepository).delete(COURSE_ID_1);
		verify(transactionManager).careerTransaction(any());
	}

	@Test
	public void testRemoveCourseWhenCourseIsFollowedByManyStudents() {
		// Setup
		Student student = new Student(STUDENT_ID_2, STUDENT_NAME_2);
		Course course = new Course(COURSE_ID_1, COURSE_NAME_1, COURSE_CFU);
		when(courseRepository.getParticipantsStudentsIdByCourseId(COURSE_ID_1))
				.thenReturn(asList(STUDENT_ID_1, STUDENT_ID_2));
		// Exercise
		myCareerService.removeCourse(student, course);
		// Verify
		InOrder inOrder = inOrder(courseRepository, studentRepository);
		inOrder.verify(courseRepository).deleteCourseParticipant(COURSE_ID_1, student);
		inOrder.verify(studentRepository).deleteStudentParticipation(STUDENT_ID_2, course);
		verify(courseRepository, never()).delete(COURSE_ID_1);
		verify(transactionManager).careerTransaction(any());
	}

	// getStudentsByCourse tests

	@Test
	public void testGetStudentsByCourse() {
		// Setup
		Course course = new Course(COURSE_ID_1, COURSE_NAME_1, COURSE_CFU);
		Student student1 = new Student(STUDENT_ID_1, STUDENT_NAME_1);
		Student student2 = new Student(STUDENT_ID_2, STUDENT_NAME_2);
		when(courseRepository.getParticipantsStudentsIdByCourseId(COURSE_ID_1))
				.thenReturn(asList(STUDENT_ID_1, STUDENT_ID_2));
		when(studentRepository.findById(STUDENT_ID_1)).thenReturn(student1);
		when(studentRepository.findById(STUDENT_ID_2)).thenReturn(student2);
		// Exercise
		List<Student> students = myCareerService.getStudentsByCourse(course);
		// Verify
		assertThat(students).containsExactly(student1, student2);
		verify(transactionManager).careerTransaction(any());
	}
}
