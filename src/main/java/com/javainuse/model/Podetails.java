package com.javainuse.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Period;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.javainuse.main.Dbconnection;

public class Podetails {
	String po_id;
	String sku_id;
	String ordered_quantity;
	int count = 0;
	Connection connect = Dbconnection.getconnection();
	String postatus = new String();
	String typeofdelivery = new String();
	public void setSpeed(Long days,int expressdelivery)
	{
		if (days > expressdelivery) {
			typeofdelivery = "normal";
		} else {
			typeofdelivery = "express";
		}
	}
    
	public void vendorDetail(String poid) {
		System.out.println("Booking Rule is fired!");
		String poId = poid;
		String vendorId = new String();
		String vendorCity = new String();
		String carrierID = new String();
		int normaldelivery = 0;
		int expressdelivery = 0;
		Long days = (long) 0;
		Date pickupdate;
		Date deliverydate;
		long diff;
		

		{
			try {
				Statement statement = connect.createStatement();
				ResultSet rst1 = statement.executeQuery(
						"select vendor_id,po_pickup_date,po_delivery_date ,bo_status from bo_data where po_id='" + poId
								+ "'");
				while (rst1.next()) {
					vendorId = rst1.getString("vendor_id");
					pickupdate = rst1.getDate("po_pickup_date");
					deliverydate = rst1.getDate("po_delivery_date");
					diff = deliverydate.getTime() - pickupdate.getTime();
					days = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
					postatus = rst1.getString("bo_status");
				}

				Statement st1 = connect.createStatement();
				ResultSet rst2 = st1.executeQuery(
						"select vendor_city,carrier_id,normal_delivery,express_delivery from vendor_table where vendor_id='"
								+ vendorId + "'");
				while (rst2.next()) {
					vendorCity = rst2.getString("vendor_city");
					carrierID = rst2.getString("carrier_id");
					normaldelivery = rst2.getInt("normal_delivery");
					expressdelivery = rst2.getInt("express_delivery");

				}
				setSpeed(days, expressdelivery);        
				
				PreparedStatement preparedStmt1 = connect.prepareStatement(
						"update bo_data set type_of_delivery='" + typeofdelivery + "'where po_id='" + poId + "'");
				preparedStmt1.executeUpdate();
				PreparedStatement preparedStmt = connect.prepareStatement("update bo_data set origin='" + vendorCity
						+ "',carrier_id='" + carrierID + "'where po_id='" + poId + "'");
				preparedStmt.executeUpdate();

			} catch (Exception exe) {
				exe.printStackTrace();
			}
		}
	}

	public void allocationDetail(String pid) {
		System.out.println("\nAllocation Rule is fired!");
		String pcid = new String();
		String skuId = new String();
		int quantity = 0;
		String categoryId = new String();
		try {
			Statement statement = connect.createStatement();

			ResultSet rst1 = statement
					.executeQuery(" select sku_id,ordered_quantity from po_details where po_id='" + pid + "'");
			while (rst1.next()) {
				skuId = rst1.getString("sku_id");
				quantity = rst1.getInt("ordered_quantity");
			}

			System.out.println("List of all the SKU ID's with corressponding CategoryID's:");
			Statement st1 = connect.createStatement();
			ResultSet rst2 = st1.executeQuery("select product_category_id from sku_table where sku_id='" + skuId + "'");
			while (rst2.next()) {
				categoryId = rst2.getString("product_category_id");
				System.out.println(skuId + " : " + categoryId);
			}
			Statement cannot_handle_st = connect.createStatement();
			ResultSet cannot_handle_rst = cannot_handle_st.executeQuery(
					"select pc_id from pc_needs where cannot_handle_list like '" + '%' + categoryId + '%' + "'");
			System.out.println("List of PC's that CANNOT HANDLE the SKU category: ");
			while (cannot_handle_rst.next()) {
				pcid = cannot_handle_rst.getString("pc_id");
				System.out.println(skuId + " : " + pcid);
			}

			Statement st2 = connect.createStatement();
			ResultSet rst3 = st2
					.executeQuery("select shortfall_quantity,pc_id from inventory_view where product_category_id='"
							+ categoryId + "'and pc_id != '" + pcid + "' order by shortfall_quantity desc");
			System.out.println("List of PC's that can be allotted!");
			
			Inventory inventoryObject[] = new Inventory[4];
			for (int i = 0; rst3.next(); i++) {

				inventoryObject[i] = new Inventory();
				inventoryObject[i].setSfq(rst3.getInt("shortfall_quantity"));
				inventoryObject[i].setPc_id(rst3.getString("pc_id"));
				System.out.println("PC ID: "+rst3.getString("pc_id")+" ShortFall Quantity: "+rst3.getInt("shortfall_quantity"));
			}
			if (postatus.equals("not allocated")) {
				if (quantity <= inventoryObject[0].sfq) {
					inventoryObject[0].sfq = inventoryObject[0].sfq - quantity;
					int finalsfq = inventoryObject[0].sfq;
					String finalpcid = inventoryObject[0].pc_id;
					System.out.println("Final allocated PC: " + finalpcid);
					PreparedStatement preparedStmt2 = connect
							.prepareStatement("update inventory_view set shortfall_quantity='" + finalsfq
									+ "'where pc_id='" + finalpcid + "' AND product_category_id='" + categoryId + "'");
					preparedStmt2.executeUpdate();

					PreparedStatement preparedStmt1 = connect.prepareStatement("update bo_data set destination='"
							+ finalpcid + "',bo_status='not confirmed' where po_id='" + pid + "'");
					preparedStmt1.executeUpdate();
				} else {
					PreparedStatement preparedStmt1 = connect
							.prepareStatement("update bo_data set bo_status='failed' where po_id='" + pid + "'");
					preparedStmt1.executeUpdate();
					System.out.println("Failed to allocate PC because Ordered Quantity was greater than the Shortfall Quantity for every PC!");
				}
				System.out.println("\n");
			}

		} catch (Exception exe) {
			exe.printStackTrace();
		}

	}

	public String getPo_id() {
		return po_id;
	}

	public void setPo_id(String po_id) {
		this.po_id = po_id;
	}

	public String getSku_id() {
		return sku_id;
	}

	public void setSku_id(String sku_id) {
		this.sku_id = sku_id;
	}

	public String getOrdered_quantity() {
		return ordered_quantity;
	}

	public void setOrdered_quantity(String ordered_quantity) {
		this.ordered_quantity = ordered_quantity;
	}

}
