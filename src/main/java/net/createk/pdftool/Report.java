package net.createk.pdftool;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class Report implements Runnable {
	protected XSSFWorkbook reportFile = null;
	protected File reportDir = null;
	protected List<Account> accounts = null;
	public Pershing pershing = null;

	public Report(String reportFile) throws IOException {
		this(new File(reportFile));
	}

	public Report(File reportDir) throws IOException {
		this.reportDir = reportDir;
		accounts = new ArrayList<Account>();
	}

	public void load() throws IOException {
		loadReport(reportDir);
	}

	public void loadReport(File dir) throws IOException {
		if (pershing != null && pershing.pershingDir != null && dir.equals(pershing.pershingDir))
			return;
		if (dir != null && dir.exists()) {
			if (dir.isDirectory()) {
				for (File f : dir.listFiles()) {
					loadReport(f);
				}
			} else {
				if (dir.getAbsolutePath().endsWith(".pdf")) {
					Account ac = new Account(dir);
					ac.parse();
					accounts.add(ac);
				}
			}
		}
	}

	@Override
	public void run() {
		if (pershing != null)
			pershing.run();
		try {
			load();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void exportReport(File report, ClientList clientList)
			throws IOException {
		OutputStream reportStream = new FileOutputStream(report);

		reportFile = new XSSFWorkbook();

		DataFormat format = reportFile.createDataFormat();
		Account.investmentStyle = reportFile.createCellStyle();
		Account.investmentStyle.setDataFormat(format.getFormat("#0.##"));
		Account.style = reportFile.createCellStyle();
		Account.style.setDataFormat(format.getFormat("#0.00%"));
		Account.integerStyle = reportFile.createCellStyle();
		Account.integerStyle.setDataFormat(format.getFormat("#0"));
		Account.dateStyle = reportFile.createCellStyle();
		Account.dateStyle.setDataFormat(format.getFormat("m/d/yyyy"));

		Account.styles[0] = null;
		Account.styles[1] = Account.investmentStyle;
		Account.styles[2] = Account.style;
		Account.styles[3] = Account.investmentStyle;
		Account.styles[4] = Account.investmentStyle;
		Account.styles[5] = Account.style;
		Account.styles[6] = Account.style;
		Account.styles[7] = null;

		if (reportFile.getSheet("Master") != null)
			reportFile.removeSheetAt(reportFile.getSheetIndex("Master"));
		XSSFSheet master = reportFile.createSheet("Master");

		clientList.loadClientAccounts(accounts);

		clientList.createMasterSheet(master);

		if (pershing != null) {
			ClientList pershingList = new ClientList(clientList);
			XSSFSheet pershingSheet = reportFile.createSheet("Pershing Only");
			pershingList.loadClientAccounts(pershing.accounts);
			pershingList.createMasterSheet(pershingSheet);
		}

		reportFile.write(reportStream);
		reportStream.flush();
		reportStream.close();
	}
}
