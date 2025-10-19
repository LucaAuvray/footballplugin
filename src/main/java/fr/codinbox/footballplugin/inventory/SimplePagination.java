package fr.codinbox.footballplugin.inventory;

import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.function.Consumer;

public class SimplePagination implements Pagination {

    private InventoryProvider provider;

    private ClickableItem[] items;
    private int itemsPerPage;

    private int page;

    private final PagerIterator iterator;

    private Consumer<SimplePagination> onUpdateConsumer;

    public SimplePagination(InventoryProvider provider, ClickableItem[] items, int itemsPerPage, int page, PagerIterator iterator) {
        this.provider = provider;
        this.items = items;
        this.itemsPerPage = itemsPerPage;
        this.page = page;
        this.iterator = iterator;
    }

    @Override
    public void setPage(int page) {
        this.page = page;
    }

    @Override
    public void nextPage() {
        this.page = ((page + 1) > getNumberOfPages() ? page : page + 1);
    }

    @Override
    public void previousPage() {
        this.page = ((page - 1 < 0) ? page : page - 1);
    }

    @Override
    public int getNumberOfPages() {
        return items.length / itemsPerPage;
    }

    @Override
    public int getPageNumber() {
        return this.page;
    }

    @Override
    public Iterator<ClickableItem> getFullContent() {
        return Arrays.stream(this.items).iterator();
    }

    @Override
    public Iterator<ClickableItem> getPageContent(int page) {
        int beginIndex = page*itemsPerPage;
        int endIndex = Math.min(items.length - 1, (page + 1) * itemsPerPage) + 1;
        return Arrays.stream(Arrays.copyOfRange(items, beginIndex, endIndex)).iterator();
    }

    @Override
    public void render() {
        iterator.reset();
        ArrayList<ClickableItem> items = Lists.newArrayList(getPageContent(page));
        for(int i = 0; i < items.size(); i++) {
            InventorySlot slot = iterator.getNext(i);
            provider.setItem(slot.toInventorySlot(), items.get(i));
        }
        if(onUpdateConsumer != null)
            onUpdateConsumer.accept(this);
    }

    public void onUpdate(Consumer<SimplePagination> consumer) {
        this.onUpdateConsumer = consumer;
    }

}
