import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

public class MakeR
{
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
	 
	public MakeR(DB _db, int user_id)
	{
		this.db = _db;
		this.user_id = user_id;
		con = db.getConnection();
		pstmt = db.getPstmt();
		rs = db.getRs();
		R = new ArrayList<HashMap<Integer,Double>>();
		
		try
		{
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
	
	
	private void initialR() throws SQLException
	{
		StringBuilder query = new StringBuilder();
		user_id_hashmap = new HashMap<>();
		song_id_hashmap = new HashMap<>();
		con = db.getConnection();
		
		// Count users
		query.append("select count(*) as row\n");
		query.append("from user");
		pstmt=con.prepareStatement(query.toString());
		rs = pstmt.executeQuery();
		if(rs.next())
			userNum = rs.getInt("row");
		
		// Count songs rated at least once
		query.setLength(0);
		query.append("select count(distinct id) as col from song INNER JOIN rating\n");
		query.append("on song.id = rating.song_id");	
		pstmt=con.prepareStatement(query.toString());
		rs = pstmt.executeQuery();
		if(rs.next())
			songNum = rs.getInt("col");
		
		// Hashmap(user_id -> index)  
		int count = 0;
		query.setLength(0);
		query.append("select id\n");
		query.append("from user");
		pstmt = con.prepareStatement(query.toString());
		rs = pstmt.executeQuery();
		while(rs.next())
		{
			user_id_hashmap.put(rs.getInt("id"), count++);
			
			// Add user to R matrix
			R.add(new HashMap<Integer,Double>());
		}
		
		// Hashmap(song_id -> index)
		count = 0;
		query.setLength(0);
		query.append("select distinct id from song INNER JOIN rating\n");
		query.append("on song.id = rating.song_id");
		pstmt = con.prepareStatement(query.toString());
		rs = pstmt.executeQuery();
		while(rs.next())
			song_id_hashmap.put(rs.getString("id"), count++);
		
		// Initialize R
		query.setLength(0);
		query.append("select user_id, song_id, rating\n");
		query.append("from rating\n");
		query.append("order by user_id\n");
		pstmt = con.prepareStatement(query.toString());
		rs = pstmt.executeQuery();
		count = 0;
		while(rs.next())
		{
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
