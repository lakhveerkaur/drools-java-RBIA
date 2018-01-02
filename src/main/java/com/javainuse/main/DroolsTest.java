package com.javainuse.main;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.FactHandle;

import com.javainuse.model.Podetails;

public class DroolsTest {

	public static final void main(String[] args) {
		List<String> po_id = new ArrayList<String>();
		Connection connect = Dbconnection.getconnection();
		try {
			Statement statement = connect.createStatement();
			ResultSet rst = statement.executeQuery("select po_id from bo_data");
			while (rst.next()) {
				po_id.add(rst.getString("po_id"));
			}
		} catch (Exception exe) {
			exe.printStackTrace();
		}

		try {
			KieServices ks = KieServices.Factory.get();
			KieContainer kContainer = ks.getKieClasspathContainer();
			KieSession kSession = kContainer.newKieSession("ksession-rule");
			FactHandle fact1;
			Podetails[] po = new Podetails[po_id.size()];
			for (int i = po_id.size() - 1; i >= 0; i--) {
				po[i] = new Podetails();
				po[i].setPo_id(po_id.get(i));

				fact1 = kSession.insert(po[i]);
			}
			kSession.fireAllRules();

		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
}