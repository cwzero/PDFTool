package net.createk.pdftool;

import java.io.File;
import java.io.IOException;

public class Main {
	public static void main(String[] args) {
		if (args.length != 3 && args.length != 4) {
			System.err.println("Something went wrong: can't find batches/report file arguments.");
			System.exit(1);
		}
		
		File pershingDir = null;
		if (args.length == 4) {
			pershingDir = new File(args[3]);
		}
		File repDir = new File(args[0]);
		File reportFile = new File(args[1]);
		File clientList = new File(args[2]);
		
		ClientList list = new ClientList(clientList);
		
		Report report = null;
		try {
			report = new Report(repDir);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		if (pershingDir != null) {
			Pershing pershing = null;
			pershing = new Pershing(pershingDir);
			report.pershing = pershing;
		}
		report.run();
		try {
			report.exportReport(reportFile, list);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
