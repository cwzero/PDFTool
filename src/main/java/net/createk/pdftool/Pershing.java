package net.createk.pdftool;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Pershing implements Runnable {
	protected File pershingDir = null;
	protected List<Account> accounts = null;
	
	public Pershing(File pershingDir) {
		this.pershingDir = pershingDir;
		accounts = new ArrayList<Account>();
	}

	@Override
	public void run() {
		load(pershingDir);
	}
	
	public void load(File dir) {
		if (dir != null && dir.exists()) {
			if (dir.isDirectory()) {
				for (File f : dir.listFiles()) {
					load(f);
				}
			} else {
				if (dir.getAbsolutePath().endsWith(".pdf")) {
					Account ac = new Account(dir);
					try {
						ac.parse();
						accounts.add(ac);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

}
