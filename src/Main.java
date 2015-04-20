
public class Main {
	public static void main(String[] args){
		// DB 연결 
		DB db = new DB();
		
		long start = System.currentTimeMillis();
		
		// 추천 테이블 갱신
		MakeR mr = new MakeR(db);
		
		long end = System.currentTimeMillis();
		System.out.println( "실행 시간 : " + ( end - start ) + "ms" );
		
		// DB 연결 닫기 
		db.closeConnection();
	}
}
