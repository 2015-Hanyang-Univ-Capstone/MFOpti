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
			 //String url="jdbc:mysql://172.200.152.155:8889/MyPrescience/db?useUnicode=true&characterEncoding=euckr";
			 //String url="jdbc:mysql://172.200.152.155:8889/exercise_myp?useUnicode=true&characterEncoding=euckr";
			 //String url="jdbc:mysql://218.37.209.129:8889/exercise_myp?useUnicode=true&characterEncoding=euckr";
			 String url="jdbc:mysql://166.104.245.89:3306/exercise_myp?useUnicode=true&characterEncoding=euckr";
			 con = java.sql.DriverManager.getConnection(url, "root", "root");
			 System.out.println("DB CONNECT!!\n");
		} catch (Exception e) {
			System.out.print("No Connection!!\n");
			e.printStackTrace();
			
			try{
				con.close();
			} catch (SQLException e2){
				e2.printStackTrace();
			}
			
			System.exit(0);
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
