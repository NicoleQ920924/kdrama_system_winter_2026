package com.kdrama.backend.config;

public class TWOTTPlatformConfig {
    private String name;
    private String searchUrl;
    private String titleElementSelector;
    private String watchUrlElementSelector;
    private String watchUrlPrefix;
    private String nextPageBtnSelector;
    private boolean requiresLogin;

    public TWOTTPlatformConfig (String name, String searchUrl, String titleElementSelector, String watchUrlElementSelector, String watchUrlPrefix, String nextPageBtnSelector, boolean requiresLogin) {
        this.name = name;
        this.searchUrl = searchUrl;
        this.titleElementSelector = titleElementSelector;
        this.watchUrlElementSelector = watchUrlElementSelector;
        this.watchUrlPrefix = watchUrlPrefix;
        this.nextPageBtnSelector = nextPageBtnSelector;
        this.requiresLogin = requiresLogin;
    }

    public String getName() {
        return name;
    }

    public String getSearchUrl() {
        return searchUrl;
    }

    public String gettitleElementSelector() {
        return titleElementSelector;
    }

    public String getWatchUrlElementSelector() {
        return watchUrlElementSelector;
    }

    public String getWatchUrlPrefix() {
        return watchUrlPrefix;
    }

    public String getNextPageBtnSelector() {
        return nextPageBtnSelector;
    }

    public boolean isRequiresLogin() {
        return requiresLogin;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSearchUrl(String searchUrl) {
        this.searchUrl = searchUrl;
    }

    public void settitleElementSelector(String titleElementSelector) {
        this.titleElementSelector = titleElementSelector;
    }

    public void setWatchUrlSelector(String watchUrlElementSelector) {
        this.watchUrlElementSelector = watchUrlElementSelector;
    }

    public void setWatchUrlPrefix(String watchUrlPrefix) {
        this.watchUrlPrefix = watchUrlPrefix;
    }

    public void setNextPageBtnSelector(String nextPageBtnSelector) {
        this.nextPageBtnSelector = nextPageBtnSelector;
    }

    public void setRequiresLogin(boolean requiresLogin) {
        this.requiresLogin = requiresLogin;
    }
}
