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
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;

class Account {
    private static Map<String, String> fieldMap = null;

    private static String[][] columns = {{"Account Name"}, {"Account value"},
            {"Cash", "US Stock", "Non US Stock", "Bond", "Other", "Asset Allocation Not Classified", "Equity",},
            {"Large Value", "Large Blend", "Large Growth", "Medium Value", "Medium Blend", "Medium Growth",
                    "Small Value", "Small Blend", "Small Growth",},
            {"Average Effective Duration (Years)"},
            {"Defensive", "Cons Defensive", "Healthcare", "Utilities", "Sensitive", "Comm Svcs", "Energy",
                    "Industrials", "Technology", "Cyclical", "Basic Materials", "Cons Cyclical", "Financial Svcs",
                    "Real Estate", "Stock Sectors Not Classified"},
            {"Greater Asia", "Americas", "Greater Europe", "United Kingdom", "Emerging Markets"}, {"File Name"}};

    private static String[][] fields = {{"accountName"}, {"accountValue"},
            {"cash", "usStock", "nonUSStock", "bond", "other", "notClassifiedAssetAllocation", "equity",},
            {"largeValue", "largeBlend", "largeGrowth", "medValue", "medBlend", "medGrowth", "smallValue",
                    "smallBlend", "smallGrowth",},
            {"effDur"},
            {"def", "consDef", "health", "util", "sensitive", "comm", "energy", "industrials", "technology",
                    "cyclical", "basicMats", "consCyclical", "financial", "realEstate", "notClassifiedStockSectors",},
            {"greaterAsia", "americas", "greaterEurope", "unitedKingdom", "emergingMarkets"}, {"fileName"}};
    static CellStyle style;
    static CellStyle[] styles = new CellStyle[8];

    static CellStyle investmentStyle;
    static CellStyle integerStyle;
    static CellStyle dateStyle;
    /*private static final String[] sections = {"Asset Allocation", "Asset & Liabilities", "Investment Style",
            "Stock Sectors", "World Regions", "Top 10 Holdings"};*/
    private File root = null;
    boolean failed = false;

    String reportName = "";

    Client owner = null;

    private String fileName = "";
    private String accountName = "";
    private double accountValue = 0.0;

    private double cash = 0.0;
    private double usStock = 0.0;
    private double nonUSStock = 0.0;
    private double bond = 0.0;
    private double other = 0.0;
    private double notClassifiedAssetAllocation = 0.0;
    private double total = 0.0;

    private double largeValue = 0;
    private double largeBlend = 0;
    private double largeGrowth = 0;
    private double medValue = 0;
    private double medBlend = 0;
    private double medGrowth = 0;
    private double smallValue = 0;
    private double smallBlend = 0;
    private double smallGrowth = 0;

    private double effDur = 0.0;

    private double def = 0.0;
    private double consDef = 0.0;
    private double health = 0.0;
    private double util = 0.0;

    private double sensitive = 0.0;
    private double comm = 0.0;
    private double energy = 0.0;
    private double industrials = 0.0;
    private double technology = 0.0;

    private double cyclical = 0.0;
    private double basicMats = 0.0;
    private double consCyclical = 0.0;
    private double financial = 0.0;
    private double realEstate = 0.0;

    private double greaterAsia = 0.0;
    private double americas = 0.0;
    private double greaterEurope = 0.0;
    private double unitedKingdom = 0.0;
    private double emergingMarkets = 0.0;

    private double notClassifiedStockSectors = 0.0;

    private double equity = 0.0;

    Account(File file) {
        this.root = file.getParentFile();
        this.fileName = file.getName();
        loadFieldMap();
    }

    private void loadFieldMap() {
        fieldMap = new HashMap<>();

        for (int a = 0; a < columns.length; a++) {
            for (int b = 0; b < columns[a].length; b++) {
                fieldMap.put(columns[a][b], fields[a][b]);
            }
        }
    }

    void writeColumns(Row row) {
        for (String[] column : columns) {
            for (String aColumn : column) {
                Cell cell = row.createCell(row.getLastCellNum());
                writeValue(aColumn, cell);
            }
        }
    }

    void writeTitles(Row row) {
        for (String[] column : columns) {
            for (String aColumn : column) {
                Cell cell = row.createCell(row.getLastCellNum());
                cell.setCellValue(aColumn);
            }
        }
    }

    void parse() throws IOException {

        if (!fileName.endsWith(".pdf"))
            fileName += ".pdf";
        reportName = fileName.substring(fileName.indexOf(" for ") + " for ".length(), fileName.indexOf(".pdf"));

        File file = new File(root, fileName);

        if (file.exists() && file.getAbsolutePath().endsWith(".pdf")
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

    private void parse(String text) {
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
                            if (line.contains("cash")) {
                                cash = Double.parseDouble(line.split(" ")[1].replace(",", ""));
                            } else if (line.contains("non us stock")) {
                                nonUSStock = Double.parseDouble(line.split(" ")[3].replace(",", ""));
                            } else if (line.contains("us stock")) {
                                usStock = Double.parseDouble(line.split(" ")[2].replace(",", ""));
                            } else if (line.contains("bond")) {
                                bond = Double.parseDouble(line.split(" ")[1].replace(",", ""));
                            } else if (line.contains("other")) {
                                other = Double.parseDouble(line.split(" ")[1].replace(",", ""));
                            } else if (line.contains("not classified")) {
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
                    List<String> chart = new ArrayList<>();
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

                    while (!line.contains("avg eff duration (yrs) ")) {
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
                        if (line.contains("cons defensive")) {
                            consDef = Double.parseDouble(line.split(" ")[2].replace(",", ""));
                        } else if (line.contains("defensive")) {
                            def = Double.parseDouble(line.split(" ")[1].replace(",", ""));
                        } else if (line.contains("healthcare")) {
                            health = Double.parseDouble(line.split(" ")[1].replace(",", ""));
                        } else if (line.contains("utilities")) {
                            util = Double.parseDouble(line.split(" ")[1].replace(",", ""));
                        } else if (line.contains("sensitive")) {
                            sensitive = Double.parseDouble(line.split(" ")[1].replace(",", ""));
                        } else if (line.contains("comm svcs")) {
                            comm = Double.parseDouble(line.split(" ")[2].replace(",", ""));
                        } else if (line.contains("energy")) {
                            energy = Double.parseDouble(line.split(" ")[1].replace(",", ""));
                        } else if (line.contains("industrials")) {
                            industrials = Double.parseDouble(line.split(" ")[1].replace(",", ""));
                        } else if (line.contains("technology")) {
                            technology = Double.parseDouble(line.split(" ")[1].replace(",", ""));
                        } else if (line.contains("cons cyclical")) {
                            consCyclical = Double.parseDouble(line.split(" ")[2].replace(",", ""));
                        } else if (line.contains("basic matls")) {
                            basicMats = Double.parseDouble(line.split(" ")[2].replace(",", ""));
                        } else if (line.contains("cyclical")) {
                            cyclical = Double.parseDouble(line.split(" ")[1].replace(",", ""));
                        } else if (line.contains("financial svcs")) {
                            financial = Double.parseDouble(line.split(" ")[2].replace(",", ""));
                        } else if (line.contains("real estate")) {
                            realEstate = Double.parseDouble(line.split(" ")[2].replace(",", ""));
                        } else if (line.contains("not classified")) {
                            notClassifiedStockSectors = Double.parseDouble(line.split(" ")[2].replace(",", ""));
                        }
                        line = in.nextLine().trim().toLowerCase();
                    }

                    while (!line.equalsIgnoreCase("Top 10 Holdings")) {
                        if (line.contains("greater asia")) {
                            greaterAsia = Double.parseDouble(line.split(" ")[2].replace(",", ""));
                        } else if (line.contains("americas")) {
                            americas = Double.parseDouble(line.split(" ")[1].replace(",", ""));
                        } else if (line.contains("greater europe")) {
                            greaterEurope = Double.parseDouble(line.split(" ")[2].replace(",", ""));
                        } else if (line.contains("united kingdom")) {
                            in.nextLine();
                            in.nextLine();
                            in.nextLine();
                            line = in.nextLine().trim().toLowerCase();
                            unitedKingdom = Double.parseDouble(line.split(" ")[0].replace(",", ""));
                        } else if (line.contains("market maturity")) {
                            in.nextLine();
                            in.nextLine();
                            in.nextLine();
                            in.nextLine();
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

    private boolean shouldDivide(String column) {
        boolean[] divide = {false, false, true, false, false, true, true, false};
        for (int a = 0; a < columns.length; a++) {
            for (int b = 0; b < columns[a].length; b++) {
                if (column.equals(columns[a][b]))
                    return divide[a];
            }
        }
        return false;
    }

    private CellStyle getStyle(String column) {
        for (int a = 0; a < columns.length; a++) {
            for (int b = 0; b < columns[a].length; b++) {
                if (column.equals(columns[a][b]))
                    return styles[a];
            }
        }
        return null;
    }

    private void writeValue(File value, Cell cell) {
        cell.setCellValue(value.getAbsolutePath());
    }

    private void writeStringValue(String value, Cell cell) {
        cell.setCellValue(value);
    }

    private void writeValue(double value, boolean divide, CellStyle cellStyle, Cell cell) {
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

    private Object getValue(String column)
            throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        Field f = Account.class.getDeclaredField(fieldMap.get(column));
        return f.get(this);
    }

    private void writeValue(String column, Cell cell) {

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
}
