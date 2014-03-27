package org.jbpm.console.ng.documents.model.events;

import org.jboss.errai.common.client.api.annotations.Portable;

@Portable
public class DocumentsListSearchEvent {
    private String filter;

    public DocumentsListSearchEvent() {
    }

    public DocumentsListSearchEvent(String filter) {
        this.filter = filter;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }
    
}
