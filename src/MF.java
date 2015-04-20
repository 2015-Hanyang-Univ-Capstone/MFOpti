import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class MF{
	private static int N;
	private static int M;
	private static int K;
	private static int NUMBER_OF_SONG;
	
	private ArrayList<HashMap<Integer,Double>> R = null;
	private double[][] P = null;
	private double[][] Q = null;
	
    public MF(ArrayList<HashMap<Integer,Double>> R, int songNum){
    	N = R.size();
    	M = songNum;
    	K = 2;
    	this.NUMBER_OF_SONG = songNum;
    	this.R = R;
    	makePQ();
    }
    
    private void makePQ(){
    	//P,Q 배열 만들
    	P = new double[N][K];
    	Q = new double[K][M];
    	Random rand = new Random();
    	
    	for(double pi[] : P)
    		for(double pij : pi)
    			pij = rand.nextDouble();
    	
    	for(double qi[] : Q)
    		for(double qij : qi)
    			qij = rand.nextDouble();
    	
    	//matrix factorization
    	int i, z;
    	int temp_R_size;
    	HashMap<Integer,Double> temp_R;
    	
    	double alpha=0.0002;
    	double beta=0.02;
    	double eij=0;
    	double e;
    	double sigmaPQ;
    	for(int step=0;step<5000;step++){
    		for(i=0;i<N;i++){
    			temp_R = R.get(i);
    			temp_R_size = temp_R.size();
    			for(int j : temp_R.keySet()){
    				sigmaPQ=0;
					for(z=0;z<K;z++){
						sigmaPQ+=P[i][z]*Q[z][j];
					}
					eij=temp_R.get(j)-sigmaPQ;
					for(z=0;z<K;z++){
						P[i][z]=P[i][z]+alpha*(2*eij*Q[z][j]-beta*P[i][z]);
						Q[z][j]=Q[z][j]+alpha*(2*eij*P[i][z]-beta*Q[z][j]);
					}
    			}
    		}
    		e=0;
    		for(i=0;i<N;i++){
    			temp_R = R.get(i);
    			temp_R_size = temp_R.size();
    			
    			for(int j : temp_R.keySet()){
    				sigmaPQ=0;
					for(z=0;z<K;z++){
						sigmaPQ+=P[i][z]*Q[z][j];
					}
					e += Math.pow(temp_R.get(j)-sigmaPQ,2);
					for(z=0;z<K;z++){
						e += (beta/2)*(Math.pow(P[i][z],2)+Math.pow(Q[z][j],2));
					}
    			}
    		}
    		if(e<0.001)
    			break;
    	}
    }
    
    public void writeRecommendTable(DB db, HashMap<Integer, Integer> user_id_hashmap, HashMap<String, Integer> song_id_hashmap) throws SQLException{
    	int user_id_index, song_id_index, z;
    	int acc;
    	Connection con = db.getConnection();
    	PreparedStatement pstmt = db.getPstmt();
    	StringBuilder query = new StringBuilder();
    	
    	// 추천 테이블 초기화 
    	//query.append("delete from recommend");
    	query.append("create table recom(");
    	query.append("user_id int(11) not null,");
    	query.append("song_id varchar(255) not null,");
    	query.append("rating int(11) not null,");
    	query.append("primary key (user_id,song_id))");
    	pstmt = con.prepareStatement(query.toString());
		pstmt.executeUpdate();
		
		// 추천 테이블 채우기 
    	for(int user_id : user_id_hashmap.keySet()){
    		user_id_index = user_id_hashmap.get(user_id);
    		for(String song_id : song_id_hashmap.keySet()){
    			song_id_index = song_id_hashmap.get(song_id);
    			acc = 0;
    			
    			// K = 2
    			// for(z=0; z<K; z++)
        		//	acc += P[user_id_index][z]*Q[z][song_id_index];
    			acc = (int) Math.round((P[user_id_index][0]*Q[0][song_id_index] + P[user_id_index][1]*Q[1][song_id_index]) * 10);
    			
    			// 레코드 추가 
        		query.setLength(0);
        		query.append("insert into recom (user_id, song_id, rating) values (")
        		.append(user_id).append(", \"")
        		.append(song_id).append("\", ")
        		.append(acc).append(")");
        		pstmt = con.prepareStatement(query.toString());
        		pstmt.executeUpdate();
        		pstmt.close();
    		}
    	}
    	
    	//기존테이블 삭제 
    	query.setLength(0);
    	query.append("drop table recommend");
    	pstmt = con.prepareStatement(query.toString());
		pstmt.executeUpdate();
		
		//새로운 테이블에 이름 바꾸기  
		query.setLength(0);
		query.append("rename table recom to recommend");
    	pstmt = con.prepareStatement(query.toString());
		pstmt.executeUpdate();
    }
}
