package org.ow2.proactive_grid_cloud_portal.scheduler.client.view;

import org.ow2.proactive_grid_cloud_portal.scheduler.client.SchedulerListeners.PaginationListener;
import org.ow2.proactive_grid_cloud_portal.scheduler.client.controller.PaginationController;

import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.user.client.ui.TextBox;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.Layout;
import com.smartgwt.client.widgets.toolbar.ToolStrip;
import com.smartgwt.client.widgets.toolbar.ToolStripButton;

public class PaginationView implements PaginationListener{

    protected PaginationController paginationController;

    
    /**
     * Task page number
     */
    protected Label offsetRangeLabel = null;
    
    /**
     * Task previous page button
     */
    protected ToolStripButton pagePreviousButton = null;
    /**
     * Task next page button
     */
    protected ToolStripButton pageNextButton = null;
    
    
    /**
     * Task first page button
     */
    protected ToolStripButton pageFirstButton = null;
    /**
     * Task last page button
     */
    protected ToolStripButton pageLastButton = null;
    
    /**
     * Label that indicates the max number of pages
     */
    protected Label pageMaxLabel = null;
    
    /**
     * A textbox that allows the user to choose which page to display.
     */
    protected TextBox txtPageNumber = null;
    
    protected String itemTypeName;
    
    
    /**
     * Builds the view content.
     * @return a layout containing the view content.
     */
    public Layout build() {
        this.pageFirstButton = new ToolStripButton("<< First");
        this.pageFirstButton.disable();
        this.pageFirstButton.addStyleName("navPreviousPaginationButton");
        this.pageFirstButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                paginationController.firstPage();
            }
        });
        
        this.pagePreviousButton = new ToolStripButton("< Previous");
        this.pagePreviousButton.disable();
        this.pagePreviousButton.addStyleName("navPreviousPaginationButton");
        this.pagePreviousButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                paginationController.previousPage();
            }
        });
        
        
        this.pageNextButton = new ToolStripButton("Next >");
        this.pageNextButton.disable();
        this.pageNextButton.addStyleName("navNextPaginationButton");
        this.pageNextButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                paginationController.nextPage();
            }
        });
        
        
        this.pageLastButton = new ToolStripButton("Last >>");
        this.pageLastButton.disable();
        this.pageLastButton.addStyleName("navNextPaginationButton");
        this.pageLastButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                paginationController.lastPage();
            }
        });

        this.offsetRangeLabel = new Label(this.itemTypeName + " 0 - 0");
        this.offsetRangeLabel.addStyleName("navPaginationLabel");
        this.offsetRangeLabel.setAlign(Alignment.CENTER);
        this.offsetRangeLabel.setWidth100();
        this.offsetRangeLabel.setPadding(0);
        
        
        Label pageLabel = new Label("page");
        pageLabel.setWidth100();
        pageLabel.setAlign(Alignment.RIGHT);
        pageLabel.setMinWidth(30);
        pageLabel.setMargin(4);
        
        this.pageMaxLabel = new Label("of 0");
        this.pageMaxLabel.setAlign(Alignment.LEFT);
        this.pageMaxLabel.setWidth100();
        this.pageMaxLabel.setMinWidth(40);
        this.pageMaxLabel.setMargin(4);
        
        this.txtPageNumber = new TextBox();
        this.txtPageNumber.setValue("0");
        this.txtPageNumber.setWidth("25px");
        this.txtPageNumber.addStyleName("txtPageNumber");
        this.txtPageNumber.addKeyDownHandler(new KeyDownHandler() {
            @Override
            public void onKeyDown(KeyDownEvent event) {
                if(event.getNativeKeyCode() == KeyCodes.KEY_ENTER){
                    changePageNumberHandler();
                }
            }
        });

        
        HLayout labelLayout = new HLayout();
        labelLayout.addStyleName("labelPaginationLayout");
        labelLayout.addMember(pageLabel);
        labelLayout.addMember(this.txtPageNumber);
        labelLayout.addMember(this.pageMaxLabel);
        labelLayout.addMember(this.offsetRangeLabel);
        
        ToolStrip paginationLayout = new ToolStrip();
        paginationLayout.addStyleName("itemPaginationBar");
        paginationLayout.setHeight(30);
        paginationLayout.setWidth100();
        paginationLayout.setBackgroundImage("");
        paginationLayout.setBackgroundColor("#fafafa");
        paginationLayout.setBorder("0px");
        
        paginationLayout.addMember(this.pageFirstButton);
        paginationLayout.addMember(this.pagePreviousButton);
        paginationLayout.addMember(labelLayout);
        paginationLayout.addMember(this.pageLastButton);
        paginationLayout.addMember(this.pageNextButton);
        
        return paginationLayout;
    }
    
    protected void changePageNumberHandler(){
        String text = this.txtPageNumber.getText();
        try{
            int pageNumber = Integer.parseInt(text) - 1;
            this.paginationController.goToPage(pageNumber);
        }
        catch(Exception ex){
            
        }
    }
    
    
    /**
     * Disable all the buttons for the pagination.
     */
    protected void disableAllControls(){
        this.pageFirstButton.disable();
        this.pagePreviousButton.disable();
        this.pageNextButton.disable();
        this.pageLastButton.disable();
        this.txtPageNumber.setEnabled(false);
    }


    /**
     * Enables the pagination button according to the navigation status.
     */
    protected void enablePaginationControls(){
        if (this.paginationController.hasPrevious()){
            this.pageFirstButton.enable();
            this.pagePreviousButton.enable();
        }

        if (this.paginationController.hasNext()){
            this.pageNextButton.enable();
            this.pageLastButton.enable();
        }
        this.txtPageNumber.setEnabled(true);
    }
    

    @Override
    public void pageChanged() {
        this.disableAllControls();
        this.offsetRangeLabel.setContents(this.itemTypeName + " " + this.paginationController.getPaginationRangeLabel());
        this.txtPageNumber.setText(this.paginationController.getNumberPageText());
    }
    
    
    @Override
    public void totalItemChanged() {
        this.pageMaxLabel.setContents("of " + this.paginationController.getMaxPageNumberLabel());
        this.enablePaginationControls();
    }
    
    
    protected void itemsUpdated(long totalItems){
        this.disableAllControls();
        this.paginationController.computeMaxPage(totalItems);
        this.offsetRangeLabel.setContents("tasks " + this.paginationController.getPaginationRangeLabel());
        this.txtPageNumber.setText(this.paginationController.getNumberPageText());
    }
}