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

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.Dependent;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.ErrorCallback;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jbpm.console.ng.documents.client.i18n.Constants;
import org.jbpm.console.ng.documents.model.DocumentSummary;
import org.kie.workbench.common.widgets.client.search.ClearSearchEvent;
import org.uberfire.client.annotations.WorkbenchMenu;
import org.uberfire.client.annotations.WorkbenchPartTitle;
import org.uberfire.client.annotations.WorkbenchPartView;
import org.uberfire.client.annotations.WorkbenchScreen;
import org.uberfire.client.mvp.UberView;
import org.uberfire.lifecycle.OnFocus;
import org.uberfire.lifecycle.OnOpen;
import org.uberfire.mvp.Command;
import org.uberfire.workbench.model.menu.MenuFactory;
import org.uberfire.workbench.model.menu.Menus;

import com.github.gwtbootstrap.client.ui.DataGrid;
import com.google.gwt.core.client.GWT;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.ListDataProvider;

@Dependent
@WorkbenchScreen(identifier = "Documents List")
public class DocumentsListPresenter {
	
    public interface DocumentsListView extends UberView<DocumentsListPresenter> {

        void displayNotification( String text );

        String getCurrentFilter();

        void setCurrentFilter( String filter );

        DataGrid<DocumentSummary> getDataGrid();

        void showBusyIndicator( String message );

        void hideBusyIndicator();
    }

    private Menus menus;

    @Inject
    private DocumentsListView view;

    @Inject
    private Event<ClearSearchEvent> clearSearchEvent;
    

    private ListDataProvider<DocumentSummary> dataProvider = new ListDataProvider<DocumentSummary>();

    private Constants constants = GWT.create( Constants.class );

    private List<DocumentSummary> currentProcesses;

    @WorkbenchPartTitle
    public String getTitle() {
        return constants.Process_Definitions();
    }

    @WorkbenchPartView
    public UberView<DocumentsListPresenter> getView() {
        return view;
    }

    public DocumentsListPresenter() {
        makeMenuBar();
    }
    
    public void refreshProcessList() {
       
    }

    public void filterProcessList( String filter ) {
        if ( filter.equals( "" ) ) {
            if ( currentProcesses != null ) {
                dataProvider.getList().clear();
                dataProvider.getList().addAll( new ArrayList<DocumentSummary>( currentProcesses ) );
                dataProvider.refresh();

            }
        } else {
            if ( currentProcesses != null ) {
                List<DocumentSummary> processes = new ArrayList<DocumentSummary>( currentProcesses );
                List<DocumentSummary> filteredProcesses = new ArrayList<DocumentSummary>();
                for ( DocumentSummary ps : processes ) {
                    if ( ps.getName().toLowerCase().contains( filter.toLowerCase() ) ) {
                        filteredProcesses.add( ps );
                    }
                }
                dataProvider.getList().clear();
                dataProvider.getList().addAll( filteredProcesses );
                dataProvider.refresh();
            }
        }

    }

    public void reloadRepository() {

       

    }

    public void addDataDisplay( HasData<DocumentSummary> display ) {
        dataProvider.addDataDisplay( display );
    }

    public ListDataProvider<DocumentSummary> getDataProvider() {
        return dataProvider;
    }

    public void refreshData() {
        dataProvider.refresh();
    }

    @OnOpen
    public void onOpen() {
        refreshProcessList();
    }
    
    @OnFocus
    public void onFocus() {
        refreshProcessList();
    }

    @WorkbenchMenu
    public Menus getMenus() {
        return menus;
    }

    private void makeMenuBar() {
        menus = MenuFactory
                .newTopLevelMenu( constants.Refresh() )
                .respondsWith( new Command() {
                    @Override
                    public void execute() {
                        refreshProcessList();
                        view.setCurrentFilter( "" );
                        view.displayNotification( constants.Process_Definitions_Refreshed() );
                    }
                } )
                .endMenu().
                        build();

    }
    

     
}
