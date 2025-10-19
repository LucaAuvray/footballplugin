package fr.codinbox.footballplugin.inventory;

import java.util.Iterator;

public interface Pagination {

    void setPage(int page);
    void nextPage();
    void previousPage();

    int getNumberOfPages();
    int getPageNumber();

    Iterator<ClickableItem> getFullContent();
    Iterator<ClickableItem> getPageContent(int page);

    void render();

}
