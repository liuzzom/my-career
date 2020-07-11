package it.unifi.stud.my_career.repository;

import java.util.List;

import it.unifi.stud.my_career.model.Course;
import it.unifi.stud.my_career.model.Student;

public interface CourseRepository {

	public List<Course> findAll();

	public Course findById(String id);

	public void save(Course course);

	public void delete(String id);

	public List<String> getParticipantsStudentsIdByCourseId(String id);

	public void deleteCourseParticipant(String courseId, Student student);

	public void addCourseParticipant(String courseId, Student student);
}
