package it.unifi.stud.my_career.view.cli;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import it.unifi.stud.my_career.controller.MyCareerController;
import it.unifi.stud.my_career.model.Course;
import it.unifi.stud.my_career.model.Student;
import it.unifi.stud.my_career.view.MyCareerView;

public class MyCareerCLIView implements MyCareerView {

	private static final String INSERT_STUDENT_NAME = "Insert student name: ";
	private static final String INSERT_STUDENT_ID = "Insert student id: ";
	private static final String CFU_ERROR_MSG = "CFU value must be a number";
	private static final String CFU_REGEX = "\\b([1-9]|[12][0-9]|3[01])\\b";
	private static final String INSERT_COURSE_CFU = "Insert course CFU: ";
	private static final String INSERT_COURSE_NAME = "Insert course name: ";
	private static final String INSERT_COURSE_ID = "Insert course id: ";
	private static final String INSERT_NAME = "Insert name: ";
	private static final String INSERT_ID = "Insert id: ";
	private InputStream inputStream;
	private PrintStream outputStream;

	private List<Student> studentsList;
	private List<Course> coursesList;

	private MyCareerController myCareerController;
	private Scanner scanner;

	public MyCareerCLIView(InputStream inputStream, PrintStream outputStream) {
		this.inputStream = inputStream;
		this.outputStream = outputStream;

		studentsList = new ArrayList<>();
		coursesList = new ArrayList<>();
	}

	List<Student> getStudentsList() {
		return studentsList;
	}

	List<Course> getCoursesList() {
		return coursesList;
	}

	void setInputStream(InputStream inputStream) {
		this.inputStream = inputStream;
	}

	public void setMyCareerController(MyCareerController myCareerController) {
		this.myCareerController = myCareerController;
	}

	@Override
	public void showStudentError(String message, Student student) {
		outputStream.println("ERROR! " + message + " : " + student);
	}

	@Override
	public void showAllStudents(List<Student> students) {
		studentsList.clear();
		students.stream().forEach(studentsList::add);
		studentsList.stream().forEach(outputStream::println);
	}

	@Override
	public void studentAdded(String message, Student student) {
		studentsList.add(student);
		outputStream.println(message + " : " + student);
	}

	@Override
	public void studentRemoved(String message, Student student) {
		studentsList.remove(student);
		outputStream.println(message + " : " + student);
	}

	@Override
	public void showCourseError(String message, Course course) {
		outputStream.println("ERROR! " + message + " : " + course);
	}

	@Override
	public void showAllCourses(List<Course> courses) {
		coursesList.clear();
		courses.stream().forEach(coursesList::add);
		coursesList.stream().forEach(outputStream::println);
	}

	@Override
	public void courseAdded(String message, Course course) {
		coursesList.add(course);
		outputStream.println(message + " : " + course);
	}

	@Override
	public void courseRemoved(String message, Course course) {
		coursesList.remove(course);
		outputStream.println(message + " : " + course);
	}

	private void addStudent() {
		outputStream.print(INSERT_ID);
		String id = scanner.nextLine();
		outputStream.print(INSERT_NAME);
		String name = scanner.nextLine();

		myCareerController.addStudent(new Student(id, name));
	}

	private void getAllStudents() {
		myCareerController.getAllStudents();
	}

	private void deleteStudent() {
		outputStream.print(INSERT_ID);
		String id = scanner.nextLine();
		outputStream.print(INSERT_NAME);
		String name = scanner.nextLine();

		myCareerController.deleteStudent(new Student(id, name));
	}

	private void getCoursesByStudent() {
		outputStream.print(INSERT_ID);
		String id = scanner.nextLine();
		outputStream.print(INSERT_NAME);
		String name = scanner.nextLine();

		myCareerController.getCoursesByStudent(new Student(id, name));
	}

	private void addCourse() {
		
		String[] params = askingStudentAndCourse();

		if (!params[4].matches(CFU_REGEX)) {
			outputStream.println(CFU_ERROR_MSG);
			return;
		}

		myCareerController.addCourse(new Student(params[0], params[1]),
				new Course(params[2], params[3], Integer.parseInt(params[4])));
	}

	private void removeCourse() {
		
		String[] params = askingStudentAndCourse();

		if (!params[4].matches(CFU_REGEX)) {
			outputStream.println(CFU_ERROR_MSG);
			return;
		}

		myCareerController.removeCourse(new Student(params[0], params[1]),
				new Course(params[2], params[3], Integer.parseInt(params[4])));
	}

	private void getStudentsByCourse() {
		outputStream.print(INSERT_COURSE_ID);
		String courseId = scanner.nextLine();
		outputStream.print(INSERT_COURSE_NAME);
		String courseName = scanner.nextLine();
		outputStream.print(INSERT_COURSE_CFU);
		String courseCFU = scanner.nextLine();

		if (!courseCFU.matches(CFU_REGEX)) {
			outputStream.println(CFU_ERROR_MSG);
			return;
		}

		myCareerController.getStudentsByCourse(new Course(courseId, courseName, Integer.parseInt(courseCFU)));
	}

	public void showMenu() {
		outputStream.println(
				"" + "Student Section\n" + "1) Add a student\n" + "2) Get all students\n" + "3) Delete a student\n"
						+ "4) Get courses (by student)\n" + "Course Section\n" + "5) Add a course subscription\n"
						+ "6) Remove a course subscription\n" + "7) Get students (by course)\n" + "8) Exit");
	}

	public int exec() {
		showMenu();
		outputStream.println("Enter a valid digit: ");
		
		scanner = new Scanner(inputStream);
		String choice = scanner.nextLine();
		int rValue;
		
		switch (choice) {
		case "1":
			addStudent();
			rValue = 1;
			break;
		case "2":
			getAllStudents();
			rValue = 2;
			break;
		case "3":
			deleteStudent();
			rValue = 3;
			break;
		case "4":
			getCoursesByStudent();
			rValue = 4;
			break;
		case "5":
			addCourse();
			rValue = 5;
			break;
		case "6":
			removeCourse();
			rValue = 6;
			break;
		case "7":
			getStudentsByCourse();
			rValue = 7;
			break;
		case "8":
			outputStream.println("Goodbye");
			rValue = 8;
			break;
		default:
			outputStream.println("Not a valid input");
			rValue = -1;
			break;
		}
	return rValue;

	}

	private String[] askingStudentAndCourse () {
		String[] results = new String[5];
		
		outputStream.print(INSERT_STUDENT_ID);
		results[0] = scanner.nextLine();
		outputStream.print(INSERT_STUDENT_NAME);
		results[1] = scanner.nextLine();
		outputStream.print(INSERT_COURSE_ID);
		results[2] = scanner.nextLine();
		outputStream.print(INSERT_COURSE_NAME);
		results[3] = scanner.nextLine();
		outputStream.print(INSERT_COURSE_CFU);
		results[4] = scanner.nextLine();
		
		return results;
	}
}
