package net.createk.pdftool;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

public class Client {
	public static String[][] columns = {
		{
			"Short Name",
			"Client Name",
			"Report Name"
		},
		{
			"Net Assets Date"
		},
		{
			"Net Assets USD"
		},
		{
			"Investment Group"
		},
		{
			"Managed External Accounts?"
		},
		{
			"Modifier"
		},
		{
			"Risk Capacity"
		}
	};
	
	public static String[][] fields = {
		{
			"shortName",
			"clientName",
			"reportName"
		},
		{
			"netAssetsDate"
		},
		{
			"netAssetsUSD"
		},
		{
			"investmentGroup"
		},
		{
			"managedExternalAccounts"
		},
		{
			"modifier"
		},
		{
			"riskCapacity"
		}
	};
	
	public Map<String, String> fieldMap = null;
	
	public String shortName = "";
	public String clientName = "";
	public String reportName = "";
	public Date netAssetsDate = null;
	public double netAssetsUSD = 0.0;
	public String investmentGroup = "";
	public boolean managedExternalAccounts = false;
	public int modifier = 0;
	public String riskCapacity = "";
	
	public boolean failed = false;
	
	public Account account = null;
	
	public Client(String shortName, String clientName, String reportName, Date netAssetsDate, double netAssetsUSD, String investmentGroup, boolean managedExternalAccounts, int modifier, String riskCapacity) {
		this.shortName = shortName;
		this.clientName = clientName;
		this.reportName = reportName;
		this.netAssetsDate = netAssetsDate;
		this.netAssetsUSD = netAssetsUSD;
		this.investmentGroup = investmentGroup;
		this.managedExternalAccounts = managedExternalAccounts;
		this.modifier = modifier;
		this.riskCapacity = riskCapacity;
		loadFieldMap();
	}

	public void loadFieldMap() {
		fieldMap = new HashMap<String, String>();

		for (int a = 0; a < columns.length; a++) {
			for (int b = 0; b < columns[a].length; b++) {
				fieldMap.put(columns[a][b], fields[a][b]);
			}
		}
	}
	
	public void writeColumns(Row row) {
		for (int x = 0; x < columns.length; x++) {
			for (int y = 0; y < columns[x].length; y++) {
				Cell cell = null;
				if (row.getLastCellNum() == -1) {
					cell = row.createCell(0);
				} else {
					cell = row.createCell(row.getLastCellNum());
				}
				writeValue(columns[x][y], cell);
			}
		}
	}
	
	public void writeTitles(Row row) {
		for (int x = 0; x < columns.length; x++) {
			for (int y = 0; y < columns[x].length; y++) {
				Cell cell = null;
				if (row.getLastCellNum() == -1) {
					cell = row.createCell(0);
				} else {
					cell = row.createCell(row.getLastCellNum());
				}
				cell.setCellValue(columns[x][y]);
			}
		}
	}
	
	public void writeValue(String column, Cell cell) {
		if (column.equalsIgnoreCase("Managed External Accounts?")) {
			if (managedExternalAccounts) {
				cell.setCellValue("Yes");
			} else {
				cell.setCellValue("No");
			}
		} else {
			Field f = null;
			try {
				f = this.getClass().getDeclaredField(fieldMap.get(column));
			} catch (NoSuchFieldException | SecurityException e) {
				e.printStackTrace();
			}
			try {
				if (column.equalsIgnoreCase("Modifier")) {
					cell.setCellValue(modifier);
					cell.setCellStyle(Account.integerStyle);
				} else if (column.equalsIgnoreCase("Net Assets USD")) {
					cell.setCellValue(netAssetsUSD);
					cell.setCellStyle(Account.investmentStyle);
				} else if (column.equalsIgnoreCase("Net Assets Date")) {
					if (netAssetsDate != null) {
						cell.setCellValue(netAssetsDate);
						cell.setCellStyle(Account.dateStyle);
					} else {
						cell.setCellValue("");
					}
				} else {
					cell.setCellValue((String)f.get(this));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (column.equalsIgnoreCase("Modifier")) {
			cell.setCellStyle(Account.integerStyle);
		} else if (column.equalsIgnoreCase("Net Assets USD")) {
			cell.setCellStyle(Account.investmentStyle);
		}
	}
}
