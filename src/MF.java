import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Random;

public class MF{
	private static int N;
	private static int M;
	private static int K;
	private static int NUMBER_OF_SONG;
	
	private ArrayList<HashMap<Integer,Double>> R = null;
	private static double[][] P = null;
	private static double[][] Q = null;
	
    public MF(ArrayList<HashMap<Integer,Double>> R, int songNum){
    	N = R.size();
    	M = songNum;
    	K = 2;
    	this.NUMBER_OF_SONG = songNum;
    	this.R = R;
    	makePQ();
    }
    
    private void makePQ(){
    	// Make P, Q matrix
    	int i, j, z;
    	P = new double[N][K];
    	Q = new double[K][M];
    	Random rand = new Random();
    	
    	for(i=0; i<N; i++)
    		for(j=0; j<K; j++)
    			P[i][j] = rand.nextDouble();
    	
    	for(i=0; i<K; i++)
    		for(j=0; j<M; j++)
    			Q[i][j] = rand.nextDouble();
    	
    	//Matrix factorization
    	//double alpha=0.0002;
    	//double beta=0.02;
    	double eij=0;
    	double e;
    	double sigmaPQ;
    	for(int step=0;step<5000;step++){
    		System.out.println("\t:step"+step);
    		for(i=0;i<N;i++){
    			for(Entry<Integer, Double> t : R.get(i).entrySet()){
    				sigmaPQ=0;
    				j = t.getKey();
					for(z=0;z<K;z++){
						sigmaPQ+=P[i][z]*Q[z][j];
					}
					eij=(t.getValue()-sigmaPQ) * 2;
					for(z=0;z<K;z++){
						//P[i][z]=P[i][z]+alpha*(eij*Q[z][j]-beta*P[i][z]);
						//Q[z][j]=Q[z][j]+alpha*(eij*P[i][z]-beta*Q[z][j]);
						P[i][z]=P[i][z]+0.0002*(eij*Q[z][j]-0.02*P[i][z]);
						Q[z][j]=Q[z][j]+0.0002*(eij*P[i][z]-0.02*Q[z][j]);
					}
    			}
    		}
    		
    		e=0;
    		for(i=0;i<N;i++){
    			for(Entry<Integer, Double> t : R.get(i).entrySet()){
    				sigmaPQ=0;
    				j = t.getKey();
					for(z=0;z<K;z++){
						sigmaPQ+=P[i][z]*Q[z][j];
					}
					e += Math.pow(t.getValue()-sigmaPQ,2);
					for(z=0;z<K;z++){
						//e += (beta/2)*(P[i][z]*P[i][z]+Q[z][j]*Q[z][j]);
						e += (0.01)*(P[i][z]*P[i][z]+Q[z][j]*Q[z][j]);
					}
    			}
    		}
    		if(e<0.001)
    			break;
    	}
    	
    }
    
    public void writeRecommendTable(DB db, HashMap<Integer, Integer> user_id_hashmap, HashMap<String, Integer> song_id_hashmap) throws SQLException{
    	int user_id, user_id_index, song_id_index, z;
    	int acc;
    	String song_id;
    	Connection con = db.getConnection();
    	PreparedStatement pstmt = db.getPstmt();
    	StringBuilder query = new StringBuilder();
    	
    	query.append("create table recom(");
    	query.append("user_id int(11) not null,");
    	query.append("song_id varchar(255) not null,");
    	query.append("rating int(11) not null,");
    	query.append("primary key (user_id,song_id))");
    	pstmt = con.prepareStatement(query.toString());
		pstmt.executeUpdate();
		
		// Update recommend table
    	for(Entry<Integer, Integer> user_set : user_id_hashmap.entrySet()){
    		user_id = user_set.getKey();
    		user_id_index = user_set.getValue();
    		
    		// for dummy data
    		if(user_id > 5000)
    			break;
    		
    		for(Entry<String, Integer> song_set : song_id_hashmap.entrySet()){
    			song_id = song_set.getKey();
    			song_id_index = song_set.getValue();
    		
    			// for(z=0; z<K; z++)
        		//	acc += P[user_id_index][z]*Q[z][song_id_index];
    			//
    			// K = 2
    			acc = (int) Math.round((P[user_id_index][0]*Q[0][song_id_index] + P[user_id_index][1]*Q[1][song_id_index])*10);
    			
    			System.out.println(acc);
    			if(acc < 60)
    				continue;
    			
    			// Add record
        		query.setLength(0);
        		query.append("insert into recom (user_id, song_id, rating) values (")
        		.append(user_id).append(", \"")
        		.append(song_id).append("\", ")
        		.append(acc).append(")");
        		pstmt = con.prepareStatement(query.toString());
        		pstmt.executeUpdate();
        		pstmt.close();
        		System.out.println("\t:"+user_id+","+song_id);
    		}
    	}
    	
    	// Delete existing recommend table 
    	query.setLength(0);
    	query.append("drop table recommend");
    	pstmt = con.prepareStatement(query.toString());
		pstmt.executeUpdate();
		
		// Rename new recommend table
		query.setLength(0);
		query.append("rename table recom to recommend");
    	pstmt = con.prepareStatement(query.toString());
		pstmt.executeUpdate();
    }
    
    public void writeRecommendTable(DB db, HashMap<Integer, Integer> user_id_hashmap, HashMap<String, Integer> song_id_hashmap, int user_id) throws SQLException{
    	int user_id_index, song_id_index, z;
    	int acc;
    	String song_id;
    	Connection con = db.getConnection();
    	PreparedStatement pstmt = db.getPstmt();
    	ResultSet rs = db.getRs();
    	StringBuilder query = new StringBuilder();
    	
		user_id_index = user_id_hashmap.get(user_id);
		for(Entry<String, Integer> song_set : song_id_hashmap.entrySet()){
			song_id = song_set.getKey();
			song_id_index = song_set.getValue();

			// for(z=0; z<K; z++)
    		//	acc += P[user_id_index][z]*Q[z][song_id_index];
			//
			// K = 2
			acc = (int) Math.round((P[user_id_index][0]*Q[0][song_id_index] + P[user_id_index][1]*Q[1][song_id_index])*10);
			
			System.out.println(acc);
			if(acc < 60){
				// if expect score is under 60, delete record from rating table
				query.setLength(0);
	    		query.append("select * from recommend where user_id = ").append(user_id)
	    		.append(" and song_id = \"").append(song_id).append("\"");
	    		pstmt = con.prepareStatement(query.toString());
	    		rs = pstmt.executeQuery();
	    		if(rs.next()){
	    			query.setLength(0);
	    			query.append("delete from recommend where user_id = ").append(user_id)
		    		.append(" and song_id = \"").append(song_id).append("\"");
	    			pstmt = con.prepareStatement(query.toString());
	    			pstmt.executeUpdate();
	    			pstmt.close();
	    			System.out.println("\t:"+user_id+","+song_id);
	    		}
			}
			else{
				// Add record
				query.setLength(0);
				query.append("insert into recommend (user_id, song_id, rating) values(").append(user_id)
				.append(", \"").append(song_id).append("\", ").append(acc).append(")")
				.append("on duplicate key update rating = ").append(acc);
	    		pstmt = con.prepareStatement(query.toString());
	    		pstmt.executeUpdate();
	    		pstmt.close();
	    		System.out.println("\t:"+user_id+","+song_id);
			}
		}	
    }
}
