package net.createk.pdftool;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ClientList {
	protected List<Client> clientList = null;
	protected File listFile = null;

	public ClientList(ClientList other) {
		this(other.listFile);
	}

	public ClientList(File listFile) {
		clientList = new ArrayList<Client>();
		this.listFile = listFile;
		try {
			parse();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void parse() throws IOException {
		if (listFile != null) {
			if (!listFile.exists()) {
				System.out.println("List file does not exist.");
			}
			InputStream in = new FileInputStream(listFile);
			XSSFWorkbook workbook = new XSSFWorkbook(in);
			XSSFSheet master = workbook.getSheetAt(0);
			XSSFRow titleRow = master.getRow(master.getFirstRowNum());

			for (int row = master.getFirstRowNum() + 1; row < master
					.getLastRowNum(); row++) {
				String shortName = "";
				String clientName = "";
				String reportName = "";
				Date netAssetsDate = null;
				double netAssetsUSD = 0.0;
				String investmentGroup = "";
				boolean managedExternalAccounts = false;
				int modifier = 0;
				String riskCapacity = "";

				for (int cell = master.getRow(row).getFirstCellNum(); cell < master
						.getRow(row).getLastCellNum(); cell++) {
					String cellVal = titleRow.getCell(cell)
							.getStringCellValue().trim().replace("\n", "").replace("\r", "");
					if (cellVal.equalsIgnoreCase("shortname")) {
						shortName = master.getRow(row).getCell(cell)
								.getStringCellValue().trim();
					} else if (cellVal.equalsIgnoreCase("name")) {
						clientName = master.getRow(row).getCell(cell)
								.getStringCellValue().trim();
					} else if (cellVal.equalsIgnoreCase("client report name")) {
						reportName = master.getRow(row).getCell(cell)
								.getStringCellValue().trim();
					} else if (cellVal.equalsIgnoreCase("net assets date")) {
						netAssetsDate = HSSFDateUtil.getJavaDate(master.getRow(row).getCell(cell).getNumericCellValue());
					} else if (cellVal.equalsIgnoreCase("net assets usd")) {
						netAssetsUSD = master.getRow(row)
								.getCell(cell).getNumericCellValue();
					} else if (cellVal.equalsIgnoreCase("investment group")) {
						investmentGroup = master.getRow(row).getCell(cell)
								.getStringCellValue().trim();
					} else if (cellVal
							.equalsIgnoreCase("managed external accounts?")
							&& master.getRow(row).getCell(cell)
									.getStringCellValue().trim()
									.equalsIgnoreCase("yes")) {
						managedExternalAccounts = true;
					} else if (cellVal.equalsIgnoreCase("modifier")) {
						modifier = (int)(master.getRow(row).getCell(cell).getNumericCellValue());
					} else if (cellVal.equalsIgnoreCase("risk capacity") || cellVal.equalsIgnoreCase("risk group")) {
						riskCapacity = master.getRow(row).getCell(cell).getStringCellValue().trim();
					}
				}
				clientList.add(new Client(shortName, clientName, reportName, netAssetsDate, netAssetsUSD, investmentGroup, managedExternalAccounts, modifier, riskCapacity));
			}
			in.close();
		}
	}

	public Client getClientByShortName(String shortName) {
		for (Client client : clientList) {
			if (client.shortName.equals(shortName))
				return client;
		}
		return null;
	}

	public Client getClientByClientName(String clientName) {
		for (Client client : clientList) {
			if (client.clientName.equals(clientName))
				return client;
		}
		return null;
	}

	public Client getClientByReportName(String reportName) {
		for (Client client : clientList) {
			if (client.reportName.equals(reportName))
				return client;
		}
		return null;
	}

	public void createMasterSheet(XSSFSheet sheet) {
		XSSFRow titleRow = sheet.createRow(sheet.getFirstRowNum());
		boolean doneTitle = false;

		for (Client c : clientList) {
			XSSFRow row = sheet.createRow(sheet.getLastRowNum() + 1);
			c.writeColumns(row);
			if (c.account != null) {
				if (c.account.failed) {
					row.createCell(3).setCellValue("Failed");
				} else {
					if (!doneTitle) {
						c.writeTitles(titleRow);
						c.account.writeTitles(titleRow);
						doneTitle = true;
					}
					c.account.writeColumns(row);
				}
			}
		}
		for (int a = 0; a < titleRow.getLastCellNum(); a++)
			sheet.autoSizeColumn(a);
	}

	public void loadClientAccounts(List<Account> accounts) {
		for (Account ac : accounts) {
			for (Client c : clientList) {
				if (c.reportName.equalsIgnoreCase(ac.reportName)) {
					c.account = ac;
					ac.owner = c;
				}
			}
		}
	}
}
