package org.servantscode.integration.pushpay.dao;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

public class PushPayPaginatedResponse<T> {
    private int page;
    private int pageSize;
    private int total;
    private int totalPages;
    private List<T> items;

    @JsonProperty("_links")
    private Map<String, PushPayLink> links;

    // ----- Accessors -----
    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }

    public int getPageSize() { return pageSize; }
    public void setPageSize(int pageSize) { this.pageSize = pageSize; }

    public int getTotal() { return total; }
    public void setTotal(int total) { this.total = total; }

    public int getTotalPages() { return totalPages; }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }

    public List<T> getItems() { return items; }
    public void setItems(List<T> items) { this.items = items; }

    public Map<String, PushPayLink> getLinks() { return links; }
    public void setLinks(Map<String, PushPayLink> links) { this.links = links; }
}
