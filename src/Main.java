public class Main
{
	final static long start = System.currentTimeMillis();
	public static void main(String[] args)
	{
		// DB Connection
		System.out.println("DB Connection: " + (System.currentTimeMillis() - start));
		DB db = new DB();

		// Make recommend table
		System.out.println("Start making recommend table: " + (System.currentTimeMillis() - start));
		new MakeR(db, (args.length == 1)? Integer.valueOf(args[0]) : -1);
		
		// Close db connection
		System.out.println("End : " + ( System.currentTimeMillis() - start ) + "ms" );
		db.closeConnection();
	}
}
