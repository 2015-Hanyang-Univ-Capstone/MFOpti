import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;


public class MakeR {
	private ArrayList<HashMap<Integer,Double>> R;
	private DB db;
	private Connection con;
	private PreparedStatement pstmt;
	private ResultSet rs;
	private int user_id;
	private int userNum;
	private int songNum;
	private MF mf;

	private HashMap<Integer, Integer> user_id_hashmap;
	private HashMap<String, Integer> song_id_hashmap;
	 
	public MakeR(DB _db, int user_id){
		this.db = _db;
		this.user_id = user_id;
		con = db.getConnection();
		pstmt = db.getPstmt();
		rs = db.getRs();
		R = new ArrayList<HashMap<Integer,Double>>();
		
		try {
			// Initialize R matrix
			System.out.println("Initialize R matrix: " + (System.currentTimeMillis() - Main.start));
			initialR();
			
			// Initialize P,Q Matrix
			System.out.println("Initialize P,Q Matrix: " + (System.currentTimeMillis() - Main.start));
			mf = new MF(R, songNum);
			
			// Update recommend table
			System.out.println("Update recommend table: " + (System.currentTimeMillis() - Main.start));
			if(user_id == -1)
				mf.writeRecommendTable(db, user_id_hashmap, song_id_hashmap);
			else
				mf.writeRecommendTable(db, user_id_hashmap, song_id_hashmap, user_id);
			
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			db.closeConnection();
		}
		
	}
	
	
	private void initialR() throws SQLException{
		StringBuilder row = new StringBuilder();
		StringBuilder col = new StringBuilder();

		user_id_hashmap = new HashMap<>();
		song_id_hashmap = new HashMap<>();
		
		con = db.getConnection();
		
		// Count users
		row.append("select count(*) as row\n");
		row.append("from user");
		pstmt=con.prepareStatement(row.toString());
		rs = pstmt.executeQuery();
		if(rs.next()){
			userNum = rs.getInt("row");
		}
		
		// Count songs rated at least once
		col.append("select count(distinct id) as col from song INNER JOIN rating\n");
		col.append("on song.id = rating.song_id");
		pstmt=con.prepareStatement(col.toString());
		rs = pstmt.executeQuery();
		if(rs.next())
			songNum = rs.getInt("col");
		
		// Hashmap(user_id -> index)  
		int count = 0;
		StringBuilder name = new StringBuilder();
		name.append("select id\n");
		name.append("from user");
		pstmt = con.prepareStatement(name.toString());
		rs = pstmt.executeQuery();
		while(rs.next()){
			user_id_hashmap.put(rs.getInt("id"), count++);
			
			// Add user to R matrix
			R.add(new HashMap<Integer,Double>());
		}
		pstmt.close();
		rs.close();
		
		// Hashmap(song_id -> index)
		count = 0;
		StringBuilder song = new StringBuilder();
		song.append("select distinct id from song INNER JOIN rating\n");
		song.append("on song.id = rating.song_id");
		pstmt = con.prepareStatement(song.toString());
		rs = pstmt.executeQuery();
		while(rs.next())
			song_id_hashmap.put(rs.getString("id"), count++);
		
		// Initialize R
		StringBuilder data = new StringBuilder();
		data.append("select user_id, song_id, rating\n");
		data.append("from rating\n");
		data.append("order by user_id\n");
		pstmt = con.prepareStatement(data.toString());
		rs = pstmt.executeQuery();
		count = 0;
		while(rs.next()){
			if(user_id_hashmap.get(rs.getInt("user_id")) == null)
				System.out.println("user_id exception: ");
			else if(song_id_hashmap.get(rs.getString("song_id")) == null)
				System.out.println("song_id exception");
			else
				R.get(user_id_hashmap.get(rs.getInt("user_id"))).put(song_id_hashmap.get(rs.getString("song_id")), rs.getDouble("rating"));
		}
		pstmt.close();
		rs.close();
	}
}
