/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    chris.gross@us.ibm.com - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.swt.nebula.widgets.grid;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ControlEditor;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

/**
 * <p>
 * NOTE:  THIS WIDGET AND ITS API ARE STILL UNDER DEVELOPMENT.  THIS IS A PRE-RELEASE ALPHA 
 * VERSION.  USERS SHOULD EXPECT API CHANGES IN FUTURE VERSIONS.
 * </p> 
 * 
 * A GridEditor is a manager for a Control that appears above a cell in a Grid
 * and tracks with the moving and resizing of that cell. It can be used to
 * display a text widget above a cell in a Grid so that the user can edit the
 * contents of that cell. It can also be used to display a button that can
 * launch a dialog for modifying the contents of the associated cell.
 * <p>
 * @see org.eclipse.swt.custom.TableEditor
 */
public class GridEditor extends ControlEditor
{
    Grid table;

    GridItem item;

    int column = -1;

    ControlListener columnListener;
    
    Listener resizeListener;

    /**
     * Creates a TableEditor for the specified Table.
     * 
     * @param table the Table Control above which this editor will be displayed
     */
    public GridEditor(Grid table)
    {
        super(table);
        this.table = table;

        columnListener = new ControlListener()
        {
            public void controlMoved(ControlEvent e)
            {
                layout();
            }

            public void controlResized(ControlEvent e)
            {
                layout();
            }
        };
        
        resizeListener = new Listener()
        {
         public void handleEvent(Event event)
            {
                 layout();
            }   
        };
        


        // The following three listeners are workarounds for
        // Eclipse bug 105764
        // https://bugs.eclipse.org/bugs/show_bug.cgi?id=105764
        table.addListener(SWT.Resize, resizeListener);
        
        if (table.getVerticalBar() != null)
        {
            table.getVerticalBar().addListener(SWT.Selection, resizeListener);
        }
        if (table.getHorizontalBar() != null)
        {
            table.getHorizontalBar().addListener(SWT.Selection, resizeListener);
        }

        // To be consistent with older versions of SWT, grabVertical defaults to
        // true
        grabVertical = true;
    }

    Rectangle computeBounds()
    {
        if (item == null || column == -1 || item.isDisposed())
            return new Rectangle(0, 0, 0, 0);
        Rectangle cell = item.getBounds(column);
        Rectangle area = table.getClientArea();
        if (cell.x < area.x + area.width)
        {
            if (cell.x + cell.width > area.x + area.width)
            {
                cell.width = area.x + area.width - cell.x;
            }
        }
        Rectangle editorRect = new Rectangle(cell.x, cell.y, minimumWidth, minimumHeight);

        if (grabHorizontal)
        {
            editorRect.width = Math.max(cell.width, minimumWidth);
        }

        if (grabVertical)
        {
            editorRect.height = Math.max(cell.height, minimumHeight);
        }

        if (horizontalAlignment == SWT.RIGHT)
        {
            editorRect.x += cell.width - editorRect.width;
        }
        else if (horizontalAlignment == SWT.LEFT)
        {
            // do nothing - cell.x is the right answer
        }
        else
        { // default is CENTER
            editorRect.x += (cell.width - editorRect.width) / 2;
        }

        if (verticalAlignment == SWT.BOTTOM)
        {
            editorRect.y += cell.height - editorRect.height;
        }
        else if (verticalAlignment == SWT.TOP)
        {
            // do nothing - cell.y is the right answer
        }
        else
        { // default is CENTER
            editorRect.y += (cell.height - editorRect.height) / 2;
        }
        return editorRect;
    }

    /**
     * Removes all associations between the TableEditor and the cell in the
     * table. The Table and the editor Control are <b>not</b> disposed.
     */
    public void dispose()
    {
        if (!table.isDisposed() && this.column > -1 && this.column < table.getColumnCount())
        {
            GridColumn tableColumn = table.getColumn(this.column);
            tableColumn.removeControlListener(columnListener);
        }
        
        if (!table.isDisposed())
        {
            table.removeListener(SWT.Resize, resizeListener);
            
            if (table.getVerticalBar() != null)
                table.getVerticalBar().removeListener(SWT.Selection, resizeListener);
            
            if (table.getHorizontalBar() != null)
                table.getHorizontalBar().removeListener(SWT.Selection, resizeListener);
        }
        
        columnListener = null;
        resizeListener = null;
        table = null;
        item = null;
        column = -1;        
        super.dispose();
    }

    /**
     * Returns the zero based index of the column of the cell being tracked by
     * this editor.
     * 
     * @return the zero based index of the column of the cell being tracked by
     * this editor
     */
    public int getColumn()
    {
        return column;
    }

    /**
     * Returns the TableItem for the row of the cell being tracked by this
     * editor.
     * 
     * @return the TableItem for the row of the cell being tracked by this
     * editor
     */
    public GridItem getItem()
    {
        return item;
    }

    /**
     * Sets the zero based index of the column of the cell being tracked by this
     * editor.
     * 
     * @param column the zero based index of the column of the cell being
     * tracked by this editor
     */
    public void setColumn(int column)
    {
        int columnCount = table.getColumnCount();
        // Separately handle the case where the table has no TableColumns.
        // In this situation, there is a single default column.
        if (columnCount == 0)
        {
            this.column = (column == 0) ? 0 : -1;
            layout();
            return;
        }
        if (this.column > -1 && this.column < columnCount)
        {
            GridColumn tableColumn = table.getColumn(this.column);
            tableColumn.removeControlListener(columnListener);
            this.column = -1;
        }

        if (column < 0 || column >= table.getColumnCount())
            return;

        this.column = column;
        GridColumn tableColumn = table.getColumn(this.column);
        tableColumn.addControlListener(columnListener);
        layout();
    }

    public void setItem(GridItem item)
    {
        this.item = item;
        layout();
    }

    /**
     * Specify the Control that is to be displayed and the cell in the table
     * that it is to be positioned above.
     * <p>
     * Note: The Control provided as the editor <b>must</b> be created with its
     * parent being the Table control specified in the TableEditor constructor.
     * 
     * @param editor the Control that is displayed above the cell being edited
     * @param item the TableItem for the row of the cell being tracked by this
     * editor
     * @param column the zero based index of the column of the cell being
     * tracked by this editor
     */
    public void setEditor(Control editor, GridItem item, int column)
    {
        setItem(item);
        setColumn(column);
        setEditor(editor);

        layout();
    }

    public void layout()
    {

        if (table.isDisposed())
            return;
        if (item == null || item.isDisposed())
            return;
        int columnCount = table.getColumnCount();
        if (columnCount == 0 && column != 0)
            return;
        if (columnCount > 0 && (column < 0 || column >= columnCount))
            return;

        boolean hadFocus = false;

        if (getEditor() == null || getEditor().isDisposed())
            return;
        if (getEditor().getVisible())
        {
            hadFocus = getEditor().isFocusControl();
        } // this doesn't work because
        // resizing the column takes the focus away
        // before we get here
        getEditor().setBounds(computeBounds());
        if (hadFocus)
        {
            if (getEditor() == null || getEditor().isDisposed())
                return;
            getEditor().setFocus();
        }

    }

}
