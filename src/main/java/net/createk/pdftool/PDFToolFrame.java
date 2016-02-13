package net.createk.pdftool;

import java.awt.EventQueue;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.JButton;

import java.awt.Desktop;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.awt.Insets;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import javax.swing.JTextField;

public class PDFToolFrame extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTextField batchDirField;
	private JTextField clientListField;
	private JTextField reportFileField;

	public static FileFilter dirFilter = new FileFilter() {

		@Override
		public String getDescription() {
			return "Folder";
		}

		@Override
		public boolean accept(File f) {
			return f != null && f.exists() && f.isDirectory();
		}
	};

	public static FileFilter excelFilter = new FileFilter() {

		@Override
		public String getDescription() {
			return "Excel Spreadsheet (.xls or .xlsx)";
		}

		@Override
		public boolean accept(File f) {
			return f != null
					&& (f.getAbsolutePath().endsWith(".xls") || f
							.getAbsolutePath().endsWith(".xlsx"));
		}
	};

	protected boolean configLoaded = false;
	protected File batchDir = null;
	protected File pershingDir = null;
	protected boolean pershing = false;
	protected File clientList = null;
	protected File reportFile = null;
	protected File configFile = null;

	protected JFileChooser chooser = null;
	private JButton btnOpenClientList;
	private JButton btnSelectPershingOnly;
	private JTextField pershingField;
	private JButton btnEnablePershing;
	private final JPanel panel = new JPanel();
	private JButton btnOpenReport;
	private JButton btnExit;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					PDFToolFrame frame = new PDFToolFrame();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public void loadConfig() throws IOException {
		if (!configLoaded) {
			if (configFile == null) {
				configFile = new File("config.xml");
			}

			if (configFile.exists()) {
				InputStream in = new FileInputStream(configFile);
				Properties p = new Properties();
				p.loadFromXML(in);

				if (p.containsKey("batchDirPath")) {
					batchDir = new File(p.getProperty("batchDirPath"));
				}

				if (p.containsKey("clientListPath")) {
					clientList = new File(p.getProperty("clientListPath"));
				}

				if (p.containsKey("reportFilePath")) {
					reportFile = new File(p.getProperty("reportFilePath"));
				}

				if (p.containsKey("pershingDirPath")) {
					pershingDir = new File(p.getProperty("pershingDirPath"));
				}

				if (p.containsKey("runPershingOnly")) {
					pershing = Boolean.parseBoolean(p.getProperty(
							"runPershingOnly", "false"));
				}
			}

			configLoaded = true;
		}
	}

	public void saveConfig() throws IOException {
		if (configFile == null) {
			configFile = new File("config.xml");
		}

		if (!configFile.exists()) {
			configFile.createNewFile();
		}

		Properties p = new Properties();
		if (batchDir != null) {
			p.setProperty("batchDirPath", batchDir.getAbsolutePath());
		}

		if (clientList != null) {
			p.setProperty("clientListPath", clientList.getAbsolutePath());
		}

		if (reportFile != null) {
			p.setProperty("reportFilePath", reportFile.getAbsolutePath());
		}

		if (pershingDir != null && pershing) {
			p.setProperty("pershingDirPath", pershingDir.getAbsolutePath());
		}

		p.setProperty("runPershingOnly", Boolean.toString(pershing));

		OutputStream out = new FileOutputStream(configFile);
		p.storeToXML(out, "PDFTool Configuration Data version 0.0.0");
		out.flush();
		out.close();
	}

	public void update() {
		if (!configLoaded) {
			try {
				loadConfig();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		if (batchDir != null) {
			batchDirField.setText(batchDir.getAbsolutePath());
		}

		if (reportFile != null) {
			reportFileField.setText(reportFile.getAbsolutePath());
		}

		if (clientList != null) {
			clientListField.setText(clientList.getAbsolutePath());
		}
		if (pershing) {
			if (pershingDir != null) {
				pershingField.setText(pershingDir.getAbsolutePath());
			}
		} else {
			pershingField.setText("");
		}

		if (pershing) {
			btnEnablePershing.setText("Disable Pershing Only");
		} else {
			btnEnablePershing.setText("Enable Pershing Only");
		}
	}

	/**
	 * Create the frame.
	 */
	public PDFToolFrame() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException
				| IllegalAccessException | UnsupportedLookAndFeelException e1) {
			e1.printStackTrace();
		}
		try {
			loadConfig();
		} catch (IOException e2) {
			e2.printStackTrace();
		}
		chooser = new JFileChooser();
		setTitle("Report File Generator");
		this.addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent arg0) {
				try {
					saveConfig();
				} catch (IOException e) {
					e.printStackTrace();
				}
				super.windowClosing(arg0);
				System.exit(0);
			}
		});
		setBounds(100, 100, 744, 202);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		GridBagLayout gbl_contentPane = new GridBagLayout();
		gbl_contentPane.columnWidths = new int[] { 0, 0, 0 };
		gbl_contentPane.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0 };
		gbl_contentPane.columnWeights = new double[] { 0.0, 1.0,
				Double.MIN_VALUE };
		gbl_contentPane.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0,
				0.0, Double.MIN_VALUE };
		contentPane.setLayout(gbl_contentPane);

		JButton btnBatchDir = new JButton("Select Batch Directory");
		btnBatchDir.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				if (batchDir != null)
					chooser.setCurrentDirectory(batchDir.getParentFile());
				chooser.showOpenDialog(PDFToolFrame.this);
				batchDir = chooser.getSelectedFile();
				update();
			}
		});
		GridBagConstraints gbc_btnBatchDir = new GridBagConstraints();
		gbc_btnBatchDir.fill = GridBagConstraints.BOTH;
		gbc_btnBatchDir.insets = new Insets(0, 0, 5, 5);
		gbc_btnBatchDir.gridx = 0;
		gbc_btnBatchDir.gridy = 0;
		contentPane.add(btnBatchDir, gbc_btnBatchDir);

		batchDirField = new JTextField();
		batchDirField.setEditable(false);
		GridBagConstraints gbc_batchDirField = new GridBagConstraints();
		gbc_batchDirField.insets = new Insets(0, 0, 5, 0);
		gbc_batchDirField.fill = GridBagConstraints.HORIZONTAL;
		gbc_batchDirField.gridx = 1;
		gbc_batchDirField.gridy = 0;
		contentPane.add(batchDirField, gbc_batchDirField);
		batchDirField.setColumns(10);

		JButton btnChoosereportsFile = new JButton("Choose Report File");
		btnChoosereportsFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
				if (reportFile != null)
					chooser.setCurrentDirectory(reportFile.getParentFile());
				chooser.showSaveDialog(PDFToolFrame.this);
				reportFile = chooser.getSelectedFile();
				update();
			}
		});

		btnOpenClientList = new JButton("Open Client List");
		btnOpenClientList.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
				if (clientList != null) {
					chooser.setCurrentDirectory(clientList.getParentFile());
				}
				chooser.showOpenDialog(PDFToolFrame.this);
				clientList = chooser.getSelectedFile();
				update();
			}
		});
		GridBagConstraints gbc_btnOpenClientList = new GridBagConstraints();
		gbc_btnOpenClientList.fill = GridBagConstraints.BOTH;
		gbc_btnOpenClientList.insets = new Insets(0, 0, 5, 5);
		gbc_btnOpenClientList.gridx = 0;
		gbc_btnOpenClientList.gridy = 1;
		contentPane.add(btnOpenClientList, gbc_btnOpenClientList);

		clientListField = new JTextField();
		clientListField.setEditable(false);
		GridBagConstraints gbc_clientListField = new GridBagConstraints();
		gbc_clientListField.insets = new Insets(0, 0, 5, 0);
		gbc_clientListField.fill = GridBagConstraints.HORIZONTAL;
		gbc_clientListField.gridx = 1;
		gbc_clientListField.gridy = 1;
		contentPane.add(clientListField, gbc_clientListField);
		clientListField.setColumns(10);
		GridBagConstraints reportButton = new GridBagConstraints();
		reportButton.fill = GridBagConstraints.BOTH;
		reportButton.insets = new Insets(0, 0, 5, 5);
		reportButton.gridx = 0;
		reportButton.gridy = 2;
		contentPane.add(btnChoosereportsFile, reportButton);

		reportFileField = new JTextField();
		reportFileField.setEditable(false);
		GridBagConstraints gbc_reportsFileField = new GridBagConstraints();
		gbc_reportsFileField.insets = new Insets(0, 0, 5, 0);
		gbc_reportsFileField.fill = GridBagConstraints.HORIZONTAL;
		gbc_reportsFileField.gridx = 1;
		gbc_reportsFileField.gridy = 2;
		contentPane.add(reportFileField, gbc_reportsFileField);
		reportFileField.setColumns(10);

		JButton btnRun = new JButton("Run");
		btnRun.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String[] args;
				if (pershing && pershingDir != null && pershingDir.exists()) {
					args = new String[] { batchDir.getAbsolutePath(),
							reportFile.getAbsolutePath(),
							clientList.getAbsolutePath(),
							pershingDir.getAbsolutePath() };
				} else {

					args = new String[] { batchDir.getAbsolutePath(),
							reportFile.getAbsolutePath(),
							clientList.getAbsolutePath() };
				}
				try {
					saveConfig();
					Main.main(args);
					btnOpenReport.setEnabled(true);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});

		btnSelectPershingOnly = new JButton("Select Pershing Only");
		btnSelectPershingOnly.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!pershing)
					pershing = true;
				else {
					chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
					if (pershingDir != null)
						chooser.setCurrentDirectory(pershingDir.getParentFile());
					chooser.showOpenDialog(PDFToolFrame.this);
					pershingDir = chooser.getSelectedFile();
				}
				update();
			}
		});
		GridBagConstraints gbc_btnSelectPershingOnly = new GridBagConstraints();
		gbc_btnSelectPershingOnly.fill = GridBagConstraints.BOTH;
		gbc_btnSelectPershingOnly.insets = new Insets(0, 0, 5, 5);
		gbc_btnSelectPershingOnly.gridx = 0;
		gbc_btnSelectPershingOnly.gridy = 3;
		contentPane.add(btnSelectPershingOnly, gbc_btnSelectPershingOnly);

		pershingField = new JTextField();
		pershingField.setEditable(false);
		GridBagConstraints gbc_pershingField = new GridBagConstraints();
		gbc_pershingField.insets = new Insets(0, 0, 5, 0);
		gbc_pershingField.fill = GridBagConstraints.HORIZONTAL;
		gbc_pershingField.gridx = 1;
		gbc_pershingField.gridy = 3;
		contentPane.add(pershingField, gbc_pershingField);
		pershingField.setColumns(10);
		GridBagConstraints gbc_btnRun = new GridBagConstraints();
		gbc_btnRun.insets = new Insets(0, 0, 5, 5);
		gbc_btnRun.fill = GridBagConstraints.BOTH;
		gbc_btnRun.gridx = 0;
		gbc_btnRun.gridy = 4;
		contentPane.add(btnRun, gbc_btnRun);
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.insets = new Insets(0, 0, 5, 0);
		gbc_panel.fill = GridBagConstraints.BOTH;
		gbc_panel.gridx = 1;
		gbc_panel.gridy = 4;
		panel.setBorder(null);
		contentPane.add(panel, gbc_panel);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[] { 133, 133, 133, 133 };
		gbl_panel.rowHeights = new int[] { 23, 0 };
		gbl_panel.columnWeights = new double[] { 0.0, 0.0, 0.0 };
		gbl_panel.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
		panel.setLayout(gbl_panel);

		btnEnablePershing = new JButton("Enable Pershing Only");
		GridBagConstraints gbc_btnEnablePershing = new GridBagConstraints();
		gbc_btnEnablePershing.anchor = GridBagConstraints.WEST;
		gbc_btnEnablePershing.insets = new Insets(0, 0, 0, 5);
		gbc_btnEnablePershing.gridx = 0;
		gbc_btnEnablePershing.gridy = 0;
		panel.add(btnEnablePershing, gbc_btnEnablePershing);
		btnEnablePershing.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				pershing = !pershing;
				update();
			}
		});

		btnOpenReport = new JButton("Open Report");
		btnOpenReport.setEnabled(false);
		btnOpenReport.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (reportFile != null && reportFile.exists()) {
					try {
						Desktop.getDesktop().edit(reportFile);
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			}
		});
		GridBagConstraints gbc_btnOpenReport = new GridBagConstraints();
		gbc_btnOpenReport.insets = new Insets(0, 0, 0, 5);
		gbc_btnOpenReport.anchor = GridBagConstraints.WEST;
		gbc_btnOpenReport.fill = GridBagConstraints.BOTH;
		gbc_btnOpenReport.gridx = 1;
		gbc_btnOpenReport.gridy = 0;
		panel.add(btnOpenReport, gbc_btnOpenReport);

		btnExit = new JButton("Exit");
		btnExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					saveConfig();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				System.exit(0);
			}
		});
		GridBagConstraints gbc_btnExit = new GridBagConstraints();
		gbc_btnExit.fill = GridBagConstraints.BOTH;
		gbc_btnExit.insets = new Insets(0, 0, 0, 5);
		gbc_btnExit.gridx = 2;
		gbc_btnExit.gridy = 0;
		panel.add(btnExit, gbc_btnExit);
		update();
	}
}
