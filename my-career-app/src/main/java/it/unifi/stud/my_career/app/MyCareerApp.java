package it.unifi.stud.my_career.app;

import java.awt.EventQueue;
import java.util.Scanner;
import java.util.concurrent.Callable;

import it.unifi.stud.my_career.view.cli.MyCareerCLIView;
import it.unifi.stud.my_career.view.swing.MyCareerSwingView;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(mixinStandardHelpOptions = true)
public class MyCareerApp implements Callable<Void>{
	
	// TODO inserire parametri per il DB (mongoHost, mongoPort, databaseName, collectionName)
	
	// Command Line argument for UI
	@Option(names = {"--ui", "--user-interface"}, description = "User Interface type (allowable values: gui, cli)")
	private String userInterface = "gui";
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		new CommandLine(new MyCareerApp()).execute(args);
	}

	@Override
	public Void call() throws Exception {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					
					// TODO inserire creazione DB/Repository
					
					if(!userInterface.equals("gui") && !userInterface.equals("cli")) {
						System.err.println("Invalid value for option '--ui/--user-interface'\nUse --ui=gui or --ui=cli");
						System.exit(1);
					}
					
					if(userInterface.equals("gui")) {
						MyCareerSwingView frame = new MyCareerSwingView();
						// TODO inserire creazione controller e setMyCareerController
						frame.setVisible(true);
					}
					
					if(userInterface.equals("cli")) {
						MyCareerCLIView cli = new MyCareerCLIView(System.in, System.out);
						// TODO inserire creazione controller e setMyCareerController
						
						String choice;
						Scanner scanner = new Scanner(System.in);
						
						do {
							cli.showMenu();
							System.out.print("Enter a valid digit: ");
							choice = scanner.nextLine();
							switch (choice) {
							case "1":
								cli.addStudent();
								break;
							case "2":
								cli.getAllStudents();
								break;
							case "3":
								cli.deleteStudent();
								break;
							case "4":
								cli.getCoursesByStudent();
								break;
							case "5":
								cli.addCourse();
								break;
							case "6":
								cli.removeCourse();
								break;
							case "7":
								cli.getStudentsByCourse();
								break;
							case "8":
								System.out.println("Goodbye");
								break;
							default:
								System.out.println("Not a valid input");
								break;
							}
						} while (!choice.equals("8"));
						
						scanner.close();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		return null;
	}
}