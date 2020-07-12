package it.unifi.stud.my_career.repository;

import java.util.List;

import it.unifi.stud.my_career.model.Course;
import it.unifi.stud.my_career.model.Student;

public interface StudentRepository {

	public List<Student> findAll();

	public Student findById(String id);

	public void save(Student student);

	public void delete(String id);

	public List<String> getParticipatedCoursesIdByStudentId(String id);

	public void deleteStudentParticipation(String studentId, Course course);

	public void addStudentParticipation(String studentId, Course course);

}
