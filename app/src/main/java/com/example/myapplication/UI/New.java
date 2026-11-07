package com.example.myapplication.UI;

import java.util.List;

/**
 * 天行新闻接口返回数据的 Gson 映射模型。
 * 字段命名与接口 JSON 保持一致，由 Gson 直接反序列化。
 */
public class New {

    private int code;
    private String msg;
    private ResultBean result;

    public int getCode() { return code; }
    public void setCode(int code) { this.code = code; }

    public String getMsg() { return msg; }
    public void setMsg(String msg) { this.msg = msg; }

    public ResultBean getResult() { return result; }
    public void setResult(ResultBean result) { this.result = result; }

    public static class ResultBean {
        private int curpage;
        private int allnum;
        private List<NewslistBean> newslist;

        public int getCurpage() { return curpage; }
        public void setCurpage(int curpage) { this.curpage = curpage; }

        public int getAllnum() { return allnum; }
        public void setAllnum(int allnum) { this.allnum = allnum; }

        public List<NewslistBean> getNewslist() { return newslist; }
        public void setNewslist(List<NewslistBean> newslist) { this.newslist = newslist; }

        public static class NewslistBean {
            private String id;
            private String ctime;
            private String title;
            private String description;
            private String source;
            private String picUrl;
            private String url;

            public String getId() { return id; }
            public void setId(String id) { this.id = id; }

            public String getCtime() { return ctime; }
            public void setCtime(String ctime) { this.ctime = ctime; }

            public String getTitle() { return title; }
            public void setTitle(String title) { this.title = title; }

            public String getDescription() { return description; }
            public void setDescription(String description) { this.description = description; }

            public String getSource() { return source; }
            public void setSource(String source) { this.source = source; }

            public String getPicUrl() { return picUrl; }
            public void setPicUrl(String picUrl) { this.picUrl = picUrl; }

            public String getUrl() { return url; }
            public void setUrl(String url) { this.url = url; }
        }
    }
}
