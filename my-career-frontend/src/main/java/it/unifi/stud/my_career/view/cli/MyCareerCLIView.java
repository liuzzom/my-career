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

	private InputStream inputStream;
	private PrintStream outputStream;

	private List<Student> studentsList;
	private List<Course> coursesList;

	private MyCareerController myCareerController;
	private Scanner scanner;

	public MyCareerCLIView(InputStream inputStream, PrintStream outputStream) {
		this.inputStream = inputStream;
		this.outputStream = outputStream;

		studentsList = new ArrayList<Student>();
		coursesList = new ArrayList<Course>();
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
		outputStream.print("Insert id: ");
		String id = scanner.nextLine();
		outputStream.print("Insert name: ");
		String name = scanner.nextLine();

		myCareerController.addStudent(new Student(id, name));
	}

	private void getAllStudents() {
		myCareerController.getAllStudents();
	}

	private void deleteStudent() {
		outputStream.print("Insert id: ");
		String id = scanner.nextLine();
		outputStream.print("Insert name: ");
		String name = scanner.nextLine();

		myCareerController.deleteStudent(new Student(id, name));
	}

	private void getCoursesByStudent() {
		outputStream.print("Insert id: ");
		String id = scanner.nextLine();
		outputStream.print("Insert name: ");
		String name = scanner.nextLine();

		myCareerController.getCoursesByStudent(new Student(id, name));
	}

	private void addCourse() {
		outputStream.print("Insert student id: ");
		String studentId = scanner.nextLine();
		outputStream.print("Insert student name: ");
		String studentName = scanner.nextLine();
		outputStream.print("Insert course id: ");
		String courseId = scanner.nextLine();
		outputStream.print("Insert course name: ");
		String courseName = scanner.nextLine();
		outputStream.print("Insert course CFU: ");
		String courseCFU = scanner.nextLine();

		if (!courseCFU.matches("\\b([1-9]|[12][0-9]|3[01])\\b")) {
			outputStream.println("CFU value must be a number");
			return;
		}

		myCareerController.addCourse(new Student(studentId, studentName),
				new Course(courseId, courseName, Integer.parseInt(courseCFU)));
	}

	private void removeCourse() {
		outputStream.print("Insert student id: ");
		String studentId = scanner.nextLine();
		outputStream.print("Insert student name: ");
		String studentName = scanner.nextLine();
		outputStream.print("Insert course id: ");
		String courseId = scanner.nextLine();
		outputStream.print("Insert course name: ");
		String courseName = scanner.nextLine();
		outputStream.print("Insert course CFU: ");
		String courseCFU = scanner.nextLine();

		if (!courseCFU.matches("\\b([1-9]|[12][0-9]|3[01])\\b")) {
			outputStream.println("CFU value must be a number");
			return;
		}

		myCareerController.removeCourse(new Student(studentId, studentName),
				new Course(courseId, courseName, Integer.parseInt(courseCFU)));
	}

	private void getStudentsByCourse() {
		outputStream.print("Insert course id: ");
		String courseId = scanner.nextLine();
		outputStream.print("Insert course name: ");
		String courseName = scanner.nextLine();
		outputStream.print("Insert course CFU: ");
		String courseCFU = scanner.nextLine();

		if (!courseCFU.matches("\\b([1-9]|[12][0-9]|3[01])\\b")) {
			outputStream.println("CFU value must be a number");
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

	public void exec() {
		scanner = new Scanner(inputStream);
		
		showMenu();
		outputStream.println("Enter a valid digit: ");
		String choice = scanner.nextLine();
		
		switch (choice) {
		case "1":
			addStudent();
			break;
		case "2":
			getAllStudents();
			break;
		case "3":
			deleteStudent();
			break;
		case "4":
			getCoursesByStudent();
			break;
		case "5":
			addCourse();
			break;
		case "6":
			removeCourse();
			break;
		case "7":
			getStudentsByCourse();
			break;
		case "8":
			outputStream.println("Goodbye");
			break;
		default:
			outputStream.println("Not a valid input");
			System.exit(0);
		}

	scanner.close();

	}
}
