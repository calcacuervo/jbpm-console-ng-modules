/*
 * Copyright 2012 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jbpm.console.ng.documents.client.editors.documentslist;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import javax.enterprise.context.Dependent;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.jbpm.console.ng.documents.client.i18n.Constants;
import org.jbpm.console.ng.documents.client.resources.DocumentsListImages;
import org.jbpm.console.ng.documents.model.DocumentSummary;
import org.jbpm.console.ng.pr.model.events.NewProcessInstanceEvent;
import org.jbpm.console.ng.pr.model.events.ProcessDefSelectionEvent;
import org.jbpm.console.ng.pr.model.events.ProcessDefStyleEvent;
import org.jbpm.console.ng.pr.model.events.ProcessInstanceSelectionEvent;
import org.uberfire.client.common.BusyPopup;
import org.uberfire.client.mvp.PlaceManager;
import org.uberfire.client.mvp.PlaceStatus;
import org.uberfire.client.tables.ResizableHeader;
import org.uberfire.mvp.PlaceRequest;
import org.uberfire.mvp.impl.DefaultPlaceRequest;
import org.uberfire.security.Identity;
import org.uberfire.workbench.events.NotificationEvent;

import com.github.gwtbootstrap.client.ui.DataGrid;
import com.github.gwtbootstrap.client.ui.SimplePager;
import com.google.gwt.cell.client.ActionCell;
import com.google.gwt.cell.client.ActionCell.Delegate;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.CompositeCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.HasCell;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.view.client.CellPreviewEvent;
import com.google.gwt.view.client.DefaultSelectionEventManager;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionModel;

@Dependent
@Templated(value = "DocumentsListViewImpl.html")
public class DocumentsListViewImpl extends Composite
        implements DocumentsListPresenter.DocumentsListView,
        RequiresResize {

    private Constants constants = GWT.create(Constants.class);
    private DocumentsListImages images = GWT.create(DocumentsListImages.class);

    @Inject
    private Identity identity;

    @Inject
    private PlaceManager placeManager;

    private DocumentsListPresenter presenter;

    private String currentFilter = "";

    @Inject
    @DataField
    public DataGrid<DocumentSummary> documentsListGrid;

    @Inject
    @DataField
    public LayoutPanel listContainer;

    @DataField
    public SimplePager pager;

    @Inject
    private Event<NotificationEvent> notification;
    
    private ListHandler<DocumentSummary> sortHandler;

    public DocumentsListViewImpl() {
        pager = new SimplePager(SimplePager.TextLocation.LEFT, false, true);
    }

    @Override
    public String getCurrentFilter() {
        return currentFilter;
    }

    @Override
    public void setCurrentFilter(String currentFilter) {
        this.currentFilter = currentFilter;
    }

    @Override
    public void init(final DocumentsListPresenter presenter) {
        this.presenter = presenter;

        listContainer.add(documentsListGrid);
        pager.setDisplay(documentsListGrid);
        pager.setPageSize(10);

        // Set the message to display when the table is empty.
        Label emptyTable = new Label(constants.No_Documents_Found());
        emptyTable.setStyleName("");
        documentsListGrid.setEmptyTableWidget(emptyTable);

        // Attach a column sort handler to the ListDataProvider to sort the list.
        sortHandler = new ListHandler<DocumentSummary>(presenter.getDataProvider().getList());
        documentsListGrid.addColumnSortHandler(sortHandler);

        // Add a selection model so we can select cells.
        final MultiSelectionModel<DocumentSummary> selectionModel = new MultiSelectionModel<DocumentSummary>();
        selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
               
            }
        });

        documentsListGrid.setSelectionModel(selectionModel,
                DefaultSelectionEventManager.<DocumentSummary>createCheckboxManager());

        initTableColumns(selectionModel);

        presenter.addDataDisplay(documentsListGrid);

    }

    private void initTableColumns(final SelectionModel<DocumentSummary> selectionModel) {

        documentsListGrid.addCellPreviewHandler(new CellPreviewEvent.Handler<DocumentSummary>() {

            @Override
            public void onCellPreview(final CellPreviewEvent<DocumentSummary> event) {
            }
        });

        // Process Name String.
        Column<DocumentSummary, String> processNameColumn = new Column<DocumentSummary, String>(new TextCell()) {
            @Override
            public String getValue(DocumentSummary object) {
                return object.getName();
            }
        };
        processNameColumn.setSortable(true);
        sortHandler.setComparator(processNameColumn, new Comparator<DocumentSummary>() {
            @Override
            public int compare(DocumentSummary o1,
                    DocumentSummary o2) {
                return o1.getName().toLowerCase().compareTo(o2.getName().toLowerCase());
            }
        });
        documentsListGrid.addColumn(processNameColumn, new ResizableHeader(constants.Name(), documentsListGrid,
                processNameColumn));

        // Version Type
        Column<DocumentSummary, String> nameColumn = new Column<DocumentSummary, String>(new TextCell()) {
            @Override
            public String getValue(DocumentSummary object) {
                return object.getName();
            }
        };
        nameColumn.setSortable(true);
        sortHandler.setComparator(nameColumn, new Comparator<DocumentSummary>() {
            @Override
            public int compare(DocumentSummary o1,
                    DocumentSummary o2) {
                Integer version1;
                Integer version2;
                try{
                    version1 =  Integer.valueOf(o1.getName());
                    version2 = Integer.valueOf(o2.getName());
                    return version1.compareTo(version2);
                }catch(NumberFormatException nfe){
                    return o1.getName().compareTo(o2.getName());
                }
            }
        });
        documentsListGrid
                .addColumn(nameColumn, new ResizableHeader(constants.Version(), documentsListGrid, nameColumn));
        documentsListGrid.setColumnWidth(nameColumn, "90px");

        // actions (icons)
        List<HasCell<DocumentSummary, ?>> cells = new LinkedList<HasCell<DocumentSummary, ?>>();

        cells.add(new StartActionHasCell("Accion", new Delegate<DocumentSummary>() {
            @Override
            public void execute(DocumentSummary process) {
                
            }
        }));

        cells.add(new DetailsActionHasCell("Details", new Delegate<DocumentSummary>() {
            @Override
            public void execute(DocumentSummary process) {
            }
        }));

        CompositeCell<DocumentSummary> cell = new CompositeCell<DocumentSummary>(cells);
        Column<DocumentSummary, DocumentSummary> actionsColumn = new Column<DocumentSummary, DocumentSummary>(cell) {
            @Override
            public DocumentSummary getValue(DocumentSummary object) {
                return object;
            }
        };
        documentsListGrid.addColumn(actionsColumn, new ResizableHeader(constants.Actions(), documentsListGrid, actionsColumn));
        documentsListGrid.setColumnWidth(actionsColumn, "70px");
    }

    @Override
    public void onResize() {
        if ((getParent().getOffsetHeight() - 120) > 0) {
            listContainer.setHeight(getParent().getOffsetHeight() - 120 + "px");
        }
    }

    public void changeRowSelected(@Observes ProcessDefStyleEvent processDefStyleEvent) {
        
    }

    @Override
    public void displayNotification(String text) {
        notification.fire(new NotificationEvent(text));
    }

    @Override
    public DataGrid<DocumentSummary> getDataGrid() {
        return documentsListGrid;
    }

    public ListHandler<DocumentSummary> getSortHandler() {
        return sortHandler;
    }

    @Override
    public void showBusyIndicator(final String message) {
        BusyPopup.showMessage(message);
    }

    @Override
    public void hideBusyIndicator() {
        BusyPopup.close();
    }

    private class StartActionHasCell implements HasCell<DocumentSummary, DocumentSummary> {

        private ActionCell<DocumentSummary> cell;

        public StartActionHasCell(String text,
                Delegate<DocumentSummary> delegate) {
            cell = new ActionCell<DocumentSummary>(text, delegate) {
                @Override
                public void render(Cell.Context context,
                        DocumentSummary value,
                        SafeHtmlBuilder sb) {

                    AbstractImagePrototype imageProto = AbstractImagePrototype.create(images.startGridIcon());
                    SafeHtmlBuilder mysb = new SafeHtmlBuilder();
                    mysb.appendHtmlConstant("<span title='" + constants.Start() + "' style='margin-right:5px;'>");
                    mysb.append(imageProto.getSafeHtml());
                    mysb.appendHtmlConstant("</span>");
                    sb.append(mysb.toSafeHtml());
                }
            };
        }

        @Override
        public Cell<DocumentSummary> getCell() {
            return cell;
        }

        @Override
        public FieldUpdater<DocumentSummary, DocumentSummary> getFieldUpdater() {
            return null;
        }

        @Override
        public DocumentSummary getValue(DocumentSummary object) {
            return object;
        }
    }

    private class DetailsActionHasCell implements HasCell<DocumentSummary, DocumentSummary> {

        private ActionCell<DocumentSummary> cell;

        public DetailsActionHasCell(String text,
                Delegate<DocumentSummary> delegate) {
            cell = new ActionCell<DocumentSummary>(text, delegate) {
                @Override
                public void render(Cell.Context context,
                        DocumentSummary value,
                        SafeHtmlBuilder sb) {

                    AbstractImagePrototype imageProto = AbstractImagePrototype.create(images.detailsGridIcon());
                    SafeHtmlBuilder mysb = new SafeHtmlBuilder();
                    mysb.appendHtmlConstant("<span title='" + constants.Details() + "' style='margin-right:5px;'>");
                    mysb.append(imageProto.getSafeHtml());
                    mysb.appendHtmlConstant("</span>");
                    sb.append(mysb.toSafeHtml());
                }
            };
        }

        @Override
        public Cell<DocumentSummary> getCell() {
            return cell;
        }

        @Override
        public FieldUpdater<DocumentSummary, DocumentSummary> getFieldUpdater() {
            return null;
        }

        @Override
        public DocumentSummary getValue(DocumentSummary object) {
            return object;
        }
    }

}
