import java.math.BigDecimal;
import java.sql.*;
import java.util.Scanner;

public class DbApp {
	public Connection conn;
	public Scanner scn;
	public static DbApp db;

	public DbApp() {
		conn = null;
	}

	public void dbConnect(String ip, int port, String database, String username, String password) {
		try {
			// Check if postgres driver is loaded
			Class.forName("org.postgresql.Driver");
			// Establish connection with the database
			conn = DriverManager.getConnection("jdbc:postgresql://" + ip + ":" + port + "/" + database, username,
					password);
			System.out.println("Connection Established!");
			// Disable autocommit.
			conn.setAutoCommit(false);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void commit() {
		try {
			// Commit all changes
			conn.commit();
			System.out.println("Changes commited Successfully");
		} catch (SQLException e) {
			System.out.println("Changes commit Failed");
			e.printStackTrace();
		}
	}

	public void abort() {
		try {
			// Rollback all changes
			conn.rollback();
			System.out.println("Changes rolled back Successfully");
		} catch (SQLException e) {
			System.out.println("Changes rollback Failed");
			e.printStackTrace();
		}
	}

	public void waitForEnter() {
		@SuppressWarnings("resource")
		Scanner scn = new Scanner(System.in);
		System.out.println("Press Enter..");
		scn.nextLine();
	}

	public void showDiploma() {
		try {
			Statement st = conn.createStatement();
			// Execute a simple query
			ResultSet rs = st.executeQuery("select * from diploma;");
			// Read resuts row by row
			while (rs.next()) {
				// Read values of columns in row
				System.out.println(rs.getInt(1) + "|" + rs.getDate(2) + "|" + rs.getBigDecimal(3) + "|"
						+ rs.getString(4) + "|" + rs.getBigDecimal(5) + "|" + rs.getInt(6) + "|" + rs.getInt(7) + "|"
						+ rs.getInt(8) + "|" + rs.getInt(9));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	public void create_Diploma(int supervisorAmka, int professor1Amka, int professor2Amka, String studentAm,
			String thesisTitle) {
		try {
			// Create a SQL query template with parameters
			PreparedStatement pst = conn.prepareStatement("select create_diploma(?,?,?,?,?);");
			pst.setInt(1, supervisorAmka);
			pst.setInt(2, professor1Amka);
			pst.setInt(3, professor2Amka);
			pst.setString(4, studentAm);
			pst.setString(5, thesisTitle);
			pst.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void showFinalGrade(String am, String courseCode) {
		PreparedStatement pst;
		try {
			pst = conn.prepareStatement(
					"select finalgrade from get_final_grades() a join \"Student\" s on a.student_amka=s.amka where a.coursecode=? and s.am=?");
			pst.setString(1, courseCode);
			pst.setString(2, am);
			ResultSet rs = pst.executeQuery();
			while (rs.next()) {
				System.out.println("Final Grade: " + rs.getBigDecimal(1));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void changeGrade(String am, String courseCode, int serialNum, BigDecimal grade) {
		PreparedStatement pst;
		try {
			pst = conn.prepareStatement(
					"update register set exam_grade=? where amka=(select amka from \"Student\" where am=?) and course_code=? and serial_number=?");
			pst.setBigDecimal(1, grade);
			pst.setString(2, am);
			pst.setString(3, courseCode);
			pst.setInt(4, serialNum);
			pst.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void searchPerson(String surname) {
		PreparedStatement pst;
		try {
			pst = conn.prepareStatement(
					"(select amka, surname, name, 'Professor' as faculty from \"Professor\" where surname like concat(?,'%') union select amka, surname, name, 'Labstaff' as faculty from \"Labstaff\" where surname like concat(?,'%') union select amka, surname, name, 'Student' as faculty from \"Student\" where surname like concat(?,'%')) ORDER BY surname");
			pst.setString(1, surname);
			pst.setString(2, surname);
			pst.setString(3, surname);
			ResultSet rs = pst.executeQuery();
			while (rs.next()) {
				System.out
						.println(rs.getInt(1) + "|" + rs.getString(2) + "|" + rs.getString(3) + "|" + rs.getString(4));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void getAnalyticGrades(String am) {
		PreparedStatement pst;
		try {
			pst = conn.prepareStatement(
					"SELECT course_code, exam_grade, lab_grade from register where register_status='pass'::register_status_type and amka=(SELECT amka from \"Student\" where am=?)");
			pst.setString(1, am);
			ResultSet rs = pst.executeQuery();
			while (rs.next()) {
				System.out.println(rs.getString(1) + "|" + rs.getBigDecimal(2) + "|" + rs.getBigDecimal(3));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static int printMenu() {
		System.out.println("Please type the number of the option you want to execute");
		System.out.println("1 - Connect");
		System.out.println("2 - Create Diploma");
		System.out.println("3 - Get Final Grade");
		System.out.println("4 - Change Student's exam grade");
		System.out.println("5 - Search person");
		System.out.println("6 - Get analytic grades");
		System.out.println("7 - Commit changes");
		System.out.println("8 - Abort changes");
		System.out.println("0 - Exit");

		int selection = db.scn.nextInt();
		while (selection < 0 || selection > 8) {
			System.out.println("Oups! No such option. Please try again");
			selection = db.scn.nextInt();
		}
		return selection;
	}

	public static void main(String[] args) {
		db = new DbApp();
		db.scn = new Scanner(System.in);
		String am, courseCode;
		int selection = printMenu();
		while (selection != 0) {
			switch (selection) {
			case 1:
				System.out.println("IP:");
				db.scn.nextLine();
				String ip = db.scn.nextLine();
				System.out.println("Databse Name:");
				String databaseName = db.scn.nextLine();
				System.out.println("Username:");
				String username = db.scn.nextLine();
				System.out.println("Password:");
				String password = db.scn.nextLine();
				db.dbConnect(ip, 5432, databaseName, username, password);
				break;
			case 2:
				System.out.println("Supervisor AMKA:");
				int supervisorAmka = db.scn.nextInt();
				System.out.println("First Professor AMKA:");
				int professor1Amka = db.scn.nextInt();
				System.out.println("Second Professor AMKA:");
				int professor2Amka = db.scn.nextInt();
				System.out.println("Student AM:");
				String studentAm = db.scn.next();
				System.out.println("Thesis Title:");
				String thesisTitle = db.scn.next();
				db.create_Diploma(supervisorAmka, professor1Amka, professor2Amka, studentAm, thesisTitle);
				break;
			case 3:
				System.out.println("Student AM:");
				am = db.scn.next();
				System.out.println("Course code:");
				courseCode = db.scn.next() + " " + db.scn.next();
				db.showFinalGrade(am, courseCode);
				break;
			case 4:
				System.out.println("Student AM:");
				am = db.scn.next();

				System.out.println("Serial Number:");
				int serialNum = db.scn.nextInt();
				System.out.println("New exam grade:");
				BigDecimal grade = db.scn.nextBigDecimal();

				System.out.println("Course code:");
				courseCode = db.scn.next() + " " + db.scn.next();
				db.changeGrade(am, courseCode, serialNum, grade);
				break;
			case 5:
				System.out.println("Surname starting from:");
				String surname = db.scn.next();
				db.searchPerson(surname);
				break;
			case 6:
				System.out.println("Student AM:");
				am = db.scn.next();
				db.getAnalyticGrades(am);
				break;
			case 7:
				db.commit();
				break;
			case 8:
				db.abort();
				break;
			}
			selection = printMenu();
		}
	}
}