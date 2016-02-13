package net.createk.pdftool;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.preflight.parser.PreflightParser;
import org.apache.pdfbox.util.PDFTextStripper;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFRow;

public class Account {
	public static Map<String, String> fieldMap = null;

	public static String[][] columns = { { "Account Name" }, { "Account value" },
			{ "Cash", "US Stock", "Non US Stock", "Bond", "Other", "Asset Allocation Not Classified", "Equity", },
			{ "Large Value", "Large Blend", "Large Growth", "Medium Value", "Medium Blend", "Medium Growth",
					"Small Value", "Small Blend", "Small Growth", },
			{ "Average Effective Duration (Years)" },
			{ "Defensive", "Cons Defensive", "Healthcare", "Utilities", "Sensitive", "Comm Svcs", "Energy",
					"Industrials", "Technology", "Cyclical", "Basic Materials", "Cons Cyclical", "Financial Svcs",
					"Real Estate", "Stock Sectors Not Classified" },
			{ "Greater Asia", "Americas", "Greater Europe", "Emerging Markets" }, { "File Name" } };

	public static String[][] fields = { { "accountName" }, { "accountValue" },
			{ "cash", "usStock", "nonUSStock", "bond", "other", "notClassifiedAssetAllocation", "equity", },
			{ "largeValue", "largeBlend", "largeGrowth", "medValue", "medBlend", "medGrowth", "smallValue",
					"smallBlend", "smallGrowth", },
			{ "effDur" },
			{ "def", "consDef", "health", "util", "sensitive", "comm", "energy", "industrials", "technology",
					"cyclical", "basicMats", "consCyclical", "financial", "realEstate", "notClassifiedStockSectors", },
			{ "greaterAsia", "americas", "greaterEurope", "emergingMarkets" }, { "fileName" } };
	public static CellStyle style;
	public static CellStyle styles[] = new CellStyle[8];
	public static CellStyle investmentStyle;
	public static CellStyle integerStyle;
	public static CellStyle dateStyle;
	public static final String[] sections = { "Asset Allocation", "Asset & Liabilities", "Investment Style",
			"Stock Sectors", "World Regions", "Top 10 Holdings" };
	public File root = null;
	public boolean failed = false;

	public String reportName = "";

	public Client owner = null;

	public String fileName = "";
	public String accountName = "";
	public double accountValue = 0.0;

	public double cash = 0.0;
	public double usStock = 0.0;
	public double nonUSStock = 0.0;
	public double bond = 0.0;
	public double other = 0.0;
	public double notClassifiedAssetAllocation = 0.0;
	public double total = 0.0;

	public double largeValue = 0;
	public double largeBlend = 0;
	public double largeGrowth = 0;
	public double medValue = 0;
	public double medBlend = 0;
	public double medGrowth = 0;
	public double smallValue = 0;
	public double smallBlend = 0;
	public double smallGrowth = 0;

	public double effDur = 0.0;

	public double def = 0.0;
	public double consDef = 0.0;
	public double health = 0.0;
	public double util = 0.0;

	public double sensitive = 0.0;
	public double comm = 0.0;
	public double energy = 0.0;
	public double industrials = 0.0;
	public double technology = 0.0;

	public double cyclical = 0.0;
	public double basicMats = 0.0;
	public double consCyclical = 0.0;
	public double financial = 0.0;
	public double realEstate = 0.0;

	public double greaterAsia = 0.0;
	public double americas = 0.0;
	public double greaterEurope = 0.0;
	public double emergingMarkets = 0.0;

	public double notClassifiedStockSectors = 0.0;

	public double equity = 0.0;

	public Account(File file) {
		this.root = file.getParentFile();
		this.fileName = file.getName();
		loadFieldMap();
	}

	public Account(Client client, String fileName, File root) {
		this.owner = client;
		this.reportName = client.reportName;
		this.fileName = fileName;
		this.root = root;
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
				Cell cell = row.createCell(row.getLastCellNum());
				writeValue(columns[x][y], cell);
			}
		}
	}

	public void writeTitles(Row row) {
		for (int x = 0; x < columns.length; x++) {
			for (int y = 0; y < columns[x].length; y++) {
				Cell cell = row.createCell(row.getLastCellNum());
				cell.setCellValue(columns[x][y]);
			}
		}
	}

	public void parse() throws IOException {

		if (!fileName.endsWith(".pdf"))
			fileName += ".pdf";
		reportName = fileName.substring(fileName.indexOf(" for ") + " for ".length(), fileName.indexOf(".pdf"));

		File file = new File(root, fileName);

		if (file != null && file.exists() && file.getAbsolutePath().endsWith(".pdf")
				&& !file.getAbsolutePath().contains("failed") && !file.getAbsolutePath().contains("Failed")) {

			PreflightParser parser = new PreflightParser(file);

			parser.parse();

			PDFTextStripper stripper = new PDFTextStripper();

			PDDocument doc = parser.getPDDocument();

			String text = stripper.getText(doc);

			parse(text);

			doc.close();
		} else {
			failed = true;
		}
	}

	protected void parse(String text) {
		Scanner in = new Scanner(text);
		try {
			while (in.hasNextLine()) {
				String line = in.nextLine().trim();
				Pattern p = Pattern.compile("^[Cc]lient [Nn]ame: (.+) [Aa]ccount [Nn]ame: (.+)$");
				if (line.matches(p.pattern())) {
					Matcher matcher = p.matcher(line);
					if (matcher.matches()) {
						reportName = matcher.group(1);
						accountName = matcher.group(2);
					}
				}

				if (line.equalsIgnoreCase("Account Value Benchmark Account Number Report Currency")) {
					line = in.nextLine().trim().toLowerCase();
					String[] data = line.split(" ");
					accountValue = Double.parseDouble(data[0].replace(",", ""));
				}

				if (line.equalsIgnoreCase("Asset Allocation")) {
					line = in.nextLine().trim().toLowerCase();
					if (line.equalsIgnoreCase("Asset Allocation Account % Bmark %")) {
						line = in.nextLine().trim().toLowerCase();
						while (!line.equalsIgnoreCase("Asset & Liabilities")) {
							if (line.indexOf("cash") != -1) {
								cash = Double.parseDouble(line.split(" ")[1].replace(",", ""));
							} else if (line.indexOf("non us stock") != -1) {
								nonUSStock = Double.parseDouble(line.split(" ")[3].replace(",", ""));
							} else if (line.indexOf("us stock") != -1) {
								usStock = Double.parseDouble(line.split(" ")[2].replace(",", ""));
							} else if (line.indexOf("bond") != -1) {
								bond = Double.parseDouble(line.split(" ")[1].replace(",", ""));
							} else if (line.indexOf("other") != -1) {
								other = Double.parseDouble(line.split(" ")[1].replace(",", ""));
							} else if (line.indexOf("not classified") != -1) {
								notClassifiedAssetAllocation = Double.parseDouble(line.split(" ")[2].replace(",", ""));
							} else {
								total = Double.parseDouble(line.split(" ")[0].replace(",", ""));
							}
							line = in.nextLine().trim().toLowerCase();
						}
					}
				}

				if (line.equalsIgnoreCase("Investment Style")) {
					while (!line.equalsIgnoreCase("all")) {
						line = in.nextLine().trim().toLowerCase();
					}
					line = in.nextLine().trim().toLowerCase();
					List<String> chart = new ArrayList<String>();
					while (!line.equalsIgnoreCase("Value Blend Growth")) {
						chart.add(line);
						line = in.nextLine().trim().toLowerCase();
					}

					largeValue = Double.parseDouble(chart.get(0).split(" ")[0].replace(",", ""));
					largeBlend = Double.parseDouble(chart.get(0).split(" ")[1].replace(",", ""));
					largeGrowth = Double.parseDouble(chart.get(0).split(" ")[2].replace(",", ""));
					medValue = Double.parseDouble(chart.get(1).split(" ")[0].replace(",", ""));
					medBlend = Double.parseDouble(chart.get(1).split(" ")[1].replace(",", ""));
					medGrowth = Double.parseDouble(chart.get(1).split(" ")[2].replace(",", ""));
					smallValue = Double.parseDouble(chart.get(2).split(" ")[0].replace(",", ""));
					smallBlend = Double.parseDouble(chart.get(2).split(" ")[1].replace(",", ""));
					smallGrowth = Double.parseDouble(chart.get(2).split(" ")[2].replace(",", ""));

					while (line.indexOf("avg eff duration (yrs) ") == -1) {
						line = in.nextLine().trim().toLowerCase();
					}

					effDur = Double.parseDouble(
							line.substring(line.indexOf("avg eff duration (yrs) ") + "avg eff duration (yrs) ".length())
									.replace(",", ""));
				}

				if (line.equalsIgnoreCase("Stock Sectors")) {
					while (!line.equalsIgnoreCase("Account % Bmark % Rel Bmark")) {
						line = in.nextLine().trim().toLowerCase();
					}

					while (!line.equalsIgnoreCase("World Regions")) {
						if (line.indexOf("cons defensive") != -1) {
							consDef = Double.parseDouble(line.split(" ")[2].replace(",", ""));
						} else if (line.indexOf("defensive") != -1) {
							def = Double.parseDouble(line.split(" ")[1].replace(",", ""));
						} else if (line.indexOf("healthcare") != -1) {
							health = Double.parseDouble(line.split(" ")[1].replace(",", ""));
						} else if (line.indexOf("utilities") != -1) {
							util = Double.parseDouble(line.split(" ")[1].replace(",", ""));
						} else if (line.indexOf("sensitive") != -1) {
							sensitive = Double.parseDouble(line.split(" ")[1].replace(",", ""));
						} else if (line.indexOf("comm svcs") != -1) {
							comm = Double.parseDouble(line.split(" ")[2].replace(",", ""));
						} else if (line.indexOf("energy") != -1) {
							energy = Double.parseDouble(line.split(" ")[1].replace(",", ""));
						} else if (line.indexOf("industrials") != -1) {
							industrials = Double.parseDouble(line.split(" ")[1].replace(",", ""));
						} else if (line.indexOf("technology") != -1) {
							technology = Double.parseDouble(line.split(" ")[1].replace(",", ""));
						} else if (line.indexOf("cons cyclical") != -1) {
							consCyclical = Double.parseDouble(line.split(" ")[2].replace(",", ""));
						} else if (line.indexOf("basic matls") != -1) {
							basicMats = Double.parseDouble(line.split(" ")[2].replace(",", ""));
						} else if (line.indexOf("cyclical") != -1) {
							cyclical = Double.parseDouble(line.split(" ")[1].replace(",", ""));
						} else if (line.indexOf("financial svcs") != -1) {
							financial = Double.parseDouble(line.split(" ")[2].replace(",", ""));
						} else if (line.indexOf("real estate") != -1) {
							realEstate = Double.parseDouble(line.split(" ")[2].replace(",", ""));
						} else if (line.indexOf("not classified") != -1) {
							notClassifiedStockSectors = Double.parseDouble(line.split(" ")[2].replace(",", ""));
						}
						line = in.nextLine().trim().toLowerCase();
					}

					while (!line.equalsIgnoreCase("Top 10 Holdings")) {
						if (line.indexOf("greater asia") != -1) {
							greaterAsia = Double.parseDouble(line.split(" ")[2].replace(",", ""));
						} else if (line.indexOf("americas") != -1) {
							americas = Double.parseDouble(line.split(" ")[1].replace(",", ""));
						} else if (line.indexOf("greater europe") != -1) {
							greaterEurope = Double.parseDouble(line.split(" ")[2].replace(",", ""));
						} else if (line.indexOf("market maturity") != -1) {
							line = in.nextLine().trim().toLowerCase();
							line = in.nextLine().trim().toLowerCase();
							line = in.nextLine().trim().toLowerCase();
							line = in.nextLine().trim().toLowerCase();
							line = in.nextLine().trim().toLowerCase();
							emergingMarkets = Double.parseDouble(line.split(" ")[0].replace(",", ""));
						}
						line = in.nextLine().trim().toLowerCase();
					}
				}
			}
			in.close();

			equity = usStock + nonUSStock + other;
		} catch (Exception ex) {
			System.err.println("Parse error parsing " + fileName);
			ex.printStackTrace();
			in.close();
		}
	}

	public void writeRow(XSSFRow row) {
		row.createCell(0).setCellValue(reportName);
		Cell cell = row.createCell(1);
		cell.setCellValue(equity / 100);
		cell.setCellStyle(style);
	}

	public void writeEquity(Cell cell) {
		cell.setCellValue(equity / 100);
		cell.setCellStyle(style);
	}

	public boolean shouldDivide(String column) {
		boolean[] divide = { false, false, true, false, false, true, true, false };
		for (int a = 0; a < columns.length; a++) {
			for (int b = 0; b < columns[a].length; b++) {
				if (column.equals(columns[a][b]))
					return divide[a];
			}
		}
		return false;
	}

	public CellStyle getStyle(String column) {
		for (int a = 0; a < columns.length; a++) {
			for (int b = 0; b < columns[a].length; b++) {
				if (column.equals(columns[a][b]))
					return styles[a];
			}
		}
		return null;
	}

	public void writeValue(File value, Cell cell) {
		cell.setCellValue(value.getAbsolutePath());
	}

	public void writeStringValue(String value, Cell cell) {
		cell.setCellValue(value);
	}

	public void writeValue(double value, boolean divide, CellStyle cellStyle, Cell cell) {
		if (value % 1 == 0 && !divide) {
			cell.setCellValue((int) value);
			return;
		}

		if (divide) {
			cell.setCellValue(value / 100);
		} else {
			cell.setCellValue(value);
		}
		cell.setCellStyle(cellStyle);
	}

	public Object getValue(String column)
			throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		Field f = Account.class.getDeclaredField(fieldMap.get(column));
		return f.get(this);
	}

	public void writeValue(String column, Cell cell) {

		Object value = null;
		try {
			value = getValue(column);
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}

		if (value != null) {
			if (value instanceof Double) {
				writeValue((double) value, shouldDivide(column), getStyle(column), cell);
			} else if (value instanceof String) {
				writeStringValue((String) value, cell);
			} else if (value instanceof File) {
				writeValue((File) value, cell);
			}
		}
	}

	public void writeStyledValue(double value, Cell cell) {
		cell.setCellValue(value / 100);
		cell.setCellStyle(style);
	}
}
