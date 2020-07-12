package it.unifi.stud.my_career.view.swing;

import java.awt.Color;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import it.unifi.stud.my_career.controller.MyCareerController;
import it.unifi.stud.my_career.model.Course;
import it.unifi.stud.my_career.model.Student;
import it.unifi.stud.my_career.view.MyCareerView;

import java.awt.GridBagLayout;
import javax.swing.JLabel;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.util.List;

import javax.swing.JTextField;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class MyCareerSwingView extends JFrame implements MyCareerView {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;

	private JPanel studentPanel;
	private JLabel studentSectionTitle;
	private JLabel studentIDLabel;
	private JTextField studentIDTextField;
	private JTextField studentNameTextField;
	private JLabel studentNameLabel;
	private JButton addStudentButton;
	private JScrollPane studentScrollPane;
	private JList<Student> studentsList;
	private JButton deleteStudentButton;
	private JLabel studentInfoErrorMessageLabel;

	private JPanel coursePanel;
	private JLabel courseSectionTitle;
	private JLabel courseIDLabel;
	private JTextField courseIDTextField;
	private JLabel courseNameLabel;
	private JTextField courseNameTextField;
	private JLabel courseCFUsLabel;
	private JTextField courseCFUsTextField;
	private JButton addCourseButton;
	private JScrollPane coursesScrollPane;
	private JList<Course> coursesList;
	private JButton deleteCourseButton;
	private JLabel courseInfoErrorMessageLabel;

	private DefaultListModel<Student> studentsListModel;
	private DefaultListModel<Course> coursesListModel;
	private MyCareerController myCareerController;

	DefaultListModel<Student> getStudentsListModel() {
		return studentsListModel;
	}

	DefaultListModel<Course> getCoursesListModel() {
		return coursesListModel;
	}

	public void setMyCareerController(MyCareerController myCareerController) {
		this.myCareerController = myCareerController;
	}

	/**
	 * Create the frame.
	 */
	public MyCareerSwingView() {

		/**
		 * Event listeners Created during refactoring
		 */
		KeyAdapter addStudentButtonEnabler = new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				addStudentButton.setEnabled(!studentIDTextField.getText().trim().isEmpty()
						&& !studentNameTextField.getText().trim().isEmpty());
			}
		};

		ListSelectionListener studentsListListener = new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				deleteStudentButton.setEnabled(studentsList.getSelectedIndex() != -1);
				addCourseButton.setEnabled(
						!courseIDTextField.getText().trim().isEmpty() && !courseNameTextField.getText().trim().isEmpty()
								&& !courseCFUsTextField.getText().trim().isEmpty()
								&& courseCFUsTextField.getText().trim().matches("\\b([1-9]|[12][0-9]|3[01])\\b")
								&& studentsList.getSelectedIndex() != -1);
				deleteCourseButton
						.setEnabled(studentsList.getSelectedIndex() != -1 && coursesList.getSelectedIndex() != -1);

				// to avoid double trigger (click and release)
				if (!e.getValueIsAdjusting())
					myCareerController.getCoursesByStudent(studentsList.getSelectedValue());
			}
		};

		KeyAdapter addCourseButtonEnabler = new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				addCourseButton.setEnabled(
						!courseIDTextField.getText().trim().isEmpty() && !courseNameTextField.getText().trim().isEmpty()
								&& !courseCFUsTextField.getText().trim().isEmpty()
								&& courseCFUsTextField.getText().trim().matches("\\b([1-9]|[12][0-9]|3[01])\\b")
								&& studentsList.getSelectedIndex() != -1);
			}
		};

		ActionListener addStudentButtonListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				myCareerController
						.addStudent(new Student(studentIDTextField.getText(), studentNameTextField.getText()));
			}
		};

		ActionListener deleteStudentButtonListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				myCareerController.deleteStudent(studentsList.getSelectedValue());
			}
		};

		ActionListener addCourseButtonListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				myCareerController.addCourse(studentsList.getSelectedValue(), new Course(courseIDTextField.getText(),
						courseNameTextField.getText(), Integer.parseInt(courseCFUsTextField.getText())));
			}
		};

		ActionListener removeCourseButtonListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				myCareerController.removeCourse(studentsList.getSelectedValue(), coursesList.getSelectedValue());
			}
		};

		ListSelectionListener coursesListListener = new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				deleteCourseButton
						.setEnabled(studentsList.getSelectedIndex() != -1 && coursesList.getSelectedIndex() != -1);
			}
		};

		// Frame Elements
		setTitle("My Career");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 900, 550);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		GridBagLayout gbl_contentPane = new GridBagLayout();
		gbl_contentPane.columnWidths = new int[] { 450, 0, 0 };
		gbl_contentPane.rowHeights = new int[] { 0, 0 };
		gbl_contentPane.columnWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
		gbl_contentPane.rowWeights = new double[] { 1.0, Double.MIN_VALUE };
		contentPane.setLayout(gbl_contentPane);

		studentPanel = new JPanel();
		GridBagConstraints gbc_studentPanel = new GridBagConstraints();
		gbc_studentPanel.insets = new Insets(0, 0, 0, 5);
		gbc_studentPanel.fill = GridBagConstraints.BOTH;
		gbc_studentPanel.gridx = 0;
		gbc_studentPanel.gridy = 0;
		contentPane.add(studentPanel, gbc_studentPanel);
		GridBagLayout gbl_studentPanel = new GridBagLayout();
		gbl_studentPanel.columnWidths = new int[] { 0, 0, 0 };
		gbl_studentPanel.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0, 0 };
		gbl_studentPanel.columnWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
		gbl_studentPanel.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, Double.MIN_VALUE };
		studentPanel.setLayout(gbl_studentPanel);

		studentSectionTitle = new JLabel("Student Section");
		studentSectionTitle.setName("studentSectionTitle");
		GridBagConstraints gbc_studentSectionTitle = new GridBagConstraints();
		gbc_studentSectionTitle.gridwidth = 2;
		gbc_studentSectionTitle.insets = new Insets(0, 0, 5, 0);
		gbc_studentSectionTitle.gridx = 0;
		gbc_studentSectionTitle.gridy = 0;
		studentPanel.add(studentSectionTitle, gbc_studentSectionTitle);

		studentIDLabel = new JLabel("ID");
		studentIDLabel.setName("studentIDLabel");
		GridBagConstraints gbc_studentIDLabel = new GridBagConstraints();
		gbc_studentIDLabel.anchor = GridBagConstraints.EAST;
		gbc_studentIDLabel.insets = new Insets(0, 0, 5, 5);
		gbc_studentIDLabel.gridx = 0;
		gbc_studentIDLabel.gridy = 1;
		studentPanel.add(studentIDLabel, gbc_studentIDLabel);

		studentIDTextField = new JTextField();
		studentIDTextField.addKeyListener(addStudentButtonEnabler);
		studentIDTextField.setName("studentIDTextField");
		GridBagConstraints gbc_studentIDTextField = new GridBagConstraints();
		gbc_studentIDTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_studentIDTextField.insets = new Insets(0, 0, 5, 0);
		gbc_studentIDTextField.gridx = 1;
		gbc_studentIDTextField.gridy = 1;
		studentPanel.add(studentIDTextField, gbc_studentIDTextField);
		studentIDTextField.setColumns(10);

		studentNameLabel = new JLabel("Name");
		studentNameLabel.setName("studentNameLabel");
		GridBagConstraints gbc_studentNameLabel = new GridBagConstraints();
		gbc_studentNameLabel.anchor = GridBagConstraints.EAST;
		gbc_studentNameLabel.insets = new Insets(0, 0, 5, 5);
		gbc_studentNameLabel.gridx = 0;
		gbc_studentNameLabel.gridy = 2;
		studentPanel.add(studentNameLabel, gbc_studentNameLabel);

		studentNameTextField = new JTextField();
		studentNameTextField.addKeyListener(addStudentButtonEnabler);
		studentNameTextField.setName("studentNameTextField");
		GridBagConstraints gbc_studentNameTextField = new GridBagConstraints();
		gbc_studentNameTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_studentNameTextField.insets = new Insets(0, 0, 5, 0);
		gbc_studentNameTextField.gridx = 1;
		gbc_studentNameTextField.gridy = 2;
		studentPanel.add(studentNameTextField, gbc_studentNameTextField);
		studentNameTextField.setColumns(10);

		addStudentButton = new JButton("Add Student");
		addStudentButton.addActionListener(addStudentButtonListener);
		addStudentButton.setEnabled(false);
		addStudentButton.setName("addStudentButton");
		GridBagConstraints gbc_addStudentButton = new GridBagConstraints();
		gbc_addStudentButton.gridwidth = 2;
		gbc_addStudentButton.insets = new Insets(0, 0, 5, 0);
		gbc_addStudentButton.gridx = 0;
		gbc_addStudentButton.gridy = 3;
		studentPanel.add(addStudentButton, gbc_addStudentButton);

		studentScrollPane = new JScrollPane();
		GridBagConstraints gbc_studentScrollPane = new GridBagConstraints();
		gbc_studentScrollPane.fill = GridBagConstraints.BOTH;
		gbc_studentScrollPane.gridwidth = 2;
		gbc_studentScrollPane.insets = new Insets(0, 0, 5, 0);
		gbc_studentScrollPane.gridx = 0;
		gbc_studentScrollPane.gridy = 4;
		studentPanel.add(studentScrollPane, gbc_studentScrollPane);

		studentsListModel = new DefaultListModel<Student>();
		studentsList = new JList<Student>(studentsListModel);
		studentsList.addListSelectionListener(studentsListListener);
		studentsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		studentsList.setName("studentsList");
		studentScrollPane.setViewportView(studentsList);

		deleteStudentButton = new JButton("Delete Student");
		deleteStudentButton.addActionListener(deleteStudentButtonListener);
		deleteStudentButton.setEnabled(false);
		deleteStudentButton.setName("deleteStudentButton");
		GridBagConstraints gbc_deleteStudentButton = new GridBagConstraints();
		gbc_deleteStudentButton.gridwidth = 2;
		gbc_deleteStudentButton.insets = new Insets(0, 0, 5, 0);
		gbc_deleteStudentButton.gridx = 0;
		gbc_deleteStudentButton.gridy = 5;
		studentPanel.add(deleteStudentButton, gbc_deleteStudentButton);

		studentInfoErrorMessageLabel = new JLabel("");
		studentInfoErrorMessageLabel.setName("studentInfoErrorMessageLabel");
		GridBagConstraints gbc_studentInfoErrorMessageLabel = new GridBagConstraints();
		gbc_studentInfoErrorMessageLabel.gridwidth = 2;
		gbc_studentInfoErrorMessageLabel.insets = new Insets(0, 0, 0, 5);
		gbc_studentInfoErrorMessageLabel.gridx = 0;
		gbc_studentInfoErrorMessageLabel.gridy = 6;
		studentPanel.add(studentInfoErrorMessageLabel, gbc_studentInfoErrorMessageLabel);

		coursePanel = new JPanel();
		GridBagConstraints gbc_coursePanel = new GridBagConstraints();
		gbc_coursePanel.fill = GridBagConstraints.BOTH;
		gbc_coursePanel.gridx = 1;
		gbc_coursePanel.gridy = 0;
		contentPane.add(coursePanel, gbc_coursePanel);
		GridBagLayout gbl_coursePanel = new GridBagLayout();
		gbl_coursePanel.columnWidths = new int[] { 0, 0, 0 };
		gbl_coursePanel.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		gbl_coursePanel.columnWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
		gbl_coursePanel.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, Double.MIN_VALUE };
		coursePanel.setLayout(gbl_coursePanel);

		courseSectionTitle = new JLabel("Course Section");
		courseSectionTitle.setName("courseSectionTitle");
		GridBagConstraints gbc_courseSectionTitle = new GridBagConstraints();
		gbc_courseSectionTitle.gridwidth = 2;
		gbc_courseSectionTitle.insets = new Insets(0, 0, 5, 0);
		gbc_courseSectionTitle.gridx = 0;
		gbc_courseSectionTitle.gridy = 0;
		coursePanel.add(courseSectionTitle, gbc_courseSectionTitle);

		courseIDLabel = new JLabel("ID");
		courseIDLabel.setName("courseIDLabel");
		GridBagConstraints gbc_courseIDLabel = new GridBagConstraints();
		gbc_courseIDLabel.insets = new Insets(0, 0, 5, 5);
		gbc_courseIDLabel.anchor = GridBagConstraints.EAST;
		gbc_courseIDLabel.gridx = 0;
		gbc_courseIDLabel.gridy = 1;
		coursePanel.add(courseIDLabel, gbc_courseIDLabel);

		courseIDTextField = new JTextField();
		courseIDTextField.addKeyListener(addCourseButtonEnabler);
		courseIDTextField.setName("courseIDTextField");
		GridBagConstraints gbc_courseIDTextField = new GridBagConstraints();
		gbc_courseIDTextField.insets = new Insets(0, 0, 5, 0);
		gbc_courseIDTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_courseIDTextField.gridx = 1;
		gbc_courseIDTextField.gridy = 1;
		coursePanel.add(courseIDTextField, gbc_courseIDTextField);
		courseIDTextField.setColumns(10);

		courseNameLabel = new JLabel("Name");
		courseNameLabel.setName("courseNameLabel");
		GridBagConstraints gbc_courseNameLabel = new GridBagConstraints();
		gbc_courseNameLabel.anchor = GridBagConstraints.EAST;
		gbc_courseNameLabel.insets = new Insets(0, 0, 5, 5);
		gbc_courseNameLabel.gridx = 0;
		gbc_courseNameLabel.gridy = 2;
		coursePanel.add(courseNameLabel, gbc_courseNameLabel);

		courseNameTextField = new JTextField();
		courseNameTextField.addKeyListener(addCourseButtonEnabler);
		courseNameTextField.setName("courseNameTextField");
		GridBagConstraints gbc_courseNameTextField = new GridBagConstraints();
		gbc_courseNameTextField.insets = new Insets(0, 0, 5, 0);
		gbc_courseNameTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_courseNameTextField.gridx = 1;
		gbc_courseNameTextField.gridy = 2;
		coursePanel.add(courseNameTextField, gbc_courseNameTextField);
		courseNameTextField.setColumns(10);

		courseCFUsLabel = new JLabel("CFU");
		courseCFUsLabel.setName("courseCFUsLabel");
		GridBagConstraints gbc_courseCFUsLabel = new GridBagConstraints();
		gbc_courseCFUsLabel.anchor = GridBagConstraints.EAST;
		gbc_courseCFUsLabel.insets = new Insets(0, 0, 5, 5);
		gbc_courseCFUsLabel.gridx = 0;
		gbc_courseCFUsLabel.gridy = 3;
		coursePanel.add(courseCFUsLabel, gbc_courseCFUsLabel);

		courseCFUsTextField = new JTextField();
		courseCFUsTextField.addKeyListener(addCourseButtonEnabler);
		courseCFUsTextField.setName("courseCFUsTextField");
		GridBagConstraints gbc_courseCFUsTextField = new GridBagConstraints();
		gbc_courseCFUsTextField.insets = new Insets(0, 0, 5, 0);
		gbc_courseCFUsTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_courseCFUsTextField.gridx = 1;
		gbc_courseCFUsTextField.gridy = 3;
		coursePanel.add(courseCFUsTextField, gbc_courseCFUsTextField);
		courseCFUsTextField.setColumns(10);

		addCourseButton = new JButton("Add Course");
		addCourseButton.addActionListener(addCourseButtonListener);
		addCourseButton.setEnabled(false);
		addCourseButton.setName("addCourseButton");
		GridBagConstraints gbc_addCourseButton = new GridBagConstraints();
		gbc_addCourseButton.gridwidth = 2;
		gbc_addCourseButton.insets = new Insets(0, 0, 5, 0);
		gbc_addCourseButton.gridx = 0;
		gbc_addCourseButton.gridy = 4;
		coursePanel.add(addCourseButton, gbc_addCourseButton);

		coursesScrollPane = new JScrollPane();
		GridBagConstraints gbc_coursesScrollPane = new GridBagConstraints();
		gbc_coursesScrollPane.insets = new Insets(0, 0, 5, 0);
		gbc_coursesScrollPane.fill = GridBagConstraints.BOTH;
		gbc_coursesScrollPane.gridwidth = 2;
		gbc_coursesScrollPane.gridx = 0;
		gbc_coursesScrollPane.gridy = 5;
		coursePanel.add(coursesScrollPane, gbc_coursesScrollPane);

		coursesListModel = new DefaultListModel<Course>();
		coursesList = new JList<Course>(coursesListModel);
		coursesList.addListSelectionListener(coursesListListener);
		coursesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		coursesList.setName("coursesList");
		coursesScrollPane.setViewportView(coursesList);

		deleteCourseButton = new JButton("Delete Course");
		deleteCourseButton.addActionListener(removeCourseButtonListener);
		deleteCourseButton.setEnabled(false);
		deleteCourseButton.setName("deleteCourseButton");
		GridBagConstraints gbc_deleteCourseButton = new GridBagConstraints();
		gbc_deleteCourseButton.insets = new Insets(0, 0, 5, 0);
		gbc_deleteCourseButton.gridwidth = 2;
		gbc_deleteCourseButton.gridx = 0;
		gbc_deleteCourseButton.gridy = 6;
		coursePanel.add(deleteCourseButton, gbc_deleteCourseButton);

		courseInfoErrorMessageLabel = new JLabel("");
		courseInfoErrorMessageLabel.setName("courseInfoErrorMessageLabel");
		GridBagConstraints gbc_courseInfoErrorMessageLabel = new GridBagConstraints();
		gbc_courseInfoErrorMessageLabel.gridwidth = 2;
		gbc_courseInfoErrorMessageLabel.insets = new Insets(0, 0, 0, 5);
		gbc_courseInfoErrorMessageLabel.gridx = 0;
		gbc_courseInfoErrorMessageLabel.gridy = 7;
		coursePanel.add(courseInfoErrorMessageLabel, gbc_courseInfoErrorMessageLabel);
	}

	@Override
	public void showStudentError(String message, Student student) {
		studentInfoErrorMessageLabel.setForeground(Color.red);
		studentInfoErrorMessageLabel.setText(message + ": " + student);
	}

	@Override
	public void showAllStudents(List<Student> students) {
		studentsListModel.clear();
		students.stream().forEach(studentsListModel::addElement);
	}

	@Override
	public void studentAdded(String message, Student student) {
		studentsListModel.addElement(student);
		studentInfoErrorMessageLabel.setForeground(Color.black);
		studentInfoErrorMessageLabel.setText(message + ": " + student);
	}

	@Override
	public void studentRemoved(String message, Student student) {
		studentsListModel.removeElement(student);
		studentInfoErrorMessageLabel.setForeground(Color.black);
		studentInfoErrorMessageLabel.setText(message + ": " + student);
	}

	@Override
	public void showCourseError(String message, Course course) {
		courseInfoErrorMessageLabel.setForeground(Color.red);
		courseInfoErrorMessageLabel.setText(message + ": " + course);
	}

	@Override
	public void showAllCourses(List<Course> courses) {
		coursesListModel.clear();
		courses.stream().forEach(coursesListModel::addElement);
	}

	@Override
	public void courseAdded(String message, Course course) {
		coursesListModel.addElement(course);
		courseInfoErrorMessageLabel.setForeground(Color.black);
		courseInfoErrorMessageLabel.setText(message + ": " + course);
	}

	@Override
	public void courseRemoved(String message, Course course) {
		coursesListModel.removeElement(course);
		courseInfoErrorMessageLabel.setForeground(Color.black);
		courseInfoErrorMessageLabel.setText(message + ": " + course);
	}

}
