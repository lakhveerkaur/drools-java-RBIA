package com.javainuse.main;

import java.sql.Connection;
import java.sql.DriverManager;

public class Dbconnection {
	public static Connection getconnection(){
		Connection conn = null;
		try{
	         Class.forName("com.mysql.jdbc.Driver");
	        
	         conn = DriverManager
	                 .getConnection("jdbc:mysql://localhost/tjx_db?"
	                         + "user=root&password=1234");
	      
	     }
	     catch(Exception exe){
	         exe.printStackTrace();
	     }
		return conn;
	
	}
}
