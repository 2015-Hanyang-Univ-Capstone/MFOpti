import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DB {
	private Connection con;
	private PreparedStatement pstmt;
	private ResultSet rs;
	
	public DB(){
		con = null;
		pstmt = null;
		rs = null;
		
		try{
			 Class.forName("com.mysql.jdbc.Driver");
			 String url="jdbc:mysql://166.104.245.89:3306/exercise_myp?useUnicode=true&characterEncoding=euckr";
			 con = java.sql.DriverManager.getConnection(url, "root", "root");
			 System.out.println("\tDB CONNECT!\n");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeConnection();
		}
	}
	
	public Connection getConnection(){
		return con;
	}
	
	public PreparedStatement getPstmt(){
		return pstmt;
	}
	
	public ResultSet getRs(){
		return rs;
	}
	
	public void closeConnection(){
		try {
			if(con != null) con.close();
			if(pstmt != null) pstmt.close();
			if(rs != null) rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
