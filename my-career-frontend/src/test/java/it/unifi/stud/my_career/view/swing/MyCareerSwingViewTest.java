package it.unifi.stud.my_career.view.swing;

import static org.assertj.core.api.Assertions.assertThat;

import javax.swing.DefaultListModel;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.verify;
import org.assertj.swing.annotation.GUITest;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.fixture.JTextComponentFixture;
import org.assertj.swing.junit.runner.GUITestRunner;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import it.unifi.stud.my_career.controller.MyCareerController;
import it.unifi.stud.my_career.model.Course;
import it.unifi.stud.my_career.model.Student;

@RunWith(GUITestRunner.class)
public class MyCareerSwingViewTest extends AssertJSwingJUnitTestCase {

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

	private MyCareerSwingView myCareerSwingView;
	private FrameFixture window;

	@Override
	protected void onSetUp() {
		MockitoAnnotations.initMocks(this);

		GuiActionRunner.execute(() -> {
			myCareerSwingView = new MyCareerSwingView();
			myCareerSwingView.setMyCareerController(myCareerController);
			return myCareerSwingView;
		});

		window = new FrameFixture(robot(), myCareerSwingView);
		// show the frame to the test
		window.show();
	}

	// Initial States Test
	@Test
	@GUITest
	public void testElementsInitialState() {
		// Label check
		window.label("studentSectionTitle");
		window.label("studentIDLabel");
		window.label("studentNameLabel");
		window.label("studentInfoErrorMessageLabel");
		window.label("courseSectionTitle");
		window.label("courseIDLabel");
		window.label("courseNameLabel");
		window.label("courseCFUsLabel");
		window.label("courseInfoErrorMessageLabel");
		// Text fields check
		window.textBox("studentIDTextField").requireEnabled();
		window.textBox("studentNameTextField").requireEnabled();
		window.textBox("courseIDTextField").requireEnabled();
		window.textBox("courseNameTextField").requireEnabled();
		window.textBox("courseCFUsTextField").requireEnabled();
		// Buttons check
		window.button("addStudentButton").requireDisabled();
		window.button("deleteStudentButton").requireDisabled();
		window.button("addCourseButton").requireDisabled();
		window.button("deleteCourseButton").requireDisabled();
		// Lists check
		window.list("studentsList");
		window.list("coursesList");
	}

	// Tests on view elements state changes

	// Add student button tests

	@Test
	@GUITest
	public void testWhenStudentIdAndStudentNameAreNonEmptyThenAddStudentButtonShouldBeEnabled() {
		// Setup
		window.textBox("studentIDTextField").enterText(STUDENT_ID_1);
		window.textBox("studentNameTextField").enterText(STUDENT_NAME_1);
		// Verify
		window.button("addStudentButton").requireEnabled();
	}

	@Test
	@GUITest
	public void testWhenStudentIdOrStudentNameAreBlankThenAddStudentButtonShouldBeDisabled() {
		JTextComponentFixture studentIdTextField = window.textBox("studentIDTextField");
		JTextComponentFixture studentNameTextField = window.textBox("studentNameTextField");

		studentIdTextField.enterText(STUDENT_ID_1);
		studentNameTextField.enterText(" ");
		window.button("addStudentButton").requireDisabled();

		studentIdTextField.setText("");
		studentNameTextField.setText("");
		window.button("addStudentButton").requireDisabled();

		studentIdTextField.enterText(" ");
		studentNameTextField.enterText(STUDENT_NAME_1);
		window.button("addStudentButton").requireDisabled();

	}

	// Delete student button tests

	@Test
	@GUITest
	public void testDeleteStudentButtonShouldBeEnabledOnlyWhenAStudentIsSelected() {
		GuiActionRunner.execute(
				() -> myCareerSwingView.getStudentsListModel().addElement(new Student(STUDENT_ID_1, STUDENT_NAME_1)));
		window.list("studentsList").selectItem(0);
		window.button("deleteStudentButton").requireEnabled();

		window.list("studentsList").clearSelection();
		window.button("deleteStudentButton").requireDisabled();
	}

	// Add course button tests

	@Test
	@GUITest
	public void testWhenCourseIDCourseNameAndCourseCFUAreNonEmptyAndStudentIsSelectedThenAddCourseButtonShouldBeEnabled() {
		JTextComponentFixture courseIDTextField = window.textBox("courseIDTextField");
		JTextComponentFixture courseNameTextField = window.textBox("courseNameTextField");
		JTextComponentFixture courseCFUsTextField = window.textBox("courseCFUsTextField");

		courseIDTextField.enterText(COURSE_ID_1);
		courseNameTextField.enterText(COURSE_NAME_1);
		courseCFUsTextField.enterText("6");
		GuiActionRunner.execute(
				() -> myCareerSwingView.getStudentsListModel().addElement(new Student(STUDENT_ID_1, STUDENT_NAME_1)));
		window.list("studentsList").selectItem(0);

		window.button("addCourseButton").requireEnabled();

		window.list("studentsList").clearSelection();
		window.button("addCourseButton").requireDisabled();
	}

	@Test
	@GUITest
	public void testWhenCourseCFUsIsEmptyThenAddCourseButtonShouldBeDisabled() {
		JTextComponentFixture courseIDTextField = window.textBox("courseIDTextField");
		JTextComponentFixture courseNameTextField = window.textBox("courseNameTextField");
		JTextComponentFixture courseCFUsTextField = window.textBox("courseCFUsTextField");

		GuiActionRunner.execute(
				() -> myCareerSwingView.getStudentsListModel().addElement(new Student(STUDENT_ID_1, STUDENT_NAME_1)));

		courseIDTextField.enterText(COURSE_ID_1);
		courseNameTextField.enterText(COURSE_NAME_1);
		courseCFUsTextField.enterText(" ");
		window.list("studentsList").selectItem(0);

		window.button("addCourseButton").requireDisabled();
	}

	@Test
	@GUITest
	public void testWhenCourseCFUsIsNotANumberThenAddCourseButtonShouldBeDisabled() {
		JTextComponentFixture courseIDTextField = window.textBox("courseIDTextField");
		JTextComponentFixture courseNameTextField = window.textBox("courseNameTextField");
		JTextComponentFixture courseCFUsTextField = window.textBox("courseCFUsTextField");

		GuiActionRunner.execute(
				() -> myCareerSwingView.getStudentsListModel().addElement(new Student(STUDENT_ID_1, STUDENT_NAME_1)));

		courseIDTextField.enterText(COURSE_ID_1);
		courseNameTextField.enterText(COURSE_NAME_1);
		courseCFUsTextField.enterText("Error");
		window.list("studentsList").selectItem(0);

		window.button("addCourseButton").requireDisabled();
	}

	@Test
	@GUITest
	public void testWhenCourseIDIsEmptyThenAddCourseButtonShouldBeDisabled() {
		JTextComponentFixture courseIDTextField = window.textBox("courseIDTextField");
		JTextComponentFixture courseNameTextField = window.textBox("courseNameTextField");
		JTextComponentFixture courseCFUsTextField = window.textBox("courseCFUsTextField");

		GuiActionRunner.execute(
				() -> myCareerSwingView.getStudentsListModel().addElement(new Student(STUDENT_ID_1, STUDENT_NAME_1)));

		courseIDTextField.enterText(" ");
		courseNameTextField.enterText(COURSE_NAME_1);
		courseCFUsTextField.enterText("6");
		window.list("studentsList").selectItem(0);

		window.button("addCourseButton").requireDisabled();
	}

	@Test
	@GUITest
	public void testWhenCourseNameIsEmptyThenAddCourseButtonShouldBeDisabled() {
		JTextComponentFixture courseIDTextField = window.textBox("courseIDTextField");
		JTextComponentFixture courseNameTextField = window.textBox("courseNameTextField");
		JTextComponentFixture courseCFUsTextField = window.textBox("courseCFUsTextField");

		GuiActionRunner.execute(
				() -> myCareerSwingView.getStudentsListModel().addElement(new Student(STUDENT_ID_1, STUDENT_NAME_1)));

		courseIDTextField.enterText(COURSE_ID_1);
		courseNameTextField.enterText(" ");
		courseCFUsTextField.enterText("6");
		window.list("studentsList").selectItem(0);

		window.button("addCourseButton").requireDisabled();
	}

	@Test
	@GUITest
	public void testWhenOnlyTheStudentIsSelectedThenAddCourseButtonShouldBeDisabled() {
		GuiActionRunner.execute(
				() -> myCareerSwingView.getStudentsListModel().addElement(new Student(STUDENT_ID_1, STUDENT_NAME_1)));

		window.button("addCourseButton").requireDisabled();
	}
	// Delete course button tests

	@Test
	@GUITest
	public void testDeleteButtonShouldBeEnabledOnlyWhenAStudentAndACourseAreSelected() {
		GuiActionRunner.execute(() -> {
			myCareerSwingView.getStudentsListModel().addElement(new Student(STUDENT_ID_1, STUDENT_NAME_1));
			myCareerSwingView.getCoursesListModel().addElement(new Course(COURSE_ID_1, COURSE_NAME_1, 6));
		});
		window.list("studentsList").selectItem(0);
		window.list("coursesList").selectItem(0);
		window.button("deleteCourseButton").requireEnabled();

		window.list("coursesList").clearSelection();
		window.button("deleteCourseButton").requireDisabled();
	}

	@Test
	@GUITest
	public void testDeleteButtonShouldBeEnabledOnlyWhenACourseAndAStudentAreSelected() {
		GuiActionRunner.execute(() -> {
			myCareerSwingView.getStudentsListModel().addElement(new Student(STUDENT_ID_1, STUDENT_NAME_1));
			myCareerSwingView.getCoursesListModel().addElement(new Course(COURSE_ID_1, COURSE_NAME_1, 6));
		});
		window.list("coursesList").selectItem(0);
		window.list("studentsList").selectItem(0);
		window.button("deleteCourseButton").requireEnabled();

		window.list("studentsList").clearSelection();
		window.button("deleteCourseButton").requireDisabled();
	}

	// Tests on view's methods

	// showStudentError test
	@Test
	public void testShowStudentErrorShouldShowTheMessageInTheStudentInfoErrorLabel() {
		Student student = new Student(STUDENT_ID_1, STUDENT_NAME_1);
		GuiActionRunner.execute(() -> myCareerSwingView.showStudentError("error message", student));
		window.label("studentInfoErrorMessageLabel").requireText("error message: " + student);
	}

	// showAllStudent test
	@Test
	public void testShowAllStudentWithEmptyInitialListShouldAddStudentDescriptionsToTheList() {
		Student student1 = new Student(STUDENT_ID_1, STUDENT_NAME_1);
		Student student2 = new Student(STUDENT_ID_2, STUDENT_NAME_2);
		GuiActionRunner.execute(() -> myCareerSwingView.showAllStudents(asList(student1, student2)));

		String[] studentsListContents = window.list("studentsList").contents();
		assertThat(studentsListContents).containsExactly(student1.toString(), student2.toString());
	}

	@Test
	public void testShowAllStudentWithNonEmptyListShouldRefreshTheList() {
		Student student1 = new Student(STUDENT_ID_1, STUDENT_NAME_1);
		Student student2 = new Student(STUDENT_ID_2, STUDENT_NAME_2);
		Student student3 = new Student(STUDENT_ID_3, STUDENT_NAME_3);
		Student student4 = new Student(STUDENT_ID_4, STUDENT_NAME_4);

		GuiActionRunner.execute(() -> {
			myCareerSwingView.getStudentsListModel().addElement(student1);
			myCareerSwingView.getStudentsListModel().addElement(student2);
		});

		GuiActionRunner.execute(() -> myCareerSwingView.showAllStudents(asList(student3, student4)));

		String[] studentsListContents = window.list("studentsList").contents();
		assertThat(studentsListContents).containsExactly(student3.toString(), student4.toString());
	}

	// studentAdded test
	@Test
	public void testStudentAddedShouldAddTheStudentToTheListAndUpdateTheInfoErrorLabel() {
		Student student = new Student(STUDENT_ID_1, STUDENT_NAME_1);
		GuiActionRunner.execute(() -> myCareerSwingView.studentAdded("Student added", student));

		String[] studentsListContents = window.list("studentsList").contents();
		assertThat(studentsListContents).containsExactly(student.toString());
		window.label("studentInfoErrorMessageLabel").requireText("Student added: " + student);
	}

	// studentRemoved test
	@Test
	public void testStudentRemovedShouldRemoveTheStudentFromTheListAndUpdateTheInfoErrorLabel() {
		Student student1 = new Student(STUDENT_ID_1, STUDENT_NAME_1);
		Student student2 = new Student(STUDENT_ID_2, STUDENT_NAME_2);
		GuiActionRunner.execute(() -> {
			DefaultListModel<Student> studentsListModel = myCareerSwingView.getStudentsListModel();
			studentsListModel.addElement(student1);
			studentsListModel.addElement(student2);
		});

		GuiActionRunner.execute(() -> myCareerSwingView.studentRemoved("Student removed", student2));

		String[] studentsListContents = window.list("studentsList").contents();
		assertThat(studentsListContents).containsExactly(student1.toString());
		window.label("studentInfoErrorMessageLabel").requireText("Student removed: " + student2);
	}

	// showCourseError temessagest
	@Test
	public void testShowCourseErrorShouldShowTheMessageInTheCourseInfoErrorLabel() {
		Course course = new Course(COURSE_ID_1, COURSE_NAME_1, 6);
		GuiActionRunner.execute(() -> myCareerSwingView.showCourseError("Error message", course));
		window.label("courseInfoErrorMessageLabel").requireText("Error message: " + course);
	}

	// showAllCourses test
	@Test
	public void testShowAllCoursesWithInitialEmptyListShouldAddCourseDescriptionsToTheList() {
		Course course1 = new Course(COURSE_ID_1, COURSE_NAME_1, 6);
		Course course2 = new Course(COURSE_ID_2, "BDA", 9);
		GuiActionRunner.execute(() -> myCareerSwingView.showAllCourses(asList(course1, course2)));
		String[] coursesListContents = window.list("coursesList").contents();
		assertThat(coursesListContents).containsExactly(course1.toString(), course2.toString());
	}

	@Test
	public void testShowAllCoursesWithNonEmptyListShouldRefreshTheList() {
		Course course1 = new Course(COURSE_ID_1, COURSE_NAME_1, 6);
		Course course2 = new Course(COURSE_ID_2, COURSE_NAME_2, 9);
		Course course3 = new Course(COURSE_ID_3, COURSE_NAME_3, 9);
		Course course4 = new Course(COURSE_ID_4, COURSE_NAME_4, 6);

		GuiActionRunner.execute(() -> {
			myCareerSwingView.getCoursesListModel().addElement(course1);
			myCareerSwingView.getCoursesListModel().addElement(course2);
		});

		GuiActionRunner.execute(() -> myCareerSwingView.showAllCourses(asList(course3, course4)));

		String[] coursesListContents = window.list("coursesList").contents();
		assertThat(coursesListContents).containsExactly(course3.toString(), course4.toString());
	}

	// showCourseAdded test
	@Test
	public void testCourseAddedShouldAddTheCourseToTheListAndUpdateTheInfoErrorLabel() {
		Course course = new Course(COURSE_ID_1, COURSE_NAME_1, 6);
		GuiActionRunner.execute(() -> myCareerSwingView.courseAdded("Course added", course));
		String[] coursesListContents = window.list("coursesList").contents();
		assertThat(coursesListContents).containsExactly(course.toString());
		window.label("courseInfoErrorMessageLabel").requireText("Course added: " + course);
	}

	// showCourseRemoved test
	@Test
	public void testCourseRemovedShouldRemoveTheCourseFromTheListAndUpdateTheInfoErrorLabel() {
		Course course1 = new Course(COURSE_ID_1, COURSE_NAME_1, 6);
		Course course2 = new Course(COURSE_ID_2, COURSE_NAME_2, 9);
		GuiActionRunner.execute(() -> {
			DefaultListModel<Course> coursesListModel = myCareerSwingView.getCoursesListModel();
			coursesListModel.addElement(course1);
			coursesListModel.addElement(course2);
		});
		GuiActionRunner.execute(() -> myCareerSwingView.courseRemoved("Course removed", course2));
		String[] coursesListContents = window.list("coursesList").contents();
		assertThat(coursesListContents).containsExactly(course1.toString());
		window.label("courseInfoErrorMessageLabel").requireText("Course removed: " + course2);
	}

	// Tests on delegations to the controller's methods

	// Test Add Student
	@Test
	public void testAddStudentButtonShouldDelegateToControllerNewStudent() {
		window.textBox("studentIDTextField").enterText(STUDENT_ID_1);
		window.textBox("studentNameTextField").enterText(STUDENT_NAME_1);
		window.button("addStudentButton").click();

		verify(myCareerController).addStudent(new Student(STUDENT_ID_1, STUDENT_NAME_1));
	}

	// Test Delete Student
	@Test
	public void testDeleteStudentButtonShouldDelegateToControllerDeleteStudent() {
		Student student1 = new Student(STUDENT_ID_1, STUDENT_NAME_1);
		Student student2 = new Student(STUDENT_ID_2, STUDENT_NAME_2);
		GuiActionRunner.execute(() -> {
			DefaultListModel<Student> studentsListModel = myCareerSwingView.getStudentsListModel();
			studentsListModel.addElement(student1);
			studentsListModel.addElement(student2);
		});
		window.list("studentsList").selectItem(1);

		window.button("deleteStudentButton").click();

		verify(myCareerController).deleteStudent(student2);
	}

	// Test Add Course
	@Test
	public void testAddCourseButtonShouldDelegateToControllerAddCourse() {
		Student student1 = new Student(STUDENT_ID_1, STUDENT_NAME_1);
		Student student2 = new Student(STUDENT_ID_2, STUDENT_NAME_2);
		GuiActionRunner.execute(() -> {
			DefaultListModel<Student> studentsListModel = myCareerSwingView.getStudentsListModel();
			studentsListModel.addElement(student1);
			studentsListModel.addElement(student2);
		});
		window.list("studentsList").selectItem(1);
		window.textBox("courseIDTextField").enterText(COURSE_ID_1);
		window.textBox("courseNameTextField").enterText(COURSE_NAME_1);
		window.textBox("courseCFUsTextField").enterText("6");

		window.button("addCourseButton").click();

		verify(myCareerController).addCourse(student2, new Course(COURSE_ID_1, COURSE_NAME_1, 6));
	}

	// Test DeleteCourse
	@Test
	public void testDeleteCourseButtonShouldDelegateToControllerDeleteCourse() {
		Student student1 = new Student(STUDENT_ID_1, STUDENT_NAME_1);
		Student student2 = new Student(STUDENT_ID_2, STUDENT_NAME_2);
		Course course1 = new Course(COURSE_ID_1, COURSE_NAME_1, 6);
		Course course2 = new Course(COURSE_ID_2, COURSE_NAME_2, 9);
		GuiActionRunner.execute(() -> {
			DefaultListModel<Student> studentsListModel = myCareerSwingView.getStudentsListModel();
			DefaultListModel<Course> coursesListModel = myCareerSwingView.getCoursesListModel();

			studentsListModel.addElement(student1);
			studentsListModel.addElement(student2);

			coursesListModel.addElement(course1);
			coursesListModel.addElement(course2);
		});
		window.list("studentsList").selectItem(1);
		window.list("coursesList").selectItem(1);

		window.button("deleteCourseButton").click();

		verify(myCareerController).removeCourse(student2, course2);
	}

	// Test click on a student
	@Test
	public void testSelectAStudentInListShouldDelegateToControllerGetCoursesByStudent() {
		Student student = new Student(STUDENT_ID_1, STUDENT_NAME_1);
		GuiActionRunner.execute(() -> myCareerSwingView.getStudentsListModel().addElement(student));

		window.list("studentsList").selectItem(0);

		verify(myCareerController).getCoursesByStudent(student);
	}

}
