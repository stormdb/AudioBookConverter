//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.freeipodsoftware.abc;

import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;

public class InputFileSelection extends InputFileSelectionGui {
    public static final String FILE_LIST_CHANGED_EVENT = "fileListChangedEvent";
    private String lastFolder;
    private EventDispatcher eventDispatcher;

    public InputFileSelection(Composite parent, int style) {
        super(parent, style);
        this.addButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                InputFileSelection.this.addInputFile();
            }
        });
        this.removeButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                InputFileSelection.this.removeInputFiles();
            }
        });
        this.clearButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                InputFileSelection.this.list.removeAll();
            }
        });
        this.moveUpButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                InputFileSelection.this.moveUp();
            }
        });
        this.moveDownButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                InputFileSelection.this.moveDown();
            }
        });
        this.createDropTarget();
        this.list.addKeyListener(new InputFileSelection.MyKeyListener());
        this.addButton.setFocus();
    }

    private void createDropTarget() {
        DropTarget target = new DropTarget(this.list, 19);
        target.setTransfer(new Transfer[]{FileTransfer.getInstance(), TextTransfer.getInstance()});
        target.addDropListener(new DropTargetAdapter() {
            public void drop(DropTargetEvent event) {
                if(FileTransfer.getInstance().isSupportedType(event.currentDataType)) {
                    String[] files = (String[])event.data;

                    for(int i = 0; i < files.length; ++i) {
                        InputFileSelection.this.list.add(files[i]);
                    }

                    InputFileSelection.this.eventDispatcher.raiseEvent("fileListChangedEvent");
                }

            }
        });
    }

    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        SwtUtils.setEnabledRecursive(this, enabled);
    }

    public int getButtonWidthHint() {
        return this.addButton.getSize().x;
    }

    private void addInputFile() {
        FileDialog fileDialog = new FileDialog(this.getShell(), 4098);
        if(this.lastFolder != null) {
            fileDialog.setFileName(this.lastFolder);
        }

        fileDialog.setFilterNames(new String[]{Messages.getString("InputFileSelection.mp3Files"), Messages.getString("InputFileSelection.allFiles")});
        fileDialog.setFilterExtensions(new String[]{"*.mp3", "*.*"});
        String firstFile = fileDialog.open();
        if(firstFile != null) {
            this.lastFolder = firstFile;
            String[] fileNames = fileDialog.getFileNames();

            for(int i = 0; i < fileNames.length; ++i) {
                String filterPath = fileDialog.getFilterPath();
                this.list.add(filterPath + System.getProperty("file.separator") + fileNames[i]);
            }

            this.eventDispatcher.raiseEvent("fileListChangedEvent");
        }

    }

    private void removeInputFiles() {
        this.list.remove(this.list.getSelectionIndices());
        this.eventDispatcher.raiseEvent("fileListChangedEvent");
    }

    private void moveDown() {
        if(this.list.getSelectionCount() == 1) {
            int selectionIndex = this.list.getSelectionIndex();
            if(selectionIndex < this.list.getItemCount() - 1) {
                this.list.add(this.list.getItem(selectionIndex), selectionIndex + 2);
                this.list.remove(selectionIndex);
                this.list.setSelection(selectionIndex + 1);
                this.eventDispatcher.raiseEvent("fileListChangedEvent");
            }
        }

    }

    private void moveUp() {
        if(this.list.getSelectionCount() == 1) {
            int selectionIndex = this.list.getSelectionIndex();
            if(selectionIndex > 0) {
                this.list.add(this.list.getItem(selectionIndex), selectionIndex - 1);
                this.list.remove(selectionIndex + 1);
                this.list.setSelection(selectionIndex - 1);
                this.eventDispatcher.raiseEvent("fileListChangedEvent");
            }
        }

    }

    public String[] getFileList() {
        return this.list.getItems();
    }

    public void setEventDispatcher(EventDispatcher eventDispatcher) {
        this.eventDispatcher = eventDispatcher;
    }

    class MyKeyListener extends KeyAdapter implements KeyListener {
        MyKeyListener() {
        }

        public void keyPressed(KeyEvent e) {
            super.keyPressed(e);
            if(e.keyCode == 127) {
                InputFileSelection.this.removeInputFiles();
            } else if(e.keyCode == 97 && e.stateMask == 262144) {
                InputFileSelection.this.list.selectAll();
            }

        }
    }
}