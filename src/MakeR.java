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
	private int userNum;
	private int songNum;
	private MF mf;
	
	private HashMap<Integer, Integer> user_id_hashmap;
	private HashMap<String, Integer> song_id_hashmap;
	
	public MakeR(DB _db){
		this.db = _db;
		con = db.getConnection();
		pstmt = db.getPstmt();
		rs = db.getRs();
		R = new ArrayList<HashMap<Integer,Double>>();
		
		try {
			// 초기 매트릭스 생성
			System.out.println("매트릭스 생성 시간: " + (System.currentTimeMillis() - Main.start));
			initialR();
			
			// P,Q 테이블 갱신
			System.out.println("P,Q 테이블 갱신 시간: " + (System.currentTimeMillis() - Main.start));
			mf = new MF(R, songNum);
			
			// 추천 테이블 갱신
			System.out.println("추천 테이블 갱신 시간: " + (System.currentTimeMillis() - Main.start));
			mf.writeRecommendTable(db, user_id_hashmap, song_id_hashmap);
			
		} catch (SQLException e) {
			e.printStackTrace();
			db.closeConnection();
			System.exit(0);
		}
	}
	
	// 유저 테이블과 곡 테이블로 초기 매트릭스 생성
	private void initialR() throws SQLException{
		StringBuilder row = new StringBuilder();
		StringBuilder col = new StringBuilder();

		user_id_hashmap = new HashMap<>();
		song_id_hashmap = new HashMap<>();
		
		con = db.getConnection();
		
		// 유저 수 계산 
		row.append("select count(*) as row\n");
		row.append("from user");
		pstmt=con.prepareStatement(row.toString());
		rs = pstmt.executeQuery();
		if(rs.next()){
			userNum = rs.getInt("row");
		}
		
		// 곡 수 계산 
		col.append("select count(distinct id) as col\n");
		col.append("from song");
		pstmt=con.prepareStatement(col.toString());
		rs = pstmt.executeQuery();
		if(rs.next())
			songNum = rs.getInt("col");
		
		// (user_id -> index) 해시맵 생성 
		int count = 0;
		StringBuilder name = new StringBuilder();
		name.append("select id\n");
		name.append("from user");
		pstmt = con.prepareStatement(name.toString());
		rs = pstmt.executeQuery();
		while(rs.next()){
			user_id_hashmap.put(rs.getInt("id"), count++);
			
			// R 테이블에 유저 추가
			R.add(new HashMap<Integer,Double>());
		}
		pstmt.close();
		rs.close();
		
		// (song_id -> index) 해시맵 생성 
		count = 0;
		StringBuilder song = new StringBuilder();
		song.append("select distinct id\n");
		song.append("from song\n");
		song.append("order by id");
		pstmt = con.prepareStatement(song.toString());
		rs = pstmt.executeQuery();
		while(rs.next())
			song_id_hashmap.put(rs.getString("id"), count++);
		
		// R 초기화
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
		
		// R 모두 출력
		/*
		count = 0;
		for(HashMap<Integer, Double> song_map : R){
			System.out.println(count);
			for(int key : song_map.keySet()){
				System.out.println("\t"+key+"->"+song_map.get(key));
			}
			count++;
		}
		*/
	}
}
