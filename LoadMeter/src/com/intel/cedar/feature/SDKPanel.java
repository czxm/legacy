package com.intel.cedar.feature;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import com.intel.cedar.service.client.feature.model.Variable;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class SDKPanel extends JPanel {
	private boolean DEBUG = true;

	public SDKPanel(List<Variable> vars) {
		super(new FlowLayout());

		JTable table = new JTable(new VariableListModel(vars));
		table.setPreferredScrollableViewportSize(new Dimension(500, 70));

		// Create the scroll pane and add the table to it.
		JScrollPane scrollPane = new JScrollPane(table);

		// Set up column sizes.
		initColumnSizes(table);

		// Fiddle with the Sport column's cell editors/renderers.
		setUpSportColumn(table, table.getColumnModel().getColumn(1));

		// Add the scroll pane to this panel.
		add(scrollPane);
		
		JButton btn = new JButton("Start");
		btn.setSize(80, 30);
		btn.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("Action");
			}
			
		});
		add(btn);
	}

	/*
	 * This method picks good column sizes. If all column heads are wider than
	 * the column's cells' contents, then you can just use
	 * column.sizeWidthToFit().
	 */
	private void initColumnSizes(JTable table) {
		VariableListModel model = (VariableListModel) table.getModel();
		TableColumn column = null;
		Component comp = null;
		int headerWidth = 0;
		int cellWidth = 0;
		Object[] longValues = model.longValues;
		TableCellRenderer headerRenderer = table.getTableHeader()
				.getDefaultRenderer();

		for (int i = 0; i < 3; i++) {
			column = table.getColumnModel().getColumn(i);

			comp = headerRenderer.getTableCellRendererComponent(null, column
					.getHeaderValue(), false, false, 0, 0);
			headerWidth = comp.getPreferredSize().width;

			comp = table.getDefaultRenderer(model.getColumnClass(i))
					.getTableCellRendererComponent(table, longValues[i], false,
							false, 0, i);
			cellWidth = comp.getPreferredSize().width;

			if (DEBUG) {
				System.out.println("Initializing width of column " + i + ". "
						+ "headerWidth = " + headerWidth + "; cellWidth = "
						+ cellWidth);
			}

			// XXX: Before Swing 1.1 Beta 2, use setMinWidth instead.
			column.setPreferredWidth(Math.max(headerWidth, cellWidth));
		}
	}

	public void setUpSportColumn(JTable table, TableColumn sportColumn) {
		// Set up the editor for the sport cells.
		JComboBox comboBox = new JComboBox();
		comboBox.addItem("Snowboarding");
		comboBox.addItem("Rowing");
		comboBox.addItem("Knitting");
		comboBox.addItem("Speed reading");
		comboBox.addItem("Pool");
		comboBox.addItem("None of the above");
		sportColumn.setCellEditor(new DefaultCellEditor(comboBox));

		// Set up tool tips for the sport cells.
		DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
		renderer.setToolTipText("Click for combo box");
		sportColumn.setCellRenderer(renderer);
	}

	class VariableListModel extends AbstractTableModel {
		private List<Variable> varList;
		private String[] columnNames = { "Variable Name", "Values",
				"Multi-valued" };

		private Object[][] data;

		public Object[] longValues;

		public VariableListModel(List<Variable> vars){
			this.varList = vars;
			data = new Object[vars.size()][3];
			longValues = new Object[3];
			for(int i = 0; i < vars.size(); i++){
				Variable var = vars.get(i);
				data[i][0] = var.getName();
				data[i][1] = var.getValue();
				if(data[i][1] == null)
					data[i][1] = "";
				data[i][2] = new Boolean(false);
				for(int j = 0; j < 3; j++){
					if(longValues[j] == null || data[i][j].toString().length() > longValues[j].toString().length()){
						longValues[j] = data[i][j];
					}
				}
			}
		}
		
		public int getColumnCount() {
			return columnNames.length;
		}

		public int getRowCount() {
			return data.length;
		}

		public String getColumnName(int col) {
			return columnNames[col];
		}

		public Object getValueAt(int row, int col) {
			return data[row][col];
		}

		/*
		 * JTable uses this method to determine the default renderer/ editor for
		 * each cell. If we didn't implement this method, then the last column
		 * would contain text ("true"/"false"), rather than a check box.
		 */
		public Class getColumnClass(int c) {
			return getValueAt(0, c).getClass();
		}

		/*
		 * Don't need to implement this method unless your table's editable.
		 */
		public boolean isCellEditable(int row, int col) {
			// Note that the data/cell address is constant,
			// no matter where the cell appears onscreen.
			if (col < 1) {
				return false;
			} else {
				return true;
			}
		}

		/*
		 * Don't need to implement this method unless your table's data can
		 * change.
		 */
		public void setValueAt(Object value, int row, int col) {
			if (DEBUG) {
				System.out.println("Setting value at " + row + "," + col
						+ " to " + value + " (an instance of "
						+ value.getClass() + ")");
			}

			data[row][col] = value;
			fireTableCellUpdated(row, col);

			if (DEBUG) {
				System.out.println("New value of data:");
				printDebugData();
			}
		}

		private void printDebugData() {
			int numRows = getRowCount();
			int numCols = getColumnCount();

			for (int i = 0; i < numRows; i++) {
				System.out.print("    row " + i + ":");
				for (int j = 0; j < numCols; j++) {
					System.out.print("  " + data[i][j]);
				}
				System.out.println();
			}
			System.out.println("--------------------------");
		}
	}
}