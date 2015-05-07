
public class Main {
	final static long start = System.currentTimeMillis();
	public static void main(String[] args){
		// DB 연결 
		System.out.println("DB 연결 시간: " + (System.currentTimeMillis() - start));
		DB db = new DB();
		
		// 추천 테이블 갱신
		System.out.println("테이블 갱신 시작 시간: " + (System.currentTimeMillis() - start));
		MakeR mr;
		if(args[0] != null)
			mr = new MakeR(db, Integer.valueOf(args[0]));
		else
			mr = new MakeR(db);
		
		long end = System.currentTimeMillis();
		System.out.println( "실행 시간 : " + ( end - start ) + "ms" );
		
		// DB 연결 닫기 
		db.closeConnection();
	}
}
